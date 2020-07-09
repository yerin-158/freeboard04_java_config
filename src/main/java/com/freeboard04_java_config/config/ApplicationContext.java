package com.freeboard04_java_config.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ImportResource({"classpath:applicationContext.xml"})
@EnableTransactionManagement
public class ApplicationContext {

    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/free_board?serverTimezone=UTC&useSSL=false");
        dataSource.setUsername("robin");
        dataSource.setPassword("robin549866pass!");

        return dataSource;
    }

}
