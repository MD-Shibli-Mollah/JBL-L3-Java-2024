package com.temenos.t24;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.temenos.t24.api.records.customer.LegalIdClass;

/**
 * TODO: Document me!
 *
 * @author MD Shibli Mollah
 *
 */
public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        /*String myName = "MD TANVIR AHMED SHIBLI MOLLAH SON OF MD SADEK ALI MOLLAH AND LOVELY AK";
        Integer myNameLen = myName.length();
        System.out.println(myNameLen);
                
        System.out.println(myName);*/
        String dbtAmt = "USD4500";
        String yDbtAmt = "";
        yDbtAmt = dbtAmt.substring(3);
        System.out.println(yDbtAmt);
        Double dblTxnAmt = 0.0;
        dblTxnAmt = Double.valueOf(yDbtAmt);
        System.out.println(dblTxnAmt);
        
        /*if (myNameLen > 35){
            String myName35 = myName.substring(0, 35); //Total 35 characters excluding 35th number.
            String myName36 = myName.substring(35, myNameLen);
            
            System.out.println(myName35);
            System.out.println(myName36);
        }
        else
        {
            System.out.println(myName);
        }*/
        
        /*try (FileWriter fw = new FileWriter("D:\\Temenos\\t24home\\default\\DL.BP-" + currentRecordId + ".txt", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)) {
        out.println("basicAuth- " + encryptionKey + "\n" + basicAuth);
        } catch (IOException e) {
        }*/
        
     // Decrypt
        /*String encryptedBase64Credentials = basicAuth;
        String decryptedBase64 = decrypt(encryptedBase64Credentials, encryptionKey);
        apiAuthRec.setJwtToken(decryptedBase64);

        try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/DECRYPT-" + currentRecordId + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println("MyAPI- encryptedBase64Credentials: " + encryptedBase64Credentials + "\n" + "decryptedBase64: "
                    + decryptedBase64);
        } catch (IOException e) {
        }*/
        
     // AES decryption method
        /*public static String decrypt(String strToDecrypt, String secret) {
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
        }*/
        
        /*Map<String, String> nofValueMapping = new HashMap<String, String>();
        nofValueMapping.put("responseCode", "6");
        nofValueMapping.put("isSalaryPaid", "false");
        String nofStr = nofValueMapping.toString();*/
        
        // Read Multi value field & position.
        /*for (int i = 0; i < legalIdList.size(); i++) {
            LegalIdClass item = legalIdList.get(i);
            // idType = "NATIONAL.ID"
            if (idType.equals(item.getLegalDocName().getValue())) {
                cusLegalIdName = item.getLegalDocName().getValue();
                cusLegalIdNo = item.getLegalId().getValue();
                // index = i; // Found the index
                break;
            }
        }*/
// SELECT any application      
// List<String> com.temenos.t24.api.system.DataAccess.selectRecords(String companyMnemonic, String tableName, String fileSuffix, String filterAndSort)
        String input = "20210430/0";
        String[] parts = input.split("/");

        if (parts.length > 0) {
            String date = parts[0];
        }
       String stmtDate = input.substring(0, input.indexOf('/'));
    // Get a sublist of the last 10 IDs
       // List<String> lastTenIds = ids.subList(ids.size() - 10, ids.size());
    }

}
