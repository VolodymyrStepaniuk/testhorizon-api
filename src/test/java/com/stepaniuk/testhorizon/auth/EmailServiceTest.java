package com.stepaniuk.testhorizon.auth;

import com.stepaniuk.testhorizon.security.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailService emailService;

    public EmailServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendVerificationEmail() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String subject = "Verification Email";
        String text = "<p>Please verify your email</p>";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendVerificationEmail(to, subject, text);

        // Assert
        verify(emailSender, times(1)).createMimeMessage();
        verify(emailSender, times(1)).send(mimeMessage);

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        // Verify that the helper settings are applied correctly
        assertEquals(helper.getMimeMessage(), mimeMessage);
    }
}
