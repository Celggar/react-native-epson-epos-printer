package com.celggar.epos;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by celso on 2019-08-06
 */
public class InvoiceObj {
    private static final String TAG = "InvoiceObj";
    public String companyNumber, companyDV, companyName, cip, customerFullName, documentNumber,
            salesAgentNumberId, salesAgentFullName, simpleDate, hour, subTotal, tax, total,
            paymentType, authCode, issuerName, urlcufe, cufe, qrCodeImage, buildingName, buildingType,
            hoodName, floorName, cardNumber, comment,
            buildingProvince, buildingDistrict, mIpAludra, companyLogo, warehouseDescription, documentName;
    public ArrayList<LineObj> lines;

    public InvoiceObj() {
    }

    public void setSalesDoc(JSONObject salesDocInfo) {
        try {
            lines = new ArrayList<>();
            JSONArray jLines = salesDocInfo.getJSONArray("Lines");

            for (int i = 0; i < jLines.length(); i++) {
                JSONObject jLine = jLines.getJSONObject(i);
                LineObj line = new LineObj();
                line.quantity = jLine.getString("Quantity");
                line.price = jLine.getString("Price");
                line.sku = jLine.getString("Sku");
                line.productName = jLine.getString("ProductName");
                line.amount = jLine.getString("Amount");
                line.fulfillmentPlanId = jLine.isNull("FulfillmentPlanId") ? null : jLine.getString("FulfillmentPlanId");
                line.fulfillmentPlanDescription = jLine.isNull("FulfillmentPlanDescription") ? null : jLine.getString("FulfillmentPlanDescription");
                if (line.fulfillmentPlanId != null) {
                    line.warehouseName = jLine.isNull("WarehouseDescription") ? "" : jLine.getString("WarehouseDescription");
                }
                lines.add(line);
            }

            buildingName = salesDocInfo.isNull("Address_BuildingName") ? "" : salesDocInfo.getString("Address_BuildingName");
            buildingType = salesDocInfo.isNull("Address_BuildingType") ? "" : salesDocInfo.getString("Address_BuildingType");
            hoodName = salesDocInfo.isNull("Address_NeighborhoodName") ? "" : salesDocInfo.getString("Address_NeighborhoodName");
            floorName = salesDocInfo.isNull("Address_HomeOrFloorNumber") ? "" : salesDocInfo.getString("Address_HomeOrFloorNumber");
            buildingDistrict = salesDocInfo.isNull("Address_DistrictName") ? "" : salesDocInfo.getString("Address_DistrictName");
            buildingProvince = salesDocInfo.isNull("Address_ProvinceName") ? "" : salesDocInfo.getString("Address_ProvinceName");
            companyNumber = salesDocInfo.isNull("CompanyNumber") ? "" : salesDocInfo.getString("CompanyNumber");
            companyDV = salesDocInfo.isNull("CompanyDV") ? "" : salesDocInfo.getString("CompanyDV");
            warehouseDescription = salesDocInfo.isNull("WarehouseDescription") ? "" : salesDocInfo.getString("WarehouseDescription");
            companyLogo = salesDocInfo.isNull("CompanyLogo") ? "" : salesDocInfo.getString("CompanyLogo");
            companyName = salesDocInfo.isNull("CompanyName") ? "" : salesDocInfo.getString("CompanyName");
            cip = salesDocInfo.isNull("CustomerNumberId") ? "" : salesDocInfo.getString("CustomerNumberId");
            String customerId = salesDocInfo.isNull("CustomerId") ? null : salesDocInfo.getString("CustomerId");
            if (customerId == null) {
                customerFullName = "CLIENTE CONTADO";
            } else {
                String customerNameHelper = "";
                boolean continueWithHelper = false;
                if (!salesDocInfo.isNull("CustomerFullName")) {
                    String fullName = salesDocInfo.getString("CustomerFullName");
                    if (fullName.trim().equals("")) {
                        continueWithHelper = true;
                    } else {
                        customerFullName = fullName;
                    }
                } else {
                    continueWithHelper = true;
                    customerFullName = salesDocInfo.isNull("CustomerFullName") ? "" : salesDocInfo.getString("CustomerFullName");
                }
                if (continueWithHelper) {
                    customerNameHelper = salesDocInfo.isNull("CustomerFirstName") ? "" : salesDocInfo.getString("CustomerFirstName");
                    customerNameHelper += salesDocInfo.isNull("CustomerLastName") ? "" : " " + salesDocInfo.getString("CustomerLastName");
                    customerFullName = customerNameHelper;
                }

            }
            documentNumber = salesDocInfo.isNull("DocumentNumber") ? "" : salesDocInfo.getString("DocumentNumber");
            comment = salesDocInfo.isNull("Comment") ? null : salesDocInfo.getString("Comment");
            salesAgentNumberId = salesDocInfo.isNull("SalesAgentNumberId") ? "" : salesDocInfo.getString("SalesAgentNumberId");
            salesAgentFullName = salesDocInfo.isNull("SalesAgentFullName") ? "" : salesDocInfo.getString("SalesAgentFullName");
            String date = salesDocInfo.isNull("CreatedDate") ? "" : salesDocInfo.getString("CreatedDate");
            simpleDate = GeneralHelper.getSimpleDate(date);
            hour = GeneralHelper.getDateHour(date);
            subTotal = salesDocInfo.isNull("Amount") ? "" : salesDocInfo.getString("Amount");
            tax = salesDocInfo.isNull("Tax") ? "" : salesDocInfo.getString("Tax");
            total = salesDocInfo.isNull("AmountWithTax") ? "" : salesDocInfo.getString("AmountWithTax");
        } catch (Exception e) {
            Log.e("InvoiceObj", e.getMessage() + "");
        }
    }
}
