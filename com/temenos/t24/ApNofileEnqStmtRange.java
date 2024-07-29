package com.temenos.t24;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.temenos.api.TDate;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.party.Account;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.records.stmtentry.StmtEntryRecord;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: API for account statement for a date range
 * EB.API>NOF.NofileEnqStmtRange STANDARD.SELECTION>NOFILE.JBL.PSOPSP.STMT.RANGE
 * ENQUIRY>AC.API.JBL.PSOPSP.STMT.DATE.RANGE.1.0.0
 *
 * @author MD Shibli Mollah
 *
 */
public class ApNofileEnqStmtRange extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        // Init Variables
        String selAccountNo = "";
        String selFromDate = "";
        String selToDate = "";
        LocalDate fromDate = null;
        LocalDate toDate = null;
        LocalDate txnDate = null;

        DataAccess da = new DataAccess(this);
        List<String> retStmtInfo = new ArrayList<String>();
        selAccountNo = filterCriteria.get(0).getValue();
        selFromDate = filterCriteria.get(1).getValue();
        selToDate = filterCriteria.get(2).getValue();

        Account account = new Account(this);
        account.setAccountId(selAccountNo);

        try {
            fromDate = LocalDate.parse(selFromDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
            toDate = LocalDate.parse(selToDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            System.out.println("exception");
        }

        TDate startDate = new TDate();
        startDate.set(fromDate.getYear(), fromDate.getMonth().getValue(), fromDate.getDayOfMonth());
        TDate endDate = new TDate();
        endDate.set(toDate.getYear(), toDate.getMonth().getValue(), toDate.getDayOfMonth());

        List<String> stmtRecords = account.getEntries("BOOK", "", "", "", startDate, endDate);

        FundsTransferRecord ftRec = null;
        TellerRecord ttRec = null;
        AccountRecord accRec = null;

        String record = null;
        record = " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " "
                + "*" + " " + "*" + " " + "*" + stmtRecords.size();

        retStmtInfo.add(record);

        for (String stmtId : stmtRecords) {
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
            String stmtRecStatus = "";
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
