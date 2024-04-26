import SmartHotelServer.CurtainControlServer;
import SmartHotelServer.LightControlServer;

import java.io.IOException;

public class Launcher {
    public static void main(String[] args) {
        Thread serverThread = new Thread(()->{
            try{
                //Start curtaincontrolserver
                final CurtainControlServer curtainControlServer = new CurtainControlServer();
                curtainControlServer.start();
                //Start lightcontrolserver
                final LightControlServer lightControlServer = new LightControlServer();
                lightControlServer.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        //Run the SmartHotelApp
        serverThread.start();
        SmartHotelApp.main(args);
    }
}
