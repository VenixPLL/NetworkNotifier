package org.lightsolutions.notifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.lightsolutions.notifier.analyse.NetworkAnalyzer;
import org.lightsolutions.notifier.configuration.NotifierConfiguration;
import org.lightsolutions.notifier.gui.NotifierFrame;
import org.lightsolutions.notifier.task.PingingTask;
import org.lightsolutions.notifier.utils.SystemTrayUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkNotifier {

    private static NetworkNotifier INSTANCE;

    @Getter
    private final Gson gson = new GsonBuilder().setVersion(1.0).setPrettyPrinting().create();

    private final File configDest = new File("config.json");

    @Getter
    private NotifierConfiguration configuration;

    private ScheduledExecutorService pingService;
    private PingingTask pingingTask;

    @Getter
    private final LinkedHashMap<Long,Long> pingResults = new LinkedHashMap<>(100,.75f,false){
        protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
            return size() > 100;
        }
    };
    private final NotifierFrame notifierFrame = new NotifierFrame();
    private final NetworkAnalyzer networkAnalyzer = new NetworkAnalyzer(this);

    private boolean runningFirstTime = false;

    public void initialize(){
        INSTANCE = this;

        try {
            // Loading configuration
            if (!this.configDest.exists()) {
                this.runningFirstTime = true;
                this.prepareConfiguration();
            } else {
                try(var reader = new FileReader(this.configDest)) {
                    this.configuration = gson.fromJson(reader, NotifierConfiguration.class);
                }
            }
        }catch(IOException exception){
            JOptionPane.showMessageDialog(null,"Failed to load configuration!\n" +
                    exception.getClass().getSimpleName() + " " + exception.getMessage(),"NetworkNotifier", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
            return;
        }

        assert this.configuration != null : "Configuration was not loaded correctly!";
        assert this.pingService == null : "pingService should be null at initialization stage!";
        this.pingService = Executors.newSingleThreadScheduledExecutor();

        this.pingingTask = new PingingTask(this,(address,pingResult) -> {
            // This consumer will return all ping results if pingResult is set to -1, the ping failed (timed out or is unavailable)
            this.pingResults.put(System.currentTimeMillis(),pingResult);
            this.networkAnalyzer.analyzePingData(); // Tick analyzer.
            SwingUtilities.invokeLater(this.notifierFrame::updateGraph);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.pingingTask.setShuttingDown(true);
            this.pingService.shutdown();
        }));

        this.start();
    }

    private void start(){
        assert this.pingService != null : "pingService was not initialized correctly!";
        assert this.pingingTask != null : "pingingTask was not initialized correctly!";
        this.pingService.scheduleAtFixedRate(this.pingingTask,1,this.configuration.getPingInterval(), TimeUnit.SECONDS);

        if(this.runningFirstTime){
            this.notifierFrame.setVisible(true);
            return;
        }

        SystemTrayUtils.sendNotification(TrayIcon.MessageType.INFO,
                "Network status will be checked in the background!");
    }

    private void prepareConfiguration() throws IOException{
        this.configuration = new NotifierConfiguration();

        try(var writer = new FileWriter(this.configDest)){
            this.gson.toJson(this.configuration,writer);
            writer.flush();
            System.out.println("Fresh configuration created!");
        }
    }

    public ActionListener getOpenListener(){
        return e -> this.notifierFrame.setVisible(true);
    }

    public static NetworkNotifier getInstance() {
        return INSTANCE;
    }
}
