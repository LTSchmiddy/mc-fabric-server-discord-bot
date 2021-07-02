package net.lt_schmiddy.serverdiscordbot.database;

import java.util.Random;

public class Utils {
    public static String generatePairCode(int length) {
        Random r = new Random();
        String retVal = "";

        for (int i = 0; i < length; i++) {
            retVal += r.nextInt(10);
        }
        return retVal;
    }
}
