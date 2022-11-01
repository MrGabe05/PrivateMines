package com.gabrielhd.mines.database.types;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.database.DataHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLite extends DataHandler {

    private Connection connection;
    
    public SQLite() {
        this.connect();
    }

    protected synchronized void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + PrivateMines.getInstance().getDataFolder() + "/Database.db");
        } catch (SQLException | ClassNotFoundException ex) {
            PrivateMines.error("Can't initialize database connection! Please check your configuration!");
            PrivateMines.error("If this error persists, please report it to the developer!");

            ex.printStackTrace();
        }

        this.setupTable();
    }

    @Override
    protected Connection getConnection() {
        return this.connection;
    }
}
