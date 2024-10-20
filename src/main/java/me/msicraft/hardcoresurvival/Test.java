package me.msicraft.hardcoresurvival;

import me.msicraft.hardcoresurvival.Utils.MathUtil;

public class Test {

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            double random = MathUtil.getRangeRandomDouble(0.15, -0.15);
            System.out.println(random);
        }
    }

}
