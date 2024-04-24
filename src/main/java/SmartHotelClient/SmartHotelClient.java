package SmartHotelClient;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.curtaincontrol.*;
import org.example.lightcontrol.ColorRequest;
import org.example.lightcontrol.LightcontrolRespons;
import org.example.lightcontrol.LightcontrolserviceGrpc;
import org.example.lightcontrol.ToggleRequest;
import org.example.temperaturecontrol.TemperaturecontrolRequest;
import org.example.temperaturecontrol.TemperaturecontrolResponse;

public class SmartHotelClient {
    //curtain function
    ManagedChannel curtainChannel = ManagedChannelBuilder.forAddress("localhost", 8081)
            .usePlaintext()
            .build();
    curtaincontrolserviceGrpc.curtaincontrolserviceBlockingStub curtainStub = curtaincontrolserviceGrpc.newBlockingStub(curtainChannel);
    //light function
    ManagedChannel lightchannel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();
    LightcontrolserviceGrpc.LightcontrolserviceBlockingStub lightStub = LightcontrolserviceGrpc.newBlockingStub(lightchannel);

    ManagedChannel temperaturechannel = ManagedChannelBuilder.forAddress("localhost", 8082)
            .usePlaintext()
            .build();
    TemperaturecontrolserviceGrpc.TemperaturecontrolserviceBlockingStub temperatureStub = TemperaturecontrolserviceGrpc.newBlockingStub(temperaturechannel);
    //curtains
    public void toggleCurtain(String command){
        SimpleCurtaincontrolRequest request = SimpleCurtaincontrolRequest.newBuilder()
                .setMessage(command)
                .build();

        SimpleCurtaincontrolResponse response = curtainStub.curtaincontrol(request);

        System.out.println("Response from server: " + response.getMessage());
    }
    //lights
    public void toggleLight(String command){
        ToggleRequest toggleRequest = ToggleRequest.newBuilder()
                .setLightId("light1")
                .setTurnOn(true)
                .build();

        LightcontrolRespons toggleResponse = lightstub.ToggleLights(toggleRequest);
        System.out.println("Toggle Lights Response: " + toggleResponse.getMessage());

        ColorRequest colorRequest = ColorRequest.newBuilder()
                .setLightId("light1")
                .setColor("blue")
                .build();

        LightcontrolRespons colorResponse = lightstub.changeLightColor(colorRequest);
        System.out.println("Change Light Color Response: " + colorResponse.getMessage());

        lightchannel.shutdown();
    }
    }
    //curtain
    public static void main(String[] args) {
        SmartHotelClient client = new SmartHotelClient();
        client.toggleCurtain("opens");
        client.toggleCurtain("close");
    }

public void controlTemperature() {
    // Example usage of client
    TemperaturecontrolRequest request = TemperaturecontrolRequest.newBuilder().setMessage("Control temperature").build();
    TemperaturecontrolResponse response = blockingStub.temperaturecontrolStream(request);
    System.out.println("Response from server: " + response.getMessage());
}

public static void main(String[] args) throws Exception {
    TemperatureControlClient client = new TemperatureControlClient("localhost", 8082);
    try {
        client.controlTemperature();
    } finally {
        client.shutdown();
    }
}

