package com.jbl.enquiry;

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
 * TODO: Document me!
 *
 * @author rajon
 *
 */
public class NofileEnqStmtRange extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        DataAccess da = new DataAccess(this);
        List<String> retStmtInfo = new ArrayList<String>();
        String selAccountNo = filterCriteria.get(0).getValue();
        String selFromDate = filterCriteria.get(1).getValue();
        String selToDate = filterCriteria.get(2).getValue();
       
        Account account = new Account(this);
        account.setAccountId(selAccountNo);

        LocalDate fromDate = null;
        LocalDate toDate = null;
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
        StmtEntryRecord stmtRec = null;
        for (String stmtId : stmtRecords) {
            stmtRec = new StmtEntryRecord(da.getRecord("STMT.ENTRY", stmtId));
//            String txnDateTime = stmtRec.getValueDate().getValue();
            String txnReference = stmtRec.getTransReference().getValue();
            LocalDate txnDate = LocalDate.parse(stmtRec.getBookingDate().getValue(),
                    DateTimeFormatter.ofPattern("yyyyMMdd"));
            String trtype = "eJanata";
            if (txnReference.startsWith("FT")) {
                ftRec = new FundsTransferRecord(da.getRecord("FUNDS.TRANSFER", txnReference));
            } else {
               
                if (txnReference.startsWith("TT")) {                    
                    ttRec = new TellerRecord(da.getRecord("TELLER", txnReference));
                    accRec = new AccountRecord(da.getRecord("ACCOUNT", ttRec.getAccount2().getValue()));
                    String ttTheirRef = stmtRec.getTheirReference().getValue();
                    if (ttTheirRef.equals("") || ttTheirRef.equals(null)) {
                        ttTheirRef = " ";
                    }
                    retStmtInfo.add(txnDate + "*" + txnReference + "*" + ttRec.getAccount2().getValue() + "*"
                            + accRec.getCoCode() + "*" + ttRec.getAccount1(0).getAccount1().getValue() + "*"+ ttRec.getCoCode() + "*"
                            +accRec.getAccountTitle1().get(0).getValue() + "*" + stmtRec.getAmountLcy().getValue() + "*" + ttTheirRef + "*"
                            + stmtRec.getCurrency().getValue() + "*" + trtype + "*" + " ");
                }
                continue;
            }
            String debitAccNo = ftRec.getDebitAcctNo().getValue();
            try {
                accRec = new AccountRecord(da.getRecord("ACCOUNT", debitAccNo));
            } catch (Exception e) {

            }
            String debitBranchCode = accRec.getCoCode();

            String creditAccNo = ftRec.getCreditAcctNo().getValue();
            try {
                accRec = new AccountRecord(da.getRecord("ACCOUNT", creditAccNo));
            } catch (Exception e) {

            }
            String creditBranchCode = accRec.getCoCode();
            String creditAccName = accRec.getAccountTitle1().get(0).getValue();
            String amount = stmtRec.getAmountLcy().getValue();
            String txnNarrative = stmtRec.getTheirReference().getValue();
            if (txnNarrative.equals("")) {
                txnNarrative = " ";
            }
            String creditAcctCurrencyCode = stmtRec.getCurrency().getValue();

            record = txnDate + "*" + txnReference + "*" + debitAccNo + "*" + debitBranchCode + "*" + creditAccNo + "*"
                    + creditBranchCode + "*" + creditAccName + "*" + amount + "*" + txnNarrative + "*"
                    + creditAcctCurrencyCode + "*" + trtype + "*" + " ";

            retStmtInfo.add(record);
        }
        
        return retStmtInfo;

    }

}
