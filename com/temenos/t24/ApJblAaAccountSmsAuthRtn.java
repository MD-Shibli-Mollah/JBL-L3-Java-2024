package com.temenos.t24;

/*
 * MODULE         : SMS (AA)
 * PRODUCT        : CIP SAVINGS ACCOUNT
 * EB.API         : AA.SMS.POST.RTN
 * RELATED APP    : EB.JBL.SMS.BOOK, EB.JBL.SMS.PARAMETER (ETB)
 * ATTACHED AS    : Post Routine
 * ACTIVITY.API   : AFRCIP.ACCOUNT.CLOSURE
 * ACTIVITY CLASS : ACCOUNTS-NEW-ARRANGEMENT     PROPERTY CLASS : ACCOUNT   ACTION : MAINTAIN
 * ACTIVITY CLASS : ACCOUNTS-UPDATE-BALANCE      PROPERTY CLASS : ACCOUNT   ACTION : MAINTAIN
 * ACTIVITY CLASS : ACCOUNTS-CLOSE-ARRANGEMENT   PROPERTY CLASS : CLOSURE   ACTION : CLOSE
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
import com.temenos.t24.api.complex.aa.activityhook.ArrangementContext;
import com.temenos.t24.api.complex.aa.activityhook.TransactionData;
import com.temenos.t24.api.hook.arrangement.ActivityLifecycle;
import com.temenos.t24.api.records.aaaccountdetails.AaAccountDetailsRecord;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.aaarrangementactivity.AaArrangementActivityRecord;
import com.temenos.t24.api.records.aaproductcatalog.AaProductCatalogRecord;
import com.temenos.t24.api.records.company.CompanyRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookRecord;
import com.temenos.t24.api.tables.ebjblsmsparameter.EbJblSmsParameterRecord;
import com.temenos.t24.api.tables.ebjblsmsparameter.SmsTextClass;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookTable;

public class ApJblAaAccountSmsAuthRtn extends ActivityLifecycle {
    @Override
    public void postCoreTableUpdate(AaAccountDetailsRecord accountDetailRecord,
            AaArrangementActivityRecord arrangementActivityRecord, ArrangementContext arrangementContext,
            AaArrangementRecord arrangementRecord, AaArrangementActivityRecord masterActivityRecord,
            TStructure productPropertyRecord, AaProductCatalogRecord productRecord, TStructure record,
            List<TransactionData> transactionData, List<TStructure> transactionRecord) {

        if (arrangementContext.getActivityStatus().equals("AUTH")) {
            
            DataAccess da = new DataAccess(this);
            String cusId = "";
            cusId = arrangementRecord.getCustomer(0).getCustomer().toString();
            CustomerRecord cusRec = new CustomerRecord(da.getRecord("CUSTOMER", cusId));
            
            String cusPhoneNo   = "";
            String cusEmail     = "";
            try {
                cusPhoneNo  = cusRec.getPhone1(0).getPhone1().getValue();
                cusEmail    = cusRec.getPhone1(0).getEmail1().getValue();
            } catch (Exception e1) {
            }

            if (cusPhoneNo.length() == 11 || cusEmail != "") {
                if (masterActivityRecord.getActivity().getValue().equals("ACCOUNTS-NEW-ARRANGEMENT")) {

                    EbJblSmsParameterRecord parameterRecord = new EbJblSmsParameterRecord(
                            da.getRecord("EB.JBL.SMS.PARAMETER", "ACCOUNTS-NEW-ARRANGEMENT"));

                    Map<String, String> smsValueMapping = new HashMap<String, String>();
                    
                   // String priority = "";
                   // priority = parameterRecord.getLimitAmt(0).getPriority().getValue();

                    for (int i = 0; i < parameterRecord.getSmsEvent(0).getSmsText().size(); i++) {
                        String paraMeter = parameterRecord.getSmsEvent(0).getSmsText().get(i).getSmsVariable()
                                .getValue();

                        switch (paraMeter) {

                        case "PRODUCT":
                            smsValueMapping.put(paraMeter, productRecord.getDescription(0).getValue());
                            break;

                        case "CO.CODE":
                            String yCoCode = arrangementActivityRecord.getCoCode();
                            CompanyRecord comRecord = new CompanyRecord(da.getRecord("COMPANY", yCoCode));
                            String yCompName = comRecord.getCompanyName(0).getValue();
                            smsValueMapping.put(paraMeter, yCompName);
                            break;

                        case "DATE.TIME":
                            Date date;
                            try {
                                date = new SimpleDateFormat("yyMMddHHmm")
                                        .parse(arrangementActivityRecord.getDateTime(0));
                                String dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date);
                                smsValueMapping.put(paraMeter, dateTime);
                            } catch (ParseException e) {
                                System.out.println("Invalid Date Format");
                            }
                            break;

                        case "USER":
                            List<String> yUserList = arrangementActivityRecord.getInputter();
                            String yUserFull = yUserList.get(0);
                            String yUser = yUserFull.split("_")[1];
                            smsValueMapping.put(paraMeter, yUser);
                            break;

                        default:
                            break;
                        }
                    }

                    StringBuilder message = new StringBuilder();
                    List<SmsTextClass> smsParams = parameterRecord.getSmsEvent(0).getSmsText();

                    for (int i = 0; i < smsParams.size(); i++) {
                        if (!smsParams.get(i).getSmsText().getValue().equals("")) {
                            message.append((smsParams.get(i).getSmsText().getValue()) + " ");
                        } else {
                            message.append(i);
                        }
                        if (!smsParams.get(i).getSmsVariable().getValue().equals("")) {
                            message.append((smsValueMapping.get(smsParams.get(i).getSmsVariable().getValue())) + " ");
                        }
                    }

                    String smsContent = message.toString().replace(" . ", ". ").trim();

                    EbJblSmsBookTable bookTable = new EbJblSmsBookTable(this);
                    EbJblSmsBookRecord bookRecord = new EbJblSmsBookRecord();
                    
                    String priority = "2";
                    bookRecord.setSmsBody(smsContent);
                    bookRecord.setSmsStatus("PENDING");
                    bookRecord.setPhone(cusPhoneNo);
                    bookRecord.setEmail(cusEmail);
                    bookRecord.setPriority(priority);

                    try {
                        bookTable.write("NEW-" + arrangementRecord.getLinkedAppl(0).getLinkedApplId().getValue(),
                                bookRecord);
                    } catch (T24IOException e) {
                        System.out.println("Unable to write data on SMS Book Table");
                    }

                } else if ((masterActivityRecord.getActivity().getValue().equals("ACCOUNTS-UPDATE-BALANCE"))) {
                    EbJblSmsParameterRecord parameterRecord = new EbJblSmsParameterRecord(
                            da.getRecord("EB.JBL.SMS.PARAMETER", "ACCOUNTS-UPDATE-BALANCE"));

                    Map<String, String> smsValueMapping = new HashMap<String, String>();
                    
                   // String priority = parameterRecord.getLimitAmt(0).getPriority().getValue();

                    for (int i = 0; i < parameterRecord.getSmsEvent(0).getSmsText().size(); i++) {
                        String paraMeter = parameterRecord.getSmsEvent(0).getSmsText().get(i).getSmsVariable()
                                .getValue();

                        switch (paraMeter) {

                        case "PRODUCT":
                            smsValueMapping.put(paraMeter, productRecord.getDescription(0).getValue());
                            break;

                        case "CO.CODE":
                            String yCoCode = arrangementActivityRecord.getCoCode();
                            CompanyRecord comRecord = new CompanyRecord(da.getRecord("COMPANY", yCoCode));
                            String yCompName = comRecord.getCompanyName(0).getValue();
                            smsValueMapping.put(paraMeter, yCompName);
                            break;

                        case "DATE.TIME":
                            // try {
                            Date date;
                            try {
                                date = new SimpleDateFormat("yyMMddHHmm")
                                        .parse(arrangementActivityRecord.getDateTime(0));
                                String dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date);
                                smsValueMapping.put(paraMeter, dateTime);
                            } catch (ParseException e) {

                                System.out.println("Invalid Date Format");
                            }
                            break;

                        case "USER":
                            List<String> yUserList = arrangementActivityRecord.getInputter();
                            String yUserFull = yUserList.get(0);
                            String yUser = yUserFull.split("_")[1];
                            smsValueMapping.put(paraMeter, yUser);
                            break;

                        default:
                            break;
                        }
                    }

                    StringBuilder message = new StringBuilder();
                    List<SmsTextClass> smsParams = parameterRecord.getSmsEvent(0).getSmsText();

                    for (int i = 0; i < smsParams.size(); i++) {
                        if (!smsParams.get(i).getSmsText().getValue().equals("")) {
                            message.append((smsParams.get(i).getSmsText().getValue()) + " ");
                        } else {
                            message.append(i);
                        }
                        if (!smsParams.get(i).getSmsVariable().getValue().equals("")) {
                            message.append((smsValueMapping.get(smsParams.get(i).getSmsVariable().getValue())) + " ");
                        }
                    }

                    String smsContent = message.toString().replace(" . ", ". ").trim();

                    EbJblSmsBookTable bookTable = new EbJblSmsBookTable(this);
                    EbJblSmsBookRecord bookRecord = new EbJblSmsBookRecord();
                    
                    String priority = "2";

                    bookRecord.setSmsBody(smsContent);
                    bookRecord.setSmsStatus("PENDING");
                    bookRecord.setPhone(cusPhoneNo);
                    bookRecord.setEmail(cusEmail);
                    bookRecord.setPriority(priority);

                    try {
                        bookTable.write("UPDATE-" + arrangementRecord.getLinkedAppl(0).getLinkedApplId().getValue(),
                                bookRecord);
                    } catch (T24IOException e) {
                        System.out.println("Unable to write data on SMS Book Table");
                    }

                } else if (masterActivityRecord.getActivity().getValue().equals("ACCOUNTS-CLOSE-ARRANGEMENT")) {
                    EbJblSmsParameterRecord parameterRecord = new EbJblSmsParameterRecord(
                            da.getRecord("EB.JBL.SMS.PARAMETER", "ACCOUNTS-CLOSE-ARRANGEMENT"));

                    Map<String, String> smsValueMapping = new HashMap<String, String>();
                    
                   // String priority = "";
                   // priority = parameterRecord.getLimitAmt(0).getPriority().getValue();

                    for (int i = 0; i < parameterRecord.getSmsEvent(0).getSmsText().size(); i++) {
                        String paraMeter = parameterRecord.getSmsEvent(0).getSmsText().get(i).getSmsVariable()
                                .getValue();

                        switch (paraMeter) {

                        case "PRODUCT":
                            smsValueMapping.put(paraMeter, productRecord.getDescription(0).getValue());
                            break;

                        case "CO.CODE":
                            String yCoCode = arrangementActivityRecord.getCoCode();
                            CompanyRecord comRecord = new CompanyRecord(da.getRecord("COMPANY", yCoCode));
                            String yCompName = comRecord.getCompanyName(0).getValue();
                            smsValueMapping.put(paraMeter, yCompName);
                            break;

                        case "DATE.TIME":
                            // try {
                            Date date;
                            try {
                                date = new SimpleDateFormat("yyMMddHHmm")
                                        .parse(arrangementActivityRecord.getDateTime(0));
                                String dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date);
                                smsValueMapping.put(paraMeter, dateTime);
                            } catch (ParseException e) {

                                System.out.println("Invalid Date Format");
                            }
                            break;

                        case "USER":
                            List<String> yUserList = arrangementActivityRecord.getInputter();
                            String yUserFull = yUserList.get(0);
                            String yUser = yUserFull.split("_")[1];
                            smsValueMapping.put(paraMeter, yUser);
                            break;

                        default:
                            break;
                        }
                    }

                    StringBuilder message = new StringBuilder();
                    List<SmsTextClass> smsParams = parameterRecord.getSmsEvent(0).getSmsText();

                    for (int i = 0; i < smsParams.size(); i++) {
                        if (!smsParams.get(i).getSmsText().getValue().equals("")) {
                            message.append((smsParams.get(i).getSmsText().getValue()) + " ");
                        } else {
                            message.append(i);
                        }
                        if (!smsParams.get(i).getSmsVariable().getValue().equals("")) {
                            message.append((smsValueMapping.get(smsParams.get(i).getSmsVariable().getValue())) + " ");
                        }
                    }

                    String smsContent = message.toString().replace(" . ", ". ").trim();

                    EbJblSmsBookTable bookTable = new EbJblSmsBookTable(this);
                    EbJblSmsBookRecord bookRecord = new EbJblSmsBookRecord();
                    String priority = "2";
                    bookRecord.setSmsBody(smsContent);
                    bookRecord.setSmsStatus("PENDING");
                    bookRecord.setPhone(cusPhoneNo);
                    bookRecord.setEmail(cusEmail);
                    bookRecord.setPriority(priority);

                    try {
                        bookTable.write("CLOSE-" + arrangementRecord.getLinkedAppl(0).getLinkedApplId().getValue(),
                                bookRecord);
                    } catch (T24IOException e) {
                        System.out.println("Unable to write data on SMS Book Table");
                    }

                } else {
                    return;
                }

            } else {
                return;
            }
        } else {
            return;
        }
    }
}