package util;

import java.io.IOException;
import java.net.ServerSocket;

public class Networking {
    
    
    private static int tryNetworkPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0);) {
            return socket.getLocalPort();
        }
    }
    
    public static int generateAvailableNetworkPort() {
        int portNumber = -1;

        while (portNumber == -1) {
            try {
                portNumber = Networking.tryNetworkPort();
            } catch (IOException e) {}
        }
        return portNumber;
    }
}
