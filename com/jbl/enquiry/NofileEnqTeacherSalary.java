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
import com.temenos.t24.api.records.stmtentry.StmtEntryRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author rajon
 *
 */
public class NofileEnqTeacherSalary extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        DataAccess da = new DataAccess(this);
        List<String> retSalaryInfo = new ArrayList<String>();
        String selAccountNo = filterCriteria.get(0).getValue();
        String selMonth = filterCriteria.get(1).getValue();
        String selYear = filterCriteria.get(2).getValue();
        if (selAccountNo.equals("") || selAccountNo.equals(null) || selMonth.equals("") || selMonth.equals(null)
                || selYear.equals("") || selYear.equals(null)) {
            retSalaryInfo.add("6" + "*" + "false");
            return retSalaryInfo;
        }
        Account account = new Account(this);
        account.setAccountId(selAccountNo);

        LocalDate date = null;
        try{
           date = LocalDate.of(Integer.parseInt(selYear), Integer.parseInt(selMonth), 1);
        }catch (Exception e) {
            retSalaryInfo.add("6" + "*" + "false");
            return retSalaryInfo;
        }
        LocalDate lastDate = date.withDayOfMonth(date.getMonth().length(date.isLeapYear()));

        TDate startDate = new TDate();
        startDate.set(date.getYear(), date.getMonth().getValue(), 01);
        TDate endDate = new TDate();
        endDate.set(lastDate.getYear(), lastDate.getMonth().getValue(), lastDate.getDayOfMonth());

        List<String> stmtRecords = account.getEntries("VALUE", "", "", "", startDate, endDate);

        StmtEntryRecord stmtRec = null;
        for (String stmt : stmtRecords) {
            stmtRec = new StmtEntryRecord(da.getRecord("STMT.ENTRY", stmt));
            if (Integer.parseInt(stmtRec.getTransactionCode().getValue()) == 294) {
                LocalDate salaryDate = LocalDate.parse(stmtRec.getValueDate().getValue(),DateTimeFormatter.ofPattern("yyyyMMdd"));
                if (salaryDate.getDayOfMonth() >= startDate.getDay() && salaryDate.getDayOfMonth() <= endDate.getDay()) {
                    retSalaryInfo.add("0" + "*" + "true");
                    return retSalaryInfo;
                }
            }
        }
        
        retSalaryInfo.add("0" + "*" + "false");
        return retSalaryInfo;
    }

}
