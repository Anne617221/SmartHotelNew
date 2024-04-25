import SmartHotelServer.CurtainControlServer;
import SmartHotelServer.LightControlServer;

import java.io.IOException;

public class Launcher {
    public static void main(String[] args) {
        Thread serverThread = new Thread(()->{
            try{
                final CurtainControlServer curtainControlServer = new CurtainControlServer();
                curtainControlServer.start();
                final LightControlServer lightControlServer = new LightControlServer();
                lightControlServer.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        serverThread.start();
        SmartHotelApp.main(args);
    }
}
