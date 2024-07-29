package com.temenos.t24;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// import com.temenos.api.TDate;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.party.Account;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.acctstmtprint.AcctStmtPrintRecord;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.records.stmtentry.StmtEntryRecord;
//import com.temenos.t24.api.records.stmtprinted.StmtPrintedRecord;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: API for account statement for TOP N transactions
 * EB.API>NOF.NofileEnqStmtNtr STANDARD.SELECTION>NOFILE.JBL.PSOPSP.STMT.NTR
 * ENQUIRY>AC.API.JBL.PSOPSP.STMT.N.1.0.0
 *
 * @author MD Shibli Mollah
 *
 */

public class ApNofileEnqStmtNtr extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        // Init Variables
        String selAccountNo = "";
        String selNtr = "";
        LocalDate txnDate = null;

        String acStmtDateBal = "";
        String stmtDate = "";
        String stmtPrintedId = "";
        int stmtRecCount = 0;
        String stmtRecStatus = "";
        List<String> stmtRecords = new ArrayList<String>();
        List<String> topNStmtIds = new ArrayList<String>();

        DataAccess da = new DataAccess(this);
        List<String> retStmtInfo = new ArrayList<String>();
        selAccountNo = filterCriteria.get(0).getValue();
        selNtr = filterCriteria.get(1).getValue();

        int myselNtr = Integer.parseInt(selNtr);

        Account account = new Account(this);
        account.setAccountId(selAccountNo);

        AcctStmtPrintRecord acctStmtPrintRecord = new AcctStmtPrintRecord(
                da.getRecord("ACCT.STMT.PRINT", selAccountNo));
        acStmtDateBal = acctStmtPrintRecord.getStmtDateBal().getValue(); // 20210430/0

        stmtDate = acStmtDateBal.substring(0, acStmtDateBal.indexOf('/'));

        stmtPrintedId = selAccountNo + "-" + stmtDate; // 100000000088-20220602

        stmtRecords = da.getConcatValues("STMT.PRINTED", stmtPrintedId);
        // Reversing the stmtRecords List
        Collections.reverse(stmtRecords);
        // List<String> testList = stmtRecords;
        // String testId = testList.get(0);
        stmtRecCount = stmtRecords.size();

        if (stmtRecCount > myselNtr) {
            stmtRecCount = myselNtr;
            // Get a sublist of the first 10 IDs
            topNStmtIds = stmtRecords.subList(0, stmtRecCount);
        }

        else {
            topNStmtIds = stmtRecords.subList(0, stmtRecCount);
        }

        FundsTransferRecord ftRec = null;
        TellerRecord ttRec = null;
        AccountRecord accRec = null;

        String record = null;
        record = " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " "
                + "*" + " " + "*" + " " + "*" + stmtRecCount;

        retStmtInfo.add(record);
        for (String stmtId : topNStmtIds) {
            String txnReference = "";
            String debitAccNo = "";
            String debitBranchCode = "";
            String ttTheirRef = "";
            String creditAccNo = "";
            String creditBranchCode = "";
            String creditAccName = "";
            String amount = "";
            String txnNarrative = "";
            String creditAcctCurrencyCode = "";
            try {
                StmtEntryRecord stmtRec = null;
                stmtRec = new StmtEntryRecord(da.getRecord("STMT.ENTRY", stmtId));
                stmtRecStatus = stmtRec.getRecordStatus();
                if (stmtRecStatus.equals("REVE")) {
                    continue;
                }
                txnReference = stmtRec.getTransReference().getValue();

                if ((txnReference == null) || (txnReference.equals(""))) {
                    continue;
                }
                txnDate = LocalDate.parse(stmtRec.getBookingDate().getValue(), DateTimeFormatter.ofPattern("yyyyMMdd"));

                String trtype = "eJanata";
                if (txnReference.startsWith("FT")) {
                    ftRec = new FundsTransferRecord(da.getRecord("FUNDS.TRANSFER", txnReference));
                } else {
                    if (txnReference.startsWith("TT")) {
                        ttRec = new TellerRecord(da.getRecord("TELLER", txnReference));
                        accRec = new AccountRecord(da.getRecord("ACCOUNT", ttRec.getAccount2().getValue()));
                        ttTheirRef = stmtRec.getTheirReference().getValue();
                        if (ttTheirRef.equals("") || ttTheirRef.equals(null)) {
                            ttTheirRef = " ";
                        }
                        retStmtInfo.add(txnDate + "*" + txnReference + "*" + ttRec.getAccount2().getValue() + "*"
                                + accRec.getCoCode() + "*" + ttRec.getAccount1(0).getAccount1().getValue() + "*"
                                + ttRec.getCoCode() + "*" + accRec.getAccountTitle1().get(0).getValue() + "*"
                                + stmtRec.getAmountLcy().getValue() + "*" + ttTheirRef + "*"
                                + stmtRec.getCurrency().getValue() + "*" + trtype + "*" + " ");
                    }
                    continue;
                }
                debitAccNo = ftRec.getDebitAcctNo().getValue();
                accRec = new AccountRecord(da.getRecord("ACCOUNT", debitAccNo));
                debitBranchCode = accRec.getCoCode();

                creditAccNo = ftRec.getCreditAcctNo().getValue();
                accRec = new AccountRecord(da.getRecord("ACCOUNT", creditAccNo));
                creditBranchCode = accRec.getCoCode();
                creditAccName = accRec.getAccountTitle1().get(0).getValue();
                amount = stmtRec.getAmountLcy().getValue();
                txnNarrative = stmtRec.getTheirReference().getValue();

                if (txnNarrative.equals("")) {
                    txnNarrative = " ";
                }
                creditAcctCurrencyCode = stmtRec.getCurrency().getValue();

                record = txnDate + "*" + txnReference + "*" + debitAccNo + "*" + debitBranchCode + "*" + creditAccNo
                        + "*" + creditBranchCode + "*" + creditAccName + "*" + amount + "*" + txnNarrative + "*"
                        + creditAcctCurrencyCode + "*" + trtype + "*" + " ";
                retStmtInfo.add(record);
            } catch (Exception e) {
            }
        }
        return retStmtInfo;
    }
}
