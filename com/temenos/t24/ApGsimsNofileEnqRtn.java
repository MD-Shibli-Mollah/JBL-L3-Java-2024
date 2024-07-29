package com.temenos.t24;

import java.util.ArrayList;
import java.util.List;

import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.category.CategoryRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.customer.LegalIdClass;
import com.temenos.t24.api.system.DataAccess;

/*AREA             : 
EB.API             : GsimsNofileEnqRtn
STANDARD.SELECTION : NOFILE.GsimsNofileEnqRtn
ENQUIRY            : AC.API.ACCOUNTS.GSIMS.1.1.0
DEVELOPED BY       : MD Shibli Mollah*/

public class ApGsimsNofileEnqRtn extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {

        List<String> returnIds = new ArrayList<String>();

        String responseCode = "6"; // If AC.NO is invalid then default code is
                                   // 6.
        String accountType = "";
        String accountTitle = "";
        String email = "";
        String bdMobile = "";
        String abroadMobile = "";
        String cusLegalIdName = "";

        String cusLegalIdNo = "";
        String accountNo = "";
        String idType = "";
        String documentId = "";
        String category = "";
        String cusId = "";

        try {
            accountNo = filterCriteria.get(0).getValue();
            idType = filterCriteria.get(1).getValue(); // Only NID/PP/BID/DL

            if ("NID".equals(idType)) {
                idType = "NATIONAL.ID";
            } else if ("PP".equals(idType)) {
                idType = "PASSPORT";
            } else if ("BID".equals(idType)) {
                idType = "BIRTH.CERTIFICATE";
            } else {
                idType = "DRIVING.LICENSE";
            }

            documentId = filterCriteria.get(2).getValue();
        } catch (Exception e) {
        }

        DataAccess daAcc = new DataAccess(this);
        DataAccess daCat = new DataAccess(this);
        DataAccess daCus = new DataAccess(this);
        AccountRecord accRec = null;

        try {
            accRec = new AccountRecord(daAcc.getRecord("ACCOUNT", accountNo));
            category = accRec.getCategory().getValue();
            cusId = accRec.getCustomer().getValue();
            accountTitle = accRec.getAccountTitle1(0).getValue();

            if (cusId != "") {
                CategoryRecord catRec = new CategoryRecord(daCat.getRecord("CATEGORY", category));
                CustomerRecord cusRec = new CustomerRecord(daCus.getRecord("CUSTOMER", cusId));

                // List operation for LEGAL ID - NATIONAL.ID , PASSPORT ,
                // BIRTH.CERTIFICATE , DRIVING.LICENSE
                List<LegalIdClass> legalIdList = null;
                legalIdList = cusRec.getLegalId();

                for (int i = 0; i < legalIdList.size(); i++) {
                    LegalIdClass item = legalIdList.get(i);
                    // idType = "NATIONAL.ID"
                    if (idType.equals(item.getLegalDocName().getValue())) {
                        cusLegalIdName = item.getLegalDocName().getValue();
                        cusLegalIdNo = item.getLegalId().getValue();
                        // index = i; // Found the index
                        break;
                    }
                }

                if (idType.equals(cusLegalIdName) && documentId.equals(cusLegalIdNo)) {
                    responseCode = "Success";
                    accountType = catRec.getDescription(0).getValue();

                    email = cusRec.getPhone1(0).getEmail1().getValue();
                    bdMobile = cusRec.getPhone1(0).getPhone1().getValue();
                    abroadMobile = cusRec.getPhone1(0).getSms1().getValue();

                } else {
                    responseCode = "7"; // If id doc mismatch, response code is
                                        // 7
                }
            } else {
                responseCode = "6"; // If account is not found, response code is
                                    // 6
            }
            /*
             * returnIds.add(responseCode + "*" + accountType + "*" + accountNo
             * + "*" + accountTitle + "*" + email + "*" + bdMobile + "*" +
             * abroadMobile);
             */
        } catch (Exception e) {
        } finally {
            returnIds.add(responseCode + "*" + accountType + "*" + accountNo + "*" + accountTitle + "*" + email + "*"
                    + bdMobile + "*" + abroadMobile);
        }
        return returnIds;
    }
}
