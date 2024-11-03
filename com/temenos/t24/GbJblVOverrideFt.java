package com.temenos.t24;

import java.util.List;

import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.desctxsectrasparameter.OverrideProcessingClass;
import com.temenos.t24.api.records.ebbdcompanydetails.EbBdCompanyDetailsRecord;
import com.temenos.t24.api.records.ebjblcashfeeding.EbJblCashFeedingRecord;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.records.override.MessageClass;
import com.temenos.t24.api.records.override.OverrideRecord;
import com.temenos.t24.api.records.overrideclass.OverrideClassRecord;
import com.temenos.t24.api.records.overrideclass.OverrideTextClass;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author MD Shibli Mollah
 *
 */
public class GbJblVOverrideFt extends RecordLifecycle {

    @Override
    public TValidationResponse validateField(String application, String recordId, String fieldData, TStructure record) {
        // TODO Auto-generated method stub
        DataAccess da = new DataAccess(this);
        FundsTransferRecord fundsTransferRecord = new FundsTransferRecord(record);
        List<TField> ftOverrideList = null;
        String firstOverride = "";

        try {
            ftOverrideList = fundsTransferRecord.getOverride();
            firstOverride = fundsTransferRecord.getOverride(0).getValue();

            // OverrideClassRecord ocr = new OverrideClassRecord(this);
            // OverrideTextClass oct = new OverrideTextClass();
            String debitAcNo = fundsTransferRecord.getDebitAcctNo().getValue();
            String debitAcOverride = fundsTransferRecord.getDebitAcctNo().getOverride();
            OverrideRecord or = new OverrideRecord(this);

            String debitAmount = fundsTransferRecord.getDebitAmount().getValue();
            String debitAmtOverride = fundsTransferRecord.getDebitAmount().getValue();
            MessageClass myOpcNum = or.getMessage().get(0);
            String override = "";
            override = myOpcNum.getMessage().get(0).getValue();
        } catch (Exception e) {
        }

        return fundsTransferRecord.getValidationResponse();
    }

}
