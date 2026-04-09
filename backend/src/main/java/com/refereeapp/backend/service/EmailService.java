package com.refereeapp.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Spring сам підставить сюди налаштування з application.properties
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMatchAssignmentEmail(String toEmail, String refereeName, String teamA, String teamB, String dateTime, String location) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            // Вкажи тут ту саму пошту, що і в application.properties
            message.setFrom("jekaolhovskii@gmail.com"); 
            message.setTo(toEmail);
            message.setSubject("🏀 RefMate: Нове призначення на матч!");
            
            // Формуємо красивий текст листа
            String emailText = "Вітаємо, " + refereeName + "!\n\n" +
                    "Вас призначено на новий баскетбольний матч у системі RefMate.\n\n" +
                    "Деталі матчу:\n" +
                    "Команди: " + teamA + " vs " + teamB + "\n" +
                    "Дата і час: " + dateTime.replace("T", " ") + "\n" +
                    "Локація: " + location + "\n\n" +
                    "Будь ласка, зайдіть у свій кабінет (розділ 'Мої призначення'), щоб підтвердити або відхилити свою участь.\n\n" +
                    "З повагою,\nГоловна суддівська колегія";
            
            message.setText(emailText);
            mailSender.send(message);
            
            System.out.println("Лист успішно відправлено на пошту: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Помилка відправки листа: " + e.getMessage());
        }
    }
}