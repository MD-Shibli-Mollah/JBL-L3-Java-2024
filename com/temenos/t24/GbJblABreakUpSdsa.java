package com.temenos.t24;

import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
//import com.temenos.t24.api.records.category.CategoryRecord;
//import com.temenos.t24.api.records.ebjblbreakupparam.EbJblBreakupParamRecord;
import com.temenos.t24.api.records.ebjblsdsaentrydetails.EbJblSdsaEntryDetailsRecord;
import com.temenos.t24.api.records.ebjblsdsaentrydetails.OrgTransRefNoClass;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblsdsaentrydetails.EbJblSdsaEntryDetailsTable;

/*
 * MODULE         : FT/TT - BREAKUP Module
 * VERSION        : FUNDS.TRANSFER & TELLER
 * EB.API         : GbJblABreakUpSdsa
 * ATTACHED AS    : AUTH ROUTINE
 * RELATED APP    : N/A
 * AUTHOR         : MD SHIBLI MOLLAH
 * DATE           : 14-OCT-2024
 * Details        : 
                    
 * MODIFIED BY    : 
 * DATE           : 
 */

public class GbJblABreakUpSdsa extends RecordLifecycle {
    public void postUpdateRequest(java.lang.String application, java.lang.String currentRecordId,
            TStructure currentRecord, List<com.temenos.t24.api.complex.eb.servicehook.TransactionData> transactionData,
            List<TStructure> currentRecords, TransactionContext transactionContext) {
        if (transactionContext.getCurrentFunction().equals("AUTHORISE")) {
            String myApplication = "";
            String myVersion = "";

            String debitAcctNo = "";
            String creditAcctNo = "";
            // String debitAcctCat = "";
            // String creditAcctCat = "";
            String debitTheirRef = "";
            String creditTheirRef = "";
            String debitCurrency = "";
            String creditCurrency = "";
            String debitAmount = "";
            String creditAmount = "";
            // String drCrMarker = "";
            String debitValueDate = "";
            String creditValueDate = "";

            String sdsaId = "";
            String refNo = "";
            String acNumber = "";
            String acType = "";
            String orgTransRefNo = "";
            String orgTransCurncy = "";
            String orgExchRate = "";
            String orgParticular = "";
            String orgDate = "";
            String drCr = "";
            String orgAmt = "";

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
                orgExchRate = ft.getInExchRate().getValue();
                orgTransRefNo = currentRecordId;

                // SDSA FT DEBIT
                if (myVersion.equals(",JBL.BREAKUP.DEBIT")) {
                    drCr = "DR";

                    refNo = debitTheirRef;
                    acNumber = debitAcctNo;
                    acType = "A";
                    orgTransRefNo = currentRecordId;
                    orgTransCurncy = debitCurrency;
                    orgParticular = debitTheirRef;
                    orgDate = debitValueDate;
                    orgAmt = debitAmount;
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, orgTransRefNo, orgTransCurncy, orgExchRate,
                            orgParticular, orgDate, drCr, orgAmt, transactionContext);
                }

                // SDSA FT CREDIT
                if (myVersion.equals(",JBL.BREAKUP.CREDIT")) {
                    drCr = "CR";

                    refNo = creditTheirRef;
                    acNumber = creditAcctNo;
                    acType = "L";
                    orgTransRefNo = currentRecordId;
                    orgTransCurncy = creditCurrency;
                    orgParticular = creditTheirRef;
                    orgDate = creditValueDate;
                    orgAmt = creditAmount;
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, orgTransRefNo, orgTransCurncy, orgExchRate,
                            orgParticular, orgDate, drCr, orgAmt, transactionContext);
                }

                // SDSA FT DEBIT & CREDIT Both...
                if (myVersion.equals(",JBL.BREAKUP")) {
                    // CREDIT part
                    drCr = "CR";
                    refNo = creditTheirRef;
                    acNumber = creditAcctNo;
                    acType = "L";
                    orgTransRefNo = currentRecordId;
                    orgTransCurncy = creditCurrency;
                    orgParticular = creditTheirRef;
                    orgDate = creditValueDate;
                    orgAmt = debitAmount; // DEBIT.AMOUNT Field is considered
                                          // for BOTH
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, orgTransRefNo, orgTransCurncy, orgExchRate,
                            orgParticular, orgDate, drCr, orgAmt, transactionContext);

                    // DEBIT part
                    drCr = "DR";
                    refNo = debitTheirRef;
                    acNumber = debitAcctNo;
                    acType = "A";
                    orgTransRefNo = currentRecordId;
                    orgTransCurncy = debitCurrency;
                    orgParticular = debitTheirRef;
                    orgDate = debitValueDate;
                    orgAmt = debitAmount;
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, orgTransRefNo, orgTransCurncy, orgExchRate,
                            orgParticular, orgDate, drCr, orgAmt, transactionContext);
                }
                return;
            }

            else if (myApplication.equals("TELLER")) {

                // Breakup TT DEPOSIT Version
                if (myVersion.equals(",JBL.BREAKUP.DEPOSIT")) {
                    creditAcctNo = teller.getAccount2().getValue();
                    drCr = "CR";
                    refNo = teller.getNarrative2(0).getValue();
                    acNumber = creditAcctNo;
                    acType = "L";
                    orgTransRefNo = currentRecordId;
                    orgTransCurncy = teller.getCurrency2().getValue();
                    orgParticular = refNo;
                    orgDate = teller.getValueDate2().getValue();
                    orgAmt = teller.getAmountLocal2().getValue();
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, orgTransRefNo, orgTransCurncy, orgExchRate,
                            orgParticular, orgDate, drCr, orgAmt, transactionContext);
                }
                // Breakup TT DEPOSIT Version END
                // Breakup TT WITHDRAW Version
                else if (myVersion.equals(",JBL.BREAKUP.WITHDRAW")) {
                    debitAcctNo = teller.getAccount2().getValue();
                    drCr = "DR";
                    refNo = teller.getNarrative2(0).getValue();
                    acNumber = debitAcctNo;
                    acType = "A";
                    orgTransRefNo = currentRecordId;
                    orgTransCurncy = teller.getCurrency2().getValue();
                    orgParticular = refNo;
                    orgDate = teller.getValueDate2().getValue();
                    orgAmt = teller.getAmountLocal2().getValue();
                    // Calling SDSA Template
                    SdsaTempWrite(sdsaId, refNo, acNumber, acType, orgTransRefNo, orgTransCurncy, orgExchRate,
                            orgParticular, orgDate, drCr, orgAmt, transactionContext);
                }

                return;
            } else {
                return;
            }
        }
    }

    // Write and Delete in SDSA Template
    public void SdsaTempWrite(String sdsaId, String refNo, String acNumber, String acType, String orgTransRefNo,
            String orgTransCurncy, String orgExchRate, String orgParticular, String orgDate, String drCr, String orgAmt,
            TransactionContext transactionContext) {

        DataAccess da = new DataAccess(this);
        EbJblSdsaEntryDetailsTable ebJblSdsaEntryDetailsTable = new EbJblSdsaEntryDetailsTable(this);

        // temp Id generation
        sdsaId = acNumber + refNo;

        EbJblSdsaEntryDetailsRecord ebJblSdsaEntryDetailsRecord = null;
        ebJblSdsaEntryDetailsRecord = new EbJblSdsaEntryDetailsRecord(this);
        EbJblSdsaEntryDetailsRecord ebJblSdsaEntryDetailsRecordExists = null;

        // Init Multi-Value fields
        List<OrgTransRefNoClass> orgTransRefNoList = null;
        Integer orgTransRefNoListSize = 0;

        try {
            ebJblSdsaEntryDetailsRecordExists = new EbJblSdsaEntryDetailsRecord(this);
            ebJblSdsaEntryDetailsRecordExists = new EbJblSdsaEntryDetailsRecord(
                    da.getRecord("EB.JBL.SDSA.ENTRY.DETAILS", sdsaId));
            // Get Multi-Value fields
            orgTransRefNoList = ebJblSdsaEntryDetailsRecordExists.getOrgTransRefNo();
            // Get Multi-Value fields Size
            orgTransRefNoListSize = orgTransRefNoList.size();
        } catch (Exception e) {
        }
        // Set Values in the Table
        ebJblSdsaEntryDetailsRecord.setAcNumber(acNumber);
        ebJblSdsaEntryDetailsRecord.setRefNo(refNo);
        ebJblSdsaEntryDetailsRecord.setAcType(acType);

        // SET Multi-Value fields for Origination.
        OrgTransRefNoClass orgTransRefNoClass = null;
        orgTransRefNoClass = new OrgTransRefNoClass();
        orgTransRefNoClass.setOrgTransRefNo(orgTransRefNo);
        orgTransRefNoClass.setOrgAmt(orgAmt);
        orgTransRefNoClass.setOrgTransCur(orgTransCurncy);
        orgTransRefNoClass.setOrgDate(orgDate);
        orgTransRefNoClass.setOrgDrcr(drCr);
        orgTransRefNoClass.setOrgExchRate(orgExchRate);
        orgTransRefNoClass.setOrgParticular(orgParticular);

        String myOrgAmount = "0.0";
        double orgAmount = 0.0;
        double totOrgAmt = 0.0;
        String myTotOrgAmt = "0.0";

        double totadjAmt = 0.0;
        String myTotAdjAmt = "0.0";

        double outstndgAmt = 0.0;
        String myOutstndgAmt = "0.0";

        if (!(orgTransRefNoListSize == 0)) {
            ebJblSdsaEntryDetailsRecordExists.setOrgTransRefNo(orgTransRefNoClass, orgTransRefNoListSize);
            orgTransRefNoList = ebJblSdsaEntryDetailsRecordExists.getOrgTransRefNo();
            orgTransRefNoListSize = orgTransRefNoList.size(); // Size will
                                                              // change here
                                                              // with new Record

            try {
                for (int i = 0; i < orgTransRefNoListSize; i++) {
                    myOrgAmount = orgTransRefNoList.get(i).getOrgAmt().getValue();
                    orgAmount = Double.parseDouble(myOrgAmount);
                    totOrgAmt = totOrgAmt + orgAmount;
                }
                // SET Total Originating Value
                myTotOrgAmt = Double.toString(totOrgAmt);
                ebJblSdsaEntryDetailsRecordExists.setTotOrgAmt(myTotOrgAmt);

                // Get Total Outstanding Value if Available
                if (!myTotAdjAmt.equals("0.0")) {
                    myTotAdjAmt = ebJblSdsaEntryDetailsRecordExists.getTotAdjAmt().getValue();

                    totadjAmt = Double.parseDouble(myTotAdjAmt);
                    outstndgAmt = totOrgAmt - totadjAmt;
                    myOutstndgAmt = Double.toString(outstndgAmt);
                    ebJblSdsaEntryDetailsRecordExists.setOutstandingAmt(myOutstndgAmt);
                } else {
                    outstndgAmt = totOrgAmt - totadjAmt;
                    myOutstndgAmt = Double.toString(outstndgAmt);
                    ebJblSdsaEntryDetailsRecordExists.setOutstandingAmt(myOutstndgAmt);
                }

            } catch (Exception e) {
            }

            try {
                ebJblSdsaEntryDetailsTable.write(sdsaId, ebJblSdsaEntryDetailsRecordExists);
            } catch (T24IOException e) {
                System.out.println("Unable to write data on Cash Feeding Table");
            }
        }

        else {
            ebJblSdsaEntryDetailsRecord.setOrgTransRefNo(orgTransRefNoClass, 0);
            ebJblSdsaEntryDetailsRecord.setTotOrgAmt(orgAmt);

            try {
                ebJblSdsaEntryDetailsTable.write(sdsaId, ebJblSdsaEntryDetailsRecord);
            } catch (T24IOException e) {
                System.out.println("Unable to write data on Cash Feeding Table");
            }
        }

        // If FT/TT is Deleted
        if (transactionContext.getCurrentFunction().equals("DELETE")) {

            ebJblSdsaEntryDetailsRecord.setAcNumber("");
            ebJblSdsaEntryDetailsRecord.setRefNo("");
            ebJblSdsaEntryDetailsRecord.setAcType("");

            try {
                ebJblSdsaEntryDetailsTable.delete(sdsaId);
            } catch (T24IOException e) {
                System.out.println("Unable to delete data on Cash Feeding Table");
            }
        }

        return;
    }
}
