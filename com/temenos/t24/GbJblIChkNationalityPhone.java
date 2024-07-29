package com.temenos.t24;

import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;

/*******************************************************************************
 * TODO: Document me!
 *
 * @author NITCL, Taki Yasir (Software Engineer)
 *
 **************************         Description        ***********************
 *
 *         Nationality: 
 *         ------------
 *         This development is for Individual customer
 *         specific to JBL if the individual customer nationality is not BD
 *         (Foreign Nationality), the Routine will check the following local
 *         fields required mandatory information to commit customer record
 *         otherwise encountering error: Type of VISA Expiry Date Work Permit
 *         Y/N
 * 
 *         Phone Number: 
 *         ------------- 
 *         Checking phone Number Input routine
 *         will check the customer’s mobile number length (11 digits), If the
 *         length is less 11 digits, then the system will give a notification,
 *         in case of minor or power of autonomy for NRB, user need to input
 *         manually.
 ********************************************************************************/

public class GbJblIChkNationalityPhone extends RecordLifecycle {
    

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub

        CustomerRecord recordForCustomer = new CustomerRecord(currentRecord);

        /***************************
         ** Code for Nationality**
         ***************************/

        TField visaType = recordForCustomer.getLocalRefField("LT.TYPE.VISA");
        TField expDate = recordForCustomer.getLocalRefField("LT.CIB.EXPDATE");
        TField workPermit = recordForCustomer.getLocalRefField("LT.WPER.VER");
        TField nationality = recordForCustomer.getNationality();

        if (!nationality.getValue().equals("BD")) {
            if (visaType.getValue().isEmpty()) {
                visaType.setError("Visa Type is mandatory for foreign customer.");
            }

            else if (expDate.getValue().isEmpty()) {
                expDate.setError("Visa expiary date is mandatory for foreign customer.");
            }

            else if (!workPermit.getValue().equals("YES")) {
                workPermit.setError("Work permit must be 'YES' foreign customer.");
            }
        }

        /****************************
         ** Code For Phone Number**
         ****************************/

        String phoneNo = null;
        try {
            phoneNo = recordForCustomer.getPhone1(0).getPhone1().getValue();
        } catch (Exception e) {

        }

        try {

            if (phoneNo.length() != 11) {
                recordForCustomer.getPhone1(0).getPhone1()
                        .setError(phoneNo.length() + "Mobile number must be 11 digit");
            }
        } catch (Exception e) {
            throw new T24CoreException("", "Phone number is missing, please Write the validate phone number");
        }

        return recordForCustomer.getValidationResponse();
    }

}
