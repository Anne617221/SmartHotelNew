package SmartHotelClient;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.curtaincontrol.*;
import org.example.lightcontrol.*;
import org.example.temperaturecontrol.*;

import java.util.concurrent.TimeUnit;

public class SmartHotel {
    private final ManagedChannel curtainChannel;
    private final ManagedChannel lightChannel;
    private final ManagedChannel temperatureChannel;

    private final curtaincontrolserviceGrpc.curtaincontrolserviceBlockingStub curtainStub;
    private final LightcontrolserviceGrpc.LightcontrolserviceStub lightStub;
    private final temperaturecontrolserviceGrpc.temperaturecontrolserviceStub temperatureStub;

    public SmartHotel(String curtainHost, int curtainPort, String lightHost, int lightPort, String temperatureHost, int temperaturePort) {
        //Establish gRPC channels for three different service
        curtainChannel = ManagedChannelBuilder.forAddress(curtainHost, curtainPort)
                .usePlaintext()
                .build();
        curtainStub = curtaincontrolserviceGrpc.newBlockingStub(curtainChannel);

        lightChannel = ManagedChannelBuilder.forAddress(lightHost, lightPort)
                .usePlaintext()
                .build();
        lightStub = LightcontrolserviceGrpc.newStub(lightChannel);

        temperatureChannel = ManagedChannelBuilder.forAddress(temperatureHost, temperaturePort)
                .usePlaintext()
                .build();
        temperatureStub = temperaturecontrolserviceGrpc.newStub(temperatureChannel);
    }

    // Method to toggle curtains
    public void toggleCurtain(String command) {
        //Create request for curtain control
        SimpleCurtaincontrolRequest request = SimpleCurtaincontrolRequest.newBuilder()
                .setMessage(command)
                .build();
        //Send request and print response
        SimpleCurtaincontrolResponse response = curtainStub.curtaincontrol(request);

        System.out.println("Response from curtain server: " + response.getMessage());
    }

    // Method to toggle lights
    public void toggleLights(String lightId, boolean turnOn) {
        StreamObserver<LightcontrolRespons> responseObserver = new StreamObserver<LightcontrolRespons>() {
            @Override
            public void onNext(LightcontrolRespons response) {
                System.out.println("Response: " + response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Request completed.");
            }
        };
        //Create request observer for sending toggle request to light control
        StreamObserver<ToggleRequest> requestObserver = lightStub.toggleLights(responseObserver);
        //Create toggle request for specified light
        ToggleRequest request = ToggleRequest.newBuilder()
                .setLightId(lightId)
                .setTurnOn(turnOn)
                .build();
        //Send toggle request to light control service
        requestObserver.onNext(request);

        // Mark the end of requests
        requestObserver.onCompleted();
    }

    // Method to control temperature
    public void monitorTemperature() {
        //Define observer for handing temperature stream response
        StreamObserver<TemperatureStreamRequest> requestObserver = temperatureStub.temperatureStream(new StreamObserver<TemperatureStreamResponse>() {
            @Override
            public void onNext(TemperatureStreamResponse response) {
                boolean turnOnLight = response.getTurnOnLight();
                System.out.println("Received response from server to turn " + (turnOnLight ? "on" : "off") + " the light.");
                // Perform action based on the response, e.g., turn on/off the light
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Temperature Control Stream Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Temperature Control Stream completed.");
            }
        });

        // Simulate sending temperature data
        for (double temperature = 20; temperature <= 40; temperature += 5) {
            //Create temperature stream request with simulate temperature data
            TemperatureStreamRequest request = TemperatureStreamRequest.newBuilder()
                    .setTemperature(temperature)
                    .build();
            //Send temperature stream request control service
            requestObserver.onNext(request);
            try {
                Thread.sleep(1000); // Simulate sending temperature data every second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Mark the end of requests
        requestObserver.onCompleted();
    }

    //Method to gracefully shut down the client
    public void shutdown() throws InterruptedException {
        //shut down the channel and await termination
        temperatureChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        // Instantiate SmartHotel with appropriate hosts and ports
        SmartHotel smartHotel = new SmartHotel("localhost", 8081, "localhost", 8080, "localhost", 8082);

        // Example usage
        smartHotel.toggleCurtain("open");
        smartHotel.toggleLights("1", true);
        smartHotel.monitorTemperature();

    }
}
