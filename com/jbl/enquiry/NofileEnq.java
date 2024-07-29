package com.jbl.enquiry;

import java.util.ArrayList;
import java.util.List;

import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.sector.SectorRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author rajon
 *
 */
public class NofileEnq extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        String accid = "";
        if (filterCriteria.get(0).getFieldname().equals("ACCOUNT.NO")) {
            accid = filterCriteria.get(0).getValue();
        }
        DataAccess da = new DataAccess(this);
        AccountRecord accRec = new AccountRecord(da.getRecord("ACCOUNT", accid));
        String cusid = accRec.getCustomer().getValue();
        String secid = accRec.getCategory().getValue();
        List<String> ans = new ArrayList<String>();
        CustomerRecord customerRecord = new CustomerRecord(da.getRecord("CUSTOMER", cusid));
        SectorRecord sectorRecord = new SectorRecord(da.getRecord("SECTOR", secid));
        String cus = cusid;

        cus = cus + "*" + customerRecord.getShortName(0).getValue() + "*" + customerRecord.getMnemonic().getValue()
                + "*" + customerRecord.getSector().getValue() + "*" + customerRecord.getNationality().getValue()
                + "*" + secid + "*" + sectorRecord.getDescription(0).getValue();
        ans.add(cus);
        return ans;
    }

}
