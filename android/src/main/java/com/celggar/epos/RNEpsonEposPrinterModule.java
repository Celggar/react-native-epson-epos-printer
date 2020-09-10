
package com.celggar.epos;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

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
        boolean successful = printerHelper.parseData(dataToPrint);
        if (!successful) {
            String message = "No se puede conectar a la impresora (77)";
            promise.reject(PRINTER_ERROR, message);
        }
        printerHelper.executePrint(qty);
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
}
