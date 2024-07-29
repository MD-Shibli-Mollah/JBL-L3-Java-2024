package com.temenos.t24;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.complex.eb.servicehook.SynchronousTransactionData;
import com.temenos.t24.api.complex.eb.servicehook.TransactionControl;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookTable;

public class ApSmsServiceRtn extends ServiceLifecycle {
    @Override
    public List<String> getIds(ServiceData serviceData, List<String> controlList) {
        DataAccess da = new DataAccess(this);
        List<String> recordIds = da.selectRecords("", "EB.JBL.SMS.BOOK", "", "WITH SMS.STATUS EQ PENDING");
        return recordIds;
    }

    @Override
    public void updateRecord(String id, ServiceData serviceData, String controlItem,
            TransactionControl transactionControl, List<SynchronousTransactionData> transactionData,
            List<TStructure> records) {

        System.out.println("Processing Record :" + id);
        DataAccess da = new DataAccess(this);

        EbJblSmsBookTable smsBook = new EbJblSmsBookTable(this);
        EbJblSmsBookRecord smsRec = new EbJblSmsBookRecord(da.getRecord("EB.JBL.SMS.BOOK", id));

        String smsContent = "";
        String phone = "";
        String email = "";
        try {
            smsContent = smsRec.getSmsBody().getValue();
            phone = smsRec.getPhone().getValue();
            email = smsRec.getEmail().getValue();
        } catch (Exception e1) {
        }
        System.out.println(phone + "\n" + smsContent);

        String POST_URL_TP = "";
        String POST_PARAMS_TP = "";

        POST_URL_TP = smsRec.getApiLink().getValue();
        POST_PARAMS_TP = "{\n" + "  \"body\":                               {\n" + "            \"messageContent\": "
                + '"' + smsContent + '"' + ",\n" + "            \"smsNumber\": " + '"' + phone + '"' + " \n"
                + "            \"phoneNumber\": " + '"' + email + '"' + " \n"
                + "                                                        }\n" + "}";

        System.out.println("Calling SMS Api");
        StringBuilder smsResponse = this.makeRestCall(POST_URL_TP, POST_PARAMS_TP);

        JSONObject jsonSms = null;

        try {
            jsonSms = new JSONObject(smsResponse.toString());
        } catch (JSONException e) {
        }

        try {
            if (jsonSms.getJSONObject("header").get("status").toString().equals("success")) {
                smsRec.setSmsStatus("SENT");
                try {
                    smsBook.write(id, smsRec);
                } catch (T24IOException e) {
                }
            }
        } catch (JSONException e) {
        }
    }

    // END OF MAIN METHOD

    public StringBuilder makeRestCall(String POST_URL, String POST_PARAMS) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = null;
        try {
            URL url = new URL(POST_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Basic SU5QVVRUOjEyMzQ1Ng==");
            con.setDoOutput(true);
            try {
                OutputStream os = con.getOutputStream();
                byte[] input = POST_PARAMS.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                System.out.println(con);
                System.out.println("Waiting for REST call response");
                try {
                    Thread.sleep(3000);
                    if (!(con.getResponseCode() == HttpURLConnection.HTTP_OK)) {
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Rest Call Falied");
                }
            } catch (IOException e) {
                System.out.println("Connection establish failed");
                System.exit(0);
            }

            try {
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.out.println("Rest call encountered an error");
            }
            con.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}