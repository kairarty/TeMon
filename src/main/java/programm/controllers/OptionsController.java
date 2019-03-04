package programm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import programm.OtherMethods;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static programm.Constants.*;

public class OptionsController {
    // Вкладка Мониторинг
    @FXML TextField durationTF;
    @FXML TextField minTempTF;
    @FXML TextField maxTempTF;
    @FXML TextField ipTF;
    @FXML TextField oidTF;
    @FXML CheckBox autostartCB;
    // Вкладка Отправка на email
    @FXML ToggleSwitch emailSwitcher;
    @FXML Label emailHeaderLB;
    @FXML TextField emailTF;
    @FXML Button addEmailBtn;
    @FXML Button delEmailBtn;
    @FXML ListView<String> emailLV;
    @FXML Label daysHeaderLB;
    @FXML CheckBox mondayCB;
    @FXML CheckBox tuesdayCB;
    @FXML CheckBox wednesdayCB;
    @FXML CheckBox thursdayCB;
    @FXML CheckBox fridayCB;
    @FXML CheckBox saturdayCB;
    @FXML CheckBox sundayCB;
    @FXML Label sendHeaderLB;
    @FXML TextField emailSendDurationTF;

    private ObservableList<String> defaultEmailList = FXCollections.observableArrayList();
    private String selectedEmail;
    private List<CheckBox> checkBoxList;
    private List<Node> allNodes;

    @FXML
    private void initialize() {
        // Вкладка Мониторинг
        setDurationTFFilter();
        setFieldsFilter(minTempTF);
        setFieldsFilter(maxTempTF);
        setFieldsFilter(emailSendDurationTF);
        setIpTFFilter();
        setOidTFFilter();

        setFieldsDefaults();
        // Вкладка Отправка на email
        emailLV.setItems(defaultEmailList);  // далее ListView будет меняться автоматически при изменении ObservableList
        MultipleSelectionModel<String> selectionModel = emailLV.getSelectionModel();    // выбранный элемент списка
        selectionModel.selectedItemProperty().addListener((changed, oldValue, newValue) -> {    // слушатель выбранного элемента ListView
            selectedEmail = newValue;
            delEmailBtn.setDisable(false);
        });
    }

    private void setDurationTFFilter() {
        durationTF.textProperty().addListener((observable, oldValue, newValue) -> {     // ввод только цифр
            if(!newValue.matches("\\d*")) {
                durationTF.setText(oldValue);
            }
        });

        var pattern = Pattern.compile(".{0,3}");    // максимальное число символов в поле - 3
        var formatter = new TextFormatter((UnaryOperator<TextFormatter.Change>) change ->
                pattern.matcher(change.getControlNewText()).matches() ? change : null);
        durationTF.setTextFormatter(formatter);
    }

    private void setFieldsFilter(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) { // если введенный символ в поле не совпадают с регуляркой
                field.setText(oldValue);      // не вводить его
            }
        });

        var pattern = Pattern.compile(".{0,2}");    // максимально 2 символа в поле. Без точки - ошибка
        var formatter = new TextFormatter((UnaryOperator<TextFormatter.Change>) change ->
                pattern.matcher(change.getControlNewText()).matches() ? change : null);
        field.setTextFormatter(formatter);
    }

    private void setIpTFFilter() {
        ipTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("[0-9.]*")) {  // * -  или более символов в скобках
                ipTF.setText(oldValue);
            }
        });

        var pattern = Pattern.compile(".{0,15}");
        var formatter = new TextFormatter((UnaryOperator<TextFormatter.Change>) change ->
                pattern.matcher(change.getControlNewText()).matches() ? change : null);
        ipTF.setTextFormatter(formatter);
    }

    private void setOidTFFilter() {
        oidTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("[0-9.]*")) {
                oidTF.setText(oldValue);
            }
        });

        var pattern = Pattern.compile(".{0,50}");
        var formatter = new TextFormatter((UnaryOperator<TextFormatter.Change>) change ->
                pattern.matcher(change.getControlNewText()).matches() ? change : null);
        oidTF.setTextFormatter(formatter);
    }

    private void setFieldsDefaults() {
        // вкладка Монитор
        durationTF.setText(USER_PREFS.get(DURATION, "10"));     // значения вносятся ещё в MainController. По умолчанию можно вписать любые строки
        minTempTF.setText(USER_PREFS.get(MIN_TEMPERATURE_LIMIT, "19"));
        maxTempTF.setText(USER_PREFS.get(MAX_TEMPERATURE_LIMIT, "25"));
        ipTF.setText(USER_PREFS.get(IP, "10.177.82.7"));
        oidTF.setText(USER_PREFS.get(OID, "1.3.6.1.4.1.9.9.13.1.3.1.3.1006"));
        autostartCB.setSelected(USER_PREFS.getBoolean(AUTOSTART, false));
        // вкладка Почта
        allNodes = Arrays.asList(emailHeaderLB, emailTF, emailLV, addEmailBtn, daysHeaderLB, mondayCB, tuesdayCB, wednesdayCB,
                thursdayCB, fridayCB, saturdayCB, sundayCB, sendHeaderLB, emailSendDurationTF);
        if (USER_PREFS.getBoolean(SEND_EMAIL_OPTION, false)) {
            emailSwitcher.setSelected(true);
            switchEmailFunction();
        }

        defaultEmailList.addAll(
                USER_PREFS.get(EMAIL0, DEFAULT_VALUE),
                USER_PREFS.get(EMAIL1, DEFAULT_VALUE),
                USER_PREFS.get(EMAIL2, DEFAULT_VALUE),
                USER_PREFS.get(EMAIL3, DEFAULT_VALUE),
                USER_PREFS.get(EMAIL4, DEFAULT_VALUE));
        defaultEmailList.removeIf(e -> e.equals(DEFAULT_VALUE));

        checkBoxList = Arrays.asList(mondayCB, tuesdayCB, wednesdayCB, thursdayCB, fridayCB, saturdayCB, sundayCB);
        for (int i = 0; i < checkBoxList.size(); i++) {
            if(!USER_PREFS.get(LIST_OF_DAYS.get(i), DEFAULT_VALUE).equals(DEFAULT_VALUE)) {
                checkBoxList.get(i).setSelected(true);
            }
        }
        emailSendDurationTF.setText(USER_PREFS.get(EMAIL_SEND_DURATION, "15"));
    }

    @FXML
    private void saveSettingsAndCloseOptions() {
        if (saveMonitoring() && saveEmailSettings()) {
            var stage = (Stage) durationTF.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void closeOptions() {
        var stage = (Stage) durationTF.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void addEmail() {
        try {
            if (defaultEmailList.size() == 5) {
                showOptionsAlert(EMAIL_ERROR, "Максимальное количество email, добавленных на одном компьютере, " +
                        "не может быть больше 5!");
                return;
            }
            if (emailTF.getText().trim().isEmpty()) {
                showOptionsAlert(EMAIL_ERROR, "Заполните поле для email и повторите попытку.");
                return;
            }
            if (!emailTF.getText().trim().contains("@")) {
                showOptionsAlert(EMAIL_ERROR, "Вы ввели неправильный email! Повторите попытку.");
                return;
            }
            if (defaultEmailList.contains(emailTF.getText().trim())) {
                showOptionsAlert(EMAIL_ERROR, "Вы ввели уже существующий в списке email! Введите новый email и " +
                        "повторите попытку.");
                return;
            }
            defaultEmailList.add(emailTF.getText().trim());
        } finally {
            emailTF.clear();
        }
    }

    @FXML
    private void delEmail() {
        defaultEmailList.remove(selectedEmail);
        if (defaultEmailList.isEmpty()) {
            delEmailBtn.setDisable(true);
        }
    }

    private boolean saveMonitoring() {
        if (durationTF.getText().isEmpty() || minTempTF.getText().isEmpty() || maxTempTF.getText().isEmpty() ||
        ipTF.getText().isEmpty() || oidTF.getText().isEmpty()) {
            showOptionsAlert("Ошибка применения параметров", "Заполните все поля во вкладке 'Мониторинг'");
            return false;
        }
        if (durationTF.getText().matches("0*")) {
            showOptionsAlert("Ошибка применения параметра", "Период изменения температуры не может быть " +
                    "0! Введите новое значение и повторите попытку.");
            durationTF.clear();
            return false;
        }
        if (emailSendDurationTF.getText().matches("0*")) {
            showOptionsAlert("Ошибка применения параметра", "Период отправки на email не может быть 0! " +
                    "Введите новое значение и повторите попытку.");
            emailSendDurationTF.clear();
            return false;
        }

        USER_PREFS.put(DURATION, durationTF.getText());
        USER_PREFS.put(MIN_TEMPERATURE_LIMIT, minTempTF.getText());
        USER_PREFS.put(MAX_TEMPERATURE_LIMIT, maxTempTF.getText());
        USER_PREFS.put(IP, ipTF.getText());
        USER_PREFS.put(OID, oidTF.getText());

        if (autostartCB.isSelected()) {
            USER_PREFS.putBoolean(AUTOSTART, true);
            OtherMethods.addJarToStartup(true);
        } else {
            USER_PREFS.putBoolean(AUTOSTART, false);
            OtherMethods.addJarToStartup(false);
        }
        return true;
    }

    private boolean saveEmailSettings() {
        var isAnyDaySelected = false;
        for (int i = 0; i < checkBoxList.size(); i++) {
            if(checkBoxList.get(i).isSelected()) {
                isAnyDaySelected = true;
                USER_PREFS.put(LIST_OF_DAYS.get(i), LIST_OF_SHORT_DAYS.get(i));
            } else {
                USER_PREFS.remove(LIST_OF_DAYS.get(i));
            }
        }

        for (int i = 0; i < LIST_OF_EMAIL_NAMES_PREFERENCES.size(); i++) {
            if (i < defaultEmailList.size()) {
                USER_PREFS.put(LIST_OF_EMAIL_NAMES_PREFERENCES.get(i), defaultEmailList.get(i));
            } else {
                USER_PREFS.remove(LIST_OF_EMAIL_NAMES_PREFERENCES.get(i));
            }
        }

        USER_PREFS.put(EMAIL_SEND_DURATION, emailSendDurationTF.getText());

        if (emailSwitcher.isSelected()) {
            USER_PREFS.putBoolean(SEND_EMAIL_OPTION, true);
        } else {
            USER_PREFS.putBoolean(SEND_EMAIL_OPTION, false);
            return true;
        }

        if (defaultEmailList.size() == 0) {
            showOptionsAlert(EMAIL_ERROR, "Добавьте хотя бы 1 email в список для отправки или отключите функцию " +
                    "отправки на email!");
            return false;
        }
        if (!isAnyDaySelected) {
            showOptionsAlert("Ошибка выбора дня", "Выберите хотя бы один день недели или отключите функцию " +
                    "отправки на email");
            return false;
        }
        return true;
    }

    private void showOptionsAlert(String title, String content) {
        var alert = new Alert(Alert.AlertType.WARNING);
        MainController.setAlertIcon(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().setPrefSize(400, 80);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void switchEmailFunction() {
        if (emailSwitcher.isSelected()) {
            allNodes.forEach(e -> e.setDisable(false));
        } else {
            allNodes.forEach(e -> e.setDisable(true));
        }
    }
}