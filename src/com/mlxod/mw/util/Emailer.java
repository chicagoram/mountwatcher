// File:         [Emailer.java]
// Created:      [Dec 16, 2009 creation date]
// Last Changed: [Dec 16, 2009 date last changed]
// Author:       [Ram]
//
// This code is copyright (c) 2009 Allied Vaughn
// 
// History:
// 
//

package com.mlxod.mw.util;

/* third party packages */

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.mlxod.mw.util.Emailer;
import com.mlxod.mw.util.Util;

/**
 * The Class Emailer.
 */
public class Emailer {

	/** The Constant logger. */

	/** The mail server config. */
	private static Properties fMailServerConfig = new Properties();

	/**
	 * Simple demonstration of using the javax.mail API.
	 * 
	 * Run from the command line. Please edit the implementation to use correct
	 * email addresses and host name.
	 */

	private static final Logger logger = Logger.getLogger(Emailer.class
			.getName());

	static {
		Emailer.fetchConfig();
	}

	/**
	 * The main method.
	 * 
	 * @param aArguments
	 *            the arguments
	 */
	public static void main(String... aArguments) {

		// the domains of these email addresses should be valid,
		// or the example will fail:
		// Emailer.sendEmail("fromblah@blah.com", "toblah@blah.com",
		// "Testing 1-2-3", "blah blah blah");
		
		String extension = "";
        File f = new File("c:/hot.txt.swp");
        String fname = f.getName();
		int i = fname.lastIndexOf('.');
		if (i > 0) {
		    extension = fname.substring(i+1);
		}
		
		
	}

	// PRIVATE //

	/**
	 * Send email.
	 * 
	 * @param aFromEmailAddr
	 *            the a from email addr
	 * @param aToEmailAddr
	 *            the a to email addr
	 * @param aSubject
	 *            the a subject
	 * @param aBody
	 *            the a body
	 */
	public static void sendEmail(String subject, String aBody) {

		// Here, no Authenticator argument is used (it is null).
		// Authenticators are used to prompt the user for user
		// name and password.
		final Session session = Session.getDefaultInstance(
				Emailer.fMailServerConfig, null);
		final Message message = new MimeMessage(session);
		try {
			// the "from" address may be set in code, or set in the
			// config file under "mail.from" ; here, the latter style is used
			// message.setFrom( new InternetAddress(aFromEmailAddr) );
			message.setFrom(new InternetAddress(Util
					.getProperty("FROM_EMAIL_ADDRESS")));
			message.setRecipients(
					Message.RecipientType.TO,
					InternetAddress.parse(
							Util.getProperty("TO_EMAIL_ADDRESSES"), true));

			message.setSubject(subject);
			message.setText(aBody);
			Transport.send(message);
			Emailer.logger.debug("Emailer:sendEmail() from "
					+ Util.getProperty("FROM_EMAIL_ADDRESS") + " to user "
					+ Util.getProperty("TO_EMAIL_ADDRESSES"));
		} catch (final Exception ex) {
			Emailer.logger.error(" Email sending error " + ex);
		}
	}

	/**
	 * Fetch config.
	 */
	private static void fetchConfig() {

		Emailer.logger.debug("Emailer:fetchConfig() ");

		InputStream input = null;
		try {
			// If possible, one should try to avoid hard-coding a path in this
			// manner; in a web application, one should place such a file in
			// WEB-INF, and access it using ServletContext.getResourceAsStream.
			// Another alternative is Class.getResourceAsStream.
			// This file contains the javax.mail config properties mentioned
			// above.
			input = new FileInputStream(Util.getProperty("EMAIL_CONFIG_DIR"));
			Emailer.fMailServerConfig.load(input);
		} catch (final Exception ex) {
			Emailer.logger
					.error("Cannot open and load mail server properties file."
							+ ex.getMessage());

		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (final IOException e) {
				Emailer.logger
						.error("Cannot close mail server properties file."
								+ e.getMessage());
			}
		}

	}

}
