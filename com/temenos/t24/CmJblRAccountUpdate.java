package com.temenos.t24;

import com.temenos.api.TStructure;
import java.lang.String;
import com.temenos.t24.api.complex.aa.activityhook.ArrangementContext;
import com.temenos.t24.api.hook.arrangement.ActivityLifecycle;
import com.temenos.t24.api.records.aaaccountdetails.AaAccountDetailsRecord;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.aaarrangementactivity.AaArrangementActivityRecord;
import com.temenos.t24.api.records.aaprddesaccount.AaPrdDesAccountRecord;
import com.temenos.t24.api.records.aaproductcatalog.AaProductCatalogRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: This routine is written to fetch The Customer title,Name and set the value in AA Account Title
 *
 * Developed by: Shafiul Azam
 * Updated by: MD Shibli Mollah
 * Activity: LENDING-NEW-ARRANGEMENT
 * Property : ACCOUNT
 * Action: MAINTAIN
 * RECORD ROUTINE - CmJblRAccountUpdate
*/

public class CmJblRAccountUpdate extends ActivityLifecycle {

    @Override
    public void defaultFieldValues(AaAccountDetailsRecord accountDetailRecord,
            AaArrangementActivityRecord arrangementActivityRecord, ArrangementContext arrangementContext,
            AaArrangementRecord arrangementRecord, AaArrangementActivityRecord masterActivityRecord,
            TStructure productPropertyRecord, AaProductCatalogRecord productRecord, TStructure record) 
    {
        AaArrangementActivityRecord activityRecord = arrangementActivityRecord;
        String getCustomer = activityRecord.getCustomer(0).getCustomer().getValue();
        DataAccess da = new DataAccess(this);
        
        CustomerRecord cusRec = new CustomerRecord(da.getRecord("CUSTOMER", getCustomer));
        String customersName = "";
        customersName = cusRec.getShortName(0).getValue();
        
        AaPrdDesAccountRecord accountRecord = new AaPrdDesAccountRecord(record);
        accountRecord.addShortTitle(customersName);
        
        String customerName1 = "";
        customerName1 = cusRec.getName1(0).getValue();
        String customerName2 = "";

        try {
            customerName2 = cusRec.getName2(0).getValue();
        } catch (Exception e) {
        }
        
        // subString Method -- 35 , 35 -- customerName1
        Integer customerName1StrLen = customerName1.length(); // Total Length
        
        if (customerName1StrLen > 35){
            String myName35 = customerName1.substring(0, 35); //Total 35 characters excluding 35th number.
            String myName36 = customerName1.substring(35, customerName1StrLen);
            
            accountRecord.setAccountTitle1(myName35, 0);
            accountRecord.setAccountTitle1(myName36, 1);
            
         //IF THE CUSTOMER'S NAME.1 is more than 70 chrs then User might input the rest in NAME.2
            //Otherwise NAME.2 might be blank.
        //  subString Method -- 35 , 35 -- customerName2
            Integer customerName2StrLen = customerName2.length(); // Total Length
             
             // AaPrdDesAccountRecord accountRecord = new AaPrdDesAccountRecord(record);
             if (customerName2StrLen > 35){
                 String myNameB35 = customerName2.substring(0, 35); //Total 35 characters excluding 35th number.
                 String myNameB36 = customerName2.substring(35, customerName2StrLen);
                 
                 accountRecord.setAccountTitle1(myNameB35, 2);
                 accountRecord.setAccountTitle1(myNameB36, 3);
             }
             else
             {
                 accountRecord.setAccountTitle2(customerName2, 2);
             }
        }
        else
        {
            accountRecord.setAccountTitle1(customerName1, 0);
        }

        // AaArrAccountRecord accountRecord = new AaArrAccountRecord();
       // accountRecord.addAccountTitle1(CustomerName1);        
        //accountRecord.getAccountTitle1(3);        
        record.set(accountRecord.toStructure());
    }
}


