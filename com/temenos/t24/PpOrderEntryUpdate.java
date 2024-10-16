package com.temenos.t24;

import java.util.List;

import com.temenos.t24.api.complex.pp.paymentlifecyclehook.AccountDetails;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.AccountLocation;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.AccountLocationContext;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.ClearingContext;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.CreditParty;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.CreditTransaction;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.DebitParty;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.DebitTransaction;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.InputChannel;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.Response;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.TransactionContext;
import com.temenos.t24.api.complex.pp.paymentlifecyclehook.ValidationResponse;
import com.temenos.t24.api.hook.payments.PaymentLifecycle;

/**
 * TODO: Document me!
 *
 *@author MD Shibli Mollah
 *
 */
public class PpOrderEntryUpdate extends PaymentLifecycle {

    @Override
    public String getBulkPaymentReference(String clearingTransactionType, String companyBic, String outgoingMessageType,
            TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        return super.getBulkPaymentReference(clearingTransactionType, companyBic, outgoingMessageType, transactionContext);
    }

    @Override
    public AccountLocation getAccountLocation(AccountDetails accountDetails,
            AccountLocationContext accountLocationContext) {
        // TODO Auto-generated method stub
        return super.getAccountLocation(accountDetails, accountLocationContext);
    }

    @Override
    public Response validatePayment(TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        return super.validatePayment(transactionContext);
    }

    @Override
    public Response validateCreditParty(TransactionContext transactionContext, CreditTransaction creditTransaction) {
        // TODO Auto-generated method stub
        return super.validateCreditParty(transactionContext, creditTransaction);
    }

    @Override
    public Response validateDebitParty(TransactionContext transactionContext, DebitTransaction debitTransaction) {
        // TODO Auto-generated method stub
        return super.validateDebitParty(transactionContext, debitTransaction);
    }

    @Override
    public ValidationResponse validatePaymentForClearing(InputChannel inputChannel, List<CreditParty> creditParty,
            List<DebitParty> debitParty, ClearingContext clearingContext) {
        // TODO Auto-generated method stub
        return super.validatePaymentForClearing(inputChannel, creditParty, debitParty, clearingContext);
    }

}
