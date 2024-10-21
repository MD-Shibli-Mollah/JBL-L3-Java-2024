package com.temenos.t24;

import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.complex.eb.templatehook.TransactionData;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.ebjblcashfeeding.EbJblCashFeedingRecord;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblcashfeeding.EbJblCashFeedingTable;

/**
 * TODO: an authorization routine, and during the execution of this routine, an
 * exception occurs before a record is authorized. You need to both display the
 * exception message to the user and log the reason for the failure in a local
 * table.
 *
 * @author MD Shibli Mollah
 *
 */
public class GbJblBATtExceptionTest extends RecordLifecycle {

    @Override
    public void updateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext,
            List<TransactionData> transactionData, List<TStructure> currentRecords) {
        if (transactionContext.getCurrentFunction().equals("AUTHORISE")) {

            DataAccess da = new DataAccess(this);
            TellerRecord tellerRecord = new TellerRecord(currentRecord);

            String flag = "0";
            String myErr = "";
            // Initialize string Variables
            try {
                String tempId = null;
                // String tempId = tellerRecord.getTheirReference().getValue();
                // // TEMPLATE_I
                TellerRecord t = new TellerRecord(da.getRecord("TELLER", tempId));
                // String tempId = null;
                // tellerRecord.setTheirReference(tempId);
            } catch (Exception e) {
                flag = "1";
                myErr = e.toString();
                /*
                 * EbJblCashFeedingTable ebJblCashFeedingTable = new
                 * EbJblCashFeedingTable(this); EbJblCashFeedingRecord
                 * ebJblCashFeedingRecord = null; String testID =
                 * "CASHRM22123456F6"; ebJblCashFeedingRecord = new
                 * EbJblCashFeedingRecord(this);
                 * ebJblCashFeedingRecord.setStatus(myErr);
                 * 
                 * try { ebJblCashFeedingTable.write(testID,
                 * ebJblCashFeedingRecord); } catch (T24IOException e1) {
                 * System.out.
                 * println("Unable to write data on Cash Feeding Table"); }
                 */
                tellerRecord.getAccount2().setError(myErr);
                // throw new T24CoreException("", "ST-JBL.AML.NULL");
            }
            if (flag.equals("1")) {
                EbJblCashFeedingTable ebJblCashFeedingTable = new EbJblCashFeedingTable(this);
                EbJblCashFeedingRecord ebJblCashFeedingRecord = null;
                String testID = "CASHRM22123456F6";
                ebJblCashFeedingRecord = new EbJblCashFeedingRecord(this);
                ebJblCashFeedingRecord.setStatus(myErr);

                try {
                    ebJblCashFeedingTable.write(testID, ebJblCashFeedingRecord);
                } catch (T24IOException e1) {
                    System.out.println("Unable to write data on Cash Feeding Table");
                }
               // throw new T24CoreException("", "ST-JBL.AML.NULL");
            }
        } else {
            return;
        }
    }
}
