package com.temenos.t24;

import java.util.List;

import com.temenos.api.TDate;
import com.temenos.api.TStructure;
import com.temenos.t24.api.arrangement.accounting.Contract;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.complex.eb.templatehook.TransactionData;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.aaarrangementactivity.AaArrangementActivityRecord;
import com.temenos.t24.api.records.aaarrangementactivity.FieldNameClass;
import com.temenos.t24.api.records.aaarrangementactivity.PropertyClass;
import com.temenos.t24.api.records.collateral.CollateralRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Date;

/*
*Development Date : 18/01/2024
*Developed By     : Md. Shafiul Azam
*Designation      : Senior Techno Functional Consultant
*Email            : shafiul.ntl@nazihargroup.com
*Business Logic   : During Open Collateral the  Account tagged in Collateral, Lien mark will set to Post no Debit for that Account .
*Routine Attached :
*Version: COLLATERAL,JBL.COLLATERAL.PRIMARY.SECURITY
*Routine Type: Before Auth
*/
public class CrJblACollAcLien extends RecordLifecycle
{

    @Override
    public void updateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext,
            List<TransactionData> transactionData, List<TStructure> currentRecords) 
    {             
       DataAccess da = new DataAccess(this);
       Contract contract = new Contract(this);
       CollateralRecord collateralRecord = new CollateralRecord(currentRecord);
       String aaId = collateralRecord.getApplicationId().toString();
       if(!aaId.isEmpty())
       {
           contract.setContractId(aaId);       
           String prop = contract.getPropertyIdsForPropertyClass("ACCOUNT").get(0).toString();
           
           List<String> prop1 = contract.getPropertyIdsForPropertyClass("ACCOUNT");
          // prop1.get(0).
          
           
           try 
           {
               AaArrangementRecord aaRecord = new AaArrangementRecord(da.getRecord("AA.ARRANGEMENT", aaId));
               String account = aaRecord.getLinkedAppl(0).getLinkedApplId().toString();
               String productLine = aaRecord.getProductLine().toString();
               if(!account.isEmpty())
               {            
                   Date dt = new Date(this);
                   String yToday = dt.getDates().getToday().toString();
                   TDate toDay1 = new TDate(yToday);
                   TransactionData txnData = new TransactionData();
                   txnData.setVersionId("AA.ARRANGEMENT.ACTIVITY,JBL.INT");
                   txnData.setFunction("INPUT");
                   transactionData.add(txnData);
                   PropertyClass accProp = new PropertyClass();
                   accProp.setProperty(prop);
                   FieldNameClass f1 = new FieldNameClass();
                   f1.setFieldName("POSTING.RESTRICT");
                   f1.setFieldValue("1");
                   accProp.addFieldName(f1);
                   AaArrangementActivityRecord arc = new AaArrangementActivityRecord(this);
                   arc.getArrangement().setValue(aaId);
                   arc.setActivity(productLine+"-UPDATE-"+prop);
                   String toDay2 = toDay1.toString();
                   arc.setEffectiveDate(toDay2);
                   arc.addProperty(accProp);
                   currentRecords.add(arc.toStructure());
               }
           }
           catch (Exception e)
           {
       
           }
       } 
    } 
}
