package com.temenos.t24;

/*
EB.API      : ApCusAMLCheckAuth
Attached as : Auth Routine
VERSION     : ALL CUSTOMER VERSION
DEVELOPED BY: MD Shibli Mollah
*/

import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.t24.api.complex.eb.servicehook.TransactionData;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;

public class ApCusAMLCheckAuth extends RecordLifecycle {
    @Override
    public void postUpdateRequest(String application, String currentRecordId, TStructure currentRecord,
            List<TransactionData> transactionData, List<TStructure> currentRecords,
            TransactionContext transactionContext) {
       
       // String verName = transactionContext.getCurrentVersionId();
        String vfunc = transactionContext.getCurrentFunction();

       // if (((verName.equals(",AML"))) && ((vfunc.equals("AUTHORISE")))) {
        if (vfunc.equals("AUTHORISE")) {

            CustomerRecord cusRec = new CustomerRecord(currentRecord);
            String amlResult = cusRec.getAmlResult().getValue();

            if (amlResult.equals("NULL")) {
                throw new T24CoreException("", "ST-JBL.AML.NULL");
            } else if (amlResult.equals("RESULT.AWAITED")) {
                throw new T24CoreException("", "ST-JBL.AML.WAIT");
            } else if (amlResult.equals("POSITIVE")) {
                throw new T24CoreException("", "ST-JBL.AML.POSITIVE");
            } else {
                return;
            }
        } else {
            return;
        }
    }
}
