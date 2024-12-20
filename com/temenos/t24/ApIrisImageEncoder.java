package com.temenos.t24;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;

/**
 * TODO: EB.API>NOF.ApIrisImageEncoder , STANDARD.SELECTION>
 *       ENQUIRY>IM.API.JBL.BASE64.IMAGES.1.0.0
 * @author
 *
 */
public class ApIrisImageEncoder extends Enquiry{

//    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        String imageId = filterCriteria.get(0).getValue();
       // File file = new File("E:/Env/Common/SecureDocs/Images/photos/" + imageId);
        File file = new File("D:/R22/Temenos/t24home/default/Env/Common/SecureDocs/Images/signatures/" + imageId);
        byte[] imageBytes = new byte[(int) file.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
           
        }finally {
            try {
                fis.read(imageBytes);
                fis.close();
            } catch (IOException e) {
               
            }
        }
        
        String base64ImgStrig = Base64.getEncoder().encodeToString(imageBytes);
        List<String> ans = new ArrayList<String>();
        ans.add(base64ImgStrig + "*" + "");
        return ans;
    }

}
