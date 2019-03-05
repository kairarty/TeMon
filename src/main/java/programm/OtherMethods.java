package program.controllerMethods;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import program.model.Readings;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OtherMethods {     // класс с методами, обслуживающими класс DBUse для упрощения его понимания

    <T> String getAverageParameter(List<T> inputParametersList ) {  // приходят List<Double> и List<Integer>
        double averageParameter;
        if (inputParametersList.get(0) instanceof Double) {     // для List<Double>
            averageParameter = inputParametersList.stream().collect(Collectors.averagingDouble(r -> (double) r));
            if(averageParameter == (int) averageParameter) {
                return String.valueOf((int) averageParameter);
            }
            return String.format("%8.1f", averageParameter);
        }

        averageParameter = inputParametersList.stream().collect(Collectors.averagingInt(s -> (int) s)); // для List<Integer>
        if (averageParameter == (int) averageParameter) {
            return String.valueOf((int) averageParameter);
        }
        return String.format("%8.1f", averageParameter);
    }

    public String getDBDateFormat(String selectedDate) {
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        var formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(selectedDate, formatter).format(formatter2);
    }

    public void log(String callClassName, String username, String action) {
        var datemask = DateTimeFormatter.ofPattern("d.M.YY");
        String currentDate = LocalDateTime.now().format(datemask);
        File logFile = new File("C:/logsMTS/log_" + currentDate + ".txt");
        var timeMask = DateTimeFormatter.ofPattern("HH:mm");
        String currentTime = LocalDateTime.now().format(timeMask);
        try {
            FileUtils.writeStringToFile(logFile, currentTime + ". " + callClassName + ", " + username  +  ": " +
                    action + "\n", "UTF-8", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<Double> getRealReadingsList(List <Double> inputDeviceReadingsList) {  // вычитает от текущих поаказаний
        List<Double> readings_inList = new ArrayList<>();       // прошломесячные, чтобы получить чистый расход за каждый месяц
        var dFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        for (int i = 1; i < inputDeviceReadingsList.size() ; i++) {
            readings_inList.add(Double.valueOf(dFormat.format(inputDeviceReadingsList.get(i-1) -
                    inputDeviceReadingsList.get(i))));  // чтобы шло округление до 2-х десятичных знаков
        }
        Collections.reverse(readings_inList);
        return readings_inList;
    }

    List<Integer> convertWorktimeList(List <Integer> inputDeviceReadingsList) {
        List<Integer> readings_inList = new ArrayList<>();
        for (int i = 1; i < inputDeviceReadingsList.size() ; i++) {
            readings_inList.add(inputDeviceReadingsList.get(i-1) - inputDeviceReadingsList.get(i));
        }
        Collections.reverse(readings_inList);
        return readings_inList;
    }

    public List<String> getDatesForSelect() {
        var todayDate = LocalDate.now();
        var todayYear = todayDate.getYear();
        var todayMonth = todayDate.getMonth();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.YYYY");
        List<String> totalDates;
        if (todayDate.getDayOfMonth() >= 3) {
            var startDate = LocalDate.of(todayYear, todayMonth, 21);
            var endDate = todayDate.plusDays(1);    // день добавляется, потому что datesUntil(endDate) не учитывает
            totalDates =  startDate.datesUntil(endDate)     // последний день
                    .map(e -> e.format(dateFormatter))
                    .collect(Collectors.toList());
        } else {
            var startDate = LocalDate.of(todayYear, todayDate.minusMonths(1).getMonth(), 21);
            var endDate = todayDate.plusDays(1);
            totalDates =  startDate.datesUntil(endDate)
                    .map(e -> e.format(dateFormatter))
                    .collect(Collectors.toList());
        }
        return totalDates;
    }

    public void checkWorktime(String previousReceiptDate, int previousWorktime, int todayWorktime) {
        LocalDate startDate = LocalDate.parse(previousReceiptDate);
        System.out.println("Прошлое время: " + previousWorktime);
        float readingsDays = (float)(todayWorktime-previousWorktime)/24;
        System.out.println("Прошло дней по показаниям: " + readingsDays);
        int daysDifference = (int)(startDate.until(LocalDate.now(), ChronoUnit.DAYS));
        System.out.println("Прошло дней по пофакту: " + daysDifference);
        float finalDifference = readingsDays - daysDifference;
        System.out.println("Итоговая разница " + finalDifference);

        // если нет прошлых дат???
        // если узел без времени??
        /*if (startDate.until(LocalDate.now(), ChronoUnit.DAYS)) {

        }*/


        // адаптировать мобильную версию, сравнив со старой
        // убрать в таблице колонку worktime_out, убрать в методе saveRequest id_status и в объекте newRequest тоже, т. к. по
        // умолчанию добавляется 3
    }
}
