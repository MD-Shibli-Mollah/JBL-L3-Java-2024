package com.temenos.t24;

/*
EB.API      : ApJblCusAmlConsumeAPIService, ApJblCusAmlConsumeAPIService.SELECT
PGM.FILE    : JblCusAmlConsumeAPIService
BATCH       : BNK/CUS.AML.SERVICE
TSA.SERVICE : BNK/CUS.AML.SERVICE
DEVELOPED BY: MD Shibli Mollah
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.complex.eb.servicehook.SynchronousTransactionData;
import com.temenos.t24.api.complex.eb.servicehook.TransactionControl;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblapiauthtable.EbJblApiAuthTableRecord;
import com.temenos.t24.api.tables.ebjblapiauthtable.EbJblApiAuthTableTable;

public class ApJblCusAmlConsumeAPIService extends ServiceLifecycle {

    String encryptedBase64Credentials = "";
    String decryptedBase64 = "";
    String myJwtToken = "";

    @Override
    public List<String> getIds(ServiceData serviceData, List<String> controlList) {

        DataAccess da = new DataAccess(this);
        List<String> recordIDs = da.selectRecords("", "CUSTOMER", "$NAU",
                "WITH AML.RESULT EQ NULL AND RECORD.STATUS EQ INAU");
        // System.out.println("Record ID's Are: " + recordIDs);
        int cnt = recordIDs.size();
        System.out.println(cnt + " Are Selected");

        return recordIDs;
    }

    @Override
    public void updateRecord(String id, ServiceData serviceData, String controlItem,
            TransactionControl transactionControl, List<SynchronousTransactionData> transactionData,
            List<TStructure> records) {
        try {
            DataAccess da = new DataAccess(this);
            CustomerRecord cusRec = new CustomerRecord(da.getRecord("", "CUSTOMER", "$NAU", id));

            System.out.println("Customer Record ID is: " + id);
            // JWT Token
            String myTokenID = "";
            myTokenID = "AML";

            EbJblApiAuthTableRecord apiAuthRec = new EbJblApiAuthTableRecord(
                    da.getRecord("EB.JBL.API.AUTH.TABLE", myTokenID));

            String basicAuth = "";
            try {
                basicAuth = apiAuthRec.getBasicAuth().getValue();
                myJwtToken = apiAuthRec.getJwtToken().getValue();
                System.out.println("basicAuth Token is: " + basicAuth);
                System.out.println("JWT Token is: " + myJwtToken);
            } catch (Exception e1) {
            }

            encryptedBase64Credentials = basicAuth;

            // Check either JWT is generated or not/EXPIRED.
            if (myJwtToken == "") {
                // GET API Call to Generate JWT Auth Token
                String GET_URL_TP = "";
                GET_URL_TP = "http://localhost:9089/irf-auth-token-generation-container-21.0.59/api/v1.0.0/generateauthtoken";
                StringBuilder jwtResponse = null;
                jwtResponse = this.makeGetRequestForJWT(GET_URL_TP);

                try {
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
                        apiAuthTable.write(myTokenID, apiAuthRec);
                    } catch (T24IOException e) {
                    }
                } catch (Exception e) {
                    System.out.println("Error occurred while extracting the JWT token: " + e.getMessage());
                }
                // JWT Token Done
            }

            // COSUMING THE API FROM AML & CREATE LOGIC BASED ON THE RESPONSE
            // Calling the MAIN API - GET Method - makeGetRequestForAML
            String GET_URL_TP = "";
            GET_URL_TP = "http://localhost:9089/CusJwtContainer/api/v1.0.0/party/ws/800155";

            System.out.println("Calling AML Api");
            StringBuilder amlResponse = this.makeGetRequestForAML(GET_URL_TP);

            // UPDATE HERE
            JSONObject jsonAml = null;

            try {
                jsonAml = new JSONObject(amlResponse.toString());
            } catch (JSONException e) {
            }
            // TEST OFS for Customer Record ID is: 101153
            /*
             * if (id.equals("101153")) { cusRec.getAmlCheck().setValue("SENT");
             * cusRec.getAmlResult().setValue("RESULT.AWAITED");
             * 
             * SynchronousTransactionData txnData = new
             * SynchronousTransactionData(); txnData.setFunction("INPUTT");
             * txnData.setNumberOfAuthoriser("1");
             * txnData.setSourceId("BULK.OFS"); txnData.setTransactionId(id);
             * txnData.setVersionId("CUSTOMER,AML");
             * 
             * transactionData.add(txnData); records.add(cusRec.toStructure());
             * }
             */
            // TEST END

            try {
                if (jsonAml.getJSONObject("header").get("status").toString().equals("success")) {
                    // cusRec.getLocalRefField("LT.AML.STATUS").setValue("SENT");
                    cusRec.getAmlCheck().setValue("SENT");
                    cusRec.getAmlResult().setValue("RESULT.AWAITED");

                    /*
                     * Tracer try (FileWriter fw = new
                     * FileWriter("/Temenos/T24/UD/Tracer/CustomerRecord-" + id
                     * + ".txt", true); BufferedWriter bw = new
                     * BufferedWriter(fw); PrintWriter out = new
                     * PrintWriter(bw)) { out.println("Customer Record- " + id +
                     * "\n" + cusRec); } catch (IOException e) { } Tracer
                     */

                    SynchronousTransactionData txnData = new SynchronousTransactionData();
                    txnData.setFunction("INPUTT");
                    txnData.setNumberOfAuthoriser("1");
                    txnData.setSourceId("BULK.OFS");
                    txnData.setTransactionId(id);
                    txnData.setVersionId("CUSTOMER,AML");

                    transactionData.add(txnData);
                    records.add(cusRec.toStructure());
                } else {
                }

            } catch (JSONException e) {
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // END OF MAIN METHOD

    // AES decryption method
    public static String decrypt(String strToDecrypt, String secret) {

        System.out.println("decrypt Method is called...");
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
    } // END of AES decryption method

    // Method for API Call - GET Method -- JWT Token Generation
    public StringBuilder makeGetRequestForJWT(String GET_URL) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = null;

        System.out.println("makeGetRequestForJWT Method is called...");

        // Decryption key (must be 16 characters for AES encryption)
        String decryptionKey = "";
        decryptionKey = "MyKey$forJBLApi&";
        // Decrypt
        decryptedBase64 = decrypt(encryptedBase64Credentials, decryptionKey);
        System.out.println("My Decrypted code is: " + decryptedBase64);
        // Decryption DONE!
        try {
            URL url = new URL(GET_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            // con.setRequestProperty("Authorization", "Basic
            // SU5QVVRUOjEyMzQ1Ng==");
            String basicAuth = "Basic " + decryptedBase64;
            con.setRequestProperty("Authorization", basicAuth);
            con.setRequestProperty("expiry", "100000");
            con.setRequestProperty("issuer", "jclient");
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
            // e.printStackTrace();
            System.out.println("JWT Token API: Rest call encountered an error: " + e.getMessage());
        }
        return response;
    }
    // END of GET Method -- JWT Token Generation

    // Main API -- GET Request
    public StringBuilder makeGetRequestForAML(String GET_URL) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = null;

        System.out.println("makeGetRequestForAML Method is called...");

        try {
            URL url = new URL(GET_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");

            con.setRequestProperty("Authorization", myJwtToken);
            con.setDoOutput(true);
            con.setConnectTimeout(5000); // Set connection timeout to 5 seconds
            con.setReadTimeout(10000); // Set read timeout to 10 seconds

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
            // e.printStackTrace();
            System.out.println("AML API: Rest call encountered an error: " + e.getMessage());
        }
        return response;
    } // END of GET Method -- MAIN API
}
