package com.temenos.t24;

import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.complex.eb.templatehook.TransactionData;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.personentity.LegalIdClass;
import com.temenos.t24.api.records.personentity.PersonEntityRecord;
import com.temenos.t24.api.records.personentity.PhoneClass;
import com.temenos.t24.api.records.teller.TellerRecord;

/*
 * MODULE         : LOCAL REMITTANCE (TT)
 * VERSION        : TELLER
 * EB.API         : GbJblAWalkInCusPeUpdate
 * ATTACHED AS    : AUTH ROUTINE
 * RELATED APP    : N/A
 * AUTHOR         : MD SHIBLI MOLLAH
 * DATE           : 09-SEP-2024
 * MODIFIED BY    :
 * DATE           :
 */

public class GbJblAWalkInCusPeUpdate extends RecordLifecycle {

    @Override
    public void updateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext,
            List<TransactionData> transactionData, List<TStructure> currentRecords) {

        if (transactionContext.getCurrentFunction().equals("AUTHORISE")) {

            TellerRecord tt = new TellerRecord(currentRecord);

            String txnType = "";
            String myIdType = "";
            String myNid = "";
            String myName = "";
            String myMobileNumber = "";

            txnType = tt.getLocalRefField("LT.BEARER.TYP").getValue();

            if (!txnType.equals("WALK-IN-CUS")) {
                return;
            }

            myIdType = tt.getLocalRefField("LT.WLK.LGL.DOC").getValue();
            myNid = tt.getLocalRefField("LT.NID.NO").getValue();
            myName = tt.getLocalRefField("LT.PAY.NM").getValue();
            myMobileNumber = tt.getLocalRefField("LT.TEL.NO.BEN").getValue();

            // create a person.entity customer by the above information (NID,
            // Name, Mobile Number).
            PersonEntityRecord peRecord = new PersonEntityRecord(this);

            peRecord.setName(myName, 0);

            LegalIdClass legalIdClass = new LegalIdClass();
            legalIdClass.setLegalDocName(myIdType);
            legalIdClass.setLegalId(myNid);

            PhoneClass phoneClass = new PhoneClass();
            phoneClass.setPhone(myMobileNumber);

            peRecord.setLegalId(legalIdClass, 0);
            peRecord.setPhone(phoneClass, 0);

            TransactionData txnData = new TransactionData();
            txnData.setFunction("INPUT");
            txnData.setNumberOfAuthoriser("0");
            // txnData.setSourceId("BULK.OFS");
            txnData.setVersionId("PERSON.ENTITY,JBL.WALKIN");

            transactionData.add(txnData);
            currentRecords.add(peRecord.toStructure());

        } else {
            return;
        }
    }

}
