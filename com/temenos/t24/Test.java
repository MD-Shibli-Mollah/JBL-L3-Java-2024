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
import com.temenos.tafj.jee.client.AppServerProvider;
import com.temenos.tafj.jee.client.TAFJJEEClient;
import com.temenos.tafj.jee.client.TAFJJEEClientFactory;
import com.temenos.tafj.sb.TRunCallObject;

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
        /*
         * String myName =
         * "MD TANVIR AHMED SHIBLI MOLLAH SON OF MD SADEK ALI MOLLAH AND LOVELY AK"
         * ; Integer myNameLen = myName.length(); System.out.println(myNameLen);
         * 
         * System.out.println(myName);
         */
        // String dbtAmt = "USD4500";

        String yDbtAmt = null;
        System.out.println(yDbtAmt.toString()); // Causes NullPointerException

        Double[] accrProfit = { 5.7, 2.1, 3.5 };
        System.out.println(accrProfit[4]); // Causes
                                           // ArrayIndexOutOfBoundsException

        yDbtAmt = "";
        // yDbtAmt = dbtAmt.substring(3);
        System.out.println(yDbtAmt);
        Double dblTxnAmt = 0.0;
        dblTxnAmt = Double.valueOf(yDbtAmt);
        String dbtAmt = "4500";
        Double myAmount = Double.valueOf(dbtAmt) / 0.0; // Causes
                                                        // ArithmeticException
        System.out.println(myAmount);

        System.out.println(dblTxnAmt);

        try {
            String yAmount = null; // null value
            System.out.println(yAmount.charAt(0));
        } catch (NullPointerException e) {
            System.out.println("NullPointerException is found in the program.");
        }

        // TEST OFS for Customer Record ID is: 101153
        /*
         * if (id.equals("101153")) { cusRec.getAmlCheck().setValue("SENT");
         * cusRec.getAmlResult().setValue("RESULT.AWAITED");
         * 
         * SynchronousTransactionData txnData = new
         * SynchronousTransactionData(); txnData.setFunction("INPUTT");
         * txnData.setNumberOfAuthoriser("1"); txnData.setSourceId("BULK.OFS");
         * txnData.setTransactionId(id); txnData.setVersionId("CUSTOMER,AML");
         * 
         * transactionData.add(txnData); records.add(cusRec.toStructure()); }
         */
        // TEST END

        // Get an EJB client from a JBoss7eap deployment on server 10.21.2.99
        TAFJJEEClient client = TAFJJEEClientFactory.getEjbClient(AppServerProvider.JBOSS7EAP, "10.21.2.99", "8080");

        // Process an OFS request, method argument is the OFS request
        String response = client.processOFS("ENQUIRY.SELECT,,INPUTT/123456,%CURRENCY");

        // Invoke a subroutine, method arguments are the Subroutine name and an
        // array of subroutine parameters
        String[] response1 = client.callAt("EXCHRATE",
                new String[] { "1", "CHF", "500", "GBP", "", "", "", "", "", "" });

        // Execute a program in background. A TRunCallObject instance is used to
        // setup the invocation parameters (command and optional user input
        // datas).
        // It returns a TRunCallObject which contains the response status (0
        // success / 1 failure) and eventual program output.
        // Execute program and get response back
        TRunCallObject response2 = client.trun(new TRunCallObject("PROGRAM.NAME ARG1 ARG2"));

        /*
         * if (myNameLen > 35){ String myName35 = myName.substring(0, 35);
         * //Total 35 characters excluding 35th number. String myName36 =
         * myName.substring(35, myNameLen);
         * 
         * System.out.println(myName35); System.out.println(myName36); } else {
         * System.out.println(myName); }
         */

        /*
         * try (FileWriter fw = new
         * FileWriter("D:\\Temenos\\t24home\\default\\DL.BP-" + currentRecordId
         * + ".txt", true); BufferedWriter bw = new BufferedWriter(fw);
         * PrintWriter out = new PrintWriter(bw)) { out.println("basicAuth- " +
         * encryptionKey + "\n" + basicAuth); } catch (IOException e) { }
         */

        // Decrypt
        /*
         * String encryptedBase64Credentials = basicAuth; String decryptedBase64
         * = decrypt(encryptedBase64Credentials, encryptionKey);
         * apiAuthRec.setJwtToken(decryptedBase64);
         * 
         * /* try (FileWriter fw = new
         * FileWriter("/Temenos/T24/UD/Tracer/DECRYPT-" + currentRecordId +
         * ".txt", true); BufferedWriter bw = new BufferedWriter(fw);
         * PrintWriter out = new PrintWriter(bw)) {
         * out.println("MyAPI- encryptedBase64Credentials: " +
         * encryptedBase64Credentials + "\n" + "decryptedBase64: " +
         * decryptedBase64); } catch (IOException e) { }
         * 
         * // AES decryption method /*public static String decrypt(String
         * strToDecrypt, String secret) { try { SecretKeySpec secretKey = new
         * SecretKeySpec(secret.getBytes(), "AES"); Cipher cipher =
         * Cipher.getInstance("AES/ECB/PKCS5Padding");
         * cipher.init(Cipher.DECRYPT_MODE, secretKey); byte[] decryptedBytes =
         * cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)); return new
         * String(decryptedBytes); } catch (Exception e) {
         * System.out.println("Error while decrypting: " + e.toString()); }
         * return null; }
         */

        /*
         * Map<String, String> nofValueMapping = new HashMap<String, String>();
         * nofValueMapping.put("responseCode", "6");
         * nofValueMapping.put("isSalaryPaid", "false"); String nofStr =
         * nofValueMapping.toString();
         */

        // Read Multi value field & position.
        /*
         * for (int i = 0; i < legalIdList.size(); i++) { LegalIdClass item =
         * legalIdList.get(i); // idType = "NATIONAL.ID" if
         * (idType.equals(item.getLegalDocName().getValue())) { cusLegalIdName =
         * item.getLegalDocName().getValue(); cusLegalIdNo =
         * item.getLegalId().getValue(); // index = i; // Found the index break;
         * } }
         */

        // SELECT any application
        // List<String>
        // com.temenos.t24.api.system.DataAccess.selectRecords(String
        // companyMnemonic, String tableName, String fileSuffix, String
        // filterAndSort)
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
