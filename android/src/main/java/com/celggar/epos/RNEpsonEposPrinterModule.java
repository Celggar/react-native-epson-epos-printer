
package com.celggar.epos;

import android.os.Build;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.Epos2Exception;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNEpsonEposPrinterModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static final String PRINTER_ERROR = "ERROR";
    private String mMessage = "";
    public int mPrinterSeries = 0;
    public int mTextLanguage = 0;

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
    public void print(Integer qty, String connectionString, String dataToPrint, final Promise promise) {
        mMessage = "";
        PrinterHelper printerHelper = new PrinterHelper(getCurrentActivity());
        if (mPrinterSeries > 20 ||  mPrinterSeries < 0){
            mPrinterSeries = 0;
        }
        if (mTextLanguage > 7 ||mTextLanguage < 0){
            mTextLanguage = 0;
        }
        printerHelper.setPrinterClass(mPrinterSeries, mTextLanguage);
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
        printerHelper.setPrinterId(connectionString);
        String errorMessage = printerHelper.parseData(dataToPrint);
        if (errorMessage != null) {
            promise.reject(PRINTER_ERROR, errorMessage);
        } else {
            printerHelper.runPrintReceiptSequence(qty);
        }
    }

    @ReactMethod
    public void printTest(String connectionString, final Promise promise) {
        mMessage = "";
        PrinterHelper printerHelper = new PrinterHelper(getCurrentActivity());
        if (mPrinterSeries > 20 ||  mPrinterSeries < 0){
            mPrinterSeries = 0;
        }
        if (mTextLanguage > 7 ||mTextLanguage < 0){
            mTextLanguage = 0;
        }
        printerHelper.setPrinterClass(mPrinterSeries, mTextLanguage);
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
        printerHelper.setPrinterId(connectionString);
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

    @ReactMethod
    public void setPrinterClass(int setPrinterSeries, int setTextLanguage, final Promise promise) {
        mPrinterSeries = setPrinterSeries;
        mTextLanguage = setTextLanguage;
    }

    @ReactMethod
    public void startDiscover(final Promise promise) {
        FilterOption mFilterOption = new FilterOption();
        mFilterOption.setDeviceType(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NAME);

        try {
            Discovery.start(reactContext, mFilterOption, mDiscoveryListener);
            promise.resolve(null);
        }
        catch (Epos2Exception e) {
            String errorMessage = PrinterHelper.getEposExceptionMessage(e.getErrorStatus());
            promise.reject(PRINTER_ERROR, errorMessage);
        }
    }

    private void sendEvent(String eventName, WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            try{
                WritableMap item = Arguments.createMap();
                item.putString("PrinterName", deviceInfo.getDeviceName());
                item.putString("Target", deviceInfo.getTarget());
                sendEvent("discoverPrinter", item);
            } catch (Exception e){
                WritableMap item = Arguments.createMap();
                item.putString("error", e.toString());
                sendEvent("discoverPrinterError", item);
            }
        }
    };

    @ReactMethod
    public void stopDiscovery(final Promise promise) {
        while (true) {
            try {
                Discovery.stop();
                promise.resolve(null);
                break;
            }
            catch (Epos2Exception e) {
                if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                    String errorMessage = PrinterHelper.getEposExceptionMessage(e.getErrorStatus());
                    promise.reject(PRINTER_ERROR, errorMessage);
                    break;
                }
            }
        }
    }
}
