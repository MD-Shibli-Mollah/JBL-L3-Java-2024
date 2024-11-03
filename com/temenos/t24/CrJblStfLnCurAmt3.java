package com.temenos.t24;
/**
 * 
*AA2210988PTH
*Subroutine Description: This routine calculates the Current amount without Interest only for Staff Loan
*Subroutine Type       : Calculation
*Attached To           : AA.SOURCE.CALC.TYPE(STFLNCURBALRTM)
*Attached As           : Attached to AA.SOURCE.CALC.TYPE(STFLNCURBALRTM), which will get the Current Amount without Interest
*Developed by          : #-Mehedi-#
*Incoming Parameters   : arrId  - Arrangement ID
*                        arrProp - Arrangement Property
*                        arrCcy - Arrangement currency
*                        arrRes - Arrangement record
*                        perDat -
*Outgoing Parameters   : balanceAmount
*EB.API                : CrJblStfLnCurAmt
* balanceAmount - Principal amount will be passed
*/
import com.temenos.api.TDate;
import com.temenos.api.TNumber;
import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.aa.activityhook.ArrangementContext;
import com.temenos.t24.api.hook.arrangement.Calculation;
import com.temenos.t24.api.records.aaaccountdetails.AaAccountDetailsRecord;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.aaarrangementactivity.AaArrangementActivityRecord;
import com.temenos.t24.api.records.aaproductcatalog.AaProductCatalogRecord;

public class CrJblStfLnCurAmt3 extends Calculation {

    @Override
    public void calculateSourceBalance(String arrangementId, String propertyId, String currency,
            AaArrangementRecord arrangementRecord, String activityId, TDate activityEffectiveDate,
            TNumber sourceBalance, TDate startDate, AaAccountDetailsRecord accountDetailRecord,
            AaArrangementActivityRecord arrangementActivityRecord, ArrangementContext arrangementContext,
            TStructure productPropertyRecord, AaProductCatalogRecord productRecord, TStructure record,
            AaArrangementActivityRecord masterActivityRecord) {
        
        //List<String> yBalance = new ArrayList<>(Arrays.asList("1200.00"));
        Double yBalance = 1200.00;
           sourceBalance.set(yBalance);    
          
     
           
        //List<String> yDate = new ArrayList<>(Arrays.asList("20220101"));
           String yDate = "20220101";
            startDate.set(yDate);
               
    }    
}
