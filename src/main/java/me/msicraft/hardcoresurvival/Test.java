package me.msicraft.hardcoresurvival;

import java.util.List;

public class Test {

    public static void main(String[] args) {
        List<String> list = List.of("hs_test.1", "hs_test.2", "hs_test.3", "hs.df", "v.d.v", "hs_d.h");
        for (String s : list) {
            if (s.startsWith("hs_test")) {
                s = s.substring(s.lastIndexOf(".") + 1); //
                System.out.println("True: " + s);
            } else {
                System.out.println("False: " + s);
            }
        }
    }

}
