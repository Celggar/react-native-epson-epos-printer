package com.celggar.epos;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Locale;


/**
 * Created by Celso on 2019-08-05
 */
public class PrinterHelper implements ReceiveListener {
    private static final String TAG = "PrinterHelper";
    private static Printer mPrinter = null;
    private Activity mContext;
    private String mPrinterId = null;
    private String mMessage, mWarningMsg;
    private JsonArray mParsedData = null;
    private OnPrinterFinishListener mCallback;
    private PrinterResponse mResponse;
    private int mTimesToPrint = 1, mPrinterCounter = 0;
    private boolean mIsATest = false;

    public PrinterHelper(Activity ctx) {
        mContext = ctx;
        mResponse = new PrinterResponse();
        mPrinterCounter = 0;
    }

    public void setOnPrinterFinishListener(OnPrinterFinishListener listener) {
        mCallback = listener;
    }

    /**
     * Printer IP or Mac Address
     *
     * @param ipOrMac IP or MAC Address
     */
    public void setPrinterId(String ipOrMac) {
        String formatId = null;
        if (ipOrMac != null) {
            if (!ipOrMac.contains("TCP:")) {
                formatId = "TCP:" + ipOrMac;
            }
        }
        mPrinterId = formatId;
    }

    private String getEposExceptionMessage(int state) {
        String return_text = "";
        switch (state) {
            case Epos2Exception.ERR_PARAM:
                return_text = "Error en los parámetros de impresión (1)";
                break;
            case Epos2Exception.ERR_CONNECT:
                return_text = "No se pudo conectar a la impresora (2)";
                break;
            case Epos2Exception.ERR_TIMEOUT:
                return_text = "Terminó el tiempo de espera de conexión (3)";
                break;
            case Epos2Exception.ERR_MEMORY:
                return_text = "Error de memoria (4)";
                break;
            case Epos2Exception.ERR_ILLEGAL:
                return_text = "Error ilegal (5)";
                break;
            case Epos2Exception.ERR_PROCESSING:
                return_text = "Error procesando la información (6)";
                break;
            case Epos2Exception.ERR_NOT_FOUND:
                return_text = "No se encuentra la impresora (7)";
                break;
            case Epos2Exception.ERR_IN_USE:
                return_text = "La impresora está en uso (8)";
                break;
            case Epos2Exception.ERR_TYPE_INVALID:
                return_text = "Error tipo inválido (9)";
                break;
            case Epos2Exception.ERR_DISCONNECT:
                return_text = "Error se desconectó la impresora (10)";
                break;
            case Epos2Exception.ERR_ALREADY_OPENED:
                return_text = "La conexión a la impresora ya estaba abierta (11)";
                break;
            case Epos2Exception.ERR_ALREADY_USED:
                return_text = "La impresora se encuentra en uso (12)";
                break;
            case Epos2Exception.ERR_BOX_COUNT_OVER:
                return_text = "Error recuento de cajas terminado (13)";
                break;
            case Epos2Exception.ERR_BOX_CLIENT_OVER:
                return_text = "Error cliente de cajas terminado (14)";
                break;
            case Epos2Exception.ERR_UNSUPPORTED:
                return_text = "No soportado (15)";
                break;
            case Epos2Exception.ERR_FAILURE:
                return_text = "Error falló la impresión (255)";
                break;
            default:
                return_text = String.format(Locale.US, "%d", state);
                break;
        }
        return return_text;
    }

    private String getErrorMessage(PrinterStatusInfo status) {
        String msg = "";
        if (status.getOnline() == Printer.FALSE) {
            msg += "La impresora está fuera de línea.\n";
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += "Verifique la conexión de la impresora y el terminal móvil.\nLa conexión se pierde.";
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += "Cierre la cubierta del rollo de papel.\n";
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += "Verifique el rollo de papel.\n";
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += "Suelte un interruptor de alimentación de papel.\n";
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += "Retire el papel atascado y cierre la cubierta del rollo de papel.\n Retire cualquier papel atascado o sustancias extrañas en la impresora, y luego apague la impresora y vuelva a encenderla.\n";
            msg += "Entonces, si la impresora no se recupera del error, apague y encienda el interruptor.\n";
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += "Apague y encienda el interruptor de la impresora.\nSi se produjeron los mismos errores, incluso se apagó y se apagó la impresora. ";
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += "Espere hasta que se apague el LED de error de la impresora.\n";
                msg += "El cabezal de impresión de la impresora está caliente.\n";
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += "Espere hasta que se apague el LED de error de la impresora.\n";
                msg += "El IC del controlador del motor de la impresora está caliente.\n";
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += "Espere hasta que se apague el LED de error de la impresora.\n";
                msg += "La batería de la impresora está caliente.\n";
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += "Configure el rollo de papel correcto.\n";
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += "Conecte el adaptador de CA o cambie la batería.\nLa batería de la impresora está casi vacía.\n";
        }

        return msg;
    }

    private String getCodeText(int state) {
        String return_text = "";
        switch (state) {
            case Epos2CallbackCode.CODE_SUCCESS:
                return_text = "Impresión correcta";
                break;
            case Epos2CallbackCode.CODE_PRINTING:
                return_text = "Error imprimiendo(13)";
                break;
            case Epos2CallbackCode.CODE_ERR_AUTORECOVER:
                return_text = "Error de auto recuperación (3)";
                break;
            case Epos2CallbackCode.CODE_ERR_COVER_OPEN:
                return_text = "La tapadera está abierta (4)";
                break;
            case Epos2CallbackCode.CODE_ERR_CUTTER:
                return_text = "Error en el cortador(5)";
                break;
            case Epos2CallbackCode.CODE_ERR_MECHANICAL:
                return_text = "Error mecánico(6)";
                break;
            case Epos2CallbackCode.CODE_ERR_EMPTY:
                return_text = "No se detecta papel en la impresora (7)";
                break;
            case Epos2CallbackCode.CODE_ERR_UNRECOVERABLE:
                return_text = "Error no se puede recuperar el proceso (8)";
                break;
            case Epos2CallbackCode.CODE_ERR_FAILURE:
                return_text = "Error falló la impresión (255)";
                break;
            case Epos2CallbackCode.CODE_ERR_NOT_FOUND:
                return_text = "No se encuentra la impresora (7)";
                break;
            case Epos2CallbackCode.CODE_ERR_SYSTEM:
                return_text = "Error del sistema (9)";
                break;
            case Epos2CallbackCode.CODE_ERR_PORT:
                return_text = "Error en el puerto(10)";
                break;
            case Epos2CallbackCode.CODE_ERR_TIMEOUT:
                return_text = "Terminó el tiempo de espera de conexión (1)";
                break;
            case Epos2CallbackCode.CODE_ERR_JOB_NOT_FOUND:
                return_text = "No se encuentra el trabajo de impresión (12)";
                break;
            case Epos2CallbackCode.CODE_ERR_SPOOLER:
                return_text = "Error en la cola de impresión (14)";
                break;
            case Epos2CallbackCode.CODE_ERR_BATTERY_LOW:
                return_text = "Error la batería está baja(15)";
                break;
            case Epos2CallbackCode.CODE_ERR_TOO_MANY_REQUESTS:
                return_text = "Demasiadas peticiones realizadas (16)";
                break;
            case Epos2CallbackCode.CODE_ERR_REQUEST_ENTITY_TOO_LARGE:
                return_text = "Entidad de solicitud demasiado grande(17)";
                break;
            default:
                return_text = String.format(Locale.US, "%d", state);
                break;
        }
        return return_text;
    }

    public String parseData(String jsonArray) {
        try {
            mParsedData = JsonParser.parseString(jsonArray).getAsJsonArray();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public void runPrintReceiptSequence(int timesToPrint) {
        mTimesToPrint = timesToPrint;
        runPrintReceiptSequence(false, timesToPrint);
    }

    public void runPrintReceiptSequence(boolean isATest, int timesToPrint) {
        mMessage = null;
        mWarningMsg = null;
        mPrinterCounter = 0;
        mTimesToPrint = timesToPrint;
        mIsATest = isATest;
        new PrintSafely().execute(new PrinterItem(isATest, timesToPrint));
    }

    public boolean initializeObject() {
        try {
            mPrinter = new Printer(Printer.TM_T88, Printer.MODEL_ANK, mContext);
            mPrinter.setReceiveEventListener(this);
        } catch (Exception e) {
            //
            mMessage = getException(e, "Printer");
            return false;
        }
        return true;
    }

    private void createTestReceiptData() {
        String method = "";
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addFeedLine(1);
            textData.append("------------------------------\n");
            textData.append("PRINT #").append("\n");
            textData.append("\\(´o`)/SMALL TEST FOR HAPPY TREES\\(´-.-`)ﾉ\n");
            textData.append("(/ﾟoﾟ)/PRUEBA PARA ÁRBOLES FELICES\\(ﾟoﾟ\\)\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());

            method = "addBarcode";
            mPrinter.addBarcode("240010001991",
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        } catch (Exception e) {
            //
            mMessage = getException(e, method);
        }
    }

    String[] mCommands = {"text", "centered", "line", "dotted", "left", "right", "barcode", "image"};
    int mAlignment = -1;

    public void customReceiptData() {
        try {
            for (int i = 0; i < mParsedData.size(); i++) {
                JsonObject obj = mParsedData.get(i).getAsJsonObject();
                for (String command : mCommands) {
                    JsonElement value = obj.get(command);
                    if (value != null) {
                        StringBuilder text = new StringBuilder();
                        switch (command) {
                            case "text":
                                alignTo(Printer.ALIGN_LEFT);
                                text.append(value.getAsString()).append("\n");
                                break;

                            case "centered":
                                alignTo(Printer.ALIGN_CENTER);
                                text.append(value.getAsString()).append("\n");
                                break;

                            case "line":
                                int lineQty = value.getAsInt();
                                for (int j = 0; j < lineQty; j++) {
                                    text.append("\n");
                                }
                                break;

                            case "dotted":
                                alignTo(Printer.ALIGN_CENTER);
                                int dottedQty = value.getAsInt();
                                for (int j = 0; j < dottedQty; j++) {
                                    text.append("------------------------------").append("\n");
                                }
                                break;

                            case "right":
                            case "left":
                                boolean isLeft = command.equals("left");
                                JsonElement jNextVal = obj.get(isLeft ? "right" : "left");
                                if (jNextVal != null) {
                                    text.append(padLine(value.getAsString(), jNextVal.getAsString(), 42));
                                } else {
                                    mPrinter.addFeedLine(0);
                                    alignTo(isLeft ? Printer.ALIGN_LEFT : Printer.ALIGN_RIGHT);
                                    mPrinter.addText(value.getAsString());
                                }
                                text.append("\n");
                                break;
                            case "barcode":
                                alignTo(Printer.ALIGN_CENTER);
                                JsonElement jType = obj.get("type");
                                int type = jType == null ? Printer.BARCODE_CODE93 : jType.getAsInt();
                                if (type > 15 || type < 0) {
                                    type = Printer.BARCODE_CODE93;
                                }
                                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                                mPrinter.addBarcode(
                                        value.getAsString(),
                                        type,
                                        Printer.HRI_BELOW,
                                        Printer.FONT_A,
                                        2,
                                        100);
                                text.append("\n");
                                break;

                            case "image":
                                alignTo(Printer.ALIGN_CENTER);
                                //187, 70
                                FutureTarget<Bitmap> futureBitmap = Glide.with(mContext)
                                        .asBitmap()
                                        .override(374, 70)
                                        .load(value.getAsString())
                                        .submit();
                                try {
                                    Bitmap bitmap = futureBitmap.get();
                                    mPrinter.addImage(bitmap, 0, 0,
                                            bitmap.getWidth(),
                                            bitmap.getHeight(),
                                            Printer.COLOR_1,
                                            Printer.MODE_MONO,
                                            Printer.HALFTONE_DITHER,
                                            Printer.PARAM_DEFAULT,
                                            Printer.COMPRESS_AUTO);
                                } catch (Exception e) {
                                    text.append(e.getMessage());
                                }
                                text.append("\n");
                                break;

                        }
                        mPrinter.addText(text.toString());
                        break;
                    }
                }
            }
            mPrinter.addCut(Printer.CUT_FEED);

        } catch (Exception e) {
            mMessage = getException(e, "ReceiptData");
        }
    }

    private void alignTo(int alignment) throws Epos2Exception {
        if (mAlignment != alignment) {
            mAlignment = alignment;
            mPrinter.addTextAlign(alignment);
        }
    }

    /**
     * utility: pads two strings to columns per line
     */
    private String padLine(String partOne, String partTwo, int columnsPerLine) {
        String concat;
        if (partOne.length() + partTwo.length() > columnsPerLine) {
            concat = partOne + " " + partTwo;
        } else {
            int padding = columnsPerLine - (partOne.length() + partTwo.length());
            StringBuilder builder = new StringBuilder();
            builder.append(partOne);
            for (int i = 0; i < padding; i++) {
                builder.append(" ");
            }
            builder.append(partTwo);
            concat = builder.toString();
        }
        return concat;
    }

    private void sendException(Exception e, String method) {
        String msg;
        if (e instanceof Epos2Exception) {
            msg = getEposExceptionMessage(((Epos2Exception) e).getErrorStatus());
        } else {
            msg = e.toString();
        }
        mResponse.trustStatus = false;
        mResponse.message = msg;
        mResponse.isWarning = false;
        if (mCallback != null) {
            mCallback.OnPrinterFinish(mResponse);
        }
    }

    private String getException(Exception e, String method) {
        String msg;
        if (e instanceof Epos2Exception) {
            msg = getEposExceptionMessage(((Epos2Exception) e).getErrorStatus());
        } else {
            msg = e.toString();
        }
        return msg;
    }

    private void sendException(PrinterStatusInfo status) {
        mResponse.trustStatus = false;
        mResponse.message = getErrorMessage(status);
        mResponse.isWarning = false;
        if (mCallback != null) {
            mCallback.OnPrinterFinish(mResponse);
        }
    }

    private void sendWarning(String message) {
        mResponse.trustStatus = false;
        mResponse.message = message;
        mResponse.isWarning = true;
        if (mCallback != null) {
            mCallback.OnPrinterFinish(mResponse);
        }
    }

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();
        dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            try {
                mPrinter.disconnect();
            } catch (Exception e) {
                //
            }
            mMessage = getErrorMessage(status);
            return false;
        }

        try {
            for (int i = 0; i < mTimesToPrint; i++) {
                mPrinter.sendData(Printer.PARAM_DEFAULT);
            }
        } catch (Exception e) {
            mMessage = getException(e, "sendData");
            try {
                mPrinter.disconnect();
            } catch (Exception ex) {
                //
            }
            return false;
        }

        return true;
    }

    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            mPrinter.connect(mPrinterId, Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            mMessage = getException(e, "connect");
            return false;
        }

        try {
            mPrinter.beginTransaction();
        } catch (Exception e) {
            mMessage = getException(e, "beginTransaction");
            try {
                mPrinter.disconnect();
            } catch (Epos2Exception e1) {
                return false;
            }
            return false;
        }
        return true;
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += "El rollo de papel está casi finalizado.\n";
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += "El nivel de batería de la impresora es bajo.\n";
        }

        if (!warningsMsg.equals(""))
            mWarningMsg = warningsMsg;
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }
        if (status.getConnection() == Printer.FALSE) {
            return false;
        } else return status.getOnline() != Printer.FALSE;

    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }
        mPrinter.clearCommandBuffer();
        mPrinter.setReceiveEventListener(null);
        mPrinter = null;
    }

    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        } catch (Exception e) {
            //
        }

        try {
            mPrinter.disconnect();
        } catch (Exception e) {
            //
        }

        finalizeObject();
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        mPrinterCounter++;
        if (mPrinterCounter == mTimesToPrint) {
            mPrinterCounter = 0;
            Activity activity = mContext;
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public synchronized void run() {
                        sendCallback(status);
                    }
                });
            } else {
                sendCallback(status);
            }

        }

    }

    private void sendCallback(PrinterStatusInfo status) {
        String errorMsg = getErrorMessage(status);
        mResponse.trustStatus = errorMsg.equals("");
        mResponse.message = getErrorMessage(status);
        new Thread(this::disconnectPrinter).start();

        if (mCallback != null) {
            mCallback.OnPrinterFinish(mResponse);
        }
    }

    class PrinterItem {
        public boolean isATest;
        public int timesToPrint;

        public PrinterItem() {

        }

        public PrinterItem(boolean test, int times) {
            isATest = test;
            timesToPrint = times;
        }
    }

    public class PrintSafely extends AsyncTask<PrinterItem, String, String> {


        // This is run in a background thread
        @Override
        protected String doInBackground(PrinterItem... params) {
            try {
                if (initializeObject()) {
                    boolean receiptCreated = false;
                    if (mParsedData != null) {
                        if (mIsATest) {
                            createTestReceiptData();
                            receiptCreated = mMessage == null;
                        } else {
                            //setupReceiptData();
                            customReceiptData();
                            receiptCreated = mMessage == null;
                        }
                    } else if (mIsATest) {
                        createTestReceiptData();
                        receiptCreated = mMessage == null;
                    }

                    if (receiptCreated && !printData()) {
                        finalizeObject();
                    }
                }
            } catch (Exception e) {
                return getException(e, "Async");
            }

            return mMessage;
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String e) {
            super.onPostExecute(e);
            if (mWarningMsg != null) {
                mResponse.trustStatus = false;
                mResponse.message = mWarningMsg;
                mResponse.isWarning = true;
                if (mCallback != null) {
                    mCallback.OnPrinterFinish(mResponse);
                }
            }
            if (e != null) {
                mResponse.trustStatus = false;
                mResponse.message = e;
                mResponse.isWarning = false;
                if (mCallback != null) {
                    mCallback.OnPrinterFinish(mResponse);
                }
            }

        }
    }
}
