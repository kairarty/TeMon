package programm.utils;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import programm.OtherMethods;
import programm.controllers.MainController;

import static programm.Constants.*;

public class EmailSender {
    public static void sendEmail(int temperature) {
        if (OtherMethods.isSendDayRight()) {
            // для google email войти в ящик отправителя и включить: https://www.google.com/settings/security/lesssecureapps
            try {
                Email email = new SimpleEmail();
                email.setHostName("smtp.googlemail.com");
                email.setSmtpPort(465);
                email.setAuthenticator(new DefaultAuthenticator(EMAIL_FROM, PASSWORD));
                email.setSSLOnConnect(true);

                email.setFrom(EMAIL_FROM);    // от кого
                email.setSubject("Температура в серверной");    // тема письма
                email.setMsg("Температура серверной превысила допустимую границу и составила " + temperature + " гр.! " +
                        "Примите меры по приведению температуры в допустимый диапазон.");

                for (String em : OtherMethods.getEmailsList()) {
                    email.addTo(em);
                }
                email.send();
            } catch (EmailException e) {
                MainController.showWrongEmailAlert();
                e.printStackTrace();
            }
        }
    }
}