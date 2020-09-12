
package com.celggar.epos;

import android.os.Build;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RNEpsonEposPrinterModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static final String PRINTER_ERROR = "ERROR";
    private String mMessage = "";

    public RNEpsonEposPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNEpsonEposPrinter";
    }

    @ReactMethod
    public void isAvailable(Promise promise) {
        promise.resolve(true);
    }

    @ReactMethod
    public void print(Integer qty, String ipOrMac, String dataToPrint, final Promise promise) {
        mMessage = "";
        PrinterHelper printerHelper = new PrinterHelper(getCurrentActivity());
        printerHelper.setOnPrinterFinishListener(response -> {
            if (response.trustStatus) {//trustStatus indica si la impresión se dió de manera correcta
                promise.resolve(mMessage);
            } else if (response.isWarning) {//en caso que no se haya dado, es probable que exista algún warning, si lo hay le muestra al usuario cuál es
                mMessage = "(No Data)";
                if (response.message != null)
                    mMessage = response.message;
            } else {//en caso que sea incorrecto y no exista ningún warning quiere decir que definitivamente es un error y no se podrá imprimir en esta ocasión
                String message = response.message != null ? response.message : "No se puede conectar a la impresora (77)";
                promise.reject(PRINTER_ERROR, message);
            }
        });
        printerHelper.setPrinterId(ipOrMac);
        String errorMessage = printerHelper.parseData(dataToPrint);
        if (errorMessage != null) {
            promise.reject(PRINTER_ERROR, errorMessage);
        } else {
            printerHelper.runPrintReceiptSequence(qty);
        }
    }

    @ReactMethod
    public void printTest(String ipOrMac, final Promise promise) {
        mMessage = "";
        PrinterHelper printerHelper = new PrinterHelper(getCurrentActivity());
        printerHelper.setOnPrinterFinishListener(response -> {
            if (response.trustStatus) {//trustStatus indica si la impresión se dió de manera correcta
                promise.resolve(mMessage);
            } else if (response.isWarning) {//en caso que no se haya dado, es probable que exista algún warning, si lo hay le muestra al usuario cuál es
                mMessage = "(No Data)";
                if (response.message != null)
                    mMessage = response.message;
            } else {//en caso de que sea incorrecto y no exista ningún warning quiere decir que definitivamente es un error y no se podrá imprimir en esta ocasión
                String message = response.message != null ? response.message : "No se puede conectar a la impresora (77)";
                promise.reject(PRINTER_ERROR, message);
            }
        });
        printerHelper.setPrinterId(ipOrMac);
        printerHelper.runPrintReceiptSequence(true, 1);
    }

    private String getTestPayload() {
        String json = null;
        try {
            InputStream is = getCurrentActivity().getAssets().open("printer_payload" + ".json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                json = new String(buffer, StandardCharsets.UTF_8);
            } else {
                json = new String(buffer, Charset.forName("UTF-8"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
