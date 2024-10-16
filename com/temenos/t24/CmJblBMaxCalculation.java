package com.temenos.t24;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.arrangement.accounting.Contract;
import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.complex.eb.servicehook.TransactionData;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.acctentlworkday.AcctEntLworkDayRecord;
import com.temenos.t24.api.records.ebbdoibtparam.EbBdOibtParamRecord;
import com.temenos.t24.api.records.ebbdsocbalinfo.EbBdSocBalinfoRecord;
import com.temenos.t24.api.records.stmtentry.StmtEntryRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Date;
import com.temenos.t24.api.tables.ebbdsocbalinfo.EbBdSocBalinfoTable;

/**
 * TODO: Document me!
 *
 * @author
 * Modification History: 
 * Added Transaction count update on EB.BD.SOC.BALINFO table 
 * for online transaction.
 * Tajul Islam (T24 Developer).
 *
 */

public class CmJblBMaxCalculation extends ServiceLifecycle 
{
    public  String balInfoId="";
    public  boolean ofsFlag=false;

    @Override
    public List<String> getIds(ServiceData serviceData, List<String> controlList) 
    {
        List<String> recIds = null;
        DataAccess da = new DataAccess(this);
        recIds = da.selectRecords("BNK", "ACCT.ENT.LWORK.DAY", "", "WITH @ID LIKE 0...");
        System.out.println("Total Records From getId Method : "+recIds.size());
        return recIds;
    }
    
    //////oibt txn update code start
    
    public boolean isTxnCodeAvailable(String txnCode, List<TField> txnCodes){
        int txnCodesize=txnCodes.size();
        for(int i=0;i<txnCodesize;i++){
            System.out.println("txnCode Checking......");
            if(txnCode.equals(txnCodes.get(i).toString())){
                return true;
            }
        }
        return false;
       
    }
    public static List<String> getUniqueValuesBeforeDecimal(List<String> records) {
        Set<String> seen = new HashSet<>();
        List<String> uniqueRecords = new ArrayList<>();

        for (String record : records) {
            // Split the string at the '.' and take the part before it
            String valueBeforeDecimal = record.split("\\.")[0];

            // If this value hasn't been seen before, add it to the result list
            if (!seen.contains(valueBeforeDecimal)) {
                seen.add(valueBeforeDecimal);
                uniqueRecords.add(record);
            }
        }
        //System.out.println("unique filtered done : "+uniqueRecords);
        return uniqueRecords;
    }
    
    public EbBdSocBalinfoRecord OibtTxnCountUpdate(String id, List<String> stmtRecords){
        System.out.println("STMT RECORDS from OibtTxnCountUpdate method : "+stmtRecords);
        DataAccess da = new DataAccess(this);
        Date dt = new Date(this);
        String dateYear=dt.getDates().getLastWorkingDay().getValue();
       // System.out.println("date year:  "+dateYear);
        String lastWorkingYear = dateYear.substring(0, 4);
         balInfoId=id+"-"+lastWorkingYear;
       // EbBdSocBalinfoTable balInfoTable = new EbBdSocBalinfoTable(this);
        EbBdSocBalinfoRecord balInfoRecord=null;
        
        try {
            
           
            int oibtTxnCount=0;
            try {
                balInfoRecord = new EbBdSocBalinfoRecord(da.getRecord("EB.BD.SOC.BALINFO", balInfoId));
        
                try {
                    oibtTxnCount= Integer.valueOf(balInfoRecord.getOibtTxnCount().getValue());
                } catch (Exception e) {
                    oibtTxnCount=0;
                }
            } catch (Exception e) {
                balInfoRecord = new EbBdSocBalinfoRecord();
                
                balInfoRecord.setOibtTxnCount("0");
              
            }
    
            StmtEntryRecord stmtEntryRecord = null;
            EbBdOibtParamRecord oibtParamRecord=null;
            try {
                //System.out.println("param opening trying.......................................");
                oibtParamRecord = new EbBdOibtParamRecord(da.getRecord("EB.BD.GB.PARAM","SYSTEM"));
            } catch (Exception e1) {
                System.out.println("4: couldn't open oibt param: with ID: SYSTEM: "+e1);
            }
            
            try {

                List<TField> oibttxnCodefromParam = oibtParamRecord.getOibtTxnCode();

                List<String> stmtUniqueRecords=getUniqueValuesBeforeDecimal(stmtRecords);
                System.out.println("STMT UNIQUE RECORDS : "+stmtUniqueRecords);
         
                int stmtRecordSize = stmtUniqueRecords.size();
                String txnCode="";
                String txnRef="";
                int countFlag=0;
                for(int i = 0;i<stmtRecordSize;i++)
                {
                    String stmtId = stmtUniqueRecords.get(i);
                    try {
                        stmtEntryRecord = new StmtEntryRecord(da.getRecord("STMT.ENTRY", stmtId));
                    } catch (Exception e) {
                        System.out.println("STMT.ENTRY COULDN'T OPEN FOR THE ID : "+stmtId);
                    }
                    txnCode=stmtEntryRecord.getTransactionCode().getValue();
                    System.out.println("transaction Code:***************: "+txnCode);
                    txnRef=stmtEntryRecord.getTransReference().getValue();
                    int index = txnRef.indexOf('\\');
                    System.out.println("online transactions found: "+index);
                    if(index != -1){
                    if(!isTxnCodeAvailable(txnCode, oibttxnCodefromParam) ){
                        countFlag+=1;
                    }
                    }
                }
                countFlag+=oibtTxnCount;
                System.out.println("Transaction Count, CountFlag : "+countFlag);
                if(countFlag == oibtTxnCount){
                    ofsFlag=true;
                }

                balInfoRecord.setOibtTxnCount(String.valueOf(countFlag));
                System.out.println("BalInfoRecord: "+balInfoRecord);
                return balInfoRecord;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                // Uncomment and replace with appropriate logger
                // LOGGER.error(e, e);
                System.out.println("BalInfoRecord from exception: "+balInfoRecord);
                return balInfoRecord;
            }
            
            
        } catch (Exception e) {
            System.out.println(e);
            
        }
        
        
        System.out.println("BalInfoRecord from Last Line: "+balInfoRecord);
        return balInfoRecord;   
        
        
    }
//////oibt txn update code end

    @Override
    public void postUpdateRequest(String id, ServiceData serviceData, String controlItem,
            List<TransactionData> transactionData, List<TStructure> records) 
    {
        
        DataAccess dRead = new DataAccess(this); 
        EbBdSocBalinfoTable EtbTab = new EbBdSocBalinfoTable(this);
        EbBdSocBalinfoRecord record = null;
        AccountRecord acRec = null;
        ofsFlag=false;
        
        String aaId = null;
        String ProductLine = null;
        String ProductGroup = null;
        try 
        {
            acRec = new AccountRecord(dRead.getRecord("ACCOUNT", id));
            aaId = acRec.getArrangementId().getValue();
        } 
        catch (Exception e1) 
        {
        }
       
        if(!aaId.isEmpty())
        {
        AaArrangementRecord aaa = new AaArrangementRecord(dRead.getRecord("AA.ARRANGEMENT", aaId)); 
         ProductLine = aaa.getProductLine().getValue();
         ProductGroup = aaa.getProductGroup().getValue();
        }
        
        StmtEntryRecord entryRecord;
        String Etbamtstr = null;
        BigDecimal StmtAmt = BigDecimal.ZERO;
        BigDecimal numEtbamt = BigDecimal.ZERO;
        BigDecimal Etbamt = BigDecimal.ZERO;
        BigDecimal Multi = new BigDecimal(-1.00);
        
        try 
        {
            try 
            {
                record = new EbBdSocBalinfoRecord(dRead.getRecord("EB.BD.SOC.BALINFO", id));
                Etbamtstr = record.getHighBalance().toString();
                double Etbamtdbl = Double.valueOf(Etbamtstr);
                Etbamt = BigDecimal.valueOf(Etbamtdbl);
            } 
            catch (Exception e) 
            {
                
            }
            /////oibt txn count update start
            EbBdSocBalinfoRecord balInfoRecord;
            
            //System.out.println("txnUpdate method Calling.....");
            List<String> stmtRecords = dRead.getConcatValues("ACCT.ENT.LWORK.DAY", id);
            //System.out.println("STMT RECORDS: "+stmtRecords);
            int m = stmtRecords.size();

            if(!ofsFlag){
                balInfoRecord=OibtTxnCountUpdate(id,stmtRecords);
                
            TransactionData tData = new TransactionData();
            tData.setVersionId("EB.BD.SOC.BALINFO,INPUT");
            tData.setFunction("INPUT");
            tData.setNumberOfAuthoriser("0");
            tData.setSourceId("BULK.OFS");
            tData.setUserName("INPUTT");
            tData.setCompanyId("BD0019999");
            tData.setTransactionId(balInfoId);
            System.out.println("BalInfo ID From postrequest: ########################: "+balInfoId);
            //System.out.println();
            transactionData.add(tData);
            
            System.out.println("BalInfo Record from post Method: "+balInfoRecord);
            System.out.println("TransactionData : "+tData);
            records.add(balInfoRecord.toStructure());
            }
            
            //System.out.println("txnUpdate method end.....");
            
            
      //MAX CALC
/*            for(int i = 0;i<m;i++)
            {
                String stmtId = stmtRecords.get(i);
                entryRecord = new StmtEntryRecord(dRead.getRecord("STMT.ENTRY", stmtId));
                //System.out.println("STMT ID" +stmtId);
                String Entryamt = entryRecord.getAmountLcy().toString();
                double Entryamt1 = Double.parseDouble(Entryamt);
               
                StmtAmt = BigDecimal.valueOf(Entryamt1); 
                
                if(!ProductLine.equals("LENDING") || ProductGroup.equals("JBL.CONT.GRP.LN"))
                {
                    StmtAmt = StmtAmt.multiply(Multi);
                    
                }    
                
                BigDecimal TotalAmt = Etbamt.add(StmtAmt);   //(ETB amount + STMT AMT )  
                
                if(TotalAmt.compareTo(numEtbamt) == 1 )
                {
                    numEtbamt = TotalAmt;
                   
                }                
            }    
        } 
        catch (Exception e) 
        {
        }
        String numEtbamt1 = String.valueOf(numEtbamt);
        record.setHighBalance(numEtbamt1);
       
        try 
        {                
        EtbTab.write(id, record);
        } 
        catch (T24IOException e) 
        {           
        }*/
        
     
    
}
        catch (Exception e) {
            // TODO Auto-generated catch block
            // Uncomment and replace with appropriate logger
            // LOGGER.error(exception_var, exception_var);
        }
}
}

