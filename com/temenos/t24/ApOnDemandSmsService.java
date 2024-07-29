package com.temenos.t24;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.complex.eb.servicehook.SynchronousTransactionData;
import com.temenos.t24.api.complex.eb.servicehook.TransactionControl;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.tables.ebjblsmsondemand.EbJblSmsOnDemandRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblsmsondemand.EbJblSmsOnDemandTable;

public class ApOnDemandSmsService extends ServiceLifecycle {

    private static String smsBody = null;
    private static EbJblSmsOnDemandRecord demandRecord = null;

    @Override
    public List<String> getIds(ServiceData serviceData, List<String> controlList) {
        DataAccess da = new DataAccess(this);
        List<String> tmpIds = da.selectRecords("", "EB.JBL.SMS.ON.DEMAND", "", "WITH PROCESS.STATUS EQ ''");
        List<String> cusIds = null;

        if (tmpIds.size() != 0) {
            demandRecord = new EbJblSmsOnDemandRecord(da.getRecord("EB.JBL.SMS.ON.DEMAND", "SYSTEM"));

            String cmd = "";
            String line = "";

            try {
                cmd = demandRecord.getCmdBasedOnCus().getValue();
                smsBody = demandRecord.getSmsBody().getValue();
            } catch (Exception e) {
            }

            if (cmd == "") {
                System.out.println("Calling custom selection routine : OnDemandSmsCmd");
                ApOnDemandSmsCmd smsCmd = new ApOnDemandSmsCmd();
                cusIds = smsCmd.getIds(serviceData, controlList);
            } else {
                System.out.println("Getting selection command from template");
                cusIds = da.selectRecords("", "CUSTOMER", "", cmd);
            }

            if (smsBody == "") {
                File file = new File("/Temenos/T24/UD/Tracer/Tracer.txt");
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                } catch (FileNotFoundException e1) {
                }
                try {
                    while ((line = br.readLine()) != null) {
                        smsBody += line;
                    }
                } catch (IOException e1) {
                }
            }
        }
        return cusIds;
    }

    @Override
    public void updateRecord(String id, ServiceData serviceData, String controlItem,
            TransactionControl transactionControl, List<SynchronousTransactionData> transactionData,
            List<TStructure> records) {

        System.out.println("Processing Id with : " + id);

        try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/" + id + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println(smsBody);
        } catch (IOException e) {
        }

        demandRecord.setProcessStatus("SUCCESS");
        EbJblSmsOnDemandTable demandTable = new EbJblSmsOnDemandTable(this);
        try {
            System.out.println("SMS sent and updating template");
            demandTable.write("SYSTEM", demandRecord);
        } catch (T24IOException e) {
        }
    }
}
