package com.temenos.t24;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.complex.eb.servicehook.SynchronousTransactionData;
import com.temenos.t24.api.complex.eb.servicehook.TransactionControl;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: 10. Preparing a multi-threading routine to extract CUSTOMERS and save them in csv file.
 *
 * @author MD Shibli Mollah
 *
 */

public class ExtractCustomerService extends ServiceLifecycle {
    @Override
    // This is the SELECT routine where the all the IDs are returned...
    public List<String> getIds(ServiceData serviceData, List<String> controlList) {
        DataAccess da = new DataAccess(this);
        List<String> recordIds = da.selectRecords("", "CUSTOMER", "", "");
        return recordIds;
    }

    @Override
    // This is the PROCESS routine where the data is LOADED ...
    public void updateRecord(String id, ServiceData serviceData, String controlItem,
            TransactionControl transactionControl, List<SynchronousTransactionData> transactionData,
            List<TStructure> records) {

        System.out.println("Processing Record :" + id);
        DataAccess da = new DataAccess(this);

        CustomerRecord customerRecord = new CustomerRecord(da.getRecord("CUSTOMER", id));

        // Initialize Variables
        String mnemonic = "";
        String shortName = "";
        String email = "";
        // fetch records
        try {
            mnemonic = customerRecord.getMnemonic().getValue();
            shortName = customerRecord.getShortName().get(0).getValue();
            email = customerRecord.getPhone1(0).getEmail1().getValue();
        } catch (Exception e1) {
        }

        try (FileWriter fw = new FileWriter("/Temenos/T24/UD/customerRecords.csv", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println(id + mnemonic + shortName + email + "\n");
        } catch (IOException e) {
        }

    }
}
