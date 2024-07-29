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
import com.temenos.t24.api.records.customer.LegalIdClass;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author rajon
 *
 */
public class AccValRtn extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        DataAccess da = new DataAccess(this);

        String selAcctNo = "";
        String selPhoneNumber = "";
        String selNidNo = "";

        selAcctNo = filterCriteria.get(0).getValue();
        selPhoneNumber = filterCriteria.get(1).getValue();
        selNidNo = filterCriteria.get(2).getValue();

        List<String> accValInfo = new ArrayList<String>();
        boolean valid = true;

        AccountRecord accountRecord = null;
        CustomerRecord customerRecord = null;
        CompanyRecord companyRecord = null;
        CategoryRecord categoryRecord = null;

        try {
            accountRecord = new AccountRecord(da.getRecord("ACCOUNT", selAcctNo));
            customerRecord = new CustomerRecord(da.getRecord("CUSTOMER", accountRecord.getCustomer().getValue()));
            companyRecord = new CompanyRecord(da.getRecord("COMPANY", accountRecord.getCoCode()));
            categoryRecord = new CategoryRecord(da.getRecord("CATEGORY", accountRecord.getCategory().getValue()));
        } catch (Exception e) {
            valid = false;
        }

        String phoneNo = "";
        try {
            phoneNo = customerRecord.getPhone1(0).getPhone1().getValue();
            if (phoneNo.equals("") || phoneNo.equals(null)) {
                phoneNo = "No phone number found";
                return accValInfo;
            }
        } catch (Exception e) {
            return accValInfo;
        }
        String nidNo = "";   
        List<LegalIdClass> legalIds;
        try {
           legalIds = customerRecord.getLegalId();            
           for(LegalIdClass leagalId: legalIds){
               if(leagalId.getLegalDocName().getValue().equals("NATIONAL.ID")){
                   nidNo = leagalId.getLegalId().getValue();
                   break;
               }else{
                   return accValInfo;
               }
           }
        } catch (Exception e) {
            return accValInfo;
        }

        if (valid == true && selPhoneNumber.equals(phoneNo) && selNidNo.equals(nidNo)) {
            String accountNo = selAcctNo;
            String customerId = accountRecord.getCustomer().getValue();
            String accountTitle = accountRecord.getAccountTitle1(0).getValue();
            String currencyCode = accountRecord.getCurrency().getValue();
            String workingBalnace = accountRecord.getWorkingBalance().getValue();
            String onlineActualBalance = accountRecord.getOnlineActualBal().getValue();
            String categoryCode = accountRecord.getCategory().getValue();
            String categoryDesc = categoryRecord.getDescription(0).getValue();
            String coCode = accountRecord.getCoCode();
            String companyName = companyRecord.getCompanyName(0).getValue();

            String legacyAcctNo;
            try {
                legacyAcctNo = accountRecord.getAltAcctType().get(0).getAltAcctId().getValue();
                if (legacyAcctNo.equals("") || legacyAcctNo.equals(null)) {
                    legacyAcctNo = "No legacy account found";
                }
            } catch (Exception e) {
                legacyAcctNo = "No legacy account found";
            }

            String smsAlert;
            try {
                smsAlert = accountRecord.getLocalRefField("LT.SMS.ALERT").getValue();
                if (smsAlert.equals("") || smsAlert.equals(null)) {
                    smsAlert = "NO";
                }
            } catch (Exception e) {
                smsAlert = "NO";
            }

            String accountStatusCode;
            try {
                accountStatusCode = accountRecord.getPostingRestrict(0).getValue();
                if (accountStatusCode.equals("") || accountStatusCode.equals(null)) {
                    accountStatusCode = "0";
                }
            } catch (Exception e) {
                accountStatusCode = "0";
            }

            accValInfo.add("valid" + "*" + accountNo + "*" + legacyAcctNo + "*" + customerId + "*" + accountTitle + "*"
                    + phoneNo + "*" + nidNo + "*" + currencyCode + "*" + workingBalnace + "*" + onlineActualBalance
                    + "*" + categoryCode + "*" + categoryDesc + "*" + coCode + "*" + companyName + "*" + smsAlert + "*"
                    + accountStatusCode);

        } else {
//            accValInfo.add("Not valid" + "*" + selAcctNo);
            return accValInfo;
        }
        return accValInfo;
    }

}
