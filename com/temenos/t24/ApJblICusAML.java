package com.temenos.t24;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
// import com.temenos.t24.api.complex.eb.servicehook.SynchronousTransactionData;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblapiauthtable.EbJblApiAuthTableRecord;
import com.temenos.t24.api.tables.ebjblapiauthtable.EbJblApiAuthTableTable;

/**
 * TODO: Decrypt the encrypted Basic Auth and then pass it to generate the JWT
 * Token. Using JWT Token for Authorization and consume the API. TEMPLATE NAME:
 * EB.JBL.API.AUTH.TABLE
 * 
 * @author MD Shibli Mollah
 *
 */
public class ApJblICusAML extends RecordLifecycle {

    String encryptedBase64Credentials = "";
    String decryptedBase64 = "";
    String myJwtToken = "";

    @Override
    public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        try {
            CustomerRecord recordForCustomer = new CustomerRecord(currentRecord);

            // JWT Token
            String myTokenID = "";
            myTokenID = "AML";
            DataAccess da = new DataAccess(this);
            EbJblApiAuthTableRecord apiAuthRec = new EbJblApiAuthTableRecord(da.getRecord("EB.JBL.API.AUTH.TABLE", myTokenID));

            String basicAuth = "";
            try {
                basicAuth = apiAuthRec.getBasicAuth().getValue();
                myJwtToken = apiAuthRec.getJwtToken().getValue();
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

                    // Tracer
                    try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/JwtToken-" + currentRecordId + ".txt",
                            true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
                        out.println(" token: " + token + "\n");
                    } catch (IOException e) {
                    }
                    // Tracer END

                    // Write the JWT Token in EB.JBL.API.AUTH.TABLE Template...
                    EbJblApiAuthTableTable apiAuthTable = new EbJblApiAuthTableTable(this);
                    apiAuthRec.setJwtToken(token);

                    try {
                        apiAuthTable.write(myTokenID, apiAuthRec);
                    } catch (T24IOException e) {
                    }
                } catch (Exception e) {
                    System.out.println("Error occurred while extracting token: " + e.getMessage());
                }
                // JWT Token Done
            }

            // Calling the MAIN API - GET Method - makeGetRequestForAML
            String GET_URL_TP = "";
            GET_URL_TP = "http://localhost:9089/CusJwtContainer/api/v1.0.0/party/ws/800155";

            StringBuilder amlResponse = null;
            try {
                amlResponse = this.makeGetRequestForAML(GET_URL_TP);
            } catch (NullPointerException e) {
            }
            /*
             * try { // Sleep for 6 seconds (6000 milliseconds)
             * Thread.sleep(6000); } catch (InterruptedException e) { // Handle
             * interrupted exception if necessary e.printStackTrace(); }
             */

            // Tracer
            try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/AML-" + currentRecordId + ".txt", true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw)) {
                out.println("AmlResponse: " + amlResponse + "\n");
            } catch (IOException e) {
            }
            // Tracer END

            JSONObject jsonAml = null;
            try {
                jsonAml = new JSONObject(amlResponse.toString());
                if (jsonAml.getJSONObject("header").get("status").toString().equals("success")) {
                    recordForCustomer.setAmlCheck("SENT");
                }

                else {
                    // EMPTY the EXPIRED JWT Token in EB.JBL.API.AUTH.TABLE
                    // Template...
                    String token = "";
                    EbJblApiAuthTableTable apiAuthTable = new EbJblApiAuthTableTable(this);
                    apiAuthRec.setJwtToken(token);

                    try {
                        apiAuthTable.write(myTokenID, apiAuthRec);
                    } catch (T24IOException e) {
                    }
                }

            } catch (JSONException e) {
            }
            currentRecord.set(recordForCustomer.toStructure());
        } catch (Exception e1) {
        }
    }
    // END of MAIN Method

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
    } // END of AES decryption method

    // Method for API Call - GET Method -- JWT Token Generation
    public StringBuilder makeGetRequestForJWT(String GET_URL) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = null;

        // Decryption key (must be 16 characters for AES encryption)
        String decryptionKey = "";
        decryptionKey = "MyKey$forJBLApi&";
        // Decrypt
        decryptedBase64 = decrypt(encryptedBase64Credentials, decryptionKey);
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
            e.printStackTrace();
        }
        return response;
    } // END of GET Method -- JWT Token Generation

    // Main API -- GET Request
    public StringBuilder makeGetRequestForAML(String GET_URL) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = null;

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
            e.printStackTrace();
        }
        return response;
    } // END of GET Method -- MAIN API

}
