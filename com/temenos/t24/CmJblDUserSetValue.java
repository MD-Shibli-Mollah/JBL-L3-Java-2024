package com.temenos.t24;

import java.util.List;

import com.temenos.api.LocalRefGroup;
import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.templatehook.InputValue;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.deptacctofficer.DeptAcctOfficerRecord;
import com.temenos.t24.api.records.user.CompanyRestrClass;
import com.temenos.t24.api.records.user.UserRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * /**
 * TODO: Document me!
 *
 * @author nazihar
 * 
 *         Development Date : 01/06/2024
 *         Developed By : Taki Yasir 
 *         Designation :Software Engineer 
 *         Email : taki@nazihargroup.com 
 *         Business Logic : When we give the value in department code field in USER,JBL.INPUT version. After that routine will be triggered.
 *                          If LT.USER.COMPANY and LT.SMS field is value in DEPT.ACCT.OFFICER, the value will be set Company Code, Company Restr and 
 *                          Application field in version USER,JBL.INPUT.
 *         Routine Attached : Default Routine
 *         Version : USER,JBL.INPUT
 *         Routine Type : Version Routine
 *         
 */
public class CmJblDUserSetValue extends RecordLifecycle {
    
    @Override
    public void defaultFieldValuesOnHotField(String application, String currentRecordId, TStructure currentRecord,
            InputValue currentInputValue, TStructure unauthorisedRecord, TStructure liveRecord,
            TransactionContext transactionContext) {
        
        DataAccess da = new DataAccess(this);
        UserRecord uRecord = new UserRecord(currentRecord);
        String deptCode = uRecord.getDepartmentCode().getValue().toString();
        String smsGroup = "";
        DeptAcctOfficerRecord deptAcctOfficerRecord = null;
        List<LocalRefGroup> userCompnany = null;
        Boolean flag = false;
        try {
            deptAcctOfficerRecord = new DeptAcctOfficerRecord(da.getRecord("DEPT.ACCT.OFFICER", deptCode));
            userCompnany = deptAcctOfficerRecord.getLocalRefGroups("LT.USER.COMPANY");
            smsGroup = deptAcctOfficerRecord.getLocalRefField("LT.SMS").getValue();

            if (userCompnany.isEmpty()) {
                flag = true;
            } else if (smsGroup.isEmpty()) {
                flag = true;
            }
        } catch (Exception e) {

        }
        int lenghthOfUserCompany = userCompnany.size();

        try {

            uRecord.clearCompanyRestr();
            uRecord.clearCompanyCode();

            for (int i = 0; i < lenghthOfUserCompany; i++) {

                if (flag) {
                    break;
                }
                CompanyRestrClass companyRestar = new CompanyRestrClass();
                String setUserCompanyValue = userCompnany.get(i).getLocalRefField("LT.USER.COMPANY").getValue()
                        .toString();

                companyRestar.setCompanyRestr(setUserCompanyValue);
                companyRestar.setApplication("@" + smsGroup);

                uRecord.setCompanyRestr(companyRestar, i);
                uRecord.setCompanyCode(setUserCompanyValue, i);

            }

        } catch (Exception e) {

        }
        currentRecord.set(uRecord.toStructure());

    }

}
