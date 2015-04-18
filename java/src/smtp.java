

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import java.util.Properties;


public class smtp {

	
	Properties gmailprops = null;
	Session gmailsession = null;
	
	public smtp(){
		this.gmailprops = new Properties();
		this.gmailprops.put("mail.smtp.host", "smtp.gmail.com");
		this.gmailprops.put("mail.smtp.socketFactory.port", "465");
		this.gmailprops.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		this.gmailprops.put("mail.smtp.auth", "true");
		this.gmailprops.put("mail.smtp.port", "465");
	
		}
	
	// if gmailsession not instantiated set it here:
	public boolean setsession(){
		//TODO: check authenticator. props, etc?
		//if the session is not setup then set it up now:
		if (this.gmailsession == null){
			this.gmailsession = Session.getInstance(
					this.gmailprops,
					new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication("blindmurray@gmail.com","murraymaso");
								}
				});
			}
		//if it is setup or just got set up above return true:
		if (this.gmailsession != null){ return true;}
		//else
		return false;
		}
	
	public boolean close(){	this.gmailsession = null; return true;}

	
	
	public boolean sendmail(String to, String body){
		
		if (this.gmailsession == null){ this.setsession();}
		if (to == null || to.length() <1 ){to = "blindmurray@gmail.com";}
		if (body == null || body.length() <1 ){body = "testing or blank mail...";}

		try {
			//MimeMessage defaults to content-type 'text/plain'm, using text/html here below
			Message email = new MimeMessage(gmailsession);
			email.setFrom(new InternetAddress("info@dsmpubs.com"));
			email.setRecipients(Message.RecipientType.TO,	InternetAddress.parse(to));
			email.setSubject("DSM Articles Alert");
			email.setContent(body, "text/html");    		// html 
			//email.setText(body);							// plain text
			try{Transport.send(email);}
			catch (SendFailedException ex){ex.printStackTrace(); return false;}
 		} catch (MessagingException me) {me.printStackTrace(); return false;}

		System.out.println("Alert sent to "+ to);
		return true;
		}
	
	
}
