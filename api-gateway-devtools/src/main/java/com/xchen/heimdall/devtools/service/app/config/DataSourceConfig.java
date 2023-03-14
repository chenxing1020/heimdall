package com.xchen.heimdall.devtools.service.app.config;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import lombok.Data;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 数据源
 *
 * @author xchen
 * @since 2022/1/5 11:25
 */
@Configuration
@MapperScan(basePackages = "${app.main-package}.dao", sqlSessionFactoryRef = "sqlSessionFactory")
@Data
public class DataSourceConfig {

    @Value("${app.main-package}.domain")
    private String typeAliasesPackage;
    @Value("${mybatis-plus.typeEnumsPackage}")
    private String typeEnumsPackage;

    @Value("${datasource.url}")
    private String url;
    @Value("${datasource.username}")
    private String username;
    @Value("${datasource.password}")
    private String password;
    private int maxActive = 8;
    private int minIdle = 0;

    private String driverClassName = "com.mysql.jdbc.Driver";
    private String myBatisConfiguration = "mybatis-config.xml";
    private String mapperLocations = "classpath*:mapper/*.xml";

    @Primary
    @Bean(name = "dataSource")
    public DataSource getDataSource() throws Exception {
        Properties props = new Properties();
        props.put("driverClassName", driverClassName);
        props.put("url", url);
        props.put("username", username);
        props.put("password", password);
        props.put("maxActive", String.valueOf(maxActive));
        props.put("minIdle", String.valueOf(minIdle));
        return DruidDataSourceFactory.createDataSource(props);
    }

    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource, MybatisPlusInterceptor interceptor) throws Exception {
        MybatisSqlSessionFactoryBean fb = new MybatisSqlSessionFactoryBean();
        fb.setDataSource(dataSource);
        fb.setConfigLocation(new DefaultResourceLoader().getResource(myBatisConfiguration));
        fb.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperLocations));
        fb.setTypeAliasesPackage(typeAliasesPackage);
        fb.setTypeEnumsPackage(typeEnumsPackage);

        fb.setPlugins(interceptor);

        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setIdType(IdType.AUTO);
        globalConfig.setDbConfig(dbConfig);
        fb.setGlobalConfig(globalConfig);

        return fb.getObject();
    }

    @Bean(name = "transactionManager")
    @Primary
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
