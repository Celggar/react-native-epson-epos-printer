package com.celggar.epos;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains general shared Java only methods.
 */
public class GeneralHelper {

    private static final String TAG = "GeneralHelper";
    public static final String FULFILLMENT_PDV_PLAN_ID = "75963CA6-D4C7-43BD-9342-4C3FF372A37B";

    @SuppressLint("SimpleDateFormat")
    public static String getSimpleDate(String inputDate) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
            Date date = input.parse(inputDate);// parse input
            inputDate = output.format(date);// format output
        } catch (Exception e) {
            //
        }
        return inputDate;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateHour(String createdDate) {
        String date = "";
        SimpleDateFormat formatter_from = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat formatter_to = new SimpleDateFormat("hh:mm");
        try {
            Date d = formatter_from.parse(createdDate);
            date = formatter_to.format(d);
        } catch (Exception e) {
            //
        }
        return date;
    }

}
