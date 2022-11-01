package com.gabrielhd.mines.tasks;

import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.mines.Mine;

public class ResetTask implements Runnable {

    @Override
    public void run() {
        for(Mine mine : MineManager.getMines()) {
            mine.reset(true);
        }
    }
}
