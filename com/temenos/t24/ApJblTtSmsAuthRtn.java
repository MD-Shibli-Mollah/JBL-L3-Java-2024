package com.temenos.t24;

/*
 * MODULE         : SMS (TT)
 * VERSION        : TT,SMS
 * EB.API         : TtSmsAuthRtn
 * ATTACHED AS    : AUTH ROUTINE
 * RELATED APP    : EB.JBL.SMS.BOOK, EB.JBL.SMS.PARAMETER (ETB)
 * AUTHOR         : MD FARID HOSSAIN
 * DATE           : 18-OCT-2022
 * MODIFIED BY    : MD SHIBLI MOLLAH
 * DATE           : 14-MAR-2024
 */

import com.temenos.t24.api.hook.system.RecordLifecycle;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.icu.text.SimpleDateFormat;
import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.servicehook.TransactionData;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.company.CompanyRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookRecord;
import com.temenos.t24.api.tables.ebjblsmsparameter.EbJblSmsParameterRecord;
import com.temenos.t24.api.tables.ebjblsmsparameter.LimitAmtClass;
import com.temenos.t24.api.tables.ebjblsmsparameter.SmsTextClass;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookTable;

public class ApJblTtSmsAuthRtn extends RecordLifecycle {
    @Override
    public void postUpdateRequest(String application, String currentRecordId, TStructure currentRecord,
            List<TransactionData> transactionData, List<TStructure> currentRecords,
            TransactionContext transactionContext) {

        if (transactionContext.getCurrentFunction().equals("AUTHORISE")) {
            DataAccess daTmp = new DataAccess(this);
            DataAccess daAcc = new DataAccess(this);
            DataAccess daCus = new DataAccess(this);
            DataAccess daCom = new DataAccess(this);

            String versionId = "";
            try {
                versionId = transactionContext.getCurrentVersionId();
            } catch (Exception e1) {
            }

            EbJblSmsParameterRecord parameterRecord = null;

            if (versionId.equals("")) {
                parameterRecord = new EbJblSmsParameterRecord(daTmp.getRecord("EB.JBL.SMS.PARAMETER", application));
            } else {
                parameterRecord = new EbJblSmsParameterRecord(
                        daTmp.getRecord("EB.JBL.SMS.PARAMETER", application + versionId));
            }

            TellerRecord tellerRecord = new TellerRecord(currentRecord);

            String debitCreditMarker = "";
            String tellerCusAcc = "";
            String tellerCcy = "";
            String tellerCreditAmountLocal = "";
            String coCode = "";
            String tellerDateTime = "";

            debitCreditMarker = tellerRecord.getDrCrMarker().getValue();
            tellerCusAcc = tellerRecord.getAccount2().getValue();
            tellerCcy = tellerRecord.getCurrency2().getValue();
            tellerCreditAmountLocal = tellerRecord.getAmountLocal2().getValue();
            coCode = tellerRecord.getCoCode();
            tellerDateTime = tellerRecord.getDateTime(0);

            AccountRecord accRec = new AccountRecord(daAcc.getRecord("ACCOUNT", tellerCusAcc));
            String cusId = "";
            cusId = accRec.getCustomer().getValue();
            CustomerRecord cusRec = new CustomerRecord(daCus.getRecord("CUSTOMER", cusId));
            String cusPhone = null;
            String cusEmail = null;
            try {
                cusPhone = cusRec.getPhone1(0).getPhone1().getValue();
                cusEmail = cusRec.getPhone1(0).getEmail1().getValue();
            } catch (Exception e) {

            }

            if (cusPhone.length() == 11 || cusEmail != "") {

                Map<String, String> smsValueMapping = new HashMap<String, String>();

                int generateMessageType = 0;
                if (debitCreditMarker.equals("CREDIT")) {
                    generateMessageType = 1;
                }

                for (int i = 0; i < parameterRecord.getSmsEvent(generateMessageType).getSmsText().size(); i++) {

                    String parameter = parameterRecord.getSmsEvent(generateMessageType).getSmsText().get(i)
                            .getSmsVariable().getValue();

                    switch (parameter) {

                    case "ACCOUNT":
                        smsValueMapping.put(parameter, "***" + tellerCusAcc.substring(tellerCusAcc.length() - 4));
                        break;
                    case "CURRENCY":
                        smsValueMapping.put(parameter, tellerCcy);
                        break;
                    case "AMOUNT.LOCAL":
                        smsValueMapping.put(parameter, tellerCreditAmountLocal);
                        break;
                    case "CO.CODE":
                        CompanyRecord comanyRecord = new CompanyRecord(daCom.getRecord("COMPANY", coCode));
                        smsValueMapping.put(parameter, comanyRecord.getCompanyName(0).getValue());
                        break;
                    case "DATE.TIME":
                        try {
                            Date date = new SimpleDateFormat("yyMMddHHmm").parse(tellerDateTime);
                            String dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date);
                            smsValueMapping.put(parameter, dateTime);
                        } catch (ParseException e) {
                            smsValueMapping.put(parameter, tellerDateTime);
                        }
                        break;
                    case "ONLINE.ACTUAL.BAL":
                        smsValueMapping.put(parameter, accRec.getOnlineActualBal().getValue());
                        break;
                    default:
                        break;
                    }
                }

                StringBuilder message = new StringBuilder();
                List<SmsTextClass> smsParams = parameterRecord.getSmsEvent(0).getSmsText();

                for (int i = 0; i < smsParams.size(); i++) {
                    if (smsParams.get(i).getSmsText().getValue() != "") {
                        message.append((smsParams.get(i).getSmsText().getValue()) + " ");
                    }
                    if (!smsParams.get(i).getSmsVariable().getValue().equals("")) {
                        message.append((smsValueMapping.get(smsParams.get(i).getSmsVariable().getValue())) + " ");
                    }
                }

                String smsContent = message.toString().replace(" . ", ". ").trim();

                EbJblSmsBookTable smsTable = new EbJblSmsBookTable(this);
                EbJblSmsBookRecord smsBookRecord = new EbJblSmsBookRecord();

                smsBookRecord.setSmsBody(smsContent);
                smsBookRecord.setSmsStatus("PENDING");
                smsBookRecord.setPhone(cusPhone);
                smsBookRecord.setEmail(cusEmail);

                List<LimitAmtClass> limitAmtClasses = parameterRecord.getLimitAmt();

                String priority = "3";
                String apiLink = "";
                Double dblTxnAmt = 0.0;
                dblTxnAmt = Double.valueOf(tellerCreditAmountLocal);

                for (LimitAmtClass limitAmtClass : limitAmtClasses) {
                    
                    Double dblLimAmt = 0.0;
                    // Try Catch for which returns empty value
                    try {
                        dblLimAmt = Double.valueOf(limitAmtClass.getLimitAmt().getValue());
                    } catch (Exception e1) {
                    }
                   // Double dblLimAmt = Double.valueOf(limitAmtClass.getLimitAmt().getValue());
                    if (dblLimAmt <= dblTxnAmt) {
                        priority = limitAmtClass.getPriority().getValue();
                        apiLink = limitAmtClass.getApiLink().getValue();
                        break;
                    }
                }

                smsBookRecord.setPriority(priority);
                smsBookRecord.setApiLink(apiLink);

                try {
                    smsTable.write(debitCreditMarker + "-" + currentRecordId, smsBookRecord);
                } catch (T24IOException e) {
                }
            }
        } else {
            return;
        }
    }
}

