package com.temenos.t24;

import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.ebjblsdsaentrydetails.AdjTransRefNoClass;
import com.temenos.t24.api.records.ebjblsdsaentrydetails.EbJblSdsaEntryDetailsRecord;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblsdsaentrydetails.EbJblSdsaEntryDetailsTable;

/*
 * MODULE         : FT/TT - BREAKUP Module
 * VERSION        : FUNDS.TRANSFER & TELLER
 * EB.API         : GbJblABreakupAdj
 * ATTACHED AS    : AUTH ROUTINE
 * RELATED APP    : N/A
 * AUTHOR         : MD SHIBLI MOLLAH
 * DATE           : 14-OCT-2024
 * Details        : 
                    
 * MODIFIED BY    : 
 * DATE           : 
 */

public class GbJblABreakupAdj extends RecordLifecycle {
    public void postUpdateRequest(java.lang.String application, java.lang.String currentRecordId,
            TStructure currentRecord, List<com.temenos.t24.api.complex.eb.servicehook.TransactionData> transactionData,
            List<TStructure> currentRecords, TransactionContext transactionContext) {
        if (transactionContext.getCurrentFunction().equals("AUTHORISE")) {
            String myApplication = "";
            String myVersion = "";

            String debitAcctNo = "";
            String creditAcctNo = "";
            String debitTheirRef = "";
            String creditTheirRef = "";
            String debitCurrency = "";
            String creditCurrency = "";
            String debitAmount = "";
            String creditAmount = "";
            String debitValueDate = "";
            String creditValueDate = "";

            String sdsaId = "";
            String refNo = "";
            String acNumber = "";
            String acType = "";
            String adjTransRefNo = "";
            String adjTransCurncy = "";
            String adjExchRate = "";
            String adjParticular = "";
            String adjDate = "";
            String drCr = "";
            String adjAmt = "";

            // DataAccess da = new DataAccess(this);
            TellerRecord teller = new TellerRecord(currentRecord);
            FundsTransferRecord ft = new FundsTransferRecord(currentRecord);

            myVersion = transactionContext.getCurrentVersionId();
            myApplication = transactionContext.getApplicationName();

            if (myApplication.equals("FUNDS.TRANSFER")) {
                debitAcctNo = ft.getDebitAcctNo().getValue();
                debitValueDate = ft.getDebitValueDate().getValue();
                debitCurrency = ft.getDebitCurrency().getValue();
                debitAmount = ft.getDebitAmount().getValue();
                debitTheirRef = ft.getDebitTheirRef().getValue();

                creditAcctNo = ft.getCreditAcctNo().getValue();
                creditValueDate = ft.getCreditValueDate().getValue();
                creditCurrency = ft.getCreditCurrency().getValue();
                creditAmount = ft.getCreditAmount().getValue();
                creditTheirRef = ft.getCreditTheirRef().getValue();
                adjExchRate = ft.getInExchRate().getValue();
                adjTransRefNo = currentRecordId;

                // SDSA FT DEBIT
                if (myVersion.equals(",JBL.BREAKUP.DEBIT.ADJ")) {
                    // drCr = "DR";

                    refNo = debitTheirRef;
                    // acNumber = debitAcctNo;
                    // acNumber & drCr will be altered
                    drCr = "CR";
                    acNumber = creditAcctNo;
                    acType = "A";
                    adjTransRefNo = currentRecordId;
                    adjTransCurncy = debitCurrency;
                    adjParticular = debitTheirRef;
                    adjDate = debitValueDate;
                    adjAmt = debitAmount;
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, adjTransRefNo, adjTransCurncy, adjExchRate,
                            adjParticular, adjDate, drCr, adjAmt, transactionContext);
                }

                // SDSA FT CREDIT
                if (myVersion.equals(",JBL.BREAKUP.CREDIT.ADJ")) {
                    // drCr = "CR";

                    refNo = creditTheirRef;
                    // acNumber = creditAcctNo;
                    // acNumber & drCr will be altered
                    drCr = "DR";
                    acNumber = debitAcctNo;
                    acType = "L";
                    adjTransRefNo = currentRecordId;
                    adjTransCurncy = creditCurrency;
                    adjParticular = creditTheirRef;
                    adjDate = creditValueDate;
                    adjAmt = creditAmount;
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, adjTransRefNo, adjTransCurncy, adjExchRate,
                            adjParticular, adjDate, drCr, adjAmt, transactionContext);
                }
                return;
            }

            else if (myApplication.equals("TELLER")) {

                // Breakup TT DEPOSIT Version
                if (myVersion.equals(",JBL.BREAKUP.DEPOSIT.ADJ")) {
                    creditAcctNo = teller.getAccount2().getValue();
                    drCr = "CR";
                    refNo = teller.getNarrative2(0).getValue();
                    acNumber = creditAcctNo;
                    acType = "L";
                    adjTransRefNo = currentRecordId;
                    adjTransCurncy = teller.getCurrency2().getValue();
                    adjParticular = refNo;
                    adjDate = teller.getValueDate2().getValue();
                    adjAmt = teller.getAmountLocal2().getValue();
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, adjTransRefNo, adjTransCurncy, adjExchRate,
                            adjParticular, adjDate, drCr, adjAmt, transactionContext);
                }
                // Breakup TT DEPOSIT Version END
                // Breakup TT WITHDRAW Version
                else if (myVersion.equals(",JBL.BREAKUP.WITHDRAW.ADJ")) {
                    debitAcctNo = teller.getAccount2().getValue();
                    drCr = "DR";
                    refNo = teller.getNarrative2(0).getValue();
                    acNumber = debitAcctNo;
                    acType = "A";
                    adjTransRefNo = currentRecordId;
                    adjTransCurncy = teller.getCurrency2().getValue();
                    adjParticular = refNo;
                    adjDate = teller.getValueDate2().getValue();
                    adjAmt = teller.getAmountLocal2().getValue();
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, adjTransRefNo, adjTransCurncy, adjExchRate,
                            adjParticular, adjDate, drCr, adjAmt, transactionContext);
                }

                return;
            } else {
                return;
            }
        }
    }

    // Write and Delete in SDSA Template
    public void SdsaTempWrite(String sdsaId, String refNo, String acNumber, String acType, String adjTransRefNo,
            String adjTransCurncy, String adjExchRate, String adjParticular, String adjDate, String drCr, String adjAmt,
            TransactionContext transactionContext) {

        DataAccess da = new DataAccess(this);
        EbJblSdsaEntryDetailsTable ebJblSdsaEntryDetailsTable = new EbJblSdsaEntryDetailsTable(this);

        // temp Id generation
        sdsaId = acNumber + refNo;

        EbJblSdsaEntryDetailsRecord ebJblSdsaEntryDetailsRecord = null;
        ebJblSdsaEntryDetailsRecord = new EbJblSdsaEntryDetailsRecord(
                da.getRecord("EB.JBL.SDSA.ENTRY.DETAILS", sdsaId));
        EbJblSdsaEntryDetailsRecord ebJblSdsaEntryDetailsRecordExists = null;

        // Init Multi-Value fields
        List<AdjTransRefNoClass> adjTransRefNoList = null;
        Integer adjTransRefNoListSize = 0;

        try {
            ebJblSdsaEntryDetailsRecordExists = new EbJblSdsaEntryDetailsRecord(this);
            ebJblSdsaEntryDetailsRecordExists = new EbJblSdsaEntryDetailsRecord(
                    da.getRecord("EB.JBL.SDSA.ENTRY.DETAILS", sdsaId));
            // Get Multi-Value fields
            adjTransRefNoList = ebJblSdsaEntryDetailsRecordExists.getAdjTransRefNo();
            // Get Multi-Value fields Size
            adjTransRefNoListSize = adjTransRefNoList.size();
        } catch (Exception e) {
        }
        // Set Values in the Table
        /*
         * ebJblSdsaEntryDetailsRecord.setAcNumber(acNumber);
         * ebJblSdsaEntryDetailsRecord.setRefNo(refNo);
         * ebJblSdsaEntryDetailsRecord.setAcType(acType);
         */

        // SET Multi-Value fields for Origination.
        AdjTransRefNoClass AdjTransRefNoClass = null;
        AdjTransRefNoClass = new AdjTransRefNoClass();
        AdjTransRefNoClass.setAdjTransRefNo(adjTransRefNo);
        AdjTransRefNoClass.setAdjAmt(adjAmt);
        AdjTransRefNoClass.setAdjTransCur(adjTransCurncy);
        AdjTransRefNoClass.setAdjDate(adjDate);
        AdjTransRefNoClass.setAdjDrcr(drCr);
        AdjTransRefNoClass.setAdjExchRate(adjExchRate);
        AdjTransRefNoClass.setAdjParticular(adjParticular);

        String myOrgTotAmt = "0.0";
        double orgTotAmt = 0.0;

        String myadjAmount = "0.0";
        double adjAmount = 0.0;
        double totadjAmt = 0.0;
        String myTotadjAmt = "0.0";

        double outstndgAmt = 0.0;
        String myOutstndgAmt = "0.0";

        if (!(adjTransRefNoListSize == 0)) {
            ebJblSdsaEntryDetailsRecordExists.setAdjTransRefNo(AdjTransRefNoClass, adjTransRefNoListSize);
            adjTransRefNoList = ebJblSdsaEntryDetailsRecordExists.getAdjTransRefNo();
            adjTransRefNoListSize = adjTransRefNoList.size(); // Size will
                                                              // change here
                                                              // with new Record
            try {
                for (int i = 0; i < adjTransRefNoListSize; i++) {
                    myadjAmount = adjTransRefNoList.get(i).getAdjAmt().getValue();
                    adjAmount = Double.parseDouble(myadjAmount);
                    totadjAmt = totadjAmt + adjAmount;
                }
                // SET Total Adjustment Value
                myTotadjAmt = Double.toString(totadjAmt);
                ebJblSdsaEntryDetailsRecordExists.setTotAdjAmt(myTotadjAmt);

                // Get total Org Amount
                myOrgTotAmt = ebJblSdsaEntryDetailsRecordExists.getTotOrgAmt().getValue();
                orgTotAmt = Double.parseDouble(myOrgTotAmt);

                // Outstanding Amount
                outstndgAmt = orgTotAmt - totadjAmt;
                myOutstndgAmt = Double.toString(outstndgAmt);
                ebJblSdsaEntryDetailsRecordExists.setOutstandingAmt(myOutstndgAmt);

            } catch (Exception e) {
            }

            try {
                ebJblSdsaEntryDetailsTable.write(sdsaId, ebJblSdsaEntryDetailsRecordExists);
            } catch (T24IOException e) {
                System.out.println("Unable to write data on Cash Feeding Table");
            }
        }

        else {
            ebJblSdsaEntryDetailsRecord.setAdjTransRefNo(AdjTransRefNoClass, 0);

            // SET Total Adjustment Value
            ebJblSdsaEntryDetailsRecord.setTotAdjAmt(adjAmt);
            totadjAmt = Double.parseDouble(adjAmt);

            // Get total Org Amount
            myOrgTotAmt = ebJblSdsaEntryDetailsRecord.getTotOrgAmt().getValue();
            orgTotAmt = Double.parseDouble(myOrgTotAmt);

            // Outstanding Amount
            outstndgAmt = orgTotAmt - totadjAmt;
            myOutstndgAmt = Double.toString(outstndgAmt);
            ebJblSdsaEntryDetailsRecord.setOutstandingAmt(myOutstndgAmt);
            try {
                ebJblSdsaEntryDetailsTable.write(sdsaId, ebJblSdsaEntryDetailsRecord);
            } catch (T24IOException e) {
                System.out.println("Unable to write data on Cash Feeding Table");
            }
        }

        // If FT/TT is Deleted
        /*
         * if (transactionContext.getCurrentFunction().equals("DELETE")) {
         * 
         * ebJblSdsaEntryDetailsRecord.setAcNumber("");
         * ebJblSdsaEntryDetailsRecord.setRefNo("");
         * ebJblSdsaEntryDetailsRecord.setAcType("");
         * 
         * try { ebJblSdsaEntryDetailsTable.delete(sdsaId); } catch
         * (T24IOException e) {
         * System.out.println("Unable to delete data on Cash Feeding Table"); }
         * }
         */

        return;
    }
}
