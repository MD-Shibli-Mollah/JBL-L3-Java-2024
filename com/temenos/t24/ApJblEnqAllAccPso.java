package com.temenos.t24;

import java.util.ArrayList;
import java.util.List;

import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.party.Customer;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.category.CategoryRecord;
import com.temenos.t24.api.records.company.CompanyRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: API for Customer’s All Account Information EB.API>NOF.JblEnqAllAccPso
 * STANDARD.SELECTION>NOFILE.JBL.PSOPSP.ALL.ACC
 * ENQUIRY>AC.API.JBL.PSOPSP.CUSTOMER.ALL.ACCOUNTS.1.0.0
 *
 * @author MD Shibli Mollah
 *
 */
public class ApJblEnqAllAccPso extends Enquiry {
    private static final String NO_DATA_FOUND = "Not Available";

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        DataAccess da = new DataAccess(this);
        String recordId = filterCriteria.get(0).getValue();

        AccountRecord accountRecord = null;
        CustomerRecord customerRecord = null;

        Customer customer = new Customer(this);
        customer.setCustomerId(recordId);

        List<String> customerAccounts = customer.getAccountNumbers();
        List<String> allAccountInfo = new ArrayList<String>();
        String leagcyAccountNo = "";
        String accountTitle = "";
        String coCode = "";
        String coName = "";
        String workingBalance = "";
        String onlineActBalance = "";
        String categoryCode = "";
        String categoryDescription = "";
        String currencycode = "";
        String record = "";
        String accountStatusCode = "";
        try {
            customerRecord = new CustomerRecord(da.getRecord("CUSTOMER", recordId));
        } catch (Exception e) {

        }

        String phoneNumber = "";
        try {
            phoneNumber = customerRecord.getPhone1(0).getPhone1().getValue();
            if (phoneNumber.equals("") || phoneNumber.equals(null)) {
                phoneNumber = NO_DATA_FOUND;
            }
        } catch (Exception e) {
            phoneNumber = NO_DATA_FOUND;
        }

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
            motherName = customerRecord.getLocalRefField("LT.MOTHER.NAME").getValue();
            if (motherName.equals("") || motherName.equals(null)) {
                motherName = NO_DATA_FOUND;
            }
        } catch (Exception e) {
            motherName = NO_DATA_FOUND;
        }

        String permanentAddr = "";

        try {
           // permanentAddr = customerRecord.getLocalRefField("LT.CUS.PER.ADD").getValue();
            permanentAddr = customerRecord.getAddressCountry().getValue();
            if (permanentAddr.equals("") || permanentAddr.equals(null)) {
                permanentAddr = NO_DATA_FOUND;
            }
        } catch (Exception e) {
            permanentAddr = NO_DATA_FOUND;
        }

        String presentAddr = "";
        try {
            presentAddr = customerRecord.getAddress(0).get(0).getValue();
            if (presentAddr.equals("") || presentAddr.equals(null)) {
                presentAddr = NO_DATA_FOUND;
            }
        } catch (Exception e2) {
            presentAddr = NO_DATA_FOUND;
        }

        String dob = customerRecord.getDateOfBirth().getValue();
        if (dob.equals("") || dob.equals(null)) {
            dob = NO_DATA_FOUND;
        }

        String addRecord = " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*"
                + " " + "*" + " " + "*" + " " + "*" + " " + "*" + " " + "*" + phoneNumber + "*" + fatherName + "*"
                + motherName + "*" + permanentAddr + "*" + presentAddr + "*" + dob;

        allAccountInfo.add(addRecord);

        for (String customerAccount : customerAccounts) {
            accountRecord = new AccountRecord(da.getRecord("ACCOUNT", customerAccount));

            try {
                leagcyAccountNo = accountRecord.getAltAcctType(0).getAltAcctId().getValue();
                if (leagcyAccountNo.equals("") || leagcyAccountNo.equals(null)) {
                    leagcyAccountNo = "NO";
                }
            } catch (Exception e1) {
                leagcyAccountNo = "NO";
            }

            accountTitle = accountRecord.getAccountTitle1(0).getValue();
            coCode = accountRecord.getCoCode();
            coName = new CompanyRecord(da.getRecord("COMPANY", coCode)).getCompanyName(0).getValue();

            try {
                workingBalance = accountRecord.getWorkingBalance().getValue();
                if (workingBalance.equals("") || workingBalance.equals(null)) {
                    workingBalance = "No";
                }
            } catch (Exception e) {
                workingBalance = "No";
            }

            try {
                onlineActBalance = accountRecord.getOnlineActualBal().getValue();
                if (onlineActBalance.equals("") || onlineActBalance.equals(null)) {
                    onlineActBalance = "NO";
                }
            } catch (Exception e) {
                onlineActBalance = "NO";
            }
            categoryCode = accountRecord.getCategory().getValue();
            categoryDescription = new CategoryRecord(da.getRecord("CATEGORY", categoryCode)).getDescription(0)
                    .getValue();
            currencycode = accountRecord.getCurrency().getValue();
            try {
                accountStatusCode = accountRecord.getPostingRestrict(0).getValue();
                if (accountStatusCode.equals("") || accountStatusCode.equals(null)) {
                    accountStatusCode = "0";
                }
            } catch (Exception e) {
                accountStatusCode = "0";
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

            record = customerAccount + "*" + leagcyAccountNo + "*" + recordId + "*" + accountTitle + "*" + coCode + "*"
                    + coName + "*" + workingBalance + "*" + onlineActBalance + "*" + categoryCode + "*"
                    + categoryDescription + "*" + currencycode + "*" + smsAlert + "*" + accountStatusCode;

            allAccountInfo.add(record);
        }

        return allAccountInfo;
    }

}
