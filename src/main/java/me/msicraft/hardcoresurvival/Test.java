package me.msicraft.hardcoresurvival;

public class Test {

    public static void main(String[] args) {
        int x = 10;
        for (int i = 0; i < 10000; i++) {
            String a = "- world:" + x + ":15:0";
            if (i > 5000) {
                a = "- world:0:15:" + x;
            }
            System.out.println(a);
            x++;
        }
    }

}
