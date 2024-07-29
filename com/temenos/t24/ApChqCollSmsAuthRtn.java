package com.temenos.t24;

/*
 * MODULE         : SMS (CHEQUE.COLLECTION)
 * VERSION        : CHEQUE.COLLECTION,SMS
 * EB.API         : ChqCollSmsAuthRtn
 * RELATED APP    : EB.JBL.SMS.BOOK, EB.JBL.SMS.PARAMETER (ETB)
 * ATTACHED AS    : Auth Routine
 * AUTHOR         : MD FARID HOSSAIN
 * LAST MODIFIED  : 19-OCT-2022
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.servicehook.TransactionData;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.chequecollection.ChequeCollectionRecord;
import com.temenos.t24.api.records.company.CompanyRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookRecord;
import com.temenos.t24.api.tables.ebjblsmsparameter.EbJblSmsParameterRecord;
import com.temenos.t24.api.tables.ebjblsmsparameter.SmsTextClass;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookTable;

public class ApChqCollSmsAuthRtn extends RecordLifecycle {
    @Override
    public void postUpdateRequest(String application, String currentRecordId, TStructure currentRecord,
            List<TransactionData> transactionData, List<TStructure> currentRecords,
            TransactionContext transactionContext) {

        DataAccess daAcc = new DataAccess(this);
        DataAccess daCus = new DataAccess(this);
        DataAccess daTmp = new DataAccess(this);
        DataAccess daCom = new DataAccess(this);

        if (transactionContext.getCurrentFunction().equals("AUTHORISE")) {

            ChequeCollectionRecord chqRec   = new ChequeCollectionRecord(currentRecord);
            String currency                 = chqRec.getCurrency().getValue();
            String creditAccNo              = chqRec.getCreditAccNo(0).getCreditAccNo().getValue();
            AccountRecord accRec            = new AccountRecord(daAcc.getRecord("ACCOUNT", creditAccNo));
            String customerId               = accRec.getCustomer().getValue();
            CustomerRecord cusRec           = new CustomerRecord(daCus.getRecord("CUSTOMER", customerId));

            String cusPhone = "";
            String cusEmail = "";
            try {
                cusPhone = cusRec.getPhone1(0).getPhone1().getValue();
                cusPhone = cusRec.getPhone1(0).getEmail1().getValue();
            } catch (Exception e1) {

            }

            if (cusPhone.length() == 11 || cusEmail != "") {
                
                String versionId = "";
                try {
                    versionId = transactionContext.getCurrentVersionId();
                } catch (Exception e1) {
                }

                EbJblSmsParameterRecord parameterRecord = null;

                if (versionId.equals("")) {
                    parameterRecord = new EbJblSmsParameterRecord(
                            daTmp.getRecord("EB.JBL.SMS.PARAMETER", application));
                } else {
                    parameterRecord = new EbJblSmsParameterRecord(
                            daTmp.getRecord("EB.JBL.SMS.PARAMETER", application + versionId));
                }
                
                String priority = "3";
                String apiLink = "";
                try {
                   priority = parameterRecord.getLimitAmt(0).getPriority().getValue();
                   apiLink  = parameterRecord.getLimitAmt(0).getApiLink().getValue();
                } catch (Exception e1) {
                }
                
                Map<String, String> smsValueMapping = new HashMap<String, String>();

                for (int i = 0; i < parameterRecord.getSmsEvent(0).getSmsText().size(); i++) {

                    String paraMeter = parameterRecord.getSmsEvent(0).getSmsText(i).getSmsVariable().getValue();
                    
                    switch (paraMeter) {

                    case "CREDIT.ACC.NO":
                        smsValueMapping.put(paraMeter, "***" + creditAccNo.substring(creditAccNo.length() - 4));
                        break;

                    case "CO.CODE":
                        String coCode = chqRec.getCoCode();
                        CompanyRecord comRec = new CompanyRecord(daCom.getRecord("COMPANY", coCode));
                        String branchName = comRec.getCompanyName(0).getValue();
                        smsValueMapping.put(paraMeter, branchName);
                        break;

                    case "AMOUNT":
                        String chqAmt = chqRec.getCreditAccNo(0).getAmount().getValue();
                        smsValueMapping.put(paraMeter, currency + " " + chqAmt);
                        break;

                    case "ONLINE.ACTUAL.BALANCE":
                        String onlineActBal = accRec.getOnlineActualBal().getValue();
                        smsValueMapping.put(paraMeter, currency + " " + onlineActBal);
                        break;

                    case "DATE.TIME":
                        String dateTime = chqRec.getDateTime(0);
                        Date date;
                        try {
                            date = new SimpleDateFormat("yyMMddHHmm").parse(dateTime);
                            String frmtDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date);
                            smsValueMapping.put(paraMeter, frmtDateTime);
                        } catch (ParseException e) {
                            System.out.println("Invalid Date Format");
                        }
                        break;

                    default:
                        break;
                    }
                }

                StringBuilder Message = new StringBuilder();
                List<SmsTextClass> smsParams = parameterRecord.getSmsEvent(0).getSmsText();

                for (int i = 0; i < smsParams.size(); i++) {
                    if (!smsParams.get(i).getSmsText().getValue().equals("")) {
                        Message.append((smsParams.get(i).getSmsText().getValue()) + " ");
                    } else {
                        Message.append(i);
                    }
                    if (!smsParams.get(i).getSmsVariable().getValue().equals("")) {
                        Message.append((smsValueMapping.get(smsParams.get(i).getSmsVariable().getValue())) + " ");
                    }
                }
                String SmsContent = Message.toString().replace(" . ", ". ").trim();

                EbJblSmsBookTable bookTable = new EbJblSmsBookTable(this);
                EbJblSmsBookRecord bookRecord = new EbJblSmsBookRecord();

                bookRecord.setSmsBody(SmsContent);
                bookRecord.setSmsStatus("PENDING");
                bookRecord.setPhone(cusPhone);
                bookRecord.setEmail(cusEmail);
                bookRecord.setPriority(priority);
                bookRecord.setApiLink(apiLink);

                try {
                    bookTable.write("CHQ" + "-" + currentRecordId, bookRecord);
                } catch (T24IOException e) {
                    System.out.println("Unable to write data on SMS Book Table");
                }
            } 
        } 
    }
}
