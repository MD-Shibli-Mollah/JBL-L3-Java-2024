package com.temenos.t24;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.tables.ebjblapiauthtable.EbJblApiAuthTableRecord;

/**
 * TODO: Encryption for BASIC Auth Attached to - EB.JBL.API.AUTH.TABLE, EB.API -
 * GbJblIApiAuth
 * 
 * @author MD Shibli Mollah
 * 
 */
public class ApJblIApiAuth extends RecordLifecycle {

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {

        EbJblApiAuthTableRecord apiAuthRec = new EbJblApiAuthTableRecord(currentRecord);

        String username = "";
        String password = "";
        String basicAuth = "";
        try {
            username = apiAuthRec.getUsername().getValue();
            password = apiAuthRec.getPassword().getValue();
        } catch (Exception e1) {
        }

        if (username == "" || password == "") {
            return apiAuthRec.getValidationResponse();
        }

        // Encode username and password in Base64
        String authString = username + ":" + password;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());

        // Encryption key (must be 16 characters for AES encryption)
        // String encryptionKey = "ThisIsASecretKey!";
        String encryptionKey = "MyKey$forJBLApi&";

        // Encrypt username and password
        try {
            // basicAuth = this.encryptCredentials(username + ":" + password,
            // encryptionKey);
            basicAuth = this.encryptCredentials(encodedAuthString, encryptionKey);
            // Tracer
            /*
             * try (FileWriter fw = new
             * FileWriter("/Temenos/T24/UD/Tracer/ENCRYPT-" + currentRecordId +
             * ".txt", true); BufferedWriter bw = new BufferedWriter(fw);
             * PrintWriter out = new PrintWriter(bw)) {
             * out.println("MyAPI- encodedAuthString: " + encodedAuthString +
             * "\n" + "Basic Auth: " + basicAuth); } catch (IOException e) { }
             */
            // Tracer end
        } catch (Exception e) {
        }

        apiAuthRec.setBasicAuth(basicAuth);
        apiAuthRec.setUsername("");
        apiAuthRec.setPassword("");

        currentRecord.set(apiAuthRec.toStructure());
        return apiAuthRec.getValidationResponse();
    }

    // AES encryption method
    public String encryptCredentials(String credentials, String encryptionKey) throws Exception {
        byte[] key = encryptionKey.getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(credentials.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

}
