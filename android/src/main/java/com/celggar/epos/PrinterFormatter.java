package com.celggar.epos;

import android.app.Activity;
import android.util.Log;


/**
 * Created by Celso on 2020-Sept-11
 */
public class PrinterFormatter {

    Activity mContext;

    public PrinterFormatter(Activity activity) {
        mContext = activity;
    }

    public void runTest(String jsonArray) {
        PrinterHelper printerHelper = new PrinterHelper(mContext);
        printerHelper.setOnPrinterFinishListener(response -> {
            Log.e("response", response.trustStatus + "");
        });
        printerHelper.setPrinterId("50:57:9C:56:8B:C9");
        String errorMessage = printerHelper.parseData(jsonArray);
        if (errorMessage != null) {
            Log.e("error", errorMessage + "");
        } else {
            printerHelper.runPrintReceiptSequence(1);
        }

    }

}
