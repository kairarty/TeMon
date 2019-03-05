package programm;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import programm.controllers.TrayController;

import javax.swing.*;

public class Main extends Application {
    @Override
    public void start(Stage mainStage) throws Exception {
        mainStage.getIcons().add(new Image("images/icon.png"));
        Parent root = FXMLLoader.load(getClass().getResource("/mainWindow.fxml"));
        mainStage.setTitle("ТеМон");
        mainStage.setScene(new Scene(root));
        mainStage.setResizable(false);
        Platform.setImplicitExit(false);    // не завершать программу при закрытии последнего окна
        SwingUtilities.invokeLater(() -> TrayController.createTrayMenu(mainStage));     // запуск трея
    }

    public static void main(String[] args) {
        launch(args);
    }
}
