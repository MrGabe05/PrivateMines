package com.gabrielhd.mines.database;

import com.gabrielhd.mines.config.Config;
import com.gabrielhd.mines.database.types.MySQL;
import com.gabrielhd.mines.database.types.SQLite;

public class Database {

    private static DataHandler storage;
    
    public Database() {
        String host = Config.HOST;
        String port = Config.PORT;
        String db = Config.DATABASE;
        String user = Config.USERNAME;
        String pass = Config.PASSWORD;

        if (Config.TYPE.equalsIgnoreCase("mysql")) {
            storage = new MySQL(host, port, db, user, pass);
        } else {
            storage = new SQLite();
        }
    }
    
    public static DataHandler getStorage() {
        return Database.storage;
    }
}
