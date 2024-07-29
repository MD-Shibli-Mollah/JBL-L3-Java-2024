package com.jbl.enquiry;

import java.util.ArrayList;
import java.util.List;

import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.category.CategoryRecord;
import com.temenos.t24.api.records.company.CompanyRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author rajon
 *
 */
public class JblENofAcctValPSO extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        DataAccess da = new DataAccess(this);

        String selAccountNo = "";
        if (filterCriteria.get(0).getFieldname().equals("ACCOUNT.NO")) {
            selAccountNo = filterCriteria.get(0).getValue();
        }

        String selPhoneNumber = "";
        if (filterCriteria.get(1).getFieldname().equals("PHONE.NUMBER")) {
            selPhoneNumber = filterCriteria.get(1).getValue();
        }

        String selNidNo = "";
        if (filterCriteria.get(2).getFieldname().equals("NID.NO")) {
            selNidNo = filterCriteria.get(2).getValue();
        }

        ArrayList<String> retIds = new ArrayList<>();
        AccountRecord accRec = null;
        boolean isAccountValid = false;
        try {
            accRec = new AccountRecord(da.getRecord("ACCOUNT", selAccountNo));
            isAccountValid = true;
        } catch (Exception e) {
            retIds.add("false");
        }
        if (accRec != null && isAccountValid == true) {
            String customerNo = accRec.getCustomer().getValue();
            String accountNo = selAccountNo;
            String altAccountId = accRec.getAltAcctType().get(0).getAltAcctId().getValue();
            String accountTitle = accRec.getAccountTitle1().get(0).getValue();
            String accountCategory = accRec.getCategory().getValue();
            String coCode = accRec.getCoCode();
            String accountCcy = accRec.getCurrency().getValue();
            String workingBalance = accRec.getWorkingBalance().getValue();
            String onlineActualBalance = accRec.getOnlineActualBal().getValue();
            String smsAlert = accRec.getLocalRefField("LT.SMS.ALERT").getValue();
            
            String accountStatusCode;
            try{
                accountStatusCode = accRec.getPostingRestrict(0).getValue();
            }catch (Exception e) {
                accountStatusCode = "0";
            }

            CustomerRecord cusRec = new CustomerRecord(da.getRecord("CUSTOMER", customerNo));
            String phoneNumber = cusRec.getPhone1(0).getPhone1().getValue();
            String nidNo = cusRec.getLegalId(0).getLegalId().getValue();
            CategoryRecord categoryRecord = new CategoryRecord(da.getRecord("CATEGORY", accountCategory));
            String accountCategoryDescription = categoryRecord.getDescription(0).getValue();

            CompanyRecord companyRecord = new CompanyRecord(da.getRecord("COMPANY", coCode));
            String companyName = companyRecord.getCompanyName(0).getValue();

            retIds.add(accountNo + "*" + altAccountId + "*" + customerNo + "*" + accountTitle + "*" + workingBalance
                    + "*" + onlineActualBalance + "*" + accountCcy + "*" + coCode + "*" + companyName 
                    + "*" + accountStatusCode + "*" + accountCategory + "*" + accountCategoryDescription 
                    + "*" + smsAlert + "*" + phoneNumber + "*" + nidNo + "*" + isAccountValid);
        }

        return retIds;
    }

}
