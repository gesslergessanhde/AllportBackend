package com.allport.service;

import com.allport.model.Resultado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarReporteAxiologico(Resultado aspirante, String correoDestino) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String cuerpoHtml = "<div style='font-family: sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px;'>"
                + "<h2 style='color: #4f46e5; text-align: center;'>🏛️ Sistema Evaluativo Allport</h2>"
                + "<p><strong>Estimado(a) " + aspirante.getNombre() + ",</strong></p>"
                + "<p>A continuación, compartimos el dictamen formal de su perfil axiológico:</p>"
                + "<ul>"
                + "<li><strong>Teorético:</strong> " + aspirante.getTotal_teorico() + " pts</li>"
                + "<li><strong>Económico:</strong> " + aspirante.getTotal_economico() + " pts</li>"
                + "<li><strong>Estético:</strong> " + aspirante.getTotal_estetico() + " pts</li>"
                + "<li><strong>Social:</strong> " + aspirante.getTotal_social() + " pts</li>"
                + "<li><strong>Político:</strong> " + aspirante.getTotal_politico() + " pts</li>"
                + "<li><strong>Religioso:</strong> " + aspirante.getTotal_religioso() + " pts</li>"
                + "</ul>"
                + "<div style='background-color: #f8fafc; padding: 15px; border-left: 4px solid #10b981;'>"
                + "<strong>Anotaciones Clínicas:</strong><p>\"" + (aspirante.getNotas_entrevista() != null ? aspirante.getNotas_entrevista() : "Sin observaciones.") + "\"</p>"
                + "</div></div>";

        helper.setFrom("Admisiones Universitarias <diseniowebproyecto@gmail.com>");
        helper.setTo(correoDestino);
        helper.setSubject("Resultados Oficiales: Test de Allport - " + aspirante.getNombre());
        helper.setText(cuerpoHtml, true);

        mailSender.send(message);
    }
}