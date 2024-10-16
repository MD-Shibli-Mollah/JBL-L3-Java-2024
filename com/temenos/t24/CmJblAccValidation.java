package com.temenos.t24;

import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author Md Hasnain Rabby EB.API Name: CmJblAccValidation Version Name :
 *         EB.JBL.ATM.CARD.MGT,ISSUE Routine Type: Validation Routine Validate
 *         Field : ACCT.NO
 */
public class CmJblAccValidation extends RecordLifecycle {
    @Override
    public TValidationResponse validateField(String application, String recordId, String fieldData, TStructure record) {
        // TODO Auto-generated method stub

        DataAccess da = new DataAccess(this);
        String cardName = "";

        try {

            AccountRecord accRec = new AccountRecord(da.getRecord("ACCOUNT", fieldData));
            String cusId = accRec.getCustomer().getValue();
            CustomerRecord cusRec = new CustomerRecord(da.getRecord("CUSTOMER", cusId));
            // cardName = cusRec.getName1(0).getValue();

        
        /*
         * EbJblCardMgmtInfoRecord mgt = new
         * EbJblCardMgmtInfoRecord(da.getRecord("EB.JBL.CARD.MGMT.INFO",
         * "SYSTEM")); 
         * String titleLength = mgt.getTitleLength().getValue(); int
         * length = Integer.parseInt(titleLength);
         * 
         * EbJblAtmCardMgtRecord atmCardMgtRec = new
         * EbJblAtmCardMgtRecord(record);
         * 
         * if(cardName.length() > length) { String cardHolderName =
         * cardName.substring(0, 20); atmCardMgtRec.setCardName(cardHolderName);
         * } else { atmCardMgtRec.setCardName(cardName); }
         * record.set(atmCardMgtRec.toStructure()); return
         * atmCardMgtRec.getValidationResponse();
         */
        record.set(cusRec.toStructure());
        return cusRec.getValidationResponse();
        } catch (Exception e) {
        }
        return null;
    }

}
