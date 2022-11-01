package com.gabrielhd.mines.database.types;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.database.DataHandler;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL extends DataHandler {

    private final String url;
    private final String username;
    private final String password;

    private Connection connection;
    private HikariDataSource ds;
    
    public MySQL(String host, String port, String database, String username, String password) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;

        try {
            this.setConnectionArguments();
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException) {
                PrivateMines.error("Invalid database arguments! Please check your configuration!");
                PrivateMines.error("If this error persists, please report it to the developer!");

                throw new IllegalArgumentException(e);
            }
            if (e instanceof HikariPool.PoolInitializationException) {
                PrivateMines.error("Can't initialize database connection! Please check your configuration!");
                PrivateMines.error("If this error persists, please report it to the developer!");
                throw new HikariPool.PoolInitializationException(e);
            }
            PrivateMines.error("Can't use the Hikari Connection Pool! Please, report this error to the developer!");
            throw e;
        }

        this.setupTable();
    }

    protected synchronized void setConnectionArguments() throws RuntimeException {
        (this.ds = new HikariDataSource()).setPoolName("PrivateMines MySQL");

        this.ds.setDriverClassName("com.mysql.jdbc.Driver");
        this.ds.setJdbcUrl(this.url);
        this.ds.addDataSourceProperty("cachePrepStmts", "true");
        this.ds.addDataSourceProperty("prepStmtCacheSize", "250");
        this.ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.ds.addDataSourceProperty("characterEncoding", "utf8");
        this.ds.addDataSourceProperty("encoding", "UTF-8");
        this.ds.addDataSourceProperty("useUnicode", "true");
        this.ds.addDataSourceProperty("useSSL", "false");
        this.ds.setUsername(this.username);
        this.ds.setPassword(this.password);
        this.ds.setMaxLifetime(180000L);
        this.ds.setIdleTimeout(60000L);
        this.ds.setMinimumIdle(1);
        this.ds.setMaximumPoolSize(8);
        try {
            this.connection = this.ds.getConnection();
        }
        catch (SQLException e) {
            PrivateMines.error("Error on setting connection!");
        }

        PrivateMines.info("Connection arguments loaded, Hikari ConnectionPool ready!");
    }

    @Override
    protected Connection getConnection() {
        return this.connection;
    }
}
