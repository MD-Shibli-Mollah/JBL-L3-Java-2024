package com.temenos.t24;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.ebbdcompanydetails.EbBdCompanyDetailsRecord;
import com.temenos.t24.api.records.ebjblcashfeeding.EbJblCashFeedingRecord;
import com.temenos.t24.api.system.DataAccess;

/*
 * MODULE         : REMITTANCE - EB.JBL.CASH.FEEDING
 * VERSION        : EB.JBL.CASH.FEEDING,INPUT
 * EB.API         : GbJblVZoneFeedBr
 * ATTACHED AS    : VALIDATION ROUTINE
 * Details        : Feeding Branch and zone auto default from issuingBranch of EB.BD.COMPANY.DETAILS
 * AUTHOR         : MD SHIBLI MOLLAH
 * DATE           : 06-OCT-2024
 * MODIFIED BY    : 
 * DATE           :
 */
public class GbJblVZoneFeedBr extends RecordLifecycle {

    @Override
    public TValidationResponse validateField(String application, String recordId, String fieldData, TStructure record) {
        // TODO Auto-generated method stub

        DataAccess da = new DataAccess(this);
        EbJblCashFeedingRecord ebJblCashFeedingRecord = new EbJblCashFeedingRecord(record);

        String issuingBranch = "";
        String zone = "";
        String feedingBr = "";

        try {

            issuingBranch = ebJblCashFeedingRecord.getIssuingBranch().getValue();

            EbBdCompanyDetailsRecord ebBdCompanyDetailsRecord = new EbBdCompanyDetailsRecord(
                    da.getRecord("EB.BD.COMPANY.DETAILS", issuingBranch));
            // get Values from EB.BD.COMPANY.DETAILS
            zone = ebBdCompanyDetailsRecord.getJblZoneCode().getValue();
            feedingBr = ebBdCompanyDetailsRecord.getFeedingBranch().getValue();
        } catch (Exception e) {
        }
        
        // set Values in EB.JBL.CASH.FEEDING
        ebJblCashFeedingRecord.setZone(zone);
        ebJblCashFeedingRecord.setFeedingBranch(feedingBr);

        record.set(ebJblCashFeedingRecord.toStructure());
        return ebJblCashFeedingRecord.getValidationResponse();
    }
}
