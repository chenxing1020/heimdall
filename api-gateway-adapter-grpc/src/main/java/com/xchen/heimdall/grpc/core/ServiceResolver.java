package com.xchen.heimdall.grpc.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.xchen.heimdall.grpc.model.GrpcMethodDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @reference https://github.com/grpc-swagger/grpc-swagger
 */

/**
 * A locator used to read proto file descriptors and extract method definitions.
 */
public class ServiceResolver {
    private static final Logger logger = LoggerFactory.getLogger(ServiceResolver.class);
    private final ImmutableList<Descriptors.FileDescriptor> fileDescriptors;

    /**
     * Creates a resolver which searches the supplied {@link DescriptorProtos.FileDescriptorSet}.
     */
    public static ServiceResolver fromFileDescriptorSet(DescriptorProtos.FileDescriptorSet descriptorSet) {
        ImmutableMap<String, DescriptorProtos.FileDescriptorProto> descriptorProtoIndex =
                computeDescriptorProtoIndex(descriptorSet);
        Map<String, Descriptors.FileDescriptor> descriptorCache = new HashMap<>();

        ImmutableList.Builder<Descriptors.FileDescriptor> result = ImmutableList.builder();
        for (DescriptorProtos.FileDescriptorProto descriptorProto : descriptorSet.getFileList()) {
            try {
                result.add(descriptorFromProto(descriptorProto, descriptorProtoIndex, descriptorCache));
            } catch (Descriptors.DescriptorValidationException e) {
                logger.warn("Skipped descriptor " + descriptorProto.getName() + " due to error", e);
            }
        }
        return new ServiceResolver(result.build());
    }

    private ServiceResolver(Iterable<Descriptors.FileDescriptor> fileDescriptors) {
        this.fileDescriptors = ImmutableList.copyOf(fileDescriptors);
    }

    /**
     * Lists all of the services found in the file descriptors
     */
    public Iterable<Descriptors.ServiceDescriptor> listServices() {
        ArrayList<Descriptors.ServiceDescriptor> serviceDescriptors = new ArrayList<Descriptors.ServiceDescriptor>();
        for (Descriptors.FileDescriptor fileDescriptor : fileDescriptors) {
            serviceDescriptors.addAll(fileDescriptor.getServices());
        }
        return serviceDescriptors;
    }

    /**
     * Lists all the known message types.
     */
    public ImmutableSet<Descriptors.Descriptor> listMessageTypes() {
        ImmutableSet.Builder<Descriptors.Descriptor> resultBuilder = ImmutableSet.builder();
        fileDescriptors.forEach(d -> resultBuilder.addAll(d.getMessageTypes()));
        return resultBuilder.build();
    }

    /**
     * Returns the descriptor of a protobuf method with the supplied grpc method name. If the method
     * cannot be found, this throws {@link IllegalArgumentException}.
     */
    public Descriptors.MethodDescriptor resolveServiceMethod(GrpcMethodDefinition definition) {

        Descriptors.ServiceDescriptor service = findService(definition.getPackageName(), definition.getServiceName());
        Descriptors.MethodDescriptor method = service.findMethodByName(definition.getMethodName());
        if (method == null) {
            throw new IllegalArgumentException(
                    "Unable to find method " + definition.getMethodName()
                            + " in service " + definition.getServiceName());
        }
        return method;
    }

    private Descriptors.ServiceDescriptor findService(String packageName, String serviceName) {
        // TODO(dino): Consider creating an index.
        for (Descriptors.FileDescriptor fileDescriptor : fileDescriptors) {
            if (!fileDescriptor.getPackage().equals(packageName)) {
                // Package does not match this file, ignore.
                continue;
            }

            Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(serviceName);
            if (serviceDescriptor != null) {
                return serviceDescriptor;
            }
        }
        throw new IllegalArgumentException("Unable to find service with name: " + serviceName);
    }

    /**
     * Returns a map from descriptor proto name as found inside the descriptors to protos.
     */
    private static ImmutableMap<String, DescriptorProtos.FileDescriptorProto> computeDescriptorProtoIndex(
            DescriptorProtos.FileDescriptorSet fileDescriptorSet) {
        ImmutableMap.Builder<String, DescriptorProtos.FileDescriptorProto> resultBuilder = ImmutableMap.builder();
        for (DescriptorProtos.FileDescriptorProto descriptorProto : fileDescriptorSet.getFileList()) {
            resultBuilder.put(descriptorProto.getName(), descriptorProto);
        }
        return resultBuilder.build();
    }

    /**
     * Recursively constructs file descriptors for all dependencies of the supplied proto and returns
     * a {@link Descriptors.FileDescriptor} for the supplied proto itself. For maximal efficiency, reuse the
     * descriptorCache argument across calls.
     */
    private static Descriptors.FileDescriptor descriptorFromProto(
            DescriptorProtos.FileDescriptorProto descriptorProto,
            ImmutableMap<String, DescriptorProtos.FileDescriptorProto> descriptorProtoIndex,
            Map<String, Descriptors.FileDescriptor> descriptorCache) throws Descriptors.DescriptorValidationException {
        // First, check the cache.
        String descriptorName = descriptorProto.getName();
        if (descriptorCache.containsKey(descriptorName)) {
            return descriptorCache.get(descriptorName);
        }

        // Then, fetch all the required dependencies recursively.
        ImmutableList.Builder<Descriptors.FileDescriptor> dependencies = ImmutableList.builder();
        for (String dependencyName : descriptorProto.getDependencyList()) {
            if (!descriptorProtoIndex.containsKey(dependencyName)) {
                throw new IllegalArgumentException("Could not find dependency: " + dependencyName);
            }
            DescriptorProtos.FileDescriptorProto dependencyProto = descriptorProtoIndex.get(dependencyName);
            dependencies.add(descriptorFromProto(dependencyProto, descriptorProtoIndex, descriptorCache));
        }

        // Finally, construct the actual descriptor.
        Descriptors.FileDescriptor[] empty = new Descriptors.FileDescriptor[0];
        return Descriptors.FileDescriptor.buildFrom(descriptorProto, dependencies.build().toArray(empty));
    }

    public List<Descriptors.FileDescriptor> getFileDescriptors() {
        return fileDescriptors;
    }
}
