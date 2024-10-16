package com.temenos.t24;

import com.temenos.api.TBoolean;
import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.t24.api.complex.eb.templatehook.ErrorText;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.eberror.ErrorMsgClass;
import com.temenos.t24.api.records.override.MessageClass;
import com.temenos.t24.api.records.override.OverrideRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author MD Shibli Mollah
 *
 */
public class GbJblOverrideToError extends RecordLifecycle {


    @Override
    public TBoolean isOverrideAutoApprove(String application, String currentRecordId, TStructure currentRecord,
            TransactionContext transactionContext, ErrorText errorText) {
        // TODO Auto-generated method stub

        TBoolean isOverrideAccepted = new TBoolean();
        // errorText.setCustomText("Raised from Routine", 0);
        // ErrorMsgClass emc = new ErrorMsgClass();
        // emc.setErrorMsg("Customer Error", 0);
        DataAccess da = new DataAccess(this);

        OverrideRecord or = new OverrideRecord(da.getRecord("OVERRIDE", "DIFF.CUST.ACCTS"));
        MessageClass myOpcNum = null;
       // myOpcNum.addMessage("Testing Overrides");
        myOpcNum.setMessage("Testing Overrides", 0);
        // String override = "";
        // override = myOpcNum.getMessage().get(0).getValue();

        or.setMessage(myOpcNum, 0);

        // throw new T24CoreException("Override to Error");
        isOverrideAccepted.set(false);
        return isOverrideAccepted;
    }

}
