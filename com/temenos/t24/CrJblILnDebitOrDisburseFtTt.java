package com.temenos.t24;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author nazihar
 * 
 *          Development Date : 03/07/2024
 *         Developed By : Taki Yasir 
 *         Designation :Software Engineer 
 *         Email : taki@nazihargroup.com 
 *         Business Logic : 
 *                         ** System will not allow to disburse without specific Transaction type & Code.
 *                           * If transaction type is not "ACDI" or debit account arrangement product line 
 *                             is not "LENDING" and debit account arrangement product group is not 
 *                             "JBL.CONT.GRP.LN" then it will set the error.
 *                           * If transaction code is not "or" or account2 arrangement product line 
 *                             is not "LENDING" and debit account arrangement product group is not 
 *                             "JBL.CONT.GRP.LN" then it will set the error.
 *                                    
 *         Routine Attached : INPUT ROUTINE
 *         Version : FUNDS.TRANSFER,JBL.LOAN.DISBURSE.FT, TELLER,AA.DISBURSE.TT, TELLER,LCY.CASHIN
 *         Routine Type : Version Routine  
 *
 */
public class CrJblILnDebitOrDisburseFtTt extends RecordLifecycle {

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
 
        DataAccess da = new DataAccess(this);
        AccountRecord accountRecord = null;
        String arrangementId = "";
        AaArrangementRecord arrangementRecord = null;
        String productLine = "";
        String productGroup = "";

        if (application.equals("FUNDS.TRANSFER")) {
            FundsTransferRecord ft = null;
            String debitAccNo = "";
            String transactionType = "";

            try {

                ft = new FundsTransferRecord(currentRecord);
                transactionType = ft.getTransactionType().getValue().toString();
                debitAccNo = ft.getDebitAcctNo().getValue().toString();
                accountRecord = new AccountRecord(da.getRecord("ACCOUNT", debitAccNo));
                arrangementId = accountRecord.getArrangementId().getValue();
                arrangementRecord = new AaArrangementRecord(da.getRecord("AA.ARRANGEMENT", arrangementId));
                productLine = arrangementRecord.getProductLine().getValue();
                productGroup = arrangementRecord.getProductGroup().getValue();

            } catch (Exception e) {

            }

            if (!transactionType.equals("ACDI")) {

                ft.getDebitAcctNo().setError("Please use the Loan Menu.");
                return ft.getValidationResponse();

            } else if (!productLine.equals("LENDING")) {

                if (!productGroup.equals("JBL.CONT.GRP.LN")) {

                    ft.getDebitAcctNo().setError("Please use the Loan Menu.");
                    return ft.getValidationResponse();

                }

            }

            return ft.getValidationResponse();

        } else if (application.equals("TELLER")) {

            TellerRecord tellerRecord = null;
            String tellerAccNo = "";
            String tellerTransactionCode = "";

            try {

                tellerRecord = new TellerRecord(currentRecord);
                tellerAccNo = tellerRecord.getAccount2().getValue();
                tellerTransactionCode = tellerRecord.getTransactionCode().getValue();
                accountRecord = new AccountRecord(da.getRecord("ACCOUNT", tellerAccNo));
                arrangementId = accountRecord.getArrangementId().getValue();
                arrangementRecord = new AaArrangementRecord(da.getRecord("AA.ARRANGEMENT", arrangementId));
                productLine = arrangementRecord.getProductLine().getValue();
                productGroup = arrangementRecord.getProductGroup().getValue();

            } catch (Exception e) {
 
            }

            if (!tellerTransactionCode.equals("80")) {

                tellerRecord.getAccount2().setError("Please use the Loan Menu.");
                return tellerRecord.getValidationResponse();

            } else if (!productLine.equals("LENDING")) {

                if (!productGroup.equals("JBL.CONT.GRP.LN")) {

                    tellerRecord.getAccount2().setError("Please use the Loan Menu.");
                    return tellerRecord.getValidationResponse();

                }

            }

            return tellerRecord.getValidationResponse();

        }

        return null;

    }

}
