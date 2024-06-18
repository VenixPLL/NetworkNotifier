import org.junit.Test;
import org.lightsolutions.notifier.utils.SocketUtils;

import java.io.IOException;
import java.net.InetAddress;

public class PingTest {

    @Test
    public void testPinger() throws IOException {
        var addr = InetAddress.getByName("8.8.8.8");
        var time = SocketUtils.pingWithSocket(addr);
        System.out.println("Elapsed time: " + time);
        assert time < 0;
    }

}
