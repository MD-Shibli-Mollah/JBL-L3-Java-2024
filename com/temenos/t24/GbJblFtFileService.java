package com.temenos.t24;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.complex.eb.servicehook.TransactionData;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;

/**
 * GbJblFtFileService class that processes FT OFS using data from a CSV file.
 */

/*
 * EB.API : GbJblFtFileService, GbJblFtFileService.SELECT PGM.FILE :
 * GbJblFtFileService BATCH : BNK/FT.CUSTOM.OFS.SERVICE TSA.SERVICE :
 * BNK/FT.CUSTOM.OFS.SERVICE VERSION : FUNDS.TRANSFER,JBL.SERVICE DEVELOPED BY :
 * MD Shibli Mollah
 */

public class GbJblFtFileService extends ServiceLifecycle {
    
    // Initialize Variables for Total Time
    double totalDuration = 0.00;
    
    @Override
    public List<String> getIds(ServiceData serviceData, List<String> controlList) {
        // Path to the CSV file
        String filePath = "/Temenos/T24/UD/SHIBLI.BP/FUNDS.TRANSFER.RECORDS.BULK.csv";

        // Read and return FT_IDs
        List<String> idsList = readFileForIds(filePath);
        int ListCount = idsList.size();
        System.out.println(ListCount + " Are Selected");
        return idsList; // Return the list of FT_IDs to be processed
    }
    
    @Override
    public void postUpdateRequest(String id, ServiceData serviceData, String controlItem,
            List<TransactionData> transactionData, List<TStructure> records) {
        
        // Capture start time
        long startTime = System.currentTimeMillis();
        // System.out.println(id + " is Processing from the file...");

        // Path to the CSV file (again, if needed for fetching details)
        String filePath = "/Temenos/T24/UD/SHIBLI.BP/FUNDS.TRANSFER.RECORDS.BULK.csv";

        // Read entire CSV file data
        List<String[]> dataFromFile = readFile(filePath);

        // Loop through the file data to find the matching FT_ID and process it
        for (String[] row : dataFromFile) {
            // Check if the FT_ID matches the current id"
            if (row[0].equals(id)) {
                String transactionType = row[1];
                String debitAccount = row[2];
                String debitCurrency = row[3];
                String debitAmount = row[4];
                // String valueDate = row[5];
                String creditAccount = row[6];

                // Construct the OFS message
                FundsTransferRecord fundsTransferRecord = new FundsTransferRecord(this);

                fundsTransferRecord.setTransactionType(transactionType);
                fundsTransferRecord.setDebitAcctNo(debitAccount);
                fundsTransferRecord.setDebitCurrency(debitCurrency);
                fundsTransferRecord.setDebitAmount(debitAmount);
                fundsTransferRecord.setCreditAcctNo(creditAccount);

                TransactionData txnData = new TransactionData();
                txnData.setFunction("INPUT");
                txnData.setNumberOfAuthoriser("0");
                txnData.setSourceId("BULK.OFS");
                txnData.setVersionId("FUNDS.TRANSFER,JBL.SERVICE");

                transactionData.add(txnData);
                records.add(fundsTransferRecord.toStructure());
                // Uncomment for debugging
                // System.out.println("My txnData is:" + txnData.toString()+"\n");
                // System.out.println("My fundsTransferRecord is:" + fundsTransferRecord.toString() +"\n");
                // System.out.println("OFS is successful for: " + id);
            }
        }
     // Capture end time
        long endTime = System.currentTimeMillis();
        System.out.println("End time for " + id + ": " + endTime);

        // Calculate the duration in seconds
        double durationInSeconds = (endTime - startTime) / 1000.0;
        System.out.println("Duration for " + id + ": " + durationInSeconds + " seconds");

        // Add the duration to the total duration
        totalDuration += durationInSeconds;
        System.out.println("Total processing time so far: " + totalDuration + " seconds");
    }

    // Method to read CSV file and extract FT_IDs (IDs to be processed)
    public List<String> readFileForIds(String filePath) {
        List<String> idsList = new ArrayList<>();
        String line;
        String splitBy = ","; // CSV delimiter

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read each line of the CSV (assuming FT_ID is the first column)
            while ((line = br.readLine()) != null) {
                String[] data = line.split(splitBy);
                String ftId = data[0]; // Assuming the FT_ID is the first column
                idsList.add(ftId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return idsList;
    }

    // Method to read the CSV file and return all data (used in
    // postUpdateRequest)
    public List<String[]> readFile(String filePath) {
        List<String[]> dataList = new ArrayList<>();
        String line;
        String splitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                String[] data = line.split(splitBy);
                dataList.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}
