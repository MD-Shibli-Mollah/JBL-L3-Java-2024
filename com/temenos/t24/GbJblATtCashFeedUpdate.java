package com.temenos.t24;

import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblcashfeeding.EbJblCashFeedingTable;
import com.temenos.t24.api.records.ebjblcashfeeding.EbJblCashFeedingRecord;

/*
 * MODULE         : TT - CASH FEEDING
 * VERSION        : TELLER,JBL.CASHWDL.FEED
 * EB.API         : GbJblBaCashFeedUpdate
 * ATTACHED AS    : AUTH ROUTINE
 * RELATED APP    : EB.JBL.CASH.FEEDING (ETB)
 * AUTHOR         : MD SHIBLI MOLLAH
 * DATE           : 10-OCT-2024
 * Details        : STATUS  >> approve
                    Feeding branch transaction id >> Teller transaction id
                    Approve amount > Teller Amount
                    Approve amount in word > Amount in word of Teller
 * MODIFIED BY    : 
 * DATE           :
 */
public class GbJblATtCashFeedUpdate extends RecordLifecycle {
    @Override
    public void postUpdateRequest(java.lang.String application, java.lang.String currentRecordId,
            TStructure currentRecord, List<com.temenos.t24.api.complex.eb.servicehook.TransactionData> transactionData,
            List<TStructure> currentRecords, TransactionContext transactionContext) {

        if (transactionContext.getCurrentFunction().equals("AUTHORISE")) {

            DataAccess da = new DataAccess(this);
            TellerRecord tellerRecord = new TellerRecord(currentRecord);

            // Initialize string Variables
            String status = "APPROVE";
            String approveAmount = "";
            String apprvAmtInWord = "";
            String tempId = "";
            try {
                tempId = tellerRecord.getTheirReference().getValue(); // TEMPLATE_ID
                approveAmount = tellerRecord.getNetAmount().getValue();
                // apprvAmtInWord =
                // tellerRecord.getLocalRefField("LT.AMT.WORD").getValue();
                apprvAmtInWord = tellerRecord.getLocalRef("LT.AMT.WORD").get(0).getValue();
            } catch (Exception e) {
            }
            try {
                EbJblCashFeedingTable ebJblCashFeedingTable = new EbJblCashFeedingTable(this);
                EbJblCashFeedingRecord ebJblCashFeedingRecord = null;
                ebJblCashFeedingRecord = new EbJblCashFeedingRecord(da.getRecord("EB.JBL.CASH.FEEDING", tempId));

                ebJblCashFeedingRecord.setStatus(status);
                ebJblCashFeedingRecord.setApprovedAmount(approveAmount);
                ebJblCashFeedingRecord.setAmountInWord2(apprvAmtInWord);
                ebJblCashFeedingRecord.setTrIdFeedBr(currentRecordId);

                ebJblCashFeedingTable.write(tempId, ebJblCashFeedingRecord);
            } catch (T24IOException e) {
                System.out.println("Unable to write data on Cash Feeding Table");
            }

        } else {
            return;
        }
    }
}
