package com.gabrielhd.mines.tasks;

import com.gabrielhd.mines.database.Database;
import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.mines.Mine;

public class SaveTask implements Runnable {

    @Override
    public void run() {
        for(Mine mine : MineManager.getMines()) {
            Database.getStorage().saveMine(mine);
        }
    }
}
