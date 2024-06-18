package org.lightsolutions.notifier.task;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.lightsolutions.notifier.NetworkNotifier;
import org.lightsolutions.notifier.utils.SocketUtils;
import org.lightsolutions.notifier.utils.SystemTrayUtils;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class PingingTask implements Runnable{

    private final NetworkNotifier notifier;
    private final BiConsumer<InetAddress,Long> pingResultCallback;

    @Setter
    private boolean shuttingDown = false;

    private long lastTickTime = System.currentTimeMillis();
    private byte endpointCheckIndex = 0;

    @Override
    public void run() {
        if(shuttingDown) return; // Halt execution when flag is set
        if(wasSleeping()) {
            SystemTrayUtils.sendNotification(TrayIcon.MessageType.INFO,"Monitoring resumed after system halt");
            NetworkNotifier.getInstance().getPingResults().clear();
        }

        this.lastTickTime = System.currentTimeMillis();

        // Test ping logic.
        var availableEndpoints = this.notifier.getConfiguration().getPingEndpoints();
        if(this.endpointCheckIndex >= availableEndpoints.length) this.endpointCheckIndex = 0;

        var currentEndpoint = availableEndpoints[this.endpointCheckIndex];

        try {
            var address = InetAddress.getByName(currentEndpoint);
            var pingMs = SocketUtils.pingWithSocket(address);
            this.pingResultCallback.accept(address,pingMs);
        }catch(IOException exception){
            // Failed to ping. Network unreachable? classify as timeout or unavailable
            this.pingResultCallback.accept(null,-1L);
        }

        this.endpointCheckIndex++; // Increment index, don't always check in the same endpoint.
    }

    /**
     * Used to check if user pc was put to sleep, or the program halted execution
     * This is used to prevent alert spam when the pc is put into sleep, as there are time differences
     * After sleep program will erase all previous stats/pings and start from nothing.
     * @return true if it was sleeping.
     */
    private boolean wasSleeping(){
        var time = (System.currentTimeMillis() - this.lastTickTime) / 1000L;
        return time > this.notifier.getConfiguration().getPingInterval() * 10L;
    }
}
