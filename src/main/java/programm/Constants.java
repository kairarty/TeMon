package programm;

import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

public class Constants {
    public static final String ABOUT_TEXT = "ТеМон, Температурный Монитор - ПО для мониторинга температуры в серверной " +
            "комнате.\n"
            + "Организация: РУП \"Минскэнерго\" филиал \"Минские тепловые сети\".\n"
            + "Отдел: Автоматизированных систем управления.\n"
            + "Разработчик: инженер-программист Иванов Артём.\n"
            + "Разработка: 2019 год.\n" +
            "Java: ";

    public static final String HELP_TEXT = "С вопросами по работе программы обращаться к инженеру-программисту по " +
            "телефону: 24-29";

    public static final String EMAIL_ERROR = "Ошибка добавления email";

    public static final String NOTIFICATIONS_MODE = "notifications";

    // параметры
    public static final Preferences USER_PREFS = Preferences.userRoot().node("TeMonProperties");
    public static final String DEFAULT_VALUE = "empty";
    public static final String DURATION = "DURATION";
    public static final String MIN_TEMPERATURE_LIMIT = "MIN_TEMPERATURE_LIMIT";
    public static final String MAX_TEMPERATURE_LIMIT = "MAX_TEMPERATURE_LIMIT";
    public static final String IP = "ip";
    public static final String OID = "oid";
    public static final String AUTOSTART = "autostart";

    // почта
    public static final String SEND_EMAIL_OPTION = "sendEmail";
    public static final String EMAIL_FROM = "myemail";
    public static final String PASSWORD = "mypass";
    public static final String EMAIL0 = "email0";
    public static final String EMAIL1 = "email1";
    public static final String EMAIL2 = "email2";
    public static final String EMAIL3 = "email3";
    public static final String EMAIL4 = "email4";
    public static final List<String> LIST_OF_EMAIL_NAMES_PREFERENCES = Arrays.asList(EMAIL0, EMAIL1, EMAIL2, EMAIL3, EMAIL4);

    public static final List<String> LIST_OF_DAYS = Arrays.asList("monday", "tuesday", "wednesday", "thursday", "friday",
            "saturday", "sunday");
    public static final List<String> LIST_OF_SHORT_DAYS = Arrays.asList("пн", "вт", "ср", "чт", "пт", "сб", "вс");
}
