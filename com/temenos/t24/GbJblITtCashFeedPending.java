package com.temenos.t24;

import com.temenos.api.LocalRefClass;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.ebjblcashfeeding.EbJblCashFeedingRecord;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblcashfeeding.EbJblCashFeedingTable;

/*
 * MODULE         : TT - Cash Feeding
 * VERSION        : TELLER
 * EB.API         : GbJblIWalkInCusDtls
 * ATTACHED AS    : INPUT ROUTINE
 * RELATED APP    : N/A
 * AUTHOR         : MD SHIBLI MOLLAH
 * DATE           : 14-OCT-2024
 * Details        : STATUS  >> PENDING
                    Feeding branch transaction id >> Teller transaction id
                    Approve amount > Teller Amount
                    Approve amount in word > Amount in word of Teller
 * MODIFIED BY    : 
 * DATE           : 
 */
public class GbJblITtCashFeedPending extends RecordLifecycle {
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {

        DataAccess da = new DataAccess(this);
        TellerRecord tellerRecord = new TellerRecord(currentRecord);

        // Initialize string Variables
        String status = "PENDING";
        String approveAmount = "";
        String apprvAmtInWord = "";
        String tempId = "";
        try {
            tempId = tellerRecord.getTheirReference().getValue(); // TEMPLATE_ID
            approveAmount = tellerRecord.getNetAmount().getValue();

            // LocalRefClass localRefClass = new LocalRefClass();
            // LocalRefClass myLocRefClass =
            // tellerRecord.getLocalRef("LT.AMT.WORD");
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

            // If TT is Deleted
            if (transactionContext.getCurrentFunction().equals("DELETE")) {
                status = "REQUEST";
                ebJblCashFeedingRecord.setStatus(status);
                ebJblCashFeedingRecord.setApprovedAmount("");
                ebJblCashFeedingRecord.setAmountInWord2("");
                ebJblCashFeedingRecord.setTrIdFeedBr("");
            }

            ebJblCashFeedingTable.write(tempId, ebJblCashFeedingRecord);
        } catch (T24IOException e) {
            System.out.println("Unable to write data on Cash Feeding Table");
        }
        return tellerRecord.getValidationResponse();
    }
}
