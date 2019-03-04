package programm;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static programm.Constants.*;
// !!! Импортированы константы!!!

public class OtherMethods {
    public static String getStartDate() {
        var dateMask = DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm");
        return LocalDateTime.now().format(dateMask);
    }

    public static Boolean isSendDayRight() {
        var todayDate = LocalDate.now();
        DayOfWeek dayOfWeek = todayDate.getDayOfWeek();
        Locale localeRu = new Locale("ru", "RU");
        String todayDayOdWeek = dayOfWeek.getDisplayName(TextStyle.SHORT, localeRu);
        int dayOfWeekNumber = LocalDate.now().getDayOfWeek().getValue();
        return USER_PREFS.get(LIST_OF_DAYS.get(dayOfWeekNumber - 1), DEFAULT_VALUE).equals(todayDayOdWeek);
    }

    public static void log(int temperature) throws IOException {
        String filename = LocalDate.now().format(DATE_FORMATTER);
        String path = new File("").getAbsolutePath();
        var logFile = new File(path + "/TeMon logs/" + filename + ".txt");

        var dateMask = DateTimeFormatter.ofPattern("HH:mm");
        String currentTime = LocalDateTime.now().format(dateMask);

        if (temperature == 0) {
            FileUtils.writeStringToFile(logFile, currentTime + " Ошибка связи с датчиком. Восстановление соединения.\n", "UTF-8", true);
            return;
        }
        FileUtils.writeStringToFile(logFile, currentTime + " превышение температуры: " + temperature + " град.\n", "UTF-8", true);
    }

    public static void addJarToStartup(boolean startFlag) {
        File jar = new File(System.getProperty("java.class.path")).getAbsoluteFile();
        String jarname = jar.getName();
        String absolutePath = jar.getAbsolutePath();
        String command;
        if (startFlag) {
            command = "cmd /C reg add HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /v "
                    + jarname + " /t REG_SZ /d \"" + absolutePath + "\" /f";    // добавить в автозагрузку запущенный jar
        } else {                                                                // с того места, откуда он запустился
            command = "cmd /C reg delete HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /v "
                    + jarname + " /f\r\n";      // далить из автозагрузки ранее добавленный jar
        }
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static List<String> getEmailsList() {
        List<String> emailList = new java.util.ArrayList<>(java.util.Arrays.asList(
                USER_PREFS.get(EMAIL0, DEFAULT_VALUE),
                USER_PREFS.get(EMAIL1, DEFAULT_VALUE),
                USER_PREFS.get(EMAIL2, DEFAULT_VALUE),
                USER_PREFS.get(EMAIL3, DEFAULT_VALUE),
                USER_PREFS.get(EMAIL4, DEFAULT_VALUE)
        ));

        emailList.removeIf(e -> e.equals(DEFAULT_VALUE));
        return emailList;
    }

    public static String getDangerousTemperatureList() {
        String filename = LocalDate.now().format(DATE_FORMATTER);
        String path = new File("").getAbsolutePath();
        String fileName = path + "/TeMon logs/" + filename + ".txt";
        Path logfile = Paths.get(fileName);

        if (!Files.exists(logfile)) {
            System.out.println("no");
            return null;
        }

        List<String> rightTimeList = new ArrayList<>();
        var startTime = LocalTime.now().minusMinutes(30);
        try {
            List<String> logList = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8).collect(Collectors.toList());
            for (String s : logList) {
                if (LocalTime.parse(s.substring(0, 5)).isAfter(startTime)) {
                    rightTimeList.add(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (rightTimeList.isEmpty()) {
            System.out.println("пустой");
            return null;
        }
        return String.join("\n", rightTimeList);
    }

    // сделать логи через Files.write(Paths.get("./output.txt"), "Information string herer".getBytes());
}
