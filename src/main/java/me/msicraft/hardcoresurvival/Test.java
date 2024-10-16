package me.msicraft.hardcoresurvival;

import me.msicraft.hardcoresurvival.Utils.MathUtil;

public class Test {

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            double value = MathUtil.getRangeRandomDouble(0.25, 0);
            if (value > 0.25) {
                System.out.println("D: " + value);
                return;
            }
            if (value < 0) {
                System.out.println("D2: " + value + " | t: " + i);
                return;
            }
            //System.out.println("Value: " + value);
        }
    }

}
