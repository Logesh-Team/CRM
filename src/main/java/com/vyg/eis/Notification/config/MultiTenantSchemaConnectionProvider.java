package com.vyg.eis.Notification.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provider that provides tenant-specific connection handling in a multi-tenant application. The tenant
 * distinction is realized by using separate schemas, i.e. each tenant uses its own schema in a shared
 * (common) database.
 */
@Component
public class MultiTenantSchemaConnectionProvider implements MultiTenantConnectionProvider {

    private final DataSource dataSource;

    @Autowired
    public MultiTenantSchemaConnectionProvider(DataSource dataSource) {
        System.out.println("23");
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        System.out.println("29");
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        System.out.println("35");
        connection.close();
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        System.out.println("41");
        final Connection connection = getAnyConnection();
        connection.setSchema((String) tenantIdentifier);
        return connection;
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        System.out.println("49");
        connection.setSchema(CurrentTenantResolver.DEFAULT_SCHEMA);
        releaseAnyConnection(connection);
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
