package me.msicraft.hardcoresurvival.Utils;

public class MathUtil {

    private MathUtil() {}

    public static double getRangeRandomDouble(double max, double min) {
        if (max == min || Double.compare(max, min) == 0) {
            return max;
        }
        return (Math.random() * (max - min) + min);
    }

    public static int getRangeRandomInt(int max, int min) {
        if (min == max) {
            return max;
        }
        return (int) (Math.random() * (max - min + 1) + min);
    }


}
