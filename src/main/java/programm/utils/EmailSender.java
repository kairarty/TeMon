package programm.utils;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import programm.OtherMethods;
import programm.controllers.MainController;

import static programm.Constants.*;

public class EmailSender {
    public static void sendEmail() {
        if (USER_PREFS.getBoolean(SEND_EMAIL_OPTION, false)) { // если в реестре значение свойства стоит true
            if (OtherMethods.isSendDayRight()) {
                String message = OtherMethods.getDangerousTemperatureList();
                if (message == null) {
                    return;
                }

                // для google email войти в ящик отправителя и включить: https://www.google.com/settings/security/lesssecureapps
                try {
                    Email email = new SimpleEmail();
                    email.setHostName("smtp.googlemail.com");
                    email.setSmtpPort(465);
                    email.setAuthenticator(new DefaultAuthenticator(EMAIL_FROM, PASSWORD));
                    email.setSSLOnConnect(true);

                    email.setFrom(EMAIL_FROM);    // от кого
                    email.setSubject("Температура в серверной");    // тема письма
                    email.setMsg(message);

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
}
