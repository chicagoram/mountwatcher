package com.mlxod.mw.util;

//File Name SendHTMLEmail.java

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendHTMLEmail {
	public static void main(String[] args) {

		// Recipient's email ID needs to be mentioned.
		String to = "ram@3susa.com,chicagoram@gmail.com,ram_1726@yahoo.com";

		// Sender's email ID needs to be mentioned
		String from = "ram_1726@yahoo.com";

		// Assuming you are sending email from localhost
		String host = "10.8.13.60";

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			message.setRecipients(Message.RecipientType.CC,
					InternetAddress.parse(to, true));
			// Set To: header field of the header.
			// message.addRecipient(Message.RecipientType.TO,
			// new InternetAddress(to));

			// Set Subject: header field
			message.setSubject("This is the Subject Line!");

			// Send the actual HTML message, as big as you like
			message.setContent("<h1>This is actual message</h1>", "text/html");

			// Send message
			Transport.send(message);
			System.out.println("Sent message successfully....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}

	}
}