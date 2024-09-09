package com.temenos.t24;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.complex.eb.servicehook.SynchronousTransactionData;
import com.temenos.t24.api.complex.eb.servicehook.TransactionControl;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblsmsbook.EbJblSmsBookTable;

public class ApSmsServiceRtn extends ServiceLifecycle {
    @Override
    public List<String> getIds(ServiceData serviceData, List<String> controlList) {
        DataAccess da = new DataAccess(this);
        List<String> recordIds = da.selectRecords("", "EB.JBL.SMS.BOOK", "", "WITH SMS.STATUS EQ PENDING");
        return recordIds;
    }

    @Override
    public void updateRecord(String id, ServiceData serviceData, String controlItem,
            TransactionControl transactionControl, List<SynchronousTransactionData> transactionData,
            List<TStructure> records) {

        System.out.println("Processing Record :" + id);
        DataAccess da = new DataAccess(this);

        EbJblSmsBookTable smsBook = new EbJblSmsBookTable(this);
        EbJblSmsBookRecord smsRec = new EbJblSmsBookRecord(da.getRecord("EB.JBL.SMS.BOOK", id));

        String smsContent = "";
        String phone = "";
        String email = "";
        try {
            smsContent = smsRec.getSmsBody().getValue();
            phone = smsRec.getPhone().getValue();
            email = smsRec.getEmail().getValue();
        } catch (Exception e1) {
        }
        System.out.println("Email is: " + email + "\n" + smsContent);

        String host = "smtp.office365.com";
        final String user = "notification@nazihargroup.com";
        final String password = "nitsl#2024NG";

        String to = email;
        String body = smsContent;
        String subject = "JBPLC: Stay Ahead with Our Latest Financial Insights";
        try {
            // Send email
            sendEmail(host, user, password, to, subject, body);
            smsRec.setSmsStatus("SENT");
            System.out.println("Email is sent successfully...");
            try {
                smsBook.write(id, smsRec);
            } catch (T24IOException e) {
            }
        } catch (MessagingException e) {
            System.out.println("Email can not be sent for " + e);
            // e.printStackTrace();
        }
    }
    // END OF MAIN METHOD

    // Method to send email
    private void sendEmail(String host, String user, String password, String to, String subject, String body)
            throws MessagingException {
        // Set up properties for the email session
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); // Enable STARTTLS
        properties.put("mail.smtp.host", host); // SMTP host
        properties.put("mail.smtp.port", "587");

        properties.put("mail.smtp.ssl.trust", host);
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        // properties.put("mail.debug", "true"); // DEBUG is disabled

        // Create a session with an authenticator
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        // Create a new email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        // Send the email
        Transport.send(message);
        System.out.println("Email is sent successfully to " + to);
    }
}
