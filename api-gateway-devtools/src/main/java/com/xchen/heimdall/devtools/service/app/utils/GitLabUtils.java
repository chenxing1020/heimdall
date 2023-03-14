package com.xchen.heimdall.devtools.service.app.utils;

import com.xchen.heimdall.devtools.service.app.common.constant.ErrorCode;
import com.xchen.heimdall.devtools.service.app.vo.AssigneeUserIdVO;
import com.xchen.heimdall.common.exception.errorcode.CustomException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.*;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

/**
 * @author xchen
 * @date 2022/5/25
 */
@Slf4j
@Component
public class GitLabUtils {

    @Value("${config.git.sshUrl}")
    private String sshUrl;
    @Value("${config.git.httpUrl}")
    private String httpUrl;
    @Value("${config.git.branchName}")
    private String branchName;
    @Value("${config.git.accessToken}")
    private String token;
    @Value("${config.git.projectId}")
    private Long projectId;
    @Value("${config.git.targetProjectId}")
    private Long targetProjectId;

    private static final String COMMIT_MESSAGE_FORMAT = "update dubbo api by %s using devtools";

    /**
     * 默认ssh策略，采用ssh key认证
     */
    private static final SshSessionFactory SSH_SESSION_FACTORY = new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host host, Session session) {
            /*
            解除HostKey检查
             */
            session.setConfig("StrictHostKeyChecking", "no");
        }
    };

    /**
     * 生成代码前的操作，生成userId的文件夹，并clone代码
     *
     * @param filePath 文件路径
     */
    public void preGenerate(String filePath) {
        Git git = null;
        try {
            git = Git.cloneRepository()
                    .setURI(sshUrl)
                    .setDirectory(new File(filePath))
                    .setTransportConfigCallback(transport -> {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(SSH_SESSION_FACTORY);
                    })
                    .setCloneSubmodules(true)
                    .setBranch(branchName)
                    .call();

        } catch (Exception e) {
            throw CustomException.builder()
                    .errorCode(ErrorCode.GIT_CLONE_ERROR)
                    .message(e.getMessage())
                    .exception(e)
                    .build();
        } finally {
            if (Objects.nonNull(git)) {
                git.close();
            }
        }
    }

    /**
     * 生成代码后的操作，将代码push到远程仓库并mr
     *
     * @param fileName 文件名
     * @param vo       用户名
     */
    public String postGenerate(String fileName, AssigneeUserIdVO vo) {

        try (Git git = Git.open(new File(fileName))) {

            // git add all, 并打印git状态
            git.add().addFilepattern(".").call();
            logStatus(git);
            // 提交本地修改
            RevCommit commit = git.commit().setMessage(String.format(COMMIT_MESSAGE_FORMAT, vo.getUserId())).call();
            log.info("Success to commit {}", commit.getId());
            // 将修改推到远程私库
            PushResult pushResult = git.push()
                    .setRefSpecs(new RefSpec(branchName))
                    .setTransportConfigCallback(transport -> {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(SSH_SESSION_FACTORY);
                    })
                    .call()
                    .iterator()
                    .next();
            log.info("Push result: {}", pushResult.getRemoteUpdates());
            RemoteRefUpdate.Status pushStatus = pushResult.getRemoteUpdate(String.format("refs/%s", branchName)).getStatus();
            if (!RemoteRefUpdate.Status.OK.equals(pushStatus)) {
                throw CustomException.builder()
                        .errorCode(ErrorCode.GIT_PUSH_ERROR)
                        .message(pushStatus.name())
                        .build();
            }
        } catch (Exception e) {
            throw CustomException.builder()
                    .errorCode(ErrorCode.GIT_PUSH_ERROR)
                    .message(e.getMessage())
                    .exception(e)
                    .build();
        }

        // 从私库向主库发起merge
        return mergeRequest(vo);
    }

    private void logStatus(Git git) throws GitAPIException {
        Status status = git.status().call();
        log.info("Log git status: ");
        status.getAdded().forEach(item -> log.info("{} Added", item));
        status.getChanged().forEach(item -> log.info("{} Changed", item));
        status.getConflicting().forEach(item -> log.info("{} Conflicting", item));
        status.getIgnoredNotInIndex().forEach(item -> log.info("{} IgnoredNotInIndex", item));
        status.getMissing().forEach(item -> log.info("{} Missing", item));
        status.getModified().forEach(item -> log.info("{} Modified", item));
        status.getRemoved().forEach(item -> log.info("{} Removed", item));
        status.getUntracked().forEach(item -> log.info("{} UntrackedFiles", item));
        status.getUntrackedFolders().forEach(item -> log.info("{} UntrackedFolders", item));
    }

    private String mergeRequest(AssigneeUserIdVO vo) {
        try (GitLabApi gitLabApi = new GitLabApi(httpUrl, token)) {
            Long assigneeId = gitLabApi.getUserApi().getUser(vo.getAssigneeUserId()).getId();
            MergeRequest mr = gitLabApi.getMergeRequestApi()
                    .createMergeRequest(projectId, branchName, branchName,
                            String.format(COMMIT_MESSAGE_FORMAT, vo.getUserId()),
                            String.format(COMMIT_MESSAGE_FORMAT, vo.getUserId()),
                            assigneeId,
                            targetProjectId,
                            null, null, null, null
                    );
            log.info("Success to create merge request: {}", mr.getWebUrl());
            return mr.getWebUrl();
        } catch (Exception e) {
            throw CustomException.builder()
                    .errorCode(ErrorCode.GIT_MR_ERROR)
                    .message(e.getMessage())
                    .exception(e)
                    .build();
        }
    }
}
