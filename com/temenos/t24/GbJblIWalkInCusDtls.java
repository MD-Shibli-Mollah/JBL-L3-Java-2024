package com.temenos.t24;

/*
 * MODULE         : LOCAL REMITTANCE (TT)
 * VERSION        : TELLER,JBL.TT.ISSUE.INIT
 * EB.API         : GbJblIWalkInCusDtls
 * ATTACHED AS    : INPUT ROUTINE
 * RELATED APP    : N/A
 * AUTHOR         : MD FARID HOSSAIN
 * DATE           : 18-OCT-2022
 * MODIFIED BY    : MD SHIBLI MOLLAH
 * DATE           : 13-MAR-2024
 */

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.teller.TellerRecord;

public class GbJblIWalkInCusDtls extends RecordLifecycle {
   public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord, TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
      TellerRecord teller = new TellerRecord(currentRecord);
      String txnType = teller.getLocalRefField("LT.BEARER.TYP").getValue();
      if (txnType.equals("JBL-CUS") && teller.getLocalRefField("LT.ACCT.NO").getValue().equals("")) {
         teller.getLocalRefField("LT.ACCT.NO").setError("For JBL-CUS Acct No is missing");
      }

      if (txnType.equals("WALK-IN-CUS")) {
         if (teller.getLocalRefField("LT.PAY.NM").getValue().equals("")) {
            teller.getLocalRefField("LT.PAY.NM").setError("For WALK-IN-CUS Cust Name is missing");
         }

         if (teller.getLocalRefField("LT.WLK.LGL.DOC").getValue().equals("")) {
            teller.getLocalRefField("LT.WLK.LGL.DOC").setError("For WALK-IN-CUS Legal Doc Type is missing");
         }

         if (teller.getLocalRefField("LT.NID.NO").getValue().equals("")) {
            teller.getLocalRefField("LT.NID.NO").setError("For WALK-IN-CUS NID NO is missing");
         }

         if (teller.getLocalRefField("LT.TEL.NO.BEN").getValue().equals("")) {
            teller.getLocalRefField("LT.TEL.NO.BEN").setError("For WALK-IN-CUS MOB No is missing");
         }

         if (teller.getLocalRefField("LT.TXN.PURPOSE").getValue().equals("")) {
            teller.getLocalRefField("LT.TXN.PURPOSE").setError("For WALK-IN-CUS Txn Purpose is missing");
         }

         if (teller.getLocalRefField("LT.SRC.OF.FUND").getValue().equals("")) {
            teller.getLocalRefField("LT.SRC.OF.FUND").setError("For WALK-IN-CUS Source of Fund is missing");
         }
      }

      return teller.getValidationResponse();
   }
}
