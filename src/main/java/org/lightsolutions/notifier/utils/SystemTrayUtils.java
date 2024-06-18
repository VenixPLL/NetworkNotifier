package org.lightsolutions.notifier.utils;

import lombok.Getter;
import org.lightsolutions.notifier.NetworkNotifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SystemTrayUtils {

    @Getter
    private static SystemTray systemTray;
    @Getter
    private static Toolkit toolkit;

    @Getter
    private static TrayIcon programIcon;

    /**
     * Initialize all we need for System tray icon and notifications.
     */
    static {
        if(!SystemTray.isSupported()) throw new UnsupportedOperationException("System Tray is not supported on this system!");
        systemTray = SystemTray.getSystemTray();
        toolkit = Toolkit.getDefaultToolkit();

        var image = toolkit.createImage("logo.png");
        programIcon = new TrayIcon(image,"NetworkNotifier", initializeTrayElement());

        programIcon.setImageAutoSize(true);
        programIcon.setToolTip("NetworkNotifier");

        try {
            systemTray.add(programIcon);
        }catch(AWTException exception){
            JOptionPane.showMessageDialog(null,"Failed to initialize systemTray, ensure you are using supported system and java version!\n" +
                    exception.getClass().getSimpleName() + " " + exception.getMessage(),"NetworkNotifier", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    /**
     * Initializing tray popup menu
     * Exit action
     * Open action
     * @return Configured popup menu
     */
    private static PopupMenu initializeTrayElement(){
        var popup = new PopupMenu();

        var defaultItem = new MenuItem("Open");
        defaultItem.addActionListener(NetworkNotifier.getInstance().getOpenListener());
        popup.add(defaultItem);

        defaultItem = new MenuItem("Exit");
        defaultItem.addActionListener(e -> System.exit(0));
        popup.add(defaultItem);

        return popup;
    }

    public static void sendNotification(TrayIcon.MessageType messageType,String content){
        assert programIcon != null : "Program tray icon was not set correctly!";
        programIcon.displayMessage("NetworkNotifier",content,messageType);
    }

}
