package com.temenos.t24;

import java.util.List;

import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.category.CategoryRecord;
import com.temenos.t24.api.records.ebjblbreakupparam.EbJblBreakupParamRecord;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;

/*
 * MODULE         : REMITTANCE - FT/TT - EB.JBL.BREAKUP.PARAM
 * VERSION        : EB.JBL.BREAKUP.PARAM
 * EB.API         : GbJblIBreakupCatCheck
 * ATTACHED AS    : VALIDATION ROUTINE
 * Details        : VERSION>FUNDS.TRANSFER,JBL.BREAKUP.DEBIT
 *                  DEBIT.ACCT.NO : Asset – (Neg)
 *                  CREDIT.ACCT.NO :If CATEGORY available in Param then Error!
 *                  
 *                  VERSION>FUNDS.TRANSFER,JBL.BREAKUP.CREDIT
 *                  DEBIT.ACCT.NO :If available in CATEGORY, Param then Error!
 *                  CREDIT.ACCT.NO : Liab(Pos)
 *                  
 *                  VERSION>FUNDS.TRANSFER,JBL.BREAKUP
 *                  DEBIT.ACCT.NO : Asset – (Neg)
 *                  CREDIT.ACCT.NO : Liab(Pos)
 *                  
 *                  VERSION>TELLER,JBL.BREAKUP.DEPOSIT
 *                  ACCOUNT.2(Credit account) : CATEGORY PARAM ASSET TYPE - LIAB.
 *                  
 *                  VERSION>TELLER,JBL.BREAKUP.WITHDRAW
 *                  ACCOUNT.2 (Debit account): CATEGORY PARAM ASSET TYPE - ASSET.
 *                  
 *                  
 * AUTHOR         : MD SHIBLI MOLLAH
 * DATE           : 06-OCT-2024
 * MODIFIED BY    : 
 * DATE           :
 */

public class GbJblIBreakupCatCheck extends RecordLifecycle {
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {

        // Init Variables
        List<TField> paramCategoryList = null;
        Integer paramCategoryListSize = 0;
        String paramCategory = "";
        String paramCatAL = "";
        Boolean categoryMatched = false;
        Boolean creditCategoryMatched = false;
        Boolean debitCategoryMatched = false;

        String myApplication = "";
        String myVersion = "";
        String debitAcctNo = "";
        String creditAcctNo = "";
        String debitAcctCat = "";
        String creditAcctCat = "";

        DataAccess da = new DataAccess(this);
        TellerRecord teller = new TellerRecord(currentRecord);
        FundsTransferRecord ft = new FundsTransferRecord(currentRecord);

        EbJblBreakupParamRecord ebJblBreakupParamRecord = new EbJblBreakupParamRecord(
                da.getRecord("EB.JBL.BREAKUP.PARAM", "SYSTEM"));
        paramCategoryList = ebJblBreakupParamRecord.getCategory();
        paramCategoryListSize = paramCategoryList.size();

        myVersion = transactionContext.getCurrentVersionId();
        myApplication = transactionContext.getApplicationName();
        // Breakup Debit FT Version
        if (myApplication.equals("FUNDS.TRANSFER")) {
            if (myVersion.equals(",JBL.BREAKUP.DEBIT")) {
                debitAcctNo = ft.getDebitAcctNo().getValue();
                creditAcctNo = ft.getCreditAcctNo().getValue();

                debitAcctCat = getAccCat(debitAcctNo);
                creditAcctCat = getAccCat(creditAcctNo);

                for (int i = 0; i < paramCategoryListSize; i++) {
                    paramCategory = paramCategoryList.get(i).getValue();
                    paramCatAL = getCatAl(paramCategory);

                    // DEBIT.ACCT.NO : Asset – (Neg)
                    if (debitAcctCat.equals(paramCategory)) {
                        categoryMatched = true;
                        if (!paramCatAL.equals("A")) {
                            ft.getDebitAcctNo().setError("Debit Account Category Asset Type must be Asset(A)");
                        }
                    }

                    // CREDIT.ACCT.NO :If CATEGORY available in Param then
                    // Error!
                    if (creditAcctCat.equals(paramCategory)) {
                        ft.getCreditAcctNo().setError("Invalid Credit Account");
                    }
                }

                // If category doesn't match, set error
                if (!categoryMatched) {
                    ft.getDebitAcctNo().setError("Debit Account is not a Breakup Account");
                }

            }
            // Breakup Debit FT Version END

            // Breakup Credit FT Version
            else if (myVersion.equals(",JBL.BREAKUP.CREDIT")) {
                debitAcctNo = ft.getDebitAcctNo().getValue();
                creditAcctNo = ft.getCreditAcctNo().getValue();

                debitAcctCat = getAccCat(debitAcctNo);
                creditAcctCat = getAccCat(creditAcctNo);

                for (int i = 0; i < paramCategoryListSize; i++) {
                    paramCategory = paramCategoryList.get(i).getValue();
                    paramCatAL = getCatAl(paramCategory);

                    // CREDIT.ACCT.NO : Liab(Pos)
                    if (creditAcctCat.equals(paramCategory)) {
                        categoryMatched = true;
                        if (!paramCatAL.equals("L")) {
                            ft.getCreditAcctNo().setError("Credit Account Category Asset Type must be Liability(L)");
                        }
                    }

                    // DEBIT.ACCT.NO :If available in CATEGORY, Param then
                    // Error!
                    if (debitAcctCat.equals(paramCategory)) {
                        ft.getDebitAcctNo().setError("Invalid Debit Account");
                    }
                }

                // If category doesn't match, set error
                if (!categoryMatched) {
                    ft.getCreditAcctNo().setError("Credit Account is not a Breakup Account");
                }
            }
            // Breakup Credit FT Version END

            // Breakup MAIN FT Version
            else if (myVersion.equals(",JBL.BREAKUP")) {
                debitAcctNo = ft.getDebitAcctNo().getValue();
                creditAcctNo = ft.getCreditAcctNo().getValue();

                debitAcctCat = getAccCat(debitAcctNo);
                creditAcctCat = getAccCat(creditAcctNo);

                for (int i = 0; i < paramCategoryListSize; i++) {
                    paramCategory = paramCategoryList.get(i).getValue();
                    paramCatAL = getCatAl(paramCategory);

                    // CREDIT.ACCT.NO : Liab(Pos)
                    if (creditAcctCat.equals(paramCategory)) {
                        creditCategoryMatched = true;
                        if (!paramCatAL.equals("L")) {
                            ft.getCreditAcctNo().setError("Credit Account Category Asset Type must be Liability(L)");
                        }
                    }

                    // DEBIT.ACCT.NO : Asset – (Neg)
                    if (debitAcctCat.equals(paramCategory)) {
                        debitCategoryMatched = true;
                        if (!paramCatAL.equals("A")) {
                            ft.getDebitAcctNo().setError("Debit Account Category Asset Type must be Asset(A)");
                        }
                    }
                }
                // If category doesn't match, set error
                if (!creditCategoryMatched) {
                    ft.getCreditAcctNo().setError("Credit Account is not a Breakup Account");
                }
                // If category doesn't match, set error
                if (!debitCategoryMatched) {
                    ft.getDebitAcctNo().setError("Debit Account is not a Breakup Account");
                }
            }

            return ft.getValidationResponse();
        }
        // Breakup MAIN FT Version END

        else if (myApplication.equals("TELLER")) {
            // Breakup TT DEPOSIT Version
            if (myVersion.equals(",JBL.BREAKUP.DEPOSIT")) {
                creditAcctNo = teller.getAccount2().getValue();
                creditAcctCat = getAccCat(creditAcctNo);

                for (int i = 0; i < paramCategoryListSize; i++) {
                    paramCategory = paramCategoryList.get(i).getValue();
                    paramCatAL = getCatAl(paramCategory);

                    // ACCOUNT.2(Credit account) : CATEGORY PARAM ASSET TYPE
                    // LIABILITY(L).
                    if (creditAcctCat.equals(paramCategory)) {
                        categoryMatched = true;

                        if (!paramCatAL.equals("L")) {
                            teller.getAccount2().setError("Account Category Asset Type must be Liability(L)");
                        } else {
                            break;
                        }
                    }
                }
                // If category doesn't match, set error
                if (!categoryMatched) {
                    teller.getAccount2().setError("This Account is not a Breakup Account");
                }
            }
            // Breakup TT DEPOSIT Version END
            // Breakup TT WITHDRAW Version
            else if (myVersion.equals(",JBL.BREAKUP.WITHDRAW")) {
                debitAcctNo = teller.getAccount2().getValue();
                debitAcctCat = getAccCat(debitAcctNo);

                for (int i = 0; i < paramCategoryListSize; i++) {
                    paramCategory = paramCategoryList.get(i).getValue();
                    paramCatAL = getCatAl(paramCategory);

                    // ACCOUNT.2 (Debit account): CATEGORY PARAM ASSET TYPE -
                    // ASSET(A).
                    if (debitAcctCat.equals(paramCategory)) {
                        categoryMatched = true;

                        if (!paramCatAL.equals("A")) {
                            teller.getAccount2().setError("Account Category Asset Type must be ASSET(A)");
                        } else {
                            break;
                        }
                    }
                }
                // If category doesn't match, set error
                if (!categoryMatched) {
                    teller.getAccount2().setError("This Account is not a Breakup Account");
                }
            }
            // Breakup TT WITHDRAW Version END

            return teller.getValidationResponse();
        }

        else {
            return null;
        }
    }

    // get the Account Category from AccountRecord
    public String getAccCat(String acctNo) {
        try {
            DataAccess da = new DataAccess(this);
            AccountRecord accountRecord = new AccountRecord(da.getRecord("ACCOUNT", acctNo));
            String myAcctCat = "";
            myAcctCat = accountRecord.getCategory().getValue();

            return myAcctCat;
        } catch (Exception e) {
        }
        return null;
    }

    // get Asset/Liability (A/L) from Category
    public String getCatAl(String category) {
        try {
            DataAccess da = new DataAccess(this);
            CategoryRecord categoryRecord = new CategoryRecord(da.getRecord("CATEGORY", category));
            String myAL = "";
            myAL = categoryRecord.getLocalRefField("LT.ASSET.TYPE").getValue();

            return myAL;
        } catch (Exception e) {
        }
        return null;
    }

}
