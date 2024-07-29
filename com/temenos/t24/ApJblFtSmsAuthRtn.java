package com.temenos.t24;

/*
 * MODULE         : SMS (FT)
 * VERSION        : FT,SMS
 * EB.API         : FtSmsAuthRtn
 * ATTACHED AS    : AUTH ROUTINE
 * RELATED APP    : EB.JBL.SMS.BOOK, EB.JBL.SMS.PARAMETER (ETB)
 * AUTHOR         : MD FARID HOSSAIN
 * DATE           : 18-OCT-2022
 * MODIFIED BY    : MD SHIBLI MOLLAH
 * DATE           : 13-MAR-2024
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.company.CompanyRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookRecord;
import com.temenos.t24.api.tables.ebjblsmsparameter.EbJblSmsParameterRecord;
import com.temenos.t24.api.tables.ebjblsmsparameter.LimitAmtClass;
import com.temenos.t24.api.tables.ebjblsmsparameter.SmsTextClass;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookTable;

public class ApJblFtSmsAuthRtn extends RecordLifecycle {
    @Override
    public void postUpdateRequest(java.lang.String application, java.lang.String currentRecordId,
            TStructure currentRecord, List<com.temenos.t24.api.complex.eb.servicehook.TransactionData> transactionData,
            List<TStructure> currentRecords, TransactionContext transactionContext) {

        if (transactionContext.getCurrentFunction().equals("AUTHORISE")) {

            DataAccess daAcc = new DataAccess(this);
            DataAccess daCus = new DataAccess(this);
            DataAccess daCom = new DataAccess(this);
            DataAccess daTmp = new DataAccess(this);

            FundsTransferRecord ftRec = new FundsTransferRecord(currentRecord);

            // Initialize string Variables
            String dbtAcct = "";
            String cdtAcct = "";
            String dbtAmt = "";
            String cdtAmt = "";

            dbtAcct = ftRec.getDebitAcctNo().getValue();
            cdtAcct = ftRec.getCreditAcctNo().getValue();

            dbtAmt = ftRec.getAmountDebited().getValue();
            cdtAmt = ftRec.getAmountCredited().getValue();
            if (cdtAmt.equals("")) {
                cdtAmt = dbtAmt;
            }

            // Initialize string Variables
            String dbtCurr = "";
            String cdtCurr = "";

            dbtCurr = ftRec.getDebitCurrency().getValue();
            cdtCurr = ftRec.getCreditCurrency().getValue();
            if (cdtCurr.equals("")) {
                cdtCurr = dbtCurr;
            }

            StringBuilder dbtAmt2 = new StringBuilder(dbtAmt);
            StringBuilder cdtAmt2 = new StringBuilder(cdtAmt);

            dbtAmt2.delete(0, 3);
            cdtAmt2.delete(0, 3);

            String sysDateTime = ftRec.getDateTime(0);
            String coCode = ftRec.getCoCode();

            CompanyRecord comRec = new CompanyRecord(daCom.getRecord("COMPANY", coCode));
            String branchName = comRec.getCompanyName(0).getValue();

            AccountRecord dbtAccRec = new AccountRecord(daAcc.getRecord("ACCOUNT", dbtAcct));
            AccountRecord cdtAccRec = new AccountRecord(daAcc.getRecord("ACCOUNT", cdtAcct));

            String dbtCustomer = "";
            String dbtOnlineBal = "";
            String dbtCusPhone = "";
            String dbtCusEmail = "";
            try {
                dbtCustomer = dbtAccRec.getCustomer().getValue();
                dbtOnlineBal = dbtAccRec.getOnlineActualBal().getValue();
                CustomerRecord dbtCusRec = new CustomerRecord(daCus.getRecord("CUSTOMER", dbtCustomer));
                dbtCusPhone = dbtCusRec.getPhone1(0).getPhone1().getValue();
                dbtCusEmail = dbtCusRec.getPhone1(0).getEmail1().getValue();

            } catch (Exception e1) {
            }

            String cdtCustomer = "";
            String cdtOnlineBal = "";
            String cdtCusPhone = "";
            String cdtCusEmail = "";
            try {
                cdtCustomer = cdtAccRec.getCustomer().getValue();
                cdtOnlineBal = cdtAccRec.getOnlineActualBal().getValue();
                CustomerRecord cdtCusRec = new CustomerRecord(daCus.getRecord("CUSTOMER", cdtCustomer));
                cdtCusPhone = cdtCusRec.getPhone1(0).getPhone1().getValue();
                cdtCusEmail = cdtCusRec.getPhone1(0).getEmail1().getValue();

            } catch (Exception e1) {
            }

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

            String smsEvent0 = "";
            smsEvent0 = parameterRecord.getSmsEvent(0).getSmsEvent().getValue();

            if (smsEvent0.equals("DEBIT")) {

                if (dbtCusPhone.length() == 11 || dbtCusEmail != "") {

                    Map<String, String> smsValueMapping = new HashMap<String, String>();

                    for (int i = 0; i < parameterRecord.getSmsEvent(0).getSmsText().size(); i++) {

                        String paraMeter = parameterRecord.getSmsEvent(0).getSmsText(i).getSmsVariable().getValue();

                        switch (paraMeter) {

                        case "ACCOUNT":
                            smsValueMapping.put(paraMeter, "***" + dbtAcct.substring(dbtAcct.length() - 4));
                            break;

                        case "CO.CODE":
                            smsValueMapping.put(paraMeter, branchName);
                            break;

                        case "AMOUNT.DEBITED":
                            smsValueMapping.put(paraMeter, dbtCurr + " " + dbtAmt2);
                            break;

                        case "ONLINE.ACTUAL.BALANCE":
                            smsValueMapping.put(paraMeter, dbtCurr + " " + dbtOnlineBal);
                            break;

                        case "DATE.TIME":
                            Date date;
                            try {
                                date = new SimpleDateFormat("yyMMddHHmm").parse(sysDateTime);
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

                    StringBuilder dbtMessage = new StringBuilder();
                    List<SmsTextClass> smsParams = parameterRecord.getSmsEvent(0).getSmsText();

                    for (int i = 0; i < smsParams.size(); i++) {
                        if (!smsParams.get(i).getSmsText().getValue().equals("")) {
                            dbtMessage.append((smsParams.get(i).getSmsText().getValue()) + " ");
                        } else {
                            dbtMessage.append(i);
                        }
                        if (!smsParams.get(i).getSmsVariable().getValue().equals("")) {
                            dbtMessage
                                    .append((smsValueMapping.get(smsParams.get(i).getSmsVariable().getValue())) + " ");
                        }
                    }

                    String dbtSmsContent = dbtMessage.toString().replace(" . ", ". ").trim();

                    EbJblSmsBookTable bookTable = new EbJblSmsBookTable(this);
                    EbJblSmsBookRecord bookRecord = new EbJblSmsBookRecord();

                    bookRecord.setSmsBody(dbtSmsContent);
                    bookRecord.setSmsStatus("PENDING");
                    bookRecord.setPhone(dbtCusPhone);
                    bookRecord.setEmail(dbtCusEmail);

                    List<LimitAmtClass> limitAmtClasses = parameterRecord.getLimitAmt();

                    // Substring is required to get the amount, eg: USD4500 to
                    // 4500
                    String yDbtAmt = "";
                    yDbtAmt = dbtAmt.substring(3);

                    Double dblTxnAmt = 0.0;
                    dblTxnAmt = Double.valueOf(yDbtAmt);

                    String priority = "3"; // Set default priority
                    for (LimitAmtClass limitAmtClass : limitAmtClasses) {
                        
                        Double dblLimAmt = 0.0;
                        // Try Catch for which returns empty value
                        try {
                            dblLimAmt = Double.valueOf(limitAmtClass.getLimitAmt().getValue());
                        } catch (Exception e1) {
                        }

                        if (dblLimAmt <= dblTxnAmt) {
                            priority = limitAmtClass.getPriority().getValue();
                            break;
                        }
                    }

                    bookRecord.setPriority(priority);

                    try {
                        bookTable.write("DEBIT-" + currentRecordId, bookRecord);
                    } catch (T24IOException e) {
                        System.out.println("Unable to write data on SMS Book Table");
                    }
                }

                if (cdtCusPhone.length() == 11 || cdtCusEmail != "") {

                    Map<String, String> smsValueMapping = new HashMap<String, String>();

                    for (int i = 0; i < parameterRecord.getSmsEvent(1).getSmsText().size(); i++) {

                        String paraMeter = parameterRecord.getSmsEvent(1).getSmsText(i).getSmsVariable().getValue();

                        switch (paraMeter) {

                        case "ACCOUNT":
                            smsValueMapping.put(paraMeter, "***" + cdtAcct.substring(dbtAcct.length() - 4));
                            break;

                        case "CO.CODE":
                            smsValueMapping.put(paraMeter, branchName);
                            break;

                        case "AMOUNT.CREDITED":
                            smsValueMapping.put(paraMeter, cdtCurr + " " + cdtAmt2);
                            break;

                        case "ONLINE.ACTUAL.BALANCE":
                            smsValueMapping.put(paraMeter, cdtCurr + " " + dbtOnlineBal);
                            break;

                        case "DATE.TIME":
                            Date date;
                            try {
                                date = new SimpleDateFormat("yyMMddHHmm").parse(sysDateTime);
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

                    StringBuilder cdtMessage = new StringBuilder();
                    List<SmsTextClass> smsParams = parameterRecord.getSmsEvent(1).getSmsText();

                    for (int i = 0; i < smsParams.size(); i++) {
                        if (!smsParams.get(i).getSmsText().getValue().equals("")) {
                            cdtMessage.append((smsParams.get(i).getSmsText().getValue()) + " ");
                        } else {
                            cdtMessage.append(i);
                        }
                        if (!smsParams.get(i).getSmsVariable().getValue().equals("")) {
                            cdtMessage
                                    .append((smsValueMapping.get(smsParams.get(i).getSmsVariable().getValue())) + " ");
                        }
                    }
                    String cdtSmsContent = cdtMessage.toString().replace(" . ", ". ").trim();

                    EbJblSmsBookTable bookTable = new EbJblSmsBookTable(this);
                    EbJblSmsBookRecord bookRecord = new EbJblSmsBookRecord();

                    bookRecord.setSmsBody(cdtSmsContent);
                    bookRecord.setSmsStatus("PENDING");
                    bookRecord.setPhone(cdtCusPhone);
                    bookRecord.setEmail(cdtCusEmail);

                    List<LimitAmtClass> limitAmtClasses = parameterRecord.getLimitAmt();
                    // Substring is required to get the amount, eg: USD4500 to
                    // 4500
                    String yDbtAmt = "";
                    yDbtAmt = dbtAmt.substring(3);

                    Double dblTxnAmt = 0.0;
                    dblTxnAmt = Double.valueOf(yDbtAmt);

                    String priority = "3"; // Set default priority
                    String apiLink = ""; // Set default apiLink

                    for (LimitAmtClass limitAmtClass : limitAmtClasses) {
                        
                        Double dblLimAmt = 0.0;
                        // Try Catch for which returns empty value
                        try {
                            dblLimAmt = Double.valueOf(limitAmtClass.getLimitAmt().getValue());
                        } catch (Exception e1) {
                        }

                        if (dblLimAmt <= dblTxnAmt) {
                            priority = limitAmtClass.getPriority().getValue();
                            apiLink = limitAmtClass.getApiLink().getValue();
                            break;
                        }
                    }

                    bookRecord.setPriority(priority);
                    bookRecord.setApiLink(apiLink);

                    try {
                        bookTable.write("CREDIT-" + currentRecordId, bookRecord);
                    } catch (T24IOException e) {
                        System.out.println("Unable to write data on SMS Book Table");
                    }
                }

            } else {

                if (dbtCusPhone.length() == 11 || dbtCusEmail != "") {

                    Map<String, String> smsValueMapping = new HashMap<String, String>();

                    for (int i = 0; i < parameterRecord.getSmsEvent(1).getSmsText().size(); i++) {

                        String paraMeter = parameterRecord.getSmsEvent(1).getSmsText(i).getSmsVariable().getValue();

                        switch (paraMeter) {

                        case "ACCOUNT":
                            smsValueMapping.put(paraMeter, "***" + dbtAcct);
                            break;

                        case "CO.CODE":
                            smsValueMapping.put(paraMeter, branchName);
                            break;

                        case "AMOUNT.DEBITED":
                            smsValueMapping.put(paraMeter, dbtCurr + " " + dbtAmt2);
                            break;

                        case "ONLINE.ACTUAL.BALANCE":
                            smsValueMapping.put(paraMeter, dbtCurr + " " + dbtOnlineBal);
                            break;

                        case "DATE.TIME":
                            Date date;
                            try {
                                date = new SimpleDateFormat("yyMMddHHmm").parse(sysDateTime);
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

                    StringBuilder dbtMessage = new StringBuilder();
                    List<SmsTextClass> smsParams = parameterRecord.getSmsEvent(1).getSmsText();

                    for (int i = 0; i < smsParams.size(); i++) {
                        if (!smsParams.get(i).getSmsText().getValue().equals("")) {
                            dbtMessage.append((smsParams.get(i).getSmsText().getValue()) + " ");
                        } else {
                            dbtMessage.append(i);
                        }
                        if (!smsParams.get(i).getSmsVariable().getValue().equals("")) {
                            dbtMessage.append((smsValueMapping.get(smsParams.get(i).getSmsVariable().getValue())) + " ");
                        }
                    }

                    String dbtSmsContent = dbtMessage.toString().replace(" . ", ". ").trim();

                    EbJblSmsBookTable bookTable = new EbJblSmsBookTable(this);
                    EbJblSmsBookRecord bookRecord = new EbJblSmsBookRecord();

                    bookRecord.setSmsBody(dbtSmsContent);
                    bookRecord.setSmsStatus("PENDING");
                    bookRecord.setPhone(dbtCusPhone);
                    bookRecord.setEmail(dbtCusEmail);

                    List<LimitAmtClass> limitAmtClasses = parameterRecord.getLimitAmt();
                    // Substring is required to get the amount, eg: USD4500 to
                    // 4500
                    String yDbtAmt = "";
                    yDbtAmt = dbtAmt.substring(3);
                    Double dblTxnAmt = 0.0;
                    dblTxnAmt = Double.valueOf(yDbtAmt);

                    String priority = "3"; // Set default priority
                    for (LimitAmtClass limitAmtClass : limitAmtClasses) {
                        
                        Double dblLimAmt = 0.0;
                        // Try Catch for which returns empty value
                        try {
                            dblLimAmt = Double.valueOf(limitAmtClass.getLimitAmt().getValue());
                        } catch (Exception e1) {
                        }

                        if (dblLimAmt <= dblTxnAmt) {
                            priority = limitAmtClass.getPriority().getValue();
                            break;
                        }
                    }

                    bookRecord.setPriority(priority);

                    try {
                        bookTable.write("DEBIT-" + currentRecordId, bookRecord);
                    } catch (T24IOException e) {
                        System.out.println("Unable to write data on SMS Book Table");
                    }
                }

                if (cdtCusPhone.length() == 11) {

                    Map<String, String> smsValueMapping = new HashMap<String, String>();

                    for (int i = 0; i < parameterRecord.getSmsEvent(0).getSmsText().size(); i++) {

                        String paraMeter = parameterRecord.getSmsEvent(0).getSmsText(i).getSmsVariable().getValue();

                        switch (paraMeter) {

                        case "ACCOUNT":
                            smsValueMapping.put(paraMeter, "***" + cdtAcct);
                            break;

                        case "CO.CODE":
                            smsValueMapping.put(paraMeter, branchName);
                            break;

                        case "AMOUNT.CREDITED":
                            smsValueMapping.put(paraMeter, cdtCurr + " " + cdtAmt2);
                            break;

                        case "ONLINE.ACTUAL.BALANCE":
                            smsValueMapping.put(paraMeter, cdtCurr + " " + cdtOnlineBal);
                            break;

                        case "DATE.TIME":
                            Date date;
                            try {
                                date = new SimpleDateFormat("yyMMddHHmm").parse(sysDateTime);
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

                    StringBuilder cdtMessage = new StringBuilder();
                    List<SmsTextClass> smsParams = parameterRecord.getSmsEvent(0).getSmsText();

                    for (int i = 0; i < smsParams.size(); i++) {
                        if (!smsParams.get(i).getSmsText().getValue().equals("")) {
                            cdtMessage.append((smsParams.get(i).getSmsText().getValue()) + " ");
                        } else {
                            cdtMessage.append(i);
                        }
                        if (!smsParams.get(i).getSmsVariable().getValue().equals("")) {
                            cdtMessage
                                    .append((smsValueMapping.get(smsParams.get(i).getSmsVariable().getValue())) + " ");
                        }
                    }
                    String cdtSmsContent = cdtMessage.toString().replace(" . ", ". ").trim();

                    EbJblSmsBookTable bookTable = new EbJblSmsBookTable(this);
                    EbJblSmsBookRecord bookRecord = new EbJblSmsBookRecord();

                    bookRecord.setSmsBody(cdtSmsContent);
                    bookRecord.setSmsStatus("PENDING");
                    bookRecord.setPhone(cdtCusPhone);
                    bookRecord.setEmail(cdtCusEmail);

                    List<LimitAmtClass> limitAmtClasses = parameterRecord.getLimitAmt();
                    // Substring is required to get the amount, eg: USD4500 to
                    // 4500
                    String yDbtAmt = "";
                    yDbtAmt = dbtAmt.substring(3);
                    Double dblTxnAmt = 0.0;
                    dblTxnAmt = Double.valueOf(yDbtAmt);

                    String priority = "3"; // Set default priority
                    for (LimitAmtClass limitAmtClass : limitAmtClasses) {

                        Double dblLimAmt = 0.0;
                        // Try Catch for which returns empty value
                        try {
                            dblLimAmt = Double.valueOf(limitAmtClass.getLimitAmt().getValue());
                        } catch (Exception e1) {
                        }

                        if (dblLimAmt <= dblTxnAmt) {
                            priority = limitAmtClass.getPriority().getValue();
                            break;
                        }
                    }

                    bookRecord.setPriority(priority);

                    try {
                        bookTable.write("CREDIT-" + currentRecordId, bookRecord);
                    } catch (T24IOException e) {
                        System.out.println("Unable to write data on SMS Book Table");
                    }
                }
            }
        } else {
            return;
        }
    }
}
