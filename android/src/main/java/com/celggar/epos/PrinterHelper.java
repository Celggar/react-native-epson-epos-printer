package com.celggar.epos;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;


/**
 * Created by celso on 2019-08-05
 */
public class PrinterHelper implements ReceiveListener {
    private static final String TAG = "PrinterHelper";
    public static final int REQUEST_CODE = 110;
    public static String STATUS_CODE = "com.hypernovalabs.aludrasales.STATUS_CODE";
    public static String ERROR_MESSAGE = "com.hypernovalabs.aludrasales.ERROR_MESSAGE";
    private static Printer mPrinter = null;
    private Activity mContext;
    private String mPrinterId = null;
    private InvoiceObj mInvoice = null;
    private String mMessage, mWarningMsg;
    private JSONObject mParsedData = null;
    private Bitmap mLogo, mQRCode;
    private OnPrinterFinishListener mCallback;
    private PrinterResponse mResponse;
    private int mTimesToPrint = 1, mPrinterCounter = 0, mImageCounter = 0;
    private boolean mIsATest = false;

    public PrinterHelper(Activity ctx) {
        mContext = ctx;
        mResponse = new PrinterResponse();
        mPrinterCounter = 0;
        mImageCounter = 0;
        mLogo = null;
        mQRCode = null;
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

    public void setLogo(Bitmap logo) {
        mLogo = logo;
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

    public boolean parseData(String jsonObject) {
        try {
            mInvoice = new InvoiceObj();
            JSONObject data = new JSONObject(jsonObject);
            mParsedData = data;
            if (!data.isNull("cufe")) {
                mInvoice.documentName = "FACTURA";
                mInvoice.cufe = data.isNull("cufe") ? "" : data.getString("cufe");
                mInvoice.qrCodeImage = data.isNull("qrcodeimage") ? "" : data.getString("qrcodeimage");
                mInvoice.urlcufe = data.isNull("urlcufe") ? "" : data.getString("urlcufe");
                JSONObject aludraData = data.getJSONObject("aludradata");
                JSONObject salesDocInfo = aludraData.getJSONObject("Data").getJSONObject("SalesDocumentInfo");
                mInvoice.setSalesDoc(salesDocInfo);
                JSONObject posResponse = data.isNull("PosResponse") ? null : data.getJSONObject("PosResponse");
                if (posResponse != null) {
                    mInvoice.issuerName = posResponse.isNull("issuer_name") ? "" : posResponse.getString("issuer_name");
                    mInvoice.paymentType = posResponse.isNull("paymentType") ? "" : posResponse.getString("paymentType");
                    mInvoice.authCode = posResponse.isNull("authorizationCode") ? "" : posResponse.getString("authorizationCode");
                    mInvoice.cardNumber = posResponse.isNull("pan") ? null : posResponse.getString("pan");
                }
            } else {
                mInvoice.documentName = "NOTA DE CRÉDITO";
                JSONObject dgiResponse = data.getJSONObject("DgiResponse");
                mInvoice.cufe = dgiResponse.isNull("cufe") ? "" : dgiResponse.getString("cufe");
                mInvoice.qrCodeImage = dgiResponse.isNull("qrcodeImage") ? "" : dgiResponse.getString("qrcodeImage");
                mInvoice.urlcufe = dgiResponse.isNull("urlCufe") ? "" : dgiResponse.getString("urlCufe");
                mInvoice.setSalesDoc(data);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void executePrint(Integer qty) {

        if (mInvoice.companyLogo == null && mInvoice.qrCodeImage == null) {
            runPrintReceiptSequence(qty);
        } else {
            if (mInvoice.companyLogo != null) {
                /*Glide.with(mContext)
                        .asBitmap()
                        .load(getLogo())
                        .listener(new RequestListener<Bitmap>() {
                                      @Override
                                      public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                          if (mImageCounter > 0) {
                                              runPrintReceiptSequence(qty);
                                          }
                                          mImageCounter++;
                                          return true;
                                      }

                                      @Override
                                      public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                          mLogo = bitmap;
                                          if (mImageCounter > 0) {
                                              runPrintReceiptSequence(qty);
                                          }
                                          mImageCounter++;
                                          return true;
                                      }
                                  }
                        ).submit(125, 125);
                String image = mInvoice.qrCodeImage != null ? mInvoice.qrCodeImage : "";*/
            } else {
                mImageCounter++;
            }

            if (mInvoice.qrCodeImage != null) {
                /*Glide.with(mContext)
                        .asBitmap()
                        .load(mInvoice.qrCodeImage)
                        .listener(new RequestListener<Bitmap>() {
                                      @Override
                                      public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                          if (mImageCounter > 0) {
                                              runPrintReceiptSequence(qty);
                                          }
                                          mImageCounter++;
                                          return true;
                                      }

                                      @Override
                                      public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                          mQRCode = bitmap;
                                          if (mImageCounter > 0) {
                                              runPrintReceiptSequence(qty);
                                          }
                                          mImageCounter++;
                                          return true;
                                      }
                                  }
                        ).submit(500, 500);*/
            } else {
                mImageCounter++;
            }
        }

    }

    public String getLogo() {
        if (mInvoice != null && mInvoice.companyLogo != null && !mInvoice.companyLogo.equals("")) {
            String image;
            if (mInvoice.companyLogo.contains("novey")) {
                image = "https://aludrastoragedev.blob.core.windows.net/aludra-files/Pos/ml11_novey_logo.png";
            } else if (mInvoice.companyLogo.contains("cochez")) {
                image = "https://aludrastoragedev.blob.core.windows.net/aludra-files/Pos/ml11_cochez_logo.png";
            } else {
                image = mInvoice.companyLogo;
            }
            return image;
        } else {
            return null;
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

    public void setupReceiptData() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        //Bitmap QRbitmap = getQR();

        //Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.mdl11_noveylogo);
        StringBuilder textData = new StringBuilder();

        try {
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            if (mLogo != null) {
                mPrinter.addImage(mLogo, 0, 0,
                        mLogo.getWidth(),
                        mLogo.getHeight(),
                        Printer.COLOR_1,
                        Printer.MODE_MONO,
                        Printer.HALFTONE_DITHER,
                        Printer.PARAM_DEFAULT,
                        Printer.COMPRESS_AUTO);
            }

            mPrinter.addFeedLine(0);
            textData.append("RUC: ").append(mInvoice.companyNumber).append(" DV ");
            if (mInvoice.companyDV != null && !mInvoice.companyDV.equals("")) {
                textData.append(mInvoice.companyDV).append("\n");
            } else {
                textData.append("04\n");
            }
            textData.append(mInvoice.companyName).append("\n");
            textData.append(mInvoice.warehouseDescription).append("\n");
            //textData.append("Apdo. 3420 Zona 4 Panama, Panama\n");
            if (mInvoice.buildingName != null && !mInvoice.buildingName.equals("") && !mInvoice.buildingName.equals("null")) {
                textData.append(mInvoice.buildingName);
                if (mInvoice.hoodName != null && !mInvoice.hoodName.equals("") && !mInvoice.hoodName.equals("null")) {
                    textData.append(", ").append(mInvoice.hoodName);
                    if (mInvoice.buildingType != null && !mInvoice.buildingType.equals("") && !mInvoice.buildingType.equals("null")) {
                        textData.append(". ").append(mInvoice.buildingType);
                        if (mInvoice.floorName != null && !mInvoice.floorName.equals("") && !mInvoice.floorName.equals("null")) {
                            textData.append(", piso ").append(mInvoice.floorName);
                            if (mInvoice.buildingProvince != null && !mInvoice.buildingProvince.equals("") && !mInvoice.buildingProvince.equals("null")) {
                                textData.append(". ").append(mInvoice.buildingProvince);
                                if (mInvoice.buildingDistrict != null && !mInvoice.buildingDistrict.equals("") && !mInvoice.buildingDistrict.equals("null")) {
                                    textData.append(", ").append(mInvoice.buildingDistrict);
                                }
                            }
                        }
                    }
                }
            }

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(1);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            if (mInvoice.cip != null && !mInvoice.cip.equals("")) {
                textData.append("RUC/CIP: ").append(mInvoice.cip).append("\n");
            }

            textData.append("RAZON SOCIAL: ").append(mInvoice.customerFullName).append("\n");
            textData.append("NO. ").append( /*companyDV +*/ mInvoice.documentNumber).append("\n");
            textData.append("SUCURSAL: ").append(mInvoice.warehouseDescription).append("\n");
            textData.append("Vendedor: ").append(mInvoice.salesAgentNumberId).append(" ").append(mInvoice.salesAgentFullName).append("\n");
            textData.append("Cajero(a): ").append(mInvoice.salesAgentFullName).append("\n");
            if (mInvoice.comment != null) {
                textData.append("Comentario: ").append(mInvoice.comment).append("\n");
            }
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append("-------------------------------------\n");
            textData.append("COMPROBANTE AUXILIAR DE \n" +
                    "FACTURACION ELECTRONICA\n\n");

            mPrinter.addFeedLine(2);

            textData.append("FECHA: " + mInvoice.simpleDate + "    HORA: " + mInvoice.hour + "\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addBarcode(
                    mInvoice.documentNumber,
                    Printer.BARCODE_CODE93,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    2,
                    100
            );

            textData.append("-------------------------------------\n");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            textData.append(mInvoice.documentName).append(": ").append(mInvoice.documentNumber).append("\n");
            textData.append("FECHA: ").append(mInvoice.simpleDate);

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            //mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append("HORA: ").append(mInvoice.hour);

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(1);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append("-------------------------------------\n");
            textData.append(" V E N T A \n");
            textData.append("-------------------------------------");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            //Recorrer los productos comprados
            for (LineObj line : mInvoice.lines) {
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
                mPrinter.addFeedLine(0);
                mPrinter.addTextAlign(Printer.ALIGN_LEFT);

                textData.append(line.quantity).append("x B/.").append(line.price).append("\n");
                if (line.fulfillmentPlanId != null
                        && !line.fulfillmentPlanId.equalsIgnoreCase(GeneralHelper.FULFILLMENT_PDV_PLAN_ID)
                        && line.warehouseName != null
                        && line.fulfillmentPlanDescription != null) {
                    textData.append("\n[")
                            .append(line.fulfillmentPlanDescription.toLowerCase()).append("/")
                            .append(line.warehouseName.toLowerCase())
                            .append("]\n");
                }
                textData.append(line.sku).append(" ").append(line.productName).append(" ");

                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());
                mPrinter.addFeedLine(0);
                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
                textData.append("B/. ").append(line.amount).append("\n");
            }

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append("-------------------------------------\n");


            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            textData.append("SUBTOTAL: ");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append("B/.").append(mInvoice.subTotal);

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            textData.append("ITBMS: ");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append("B/.").append(mInvoice.tax).append("\n");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(1);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            textData.append("-------------------------------------");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            textData.append("TOTAL: ");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);
            textData.append("B/.").append(mInvoice.total).append("\n");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            //textData.append(mInvoice.paymentType).append("\n");
            String issuerName = "";
            if (mInvoice.issuerName != null) {
                if (mInvoice.issuerName.equals("")) {
                    issuerName = "Clave";
                } else {
                    issuerName = mInvoice.issuerName;
                }
            }
            textData.append(issuerName).append("\n");
            if (mInvoice.cardNumber != null) {
                textData.append(mInvoice.cardNumber).append("\n");
            }
            textData.append("AUTH: ").append(mInvoice.authCode).append("\n");

            textData.append("\n\nRecibido: \n");
            textData.append("_____________________________________ \n\n");

            textData.append("Nombre: ____________________\n\n");
            textData.append("Cedula: ____________________\n\n");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            textData.append("GRACIAS POR SU COMPRA\n");
            textData.append("Consulte por la clave de acceso su factura en:\n" + "http://dgi-fep.mef.gob.pa/Consultas\n" + "Usando el CUFE\n").append(mInvoice.cufe).append("\n");

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(0);

            if (mQRCode != null) {
                mPrinter.addImage(mQRCode, 0, 0,
                        mQRCode.getWidth(),
                        mQRCode.getHeight(),
                        Printer.COLOR_1,
                        Printer.MODE_GRAY16,
                        Printer.HALFTONE_DITHER,
                        Printer.PARAM_DEFAULT,
                        Printer.COMPRESS_AUTO);
            }

            mPrinter.addCut(Printer.CUT_FEED);

        } catch (Exception e) {
            //
            mMessage = getException(e, "ReceiptData");
        }
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
            /*try {
                runPrintReceiptSequence(mIsATest, mTimesToPrint);
                isBeginTransaction = false;
                mPrinter.connect(mPrinterId, Printer.PARAM_DEFAULT);
            } catch (Epos2Exception e1) {
                sendException(e, "connect");
                return false;
            }*/
        }

        try {
            mPrinter.beginTransaction();
            //isBeginTransaction = true;
        } catch (Exception e) {
            mMessage = getException(e, "beginTransaction");
            try {
                mPrinter.disconnect();
            } catch (Epos2Exception e1) {
                // Do nothing
                return false;
            }
            return false;
        }

        /*if (!isBeginTransaction) {
            try {
                mPrinter.disconnect();
            } catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }*/
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
                            setupReceiptData();
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
