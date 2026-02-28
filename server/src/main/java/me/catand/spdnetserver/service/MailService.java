package me.catand.spdnetserver.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnetserver.config.MailProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Slf4j
@Service
public class MailService {

	@Autowired
	private MailProperties mailProperties;

	private JavaMailSender mailSender;

	private JavaMailSender getMailSender() {
		if (mailSender == null && mailProperties.isEnabled()) {
			JavaMailSenderImpl sender = new JavaMailSenderImpl();
			sender.setHost(mailProperties.getHost());
			sender.setPort(mailProperties.getPort());
			sender.setUsername(mailProperties.getUsername());
			sender.setPassword(mailProperties.getPassword());

			Properties props = sender.getJavaMailProperties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.auth", "true");
			// 126邮箱使用SSL加密
			if (mailProperties.getHost().contains("126.com")) {
				props.put("mail.smtp.ssl.enable", "true");
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				props.put("mail.smtp.socketFactory.port", "465");
			} else {
				props.put("mail.smtp.starttls.enable", "true");
			}
			props.put("mail.debug", "false");

			mailSender = sender;
		}
		return mailSender;
	}

	public boolean isEnabled() {
		return mailProperties.isEnabled();
	}

	public boolean sendVerificationCode(String toEmail, String code) {
		if (!mailProperties.isEnabled()) {
			log.warn("邮件功能未启用，无法发送验证码到 {}", toEmail);
			return false;
		}

		try {
			JavaMailSender sender = getMailSender();
			if (sender == null) {
				log.error("邮件发送器初始化失败");
				return false;
			}

			MimeMessage message = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
			helper.setTo(toEmail);
			helper.setSubject("SPDNet 注册验证码");
			helper.setText(buildEmailContent(code), true);

			sender.send(message);
			log.info("验证码邮件已发送到 {}", toEmail);
			return true;
		} catch (MailException | MessagingException | UnsupportedEncodingException e) {
			log.error("发送验证码邮件失败: {}", e.getMessage());
			return false;
		}
	}

	private String buildEmailContent(String code) {
		return """
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<style>
					body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					.container { max-width: 600px; margin: 0 auto; padding: 20px; }
					.header { background: #4a90d9; color: white; padding: 20px; text-align: center; }
					.content { background: #f9f9f9; padding: 30px; margin: 20px 0; }
					.code { font-size: 32px; font-weight: bold; color: #4a90d9; text-align: center; 
					        padding: 20px; background: white; border-radius: 8px; margin: 20px 0; }
					.footer { text-align: center; color: #666; font-size: 12px; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<h1>SPDNet</h1>
					</div>
					<div class="content">
						<h2>欢迎使用 SPDNet！</h2>
						<p>您的注册验证码是：</p>
						<div class="code">%s</div>
						<p>此验证码将在 %d 分钟后过期，请勿泄露给他人。</p>
						<p>如果您没有进行注册操作，请忽略此邮件。</p>
					</div>
					<div class="footer">
						<p>此邮件由 SPDNet 系统自动发送，请勿回复。</p>
					</div>
				</div>
			</body>
			</html>
			""".formatted(code, mailProperties.getCodeExpireMinutes());
	}
}
