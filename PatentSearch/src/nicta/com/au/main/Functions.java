/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.main;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author rbouadjenek
 */
public class Functions {

    public static String getTimer(long millis) {
        return String.format("%d hour, %d min, %d sec",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?") || str.matches("-?\\d+(\\,\\d+)?");  //match a number with optional '-' and decimal.
    }
    public static final DecimalFormat df = new DecimalFormat();

    static {
        df.setMaximumFractionDigits(2); //arrondi à 2 chiffres apres la virgules
        df.setMinimumFractionDigits(2);
        df.setDecimalSeparatorAlwaysShown(true);
    }

    public static void main(String[] args) {
        System.out.println(isNumeric("a2,0"));
    }
}
