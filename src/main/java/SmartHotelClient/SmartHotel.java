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
        SimpleCurtaincontrolRequest request = SimpleCurtaincontrolRequest.newBuilder()
                .setMessage(command)
                .build();

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

        StreamObserver<ToggleRequest> requestObserver = lightStub.toggleLights(responseObserver);

        ToggleRequest request = ToggleRequest.newBuilder()
                .setLightId(lightId)
                .setTurnOn(turnOn)
                .build();

        requestObserver.onNext(request);

        // Mark the end of requests
        requestObserver.onCompleted();
    }

    // Method to control temperature
    public void monitorTemperature() {
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
            TemperatureStreamRequest request = TemperatureStreamRequest.newBuilder()
                    .setTemperature(temperature)
                    .build();
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

    public void shutdown() throws InterruptedException {
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
