package com.jbl.enquiry;

import java.util.ArrayList;
import java.util.List;

import com.temenos.api.LocalRefList;
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
public class JblPsopspEnqAccInfo extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        DataAccess da = new DataAccess(this);
        String NO_DATA_FOUND = "Not Available";
        String selAcctNo = filterCriteria.get(0).getValue();

        List<String> accInfo = new ArrayList<String>();

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
            return accInfo;
        }

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

        String fatherName = "";
        try {
            fatherName = customerRecord.getLocalRefField("LT.FATHER.NAME").getValue();
            if (fatherName.equals("") || fatherName.equals(null)) {
                fatherName = NO_DATA_FOUND;
            }
        } catch (Exception e) {
            fatherName = NO_DATA_FOUND;
        }

        String motherName = "";
        try {
            motherName = customerRecord.getLocalRefField("LT.FATHER.NAME").getValue();
            if (motherName.equals("") || motherName.equals(null)) {
                motherName = NO_DATA_FOUND;
            }
        } catch (Exception e) {
            motherName = NO_DATA_FOUND;
        }

        String dob = customerRecord.getDateOfBirth().getValue();
        if (dob.equals("") || dob.equals(null)) {
            dob = NO_DATA_FOUND;
        }
        String phoneNo = "";
        try {
            phoneNo = customerRecord.getPhone1(0).getPhone1().getValue();
            if (phoneNo.equals("") || phoneNo.equals(null)) {
                phoneNo = NO_DATA_FOUND;
            }
        } catch (Exception e) {
            phoneNo = NO_DATA_FOUND;
        }
        String nidNo = "";
        List<LegalIdClass> legalIds;
        try {
            legalIds = customerRecord.getLegalId();
            for (LegalIdClass leagalId : legalIds) {
                if (leagalId.getLegalDocName().getValue().equals("NATIONAL.ID")) {
                    nidNo = leagalId.getLegalId().getValue();
                    break;
                } else {
                    nidNo = NO_DATA_FOUND;
                }
            }
        } catch (Exception e) {
            nidNo = NO_DATA_FOUND;
        }

        String legacyAcctNo;
        try {
            legacyAcctNo = accountRecord.getAltAcctType().get(0).getAltAcctId().getValue();
            if (legacyAcctNo.equals("") || legacyAcctNo.equals(null)) {
                legacyAcctNo = NO_DATA_FOUND;
            }
        } catch (Exception e) {
            legacyAcctNo = NO_DATA_FOUND;
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
        LocalRefList addr;
        String permanentAddr;
        
        try{
            addr = customerRecord.getLocalRefGroups("LT.CUS.PER.ADD");            
            permanentAddr = addr.get(0).getLocalRefField("LT.CUS.PER.ADD").getValue();
            if(permanentAddr.equals("") || permanentAddr.equals(null)){
                permanentAddr = NO_DATA_FOUND;
            }
        } catch (Exception e) {
            permanentAddr = NO_DATA_FOUND;
        }

        accInfo.add("valid" + "*" + accountNo + "*" + legacyAcctNo + "*" + customerId + "*" + accountTitle + "*"
                + phoneNo + "*" + nidNo + "*" + dob + "*" + fatherName + "*" + motherName + "*" + currencyCode + "*" +permanentAddr +"*"
                + workingBalnace + "*" + onlineActualBalance + "*" + categoryCode + "*" + categoryDesc + "*" + coCode
                + "*" + companyName + "*" + smsAlert + "*" + accountStatusCode);

        return accInfo;
    }

}
