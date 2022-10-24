package com.tgbot.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;

@Slf4j
@Configuration
public class DataSourceConfig {
    @Value("${datasource.url:#{null}}")
    private String databaseDefaultUrl;
    @Value("${datasource.username:#{null}}")
    private String databaseDefaultUsername;
    @Value("${datasource.password:#{null}}")
    private String databaseDefaultPassword;

    @Value("${datasource.driver}")
    private String databaseDriver;

    @Bean
    @SneakyThrows
    public DataSource getDataSource() {
        String dbUrl = databaseDefaultUrl;
        String username = databaseDefaultUsername;
        String password = databaseDefaultPassword;
        if (StringUtils.isNotBlank(System.getenv("DATABASE_URL"))) {
            URI dbUri = new URI(System.getenv("DATABASE_URL"));

            dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
            username = dbUri.getUserInfo().split(":")[0];
            password = dbUri.getUserInfo().split(":")[1];
        }

        if (StringUtils.isAnyBlank(dbUrl, username, password)) {
            throw new RuntimeException("Some DB properties are not set");
        }

        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(username)
                .password(password)
                .driverClassName(databaseDriver)
                .build();
    }
}
