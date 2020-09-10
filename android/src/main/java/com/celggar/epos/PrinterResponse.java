package com.celggar.epos;

import org.json.JSONObject;

/**
 * Created by celso on 2019-08-06
 */
public class PrinterResponse {

    public int errorStatus;

    /**
     * Any data that the activity would need to receive.
     */
    public Object data;
    public JSONObject rawResponse;

    /**
     * Request response message.
     */
    public String message;
    public boolean trustStatus;
    public boolean isWarning;

    public PrinterResponse(boolean isCorrect, int errStatus) {
        trustStatus = isCorrect;
    }

    public PrinterResponse() {

    }
}
