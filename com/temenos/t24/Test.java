package com.temenos.t24;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.complex.eb.templatehook.TransactionData;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.personentity.LegalIdClass;
import com.temenos.t24.api.records.personentity.PersonEntityRecord;
import com.temenos.t24.api.records.personentity.PhoneClass;
import com.temenos.t24.api.records.teller.TellerRecord;
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
public class Test extends RecordLifecycle{

    /**
     * @param args
     */
    @Override
    public void updateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext,
            List<TransactionData> transactionData, List<TStructure> currentRecords) {
        
        // Initialize current Record in an Object
        TellerRecord tt = new TellerRecord(currentRecord);
        
        // Initialize Variables as String
        String txnType = "";
        String myNid = "";
        /*String myIdType = "";       
        String myName = "";
        String myMobileNumber = "";*/
        
       // myIdType = tt.getLocalRefField("LT.WLK.LGL.DOC").getValue();
        myNid = tt.getLocalRefField("LT.NID.NO").getValue();
        tt.getAccount2().getOverride();
        /*myName = tt.getLocalRefField("LT.PAY.NM").getValue();
        myMobileNumber = tt.getLocalRefField("LT.TEL.NO.BEN").getValue();
        
        myIdType = tt.getLocalRefField("LT.WLK.LGL.DOC").getValue();
        myNid = tt.getLocalRefField("LT.NID.NO").getValue();
        myName = tt.getLocalRefField("LT.PAY.NM").getValue();
        myMobileNumber = tt.getLocalRefField("LT.TEL.NO.BEN").getValue();*/

        // create a person.entity customer by the above information (NID,
        // Name, Mobile Number).
        
        // Initialize the Object
        PersonEntityRecord peRecord = new PersonEntityRecord(this);
        
        String myIdType = "PASSPORT";
        String myId = "BJ4319345";
        String myMobileNumber = "+8801521497732";
        
        //Initialize the Object where the Values will be set
        LegalIdClass legalIdClass = new LegalIdClass();
        legalIdClass.setLegalDocName(myIdType);
        legalIdClass.setLegalId(myId);

        PhoneClass phoneClass = new PhoneClass();
        phoneClass.setPhone(myMobileNumber);
        
        // Set the Object as a parameter at the desired position
        peRecord.setLegalId(legalIdClass, 0);
        peRecord.setPhone(phoneClass, 0);
    }
    
    
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
        
        // TellerRecord tt = new TellerRecord(currentRecord);
        
        
        
      //Get an EJB client from a JBoss7eap deployment on server 10.21.2.99
        TAFJJEEClient client = TAFJJEEClientFactory.getEjbClient(AppServerProvider.JBOSS7EAP, "10.21.2.99", "8080");
                
        //Process an OFS request, method argument is the OFS request
        String response = client.processOFS("ENQUIRY.SELECT,,INPUTT/123456,%CURRENCY");
                
        //Invoke a subroutine, method arguments are the Subroutine name and an array of subroutine parameters 
        String[] response1 = client.callAt("EXCHRATE", new String[] { "1", "CHF", "500", "GBP", "", "", "", "", "", "" });   

        //Execute a program in background. A TRunCallObject instance is used to setup the invocation parameters (command and optional user input datas).
        //It returns a TRunCallObject which contains the response status (0 success / 1 failure) and eventual program output. 
        //Execute program and get response back
        TRunCallObject response2 = client.trun(new TRunCallObject("PROGRAM.NAME ARG1 ARG2"));
        
        
        
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
        
        /*
        try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/DECRYPT-" + currentRecordId + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println("MyAPI- encryptedBase64Credentials: " + encryptedBase64Credentials + "\n" + "decryptedBase64: "
                    + decryptedBase64);
        } catch (IOException e) {
        }
        
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
