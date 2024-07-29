package com.temenos.t24;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

// import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
//import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblapiauthtable.EbJblApiAuthTableRecord;
import com.temenos.t24.api.tables.ebjblapiauthtable.EbJblApiAuthTableTable;

/**
 * TODO: check decryption for JWT Auth API
 *
 * @author MD Shibli Mollah
 *
 */
public class ApJblICusTestBase64 extends RecordLifecycle {

    String decryptedBase64 = "";

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {

        DataAccess da = new DataAccess(this);
        CustomerRecord recordForCustomer = new CustomerRecord(currentRecord);

        String id = "";
       // id = "AML";
        id = "EKYC";
        EbJblApiAuthTableRecord apiAuthRec = new EbJblApiAuthTableRecord(da.getRecord("EB.JBL.API.AUTH.TABLE", id));

        String basicAuth = "";
        try {
            basicAuth = apiAuthRec.getBasicAuth().getValue();
        } catch (Exception e1) {
        }

        // Decryption key (must be 16 characters for AES encryption)
        String decryptionKey = "MyKey$forJBLApi&";

        // Decrypt
        String encryptedBase64Credentials = basicAuth;
        decryptedBase64 = decrypt(encryptedBase64Credentials, decryptionKey);

        // API Call
        // String POST_URL_TP = "";
        String GET_URL_TP = "";
        // String POST_PARAMS_TP = "";

        // POST_URL_TP = "";
        GET_URL_TP = "http://localhost:9089/irf-auth-token-generation-container-21.0.59/api/v1.0.0/generateauthtoken";

        // POST_PARAMS_TP = "";

        StringBuilder jwtResponse = this.makeGetRequest(GET_URL_TP);

        /*
         * JSONObject jsonApiRes = null; try { jsonApiRes = new
         * JSONObject(jwtResponse.toString()); } catch (JSONException e) { } try
         * { if
         * (jsonApiRes.getJSONObject("header").get("status").toString().equals(
         * "success")) { recordForCustomer.setAmlCheck("SENT"); } } catch
         * (JSONException e) { }
         */
        try {
            // Parse the response JSON
            // Remove surrounding double quotes from the response & remove
            // backslash(\) from the response...
            String trimmedResponse = jwtResponse.toString().replaceAll("^\"|\"$", "").replace("\\", "");
            JSONObject jsonResponse = new JSONObject(trimmedResponse);

            // Extract the token value
            String token = "";
            if (jsonResponse.has("id_token")) {
                token = jsonResponse.getString("id_token");
            } else {
                System.out.println("Error: id_token is not found in JSON response");
            }

            // Write the JWT Token in EB.JBL.API.AUTH.TABLE Template...
            EbJblApiAuthTableTable apiAuthTable = new EbJblApiAuthTableTable(this);
            apiAuthRec.setJwtToken(token);

            try {
                apiAuthTable.write(id, apiAuthRec);
            } catch (T24IOException e) {
            }

            // Tracer
            try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/DECRYPT-" + currentRecordId + ".txt", true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw)) {
                out.println("Trimmed Response: " + trimmedResponse + "\n" + " Json Response: " + jsonResponse + "\n"
                        + " token: " + token + "\n");
            } catch (IOException e) {
            }
            // Tracer END

        } catch (Exception e) {
            System.out.println("Error occurred while extracting token: " + e.getMessage());
        }
        recordForCustomer.setAmlCheck("SENT");
        // N.B: MUST Require to update in T24 -- Need to use setDefaultVieldValue to set records in the CORE TABLE
        currentRecord.set(recordForCustomer.toStructure());
        return recordForCustomer.getValidationResponse();
    }
    //END of MAIN Method

    // AES decryption method
    public static String decrypt(String strToDecrypt, String secret) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(decryptedBytes);
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    // Method for API Call - GET Method -- JWT Token Generation
    public StringBuilder makeGetRequest(String GET_URL) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = null;
        try {
            URL url = new URL(GET_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            // con.setRequestProperty("Authorization", "Basic
            // SU5QVVRUOjEyMzQ1Ng==");
            String basicAuth = "Basic " + decryptedBase64;
            con.setRequestProperty("Authorization", basicAuth);
            con.setDoOutput(true);
            con.setConnectTimeout(5000); // Set connection timeout to 5 seconds
            con.setReadTimeout(5000); // Set read timeout to 5 seconds

            int responseCode = con.getResponseCode();
            try {
                if (responseCode == HttpURLConnection.HTTP_OK) {
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
