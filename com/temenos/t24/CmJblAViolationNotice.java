package com.temenos.t24;


import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.complex.eb.templatehook.TransactionData;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.desctxsectrasparameter.OverrideProcessingClass;
import com.temenos.t24.api.records.dispoitems.OverrideTextClass;
import com.temenos.t24.api.records.ebaccountviolationnotice.EbAccountViolationNoticeRecord;
import com.temenos.t24.api.records.ebaccviolationrecord.EbAccViolationRecordRecord;
import com.temenos.t24.api.records.ebaccviolationrecord.NoticeIdClass;
import com.temenos.t24.api.records.eberror.ErrorMsgClass;
import com.temenos.t24.api.records.overrideclass.OverrideClassRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Date;
import com.temenos.t24.api.tables.ebaccviolationrecord.EbAccViolationRecordTable;

/**
 * TODO: Document me!
 *
 * @author Tajul 
 * 
 */
public class CmJblAViolationNotice extends RecordLifecycle{
   String yMonth = null;
   DateTimeFormatter T24_DATE_FORMAT;
   

    @Override
    public void updateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext,
            List<TransactionData> transactionData, List<TStructure> currentRecords) {
        
        EbAccountViolationNoticeRecord liveNoticeRecord = null;
        EbAccountViolationNoticeRecord currentNoticeRecord = null;
        int existingNoticeCount = 0;
        int currentNoticeCount = 0;
        double noticeTxnAmount = 0.0;
        
        ErrorMsgClass emc = new ErrorMsgClass();        
        
        emc.getErrorMsg(0);
        List<TField> errMsgList = emc.getErrorMsg();
        String errMsg = errMsgList.get(0).toString();
        String errMsgListStr = emc.getErrorMsg().toString();
        
        
        
        OverrideClassRecord ocr = new OverrideClassRecord();
        OverrideTextClass oct = new OverrideTextClass();
        OverrideProcessingClass opc = new OverrideProcessingClass();       
        String myOpcNum = "1";
        String overrides = "";
        oct.getOverrideId();
        
        overrides = opc.getOverrideProcessing().toString();
        
        opc.setOverrideProcessing("1");
      
        // oct.
        
       // ocr.
        
        //ArrayList<AccountNoClass> noticeDataList = new ArrayList<AccountNoClass>();
        Date dt = new Date(this);           
        String yToday = dt.getDates().getToday().toString();
        //boolean isNewNoticeadded = false;
        try {
            liveNoticeRecord = new EbAccountViolationNoticeRecord(liveRecord);
            existingNoticeCount = liveNoticeRecord.getTransactionDate().size();
        } catch (Exception e) {
            
        }
        
        try {
            currentNoticeRecord = new EbAccountViolationNoticeRecord(currentRecord);
            currentNoticeCount = currentNoticeRecord.getTransactionDate().size();
            //vioRecCount=currentNoticeCount;
        } catch (Exception e) {
            
        }
        if(currentNoticeCount < existingNoticeCount){
            if (transactionContext.getCurrentFunction().equals("INPUT")){
                currentNoticeRecord.getAccountNo().setError("You can not commit the record.");
                currentNoticeRecord.getAccountNo().setEnrichment("You probably have removed previous entry / entries");
                throw new T24CoreException("You probably have removed previous entry");
                //return noticeRecord.getValidationResponse();
            }
            
        }
        String noticeTxnData="";
        List<com.temenos.t24.api.records.ebaccountviolationnotice.TransactionDateClass> noticeDataList = currentNoticeRecord.getTransactionDate();
        List <String> noticeIds = new ArrayList<String>();
        String accNumber="";
        try {
            accNumber = currentNoticeRecord.getAccountNo().getValue();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            // Uncomment and replace with appropriate logger
            // LOGGER.error(e1, e1);
        }
        for(int i=0;i<currentNoticeCount;i++){
            
            //accNumberList.add(accNumber);
            noticeTxnData="";
            String noticeDate = noticeDataList.get(i).getTransactionDate().getValue();
            if(!noticeDataList.get(i).getAmount().getValue().isEmpty())
            {
                noticeTxnAmount = Double.parseDouble(noticeDataList.get(i).getAmount().getValue());
            }
            String chequeNumber = noticeDataList.get(i).getCheque().getValue();
            
            noticeTxnData = noticeDate + "-" + String.format("%.2f", noticeTxnAmount);

            if (!chequeNumber.isEmpty()) {
                noticeTxnData += "-" + chequeNumber;
            }
         //   ************need to modify for new requirement

            noticeIds.add(noticeTxnData);        
        }//for loop end here
        
//        if(!accNumberList.isEmpty()){
//            processStringList(accNumberList);
//            countUniqueAccounts(accNumberList);
//        }
        
        if(existingNoticeCount != currentNoticeCount){
           // updateViolationTable(currentRecordId, existingNoticeCount, currentNoticeCount, noticeTxnData,noticeDataList,noticeIds,accNumber );
        //return noticeRecord.getValidationResponse();
            DataAccess da = new DataAccess(this);
            EbAccViolationRecordRecord violationRecord = null;
            NoticeIdClass noticeIdClass = new NoticeIdClass();
            String vioRecId =BuildRecId(accNumber, noticeDataList.get(0).getTransactionDate().getValue());
            
            try {
                violationRecord = new EbAccViolationRecordRecord(da.getRecord("EB.ACC.VIOLATION.RECORD", vioRecId));
                //vioNoticeDatalength = violationRecord.getNoticeId().size();
            } catch (Exception e) {
                //vioNoticeDatalength=0;
                violationRecord = new EbAccViolationRecordRecord(this);
            }

            for(int i=0,j=0;i<currentNoticeCount;i++, j++){
                
                if(i>0){
                    LocalDate t24Today = LocalDate.parse(noticeDataList.get(i-1).getTransactionDate().getValue(),T24_DATE_FORMAT);
                    String currentMonth = t24Today.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase();
                    if(!currentMonth.equals(yMonth)){
                        writeOnRecordTable(vioRecId, violationRecord);
                        vioRecId =BuildRecId(accNumber, noticeDataList.get(i).getTransactionDate().getValue());
                        try {
                            violationRecord = new EbAccViolationRecordRecord(da.getRecord("EB.ACC.VIOLATION.RECORD", vioRecId));
                            //vioNoticeDatalength = violationRecord.getNoticeId().size();
                        } catch (Exception e) {
                            //vioNoticeDatalength=0;
                            violationRecord = new EbAccViolationRecordRecord(this);
                        }
                 
                    }
                }
                
                violationRecord.setViolationVerdict("VIOLATED");
                violationRecord.setNoticeViolationFlag("YES");
                noticeIdClass = new NoticeIdClass();
                noticeIdClass.setNoticeId(currentRecordId);
                noticeIdClass.setNoticeData(noticeIds.get(j));
                violationRecord.addNoticeId(noticeIdClass);
               
     
            }
            TransactionData tData = new TransactionData();
            tData.setFunction("INPUT");
            tData.setNumberOfAuthoriser("0");
            tData.setTransactionId(vioRecId);
            tData.setVersionId("EB.ACC.VIOLATION.RECORD,INPUT");
            transactionData.add(tData);
           currentRecords.add(violationRecord.toStructure());
    }
    

    }

/*    public void updateViolationTable(String noticeRecId, int liveNoticeCount, int currentNoticeCount, String noticeData, List<TransactionDateClass> noticeDataList, List<String> noticeIds,String accNumber) {
        DataAccess da = new DataAccess(this);
        EbAccViolationRecordRecord violationRecord = null;
        NoticeIdClass noticeIdClass = new NoticeIdClass();
        String vioRecId =BuildRecId(accNumber, noticeDataList.get(0).getTransactionDate().getValue());
        
        try {
            violationRecord = new EbAccViolationRecordRecord(da.getRecord("EB.ACC.VIOLATION.RECORD", vioRecId));
            //vioNoticeDatalength = violationRecord.getNoticeId().size();
        } catch (Exception e) {
            //vioNoticeDatalength=0;
            violationRecord = new EbAccViolationRecordRecord(this);
        }

        for(int i=0,j=0;i<currentNoticeCount;i++, j++){
            
            if(i>0){
                LocalDate t24Today = LocalDate.parse(noticeDataList.get(i-1).getTransactionDate().getValue(),T24_DATE_FORMAT);
                String currentMonth = t24Today.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase();
                if(!currentMonth.equals(yMonth)){
                    writeOnRecordTable(vioRecId, violationRecord);
                    vioRecId =BuildRecId(accNumber, noticeDataList.get(i).getTransactionDate().getValue());
                    try {
                        violationRecord = new EbAccViolationRecordRecord(da.getRecord("EB.ACC.VIOLATION.RECORD", vioRecId));
                        //vioNoticeDatalength = violationRecord.getNoticeId().size();
                    } catch (Exception e) {
                        //vioNoticeDatalength=0;
                        violationRecord = new EbAccViolationRecordRecord(this);
                    }
             
                }
            }
            
            violationRecord.setViolationVerdict("VIOLATED");
            violationRecord.setNoticeViolationFlag("YES");
            noticeIdClass = new NoticeIdClass();
            noticeIdClass.setNoticeId(noticeRecId);
            noticeIdClass.setNoticeData(noticeIds.get(j));
            violationRecord.addNoticeId(noticeIdClass);
           
 
        }
        
        writeOnRecordTable(vioRecId, violationRecord);
        
    }*/
    
    public void writeOnRecordTable(String vioRecId, EbAccViolationRecordRecord violationRecord)
    {
        EbAccViolationRecordTable violationTable = new EbAccViolationRecordTable(this);
        try {
            violationTable.write(vioRecId, violationRecord);
        } catch (T24IOException e) {
            throw new T24CoreException("Unfortunately Couldn't write on violation Table " + e);
        }
    }
    public String BuildRecId(String debitAcctNo,  String txnDate){
        
       // DateTimeFormatter T24_DATE_FORMAT;
        T24_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate t24Today = LocalDate.parse(txnDate,T24_DATE_FORMAT);
        String currentMonth = t24Today.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase();
        yMonth=currentMonth;
        StringBuilder noticeRecordIdBuilder = new StringBuilder(debitAcctNo)
                .append("-").append(t24Today.getYear())
                .append("-").append(currentMonth);
        String recordId = noticeRecordIdBuilder.toString();
        return recordId;
        
    }   
//    public static Map<String, Integer> countUniqueAccounts(List<String> strings) {
//        
//        
//        for (String str : strings) {
//            if (stringCountMap.containsKey(str)) {
//                stringCountMap.put(str, stringCountMap.get(str) + 1);
//            } else {
//                stringCountMap.put(str, 1);
//            }
//        }
//        
//        return stringCountMap;
//    }
   // Method that Handle if user give more than one account number in one notice ID

//        public void processStringList(List<String> list) {
//          
//
//            for (int i = 0; i < list.size(); i++) {
//                String item = list.get(i);
//                if (!differentAccMap.containsKey(item)) {
//                    differentAccMap.put(item, i);
//                }
//            }
//
//            
//        }


}
