package me.msicraft.hardcoresurvival;

import me.msicraft.hardcoresurvival.Utils.MathUtil;

public class Test {

    public static void main(String[] args) {
        int price = 500;
        for (int i = 0; i < 200; i++) {
            double x = MathUtil.getRangeRandomDouble(0.1, -0.1);
            double c = price * x;
            price = (int) (price + c);
            System.out.println(price + " | Change: " + c);
        }
    }

}
