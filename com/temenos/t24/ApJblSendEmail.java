package com.temenos.t24;

/**
 * TODO: Send email from java application.
 * 
 * DOC: https://www.javatpoint.com/example-of-sending-email-using-java-mail-api
 *      1) Get the session object
           The javax.mail.Session class provides two methods to get the object of session, 
           Session.getDefaultInstance() method and Session.getInstance() method. 
           You can use any method to get the session object.
           
        2) Compose the message
           The javax.mail.Message class provides methods to compose the message. 
           But it is an abstract class so its subclass javax.mail.internet.MimeMessage class is mostly used.
           
        3) Send the message
           The javax.mail.Transport class provides method to send the message.
 * 
 * 
 * SMTP server:
 * SMTP server name: smtp.office365.com
   SMTP port: 587
   user: notification@nazihargroup.com
   password: nitsl#2024NG
   if Port 587 does not work (recommended) or port 25
 *       
 *       
 *
 * @author MD Shibli Mollah
 *
 */

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class ApJblSendEmail {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String host = "smtp.office365.com";
        final String user = "notification@nazihargroup.com";
        final String password = "TEST.PASSWORD";

        String to = "md.ta.shibli@gmail.com";

        // Get the session object
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); // Enable STARTTLS
        properties.put("mail.smtp.host", host); // SMTP host
        // properties.put("mail.smtp.port", "25"); // Typically, 587 is used for
        // STARTTLS
        // javax.mail.MessagingException: Could not convert socket to TLS;
        properties.put("mail.smtp.port", "587");

        // If SSL is used on port 465, set the mail.smtp.ssl.enable property
        // instead of mail.smtp.starttls.enable
        // properties.put("mail.smtp.ssl.enable", "true"); // Enable SSL
        // properties.put("mail.smtp.port", "465"); // SSL usually uses port 465

        properties.put("mail.smtp.ssl.trust", host);
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        // properties.put("mail.debug", "true");

        Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        // Compose the message
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Intro: Nazihar IT Solution Limited");
            message.setText("Welcome to NITSL");

            // send the message
            Transport.send(message);

            System.out.println("message sent successfully...");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
