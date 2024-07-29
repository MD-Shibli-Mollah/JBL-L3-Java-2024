package com.temenos.t24;

import java.util.List;

import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.system.DataAccess;

public class ApOnDemandSmsCmd extends ServiceLifecycle {
    @Override
    public List<String> getIds(ServiceData serviceData, List<String> controlList) {

        DataAccess daCus = new DataAccess(this);
        
        System.out.println("This is custom logic routine");
        
        List<String> cusIds = daCus.selectRecords("BNK", "CUSTOMER", "", "WITH @ID EQ 100000"); 
        
//        List<String> accIds = daAcc.selectRecords("", "ACCOUNT", "", "WITH WORKING.BALANCE  10000000.00");
//        for (Iterator<String> iterator = accIds.iterator(); iterator.hasNext();) {
//
//            String accId = (String) iterator.next();
//            AccountRecord acRec = new AccountRecord(daAcc.getRecord("ACCOUNT", accId));
//
//            cusId = acRec.getCustomer().getValue();
//            CustomerRecord cusRec = new CustomerRecord(daCus.getRecord("CUSTOMER", cusId));
//
//            phone = cusRec.getPhone1(0).getPhone1().getValue();
//            email = cusRec.getPhone1(0).getEmail1().getValue();
//
//            if (phone != "" || email != "") {
//                cusIds.add(cusId);
//            }
//        }
        
        
        return cusIds;  //Must be return customers ID
    }
}
