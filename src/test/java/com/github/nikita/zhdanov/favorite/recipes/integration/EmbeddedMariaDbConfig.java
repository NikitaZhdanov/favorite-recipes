package com.github.nikita.zhdanov.favorite.recipes.integration;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@Configuration
public class EmbeddedMariaDbConfig {

    @Bean
    public MariaDB4jSpringService mariaDB4jSpringService() throws IOException {
        final MariaDB4jSpringService service = new MariaDB4jSpringService();
        final int port = SocketUtils.findAvailableTcpPort();
        service.setDefaultPort(port);
        final File tempDirectory = FileUtils.getTempDirectory();
        final File baseDir = new File(tempDirectory, "mariadb_" + port + "_base");
        final File dataDir = new File(tempDirectory, "mariadb_" + port + "_data");

        FileUtils.forceMkdir(baseDir);
        FileUtils.forceMkdir(dataDir);

        service.setDefaultBaseDir(baseDir.getAbsolutePath());
        service.setDefaultDataDir(dataDir.getAbsolutePath());

        return service;
    }

    @Bean
    public DataSource dataSource(final MariaDB4jSpringService mariaDB4jSpringService) throws ManagedProcessException {
        final String databaseName = "test?useUnicode=yes&characterEncoding=UTF-8";
        final String datasourceDriver = "org.mariadb.jdbc.Driver";
        mariaDB4jSpringService.getDB().createDB(databaseName);
        final DBConfigurationBuilder config = mariaDB4jSpringService.getConfiguration();
        return DataSourceBuilder
                .create()
                .username("root")
                .password("")
                .url(config.getURL(databaseName))
                .driverClassName(datasourceDriver)
                .build();
    }
}
