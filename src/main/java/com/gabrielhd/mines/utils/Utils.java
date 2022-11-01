package com.gabrielhd.mines.utils;

import com.gabrielhd.mines.mines.MineBlock;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Utils {

    public static String Color(String s) {
        return s.replaceAll("&", "§");
    }

    public static boolean parseBoolean(String s) {
        return s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("confirmed");
    }

    public static ArrayList<MineBlock> mapComposition(Map<MaterialData, Float> compositionIn) {
        ArrayList<MineBlock> probabilityMap = new ArrayList<>();
        Map<MaterialData, Float> composition = new HashMap<>(compositionIn);
        double max = 0.0;
        for (Map.Entry<MaterialData, Float> entry : composition.entrySet()) {
            max += entry.getValue();
        }
        if (max < 1.0) {
            composition.put(new MaterialData(0), (float) (1.0 - max));
            max = 1.0;
        }
        double i = 0.0;
        for (Map.Entry<MaterialData, Float> entry2 : composition.entrySet()) {
            double v = entry2.getValue() / max;
            i += v;
            probabilityMap.add(new MineBlock(entry2.getKey(), i));
        }
        return probabilityMap;
    }
}
