package com.giraffes.tgbot.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

@Slf4j
@Configuration
public class DataSourceConfig {
    @Value("${datasource.url}")
    private String databaseDefaultUrl;
    @Value("${datasource.username}")
    private String databaseDefaultUsername;
    @Value("${datasource.password}")
    private String databaseDefaultPassword;

    @Bean
    @SneakyThrows
    public DataSource getDataSource() {
        String dbUrl = databaseDefaultUrl;
        String username = databaseDefaultUsername;
        String password = databaseDefaultPassword;
        if (StringUtils.hasText(System.getenv("DATABASE_URL"))) {
            URI dbUri = new URI(System.getenv("DATABASE_URL"));

            dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
            username = dbUri.getUserInfo().split(":")[0];
            password = dbUri.getUserInfo().split(":")[1];
        }

        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
