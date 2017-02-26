package sg.assignment.shopback.moviediscovery.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by archie on 25/2/17.
 */

public class FormatterHelper {

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static final DecimalFormat popularityPrecision = new DecimalFormat("0.0000");

    public static String formatDate(Date date) {
        return dateFormatter.format(date);
    }

    public static Date parseDate(String date) {
        try {
            return dateFormatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatPopularity(double popularity) {
        return popularityPrecision.format(Math.round(popularity * 10000.0) / 10000.0);
    }


}
