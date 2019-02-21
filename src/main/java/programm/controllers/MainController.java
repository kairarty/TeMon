package programm.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import org.controlsfx.control.ToggleSwitch;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;
import programm.OtherMethods;
import programm.utils.EmailSender;
import programm.utils.SNMPCreator;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static programm.Constants.*;

public class MainController {
    private int minTemperature;
    private int currentTemperature;
    private int maxTemperature;
    private static Timer timer;
    private int temperature;
    private static Alert temperatureAlert;
    private static Alert SNMPAlert;
    private static Alert emailAlert;
    @Getter private static Dialog connectionDialog;

    @FXML Label minLB;
    @FXML Label currentLB;
    @FXML Label maxLB;
    @FXML ToggleSwitch notifications;
    @FXML Label startDateLB;

    @FXML
    private void initialize() throws IOException {
        startDateLB.setText(OtherMethods.getStartDate());
        if (USER_PREFS.getBoolean(NOTIFICATIONS_MODE, false)) {  // получить boolean-свойство из реестра. Если такого нет - false
            notifications.setSelected(true);
        } else {
            notifications.setSelected(false);
        }
        SNMPCreator.startListening();
        temperature = SNMPCreator.getCurrentTemperature();

        runTemperatureListener();
    }

    private void runTemperatureListener() {
        //temperature = 22;

        minLB.setText(temperature+"");
        currentLB.setText(temperature+"");
        maxLB.setText(temperature+"");

        minTemperature = 50;    // чтобы после реконнекта минимальная устанавливалась верно
        currentTemperature = temperature;
        maxTemperature = temperature;

        int duration = Integer.parseInt(USER_PREFS.get(DURATION, "10")); // чтение параметра key. Если он пустой - установка по умолчанию def
        int minTemperatureLimit = Integer.parseInt(USER_PREFS.get(MIN_TEMPERATURE_LIMIT, "19"));
        int maxTemperatureLimit = Integer.parseInt(USER_PREFS.get(MAX_TEMPERATURE_LIMIT, "25"));

        /*List<Integer> list = Arrays.asList(24,24,25,25,19,19,22,30,22,22,22,22,22,22,22,22,22,22,22,22,22,
                20,21,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,
                20,21,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22);
        AtomicInteger i = new AtomicInteger();
        i.set(0);*/

        timer = FxTimer.runPeriodically(Duration.ofSeconds(duration), () -> {   // таймер для javaFX. Повторяет одно и
            try {                                                     // то же действие в try {} с промежутком DURATION
                temperature = SNMPCreator.getCurrentTemperature();
                /*temperature = list.get(i.get());
                i.getAndIncrement();*/
                System.out.println(temperature);
                if (temperature == 0) {
                    SNMPCreator.checkConnection(timer);
                    OtherMethods.log(temperature);
                    return;
                }
                if (temperature == 128 || temperature == 129) {
                    showSNMPError();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (minTemperature > temperature) {
                minTemperature = temperature;
                minLB.setText(minTemperature+"");
                if (minTemperature <= minTemperatureLimit) {
                    minLB.setStyle("-fx-text-fill: #383474; -fx-background-color: #889999");
                }
            } else if (maxTemperature < temperature) {
                maxTemperature = temperature;
                maxLB.setText(maxTemperature+"");
                if (maxTemperature >= maxTemperatureLimit) {
                    maxLB.setStyle("-fx-text-fill: #A4494B; -fx-background-color: #889999");
                }
            }

            if (currentTemperature != temperature) {
                currentTemperature = temperature;
                currentLB.setText(currentTemperature+"");
                TrayController.drawTemperatureInTray(temperature, minTemperatureLimit, maxTemperatureLimit);
                System.out.println(temperature);
                if (currentTemperature <= minTemperatureLimit || currentTemperature >= maxTemperatureLimit) {
                    try {
                        OtherMethods.log(currentTemperature);
                        currentLB.setStyle("-fx-text-fill: #A4494B; -fx-background-color: #889999");
                        showTemperatureAlert();
                        if (USER_PREFS.getBoolean(SEND_EMAIL_OPTION, false)) { // если в реестре значение свойства стоит
                            EmailSender.sendEmail(temperature);  // true. Если нет такого свойства вообще - придёт false
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    currentLB.setStyle("-fx-text-fill: green; -fx-background-color: #889999");
                }
            }
        });
    }

    @FXML
    public void reset() throws IOException {    // время появления всплывающей подсказки в css SceneBuider
        int currentTemperature = SNMPCreator.getCurrentTemperature();
        startDateLB.setText(OtherMethods.getStartDate());
        minLB.setText(currentTemperature+"");
        minLB.setStyle("-fx-text-fill: #403d3b; -fx-background-color: #889999");
        currentLB.setStyle("-fx-text-fill: #403d3b; -fx-background-color: #889999");
        maxLB.setText(currentTemperature+"");
        maxLB.setStyle("-fx-text-fill: #403d3b; -fx-background-color: #889999");
    }

    @FXML
    private void showOptions() throws IOException {   // имя метода не привязано к fxml. Наполнение содержимым в RequestInfoController
        timer.stop();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/options.fxml"));
        Parent root = fxmlLoader.load();
        var stage = new Stage();
        stage.getIcons().add(new Image("images/icon.png"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Настройки");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
        // здесь может выполнится метод OptionsController.applySettings() при изменении сохранении
        runTemperatureListener();
    }

    @FXML
    public void showHelp() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        setAlertIcon(alert);
        alert.setTitle("Справка");
        alert.setHeaderText(null);
        alert.getDialogPane().setPrefSize(400, 80);
        alert.setContentText(HELP_TEXT);
        alert.showAndWait();
    }

    @FXML
    public void showAbout() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        setAlertIcon(alert);
        alert.setTitle("О программе");
        alert.setHeaderText(null);
        String version = System.getProperty("java.version");
        alert.setContentText(ABOUT_TEXT + version);
        alert.getDialogPane().setPrefSize(460, 190);
        alert.showAndWait();
    }

    private void showTemperatureAlert() {
        if (USER_PREFS.getBoolean(NOTIFICATIONS_MODE, false)) {
            return;
        }
        var primaryStageFromMain = (Stage) startDateLB.getScene().getWindow();     // получение сцены по принадлежащему ей объекту
        primaryStageFromMain.show();
        if (temperatureAlert == null) {
            temperatureAlert = new Alert(Alert.AlertType.ERROR);
        } else {
            temperatureAlert.show();
            return;
        }

        setAlertIcon(temperatureAlert);
        temperatureAlert.setTitle("Превышение границы температуры");
        temperatureAlert.setHeaderText(null);
        temperatureAlert.setContentText("Внимание! Температура в серверной вышла за пределы установленной границы! " +
                "Примите меры по приведению температуры в допустимый диапазон.");
        temperatureAlert.getDialogPane().setPrefSize(400, 120);
        var stage = (Stage) temperatureAlert.getDialogPane().getScene().getWindow(); // получение сцены alert'a
        stage.setAlwaysOnTop(true);     // появится поверх всех открытых окон
        temperatureAlert.show();  // чтобы showAndWait вызывался из таймера
    }

    public static void showWaitConnection() {
        if (connectionDialog == null) {
            connectionDialog = new Dialog();
        } else {
            connectionDialog.show();
            return;
        }

        connectionDialog.initStyle(StageStyle.UNDECORATED);    // удаление шапки окна

        var err_conn = new ImageView(new Image("images/connErr.png"));

        var lb = new Label("Попытка повторного соединения с температурным датчиком. Убедитесь, что вы подключены к " +
                "сети и ожидайте исчезновения окна.");
        lb.setWrapText(true);   // перенос текста
        ProgressIndicator progressIndicator = new ProgressIndicator();

        var vBox = new VBox();
        vBox.getChildren().addAll(lb, progressIndicator);
        vBox.setAlignment(Pos.TOP_CENTER);

        connectionDialog.setTitle("Ошибка соединения");
        connectionDialog.setHeaderText("Ошибка соединения с температурным датчиком");
        connectionDialog.getDialogPane().setContent(vBox);
        connectionDialog.getDialogPane().setPrefSize(400, 240);
        connectionDialog.setGraphic(err_conn);  // устаовка картинки в header
        setDialogIcon(connectionDialog);
        connectionDialog.show();
    }

    public static void showWrongEmailAlert() {
        if (emailAlert == null) {
            emailAlert = new Alert(Alert.AlertType.ERROR);
        } else {
            if (!emailAlert.isShowing()) {
                emailAlert.show();
            }
            return;
        }
        MainController.setAlertIcon(emailAlert);
        emailAlert.setTitle("Ошибка отправки email");
        emailAlert.setHeaderText(null);
        emailAlert.getDialogPane().setPrefSize(400, 120);
        emailAlert.setContentText("Не удалось отправить письмо! В списке email присутствует некорректный(-ые) email-адрес(-а). Проверьте " +
                "список и удалите некорректный(-ые) email.");
        var stage = (Stage) emailAlert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        Platform.runLater(emailAlert::showAndWait);
    }

    private void showSNMPError() {
        if (SNMPAlert == null) {
            SNMPAlert = new Alert(Alert.AlertType.ERROR);
        } else {
            if (!SNMPAlert.isShowing()) {
                SNMPAlert.show();
            }
            var primaryStageFromMain = (Stage) startDateLB.getScene().getWindow();     // получение сцены по принадлежащему ей объекту
            primaryStageFromMain.show();
            return;
        }

        MainController.setAlertIcon(SNMPAlert);
        SNMPAlert.setTitle("Ошибка ip-адреса");
        SNMPAlert.setHeaderText(null);
        SNMPAlert.getDialogPane().setPrefSize(400, 120);
        SNMPAlert.setContentText("Ip-адрес устройства или OID устройства указан неверно! Проверьте данные значения в " +
                "настройках и исправьте на верные.");
        var stage = (Stage) SNMPAlert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        SNMPAlert.show(); // можно ли showAndWait?
    }

    @FXML private void switchNotifications() {
        if (notifications.isSelected()) {
            USER_PREFS.putBoolean(NOTIFICATIONS_MODE, true);
        } else {
            USER_PREFS.putBoolean(NOTIFICATIONS_MODE, false);
        }
    }

    static void setAlertIcon(Alert alert) {
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("images/icon.png"));
    }

    private static void setDialogIcon(Dialog dialog) {
        ((Stage)dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image("images/icon.png"));
    }
}