package org.lightsolutions.notifier.analyse;

import lombok.RequiredArgsConstructor;
import org.lightsolutions.notifier.NetworkNotifier;
import org.lightsolutions.notifier.utils.SystemTrayUtils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class NetworkAnalyzer {

    private final NetworkNotifier notifier;

    private byte rapidPingChanges = 0;
    private byte timedOutCounter = 0;
    private byte elevatedPingCounter = 0;
    private int timer = 0;

    public void analyzePingData() {
        var pingResults = this.notifier.getPingResults().values().toArray(new Long[]{});
        var meanPing = this.getMeanPingValue();

        var resultsSize = pingResults.length;
        if(resultsSize < 5) return;

        this.timer++;
        if(this.timer % 50 == 0) {
            System.out.println("rpc/tc timer reset!");
            this.rapidPingChanges = 0;
            this.timedOutCounter = 0;
            this.elevatedPingCounter = 0;

            if(this.timer >= 100) this.timer = 0;
        }

        var prevPing = pingResults[resultsSize - 2];
        var latestPing = pingResults[resultsSize - 1];

        var threshold = prevPing + meanPing;
        if(latestPing > threshold){
            //System.out.println("Ping spike detected ping: " + latestPing + " prev: " + prevPing + " threshold: " + threshold + " -> lag spike count: " + rapidPingChanges);
            this.rapidPingChanges++;
        }

        if(latestPing == -1){
            //System.out.println("Timed out network count -> " + timedOutCounter);
            this.timedOutCounter++;
        }

        if(latestPing> meanPing * 1.4) { // Accounting for error. higher than mean
            //Elevated
            //System.out.println("Elevated ping count: -> " + elevatedPingCounter + " mean " + meanPing + " p: " + latestPing);
            this.elevatedPingCounter++;
        }

        this.sendAlerts();
    }

    private long lastAlertTime = -1L;

    private void sendAlerts(){
       if(canSendAlert()){
           if(this.timedOutCounter >= 5){
               // Network timeout
               SystemTrayUtils.sendNotification(TrayIcon.MessageType.WARNING, "Network is unreachable, dropped packets detected!");
               this.lastAlertTime = System.currentTimeMillis();
               return;
           }

           if(elevatedPingCounter > 15){
               // elevated ping
               SystemTrayUtils.sendNotification(TrayIcon.MessageType.WARNING, "Network is overloaded, elevated ping detected!");
               this.lastAlertTime = System.currentTimeMillis();
               return;
           }

           if(rapidPingChanges > 10){
               // unstable network
               SystemTrayUtils.sendNotification(TrayIcon.MessageType.WARNING, "Network is unstable, rapid ping changes detected!");
               this.lastAlertTime = System.currentTimeMillis();
           }
       }
    }

    private boolean canSendAlert(){
        if(this.lastAlertTime == -1L) {
            this.lastAlertTime = System.currentTimeMillis() - 60000L;
            return true;
        }
        var elapsed = System.currentTimeMillis() - this.lastAlertTime;
        return TimeUnit.MILLISECONDS.toMinutes(elapsed) >= 1;
    }

    private double getMeanPingValue(){
        var results = this.notifier.getPingResults();
        var sum = results.values().stream().reduce(0L, Long::sum);
        return (double) sum / results.size();
    }


}
