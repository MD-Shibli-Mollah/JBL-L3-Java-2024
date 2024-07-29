package com.temenos.t24;

import com.temenos.t24.api.complex.atmfrm.messagehook.AtmTransactionContext;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.aaprddesaccount.AaPrdDesAccountRecord;
import com.temenos.t24.api.records.stmtentry.StmtEntryRecord;

/**
 * TODO: Document me!
 *
 * @author nazihar
 *
 */
public class JbplcTest extends Enquiry {
    

    // AaPrdDesAccountRecord accountRecord = new AaPrdDesAccountRecord(record);
    StmtEntryRecord stmtentryRecord = new StmtEntryRecord(this);

}
