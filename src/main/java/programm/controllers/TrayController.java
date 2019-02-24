package programm.controllers;

import javafx.application.Platform;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static programm.Constants.*;
public class TrayController {
    private static TrayIcon trayIcon;

    public static void createTrayMenu(Stage inputPrimaryStage) {
        PopupMenu popup = new PopupMenu();  // создание меню на иконке в трее при ПКМ по ней
        MenuItem openItem = new MenuItem("Показать окно");  // название пункта меню
        openItem.addActionListener(event -> Platform.runLater(() -> showMainStage(inputPrimaryStage)));
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.addActionListener(event -> System.exit(0));

        popup.add(openItem);
        popup.addSeparator();   // разделительная линия между элементами
        popup.add(exitItem);

        setTrayIcon(popup, inputPrimaryStage);
}

    private static void setTrayIcon(PopupMenu popup, Stage inputMainStage) {
        SystemTray tray = SystemTray.getSystemTray();
        try {
            BufferedImage im = ImageIO.read(TrayController.class.getResourceAsStream("/images/ig.png"));
            Graphics2D graphics2D = im.createGraphics();
            graphics2D.setFont(new Font("default", Font.BOLD, 14));
            int temperature = MainController.getTemperature();
            if (temperature <= Integer.parseInt(USER_PREFS.get(MIN_TEMPERATURE_LIMIT, "19"))) {
                graphics2D.setColor(Color.BLUE);
            }
            if (temperature >= Integer.parseInt(USER_PREFS.get(MAX_TEMPERATURE_LIMIT, "25"))) {
                graphics2D.setColor(Color.RED);
            }
            if (temperature > Integer.parseInt(USER_PREFS.get(MIN_TEMPERATURE_LIMIT, "19")) && temperature <
                    Integer.parseInt(USER_PREFS.get(MAX_TEMPERATURE_LIMIT, "25"))) {
                graphics2D.setColor(new Color(50,205,50));  // не все цвета есть в константах, поэтому можно вызвать цвет RGB
            }
            graphics2D.drawString(String.valueOf(temperature), 1, 13);
            graphics2D.dispose();   // освобождение ресурсов 2d гарфики
            var baos = new ByteArrayOutputStream();
            ImageIO.write(im, "png", baos);
            byte[] bytesArray = baos.toByteArray();
            var tempImageIcon = new ImageIcon(bytesArray);
            Image finalImage = tempImageIcon.getImage();


            trayIcon = new TrayIcon(finalImage, "ТеМон", popup);    // надпись при наведении мыши
            trayIcon.setImageAutoSize(true); // автоматически изменяет размер
            trayIcon.addActionListener(event -> Platform.runLater(() -> showMainStage(inputMainStage)));
            trayIcon.setPopupMenu(popup);
            tray.add(trayIcon);
        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
    }

    static void drawTemperatureInTray(int temperature, int minTemperatureLimit, int maxTemperatureLimit) {
        try {
            BufferedImage im = ImageIO.read(TrayController.class.getResourceAsStream("/images/ig.png"));
            Graphics2D graphics2D = im.createGraphics();
            graphics2D.setFont(new Font("default", Font.BOLD, 14));
            if (temperature <= minTemperatureLimit) {
                graphics2D.setColor(Color.BLUE);
            }
            if (temperature >= maxTemperatureLimit) {
                graphics2D.setColor(Color.RED);
            }
            if (temperature > minTemperatureLimit && temperature < maxTemperatureLimit) {
                graphics2D.setColor(new Color(50,205,50));  // не все цвета есть в константах, поэтому можно вызвать цвет RGB
            }

            graphics2D.drawString(String.valueOf(temperature), 1, 13);
            graphics2D.dispose();   // освобождение ресурсов 2d гарфики
            var baos = new ByteArrayOutputStream();
            ImageIO.write(im, "png", baos);
            byte[] bytesArray = baos.toByteArray();
            var tempImageIcon = new ImageIcon(bytesArray);
            Image finalImage = tempImageIcon.getImage();
            trayIcon.setImage(finalImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showMainStage(Stage stage) {
        if (stage != null) {
            stage.show();
            stage.toFront();
        }
    }
}
