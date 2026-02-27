package me.catand.spdnetserver;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailSender {
	// 给用户发送邮件的邮箱
	private String mailSenderAddress;
	// 邮箱的用户名
	private String mailSenderUsername;
	// 邮箱授权码
	private String mailSenderPassword;
	// 发送邮件的服务器地址
	private String mailSenderHost;

	public MailSender(String mailSenderAddress, String mailSenderUsername, String mailSenderPassword, String mailSenderHost) {
		this.mailSenderAddress = mailSenderAddress;
		this.mailSenderUsername = mailSenderUsername;
		this.mailSenderPassword = mailSenderPassword;
		this.mailSenderHost = mailSenderHost;
	}

	public void setMailSender(String mailSenderAddress, String mailSenderUsername, String mailSenderPassword, String mailSenderHost) {
		this.mailSenderAddress = mailSenderAddress;
		this.mailSenderUsername = mailSenderUsername;
		this.mailSenderPassword = mailSenderPassword;
		this.mailSenderHost = mailSenderHost;
	}

	public void sendMail(String recipientMailAddress, String mailSubject, String mailContent) throws Exception {
		// 使用126邮箱时配置
		Properties prop = new Properties();
		// 设置126邮件服务器
		prop.setProperty("mail.host", "smtp.126.com");
		// 邮件发送协议
		prop.setProperty("mail.transport.protocol", "smtp");
		// 需要验证用户名和密码
		prop.setProperty("mail.smtp.auth", "true");
		// 关于QQ邮箱，还要设置SSL加密，其他邮箱不需要
		//MailSSLSocketFactory sf = new MailSSLSocketFactory();
		//sf.setTrustAllHosts(true);
		//prop.put("mail.smtp.ssl.enable", "true");
		//prop.put("mail.smtp.ssl.socketFactory", sf);

		// 创建定义整个邮件程序所需的环境信息的 Session 对象，QQ才有，其他邮箱就不用了
		Session session = Session.getDefaultInstance(prop, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// 发件人邮箱用户名，授权码
				return new PasswordAuthentication(mailSenderUsername, mailSenderPassword);
			}
		});

		// 开启 Session 的 debug 模式，这样就可以查看程序发送 Email 的运行状态
		session.setDebug(false);

		// 通过 session 得到 transport 对象
		Transport ts = session.getTransport();

		// 使用邮箱的用户名和授权码连上邮箱服务器
		ts.connect(mailSenderHost, mailSenderUsername, mailSenderPassword);

		// 创建邮件，写邮件
		MimeMessage message = new MimeMessage(session);
		// 指明邮件的发件人
		message.setFrom(new InternetAddress(mailSenderAddress));
		// 指明邮件的收件人
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientMailAddress));
		// 邮件主题
		message.setSubject(mailSubject);
		// 邮件内容
		message.setContent(mailContent, "text/html;charset=utf-8");

		// 发送邮件
		ts.sendMessage(message, message.getAllRecipients());
		// 释放资源
		ts.close();
	}
}
