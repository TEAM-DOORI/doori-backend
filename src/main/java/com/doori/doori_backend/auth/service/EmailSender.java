package com.doori.doori_backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[DOORI] 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n\n5분 내로 입력해주세요.");
        mailSender.send(message);
    }
}
