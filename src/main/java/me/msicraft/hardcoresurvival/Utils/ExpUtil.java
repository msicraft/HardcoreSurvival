package me.msicraft.hardcoresurvival.Utils;

import org.bukkit.entity.Player;

public class ExpUtil {

    public static int getExpToLevelUp(int level){
        if(level <= 15){
            return 2*level+7;
        } else if(level <= 30){
            return 5*level-38;
        } else {
            return 9*level-158;
        }
    }

    public static int getExpAtLevel(int level){
        if(level <= 16){
            return (int) (Math.pow(level,2) + 6*level);
        } else if(level <= 31){
            return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
        } else {
            return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
        }
    }

    public static int getPlayerExp(Player player){
        int exp = 0;
        int level = player.getLevel();

        exp += getExpAtLevel(level);

        exp += Math.round(getExpToLevelUp(level) * player.getExp());

        return exp;
    }

    public static void changePlayerExp(Player player, int exp){
        int currentExp = getPlayerExp(player);

        player.setExp(0);
        player.setLevel(0);

        int newExp = currentExp + exp;
        player.giveExp(newExp);
    }

    public static void resetPlayerExp(Player player) {
        player.setExp(0);
        player.setLevel(0);
    }

}
