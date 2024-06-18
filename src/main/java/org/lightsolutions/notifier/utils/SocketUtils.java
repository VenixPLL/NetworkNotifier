package org.lightsolutions.notifier.utils;

import java.io.IOException;
import java.net.*;

public class SocketUtils {

    public static long pingWithSocket(InetAddress address) throws IOException {
        var time = System.currentTimeMillis();
        try(var s = new Socket()){
            s.connect(new InetSocketAddress(address,53),950);
            return System.currentTimeMillis() - time;
        }catch(IOException e){
            return -1;
        }
    }

}
