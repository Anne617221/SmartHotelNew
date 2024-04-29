package SmartHotelClient;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;

import org.example.curtaincontrol.*;
import org.example.lightcontrol.*;
import org.example.temperaturecontrol.*;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SmartHotel {
    private final ManagedChannel curtainChannel;
    private final ManagedChannel lightChannel;
    private final ManagedChannel temperatureChannel;

    private final curtaincontrolserviceGrpc.curtaincontrolserviceBlockingStub curtainStub;
    private final LightControlServiceGrpc.LightControlServiceStub lightStub;
    private final TemperatureControlServiceGrpc.TemperatureControlServiceStub temperatureStub;

    public SmartHotel(String curtainHost, int curtainPort, String lightHost, int lightPort, String temperatureHost,
            int temperaturePort) {
        // Establish gRPC channels for three different service
        curtainChannel = ManagedChannelBuilder.forAddress(curtainHost, curtainPort)
                .usePlaintext()
                .build();
        curtainStub = curtaincontrolserviceGrpc.newBlockingStub(curtainChannel);

        lightChannel = ManagedChannelBuilder.forAddress(lightHost, lightPort)
                .usePlaintext()
                .build();
        lightStub = LightControlServiceGrpc.newStub(lightChannel);

        temperatureChannel = ManagedChannelBuilder.forAddress(temperatureHost, temperaturePort)
                .usePlaintext()
                .build();
        temperatureStub = TemperatureControlServiceGrpc.newStub(temperatureChannel);
    }

    // Method to toggle curtains
    public void toggleCurtain(String command) {
        // Create request for curtain control
        SimpleCurtaincontrolRequest request = SimpleCurtaincontrolRequest.newBuilder()
                .setMessage(command)
                .build();
        // Send request and print response
        SimpleCurtaincontrolResponse response = curtainStub.curtaincontrol(request);

        System.out.println("Response from curtain server: " + response.getMessage());
    }

    public void StreamTemperatureRequest() {
        // Create a StreamObserver to handle streamed data
        StreamObserver<TemperatureData> responseObserver = new StreamObserver<TemperatureData>() {
            @Override
            public void onNext(TemperatureData temperatureData) {
                System.out.println(temperatureData.getTemperature());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                // Handle completion if needed
            }
        };
        temperatureStub.temperatureStream(StreamTemperatureRequest.newBuilder().build(), responseObserver);
    }

    public void temperatureChannelShutdown() {
        try {
            temperatureChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Error while shutting down client: " + e.getMessage());
        }
    }

    public void realTimeControl() {
        StreamObserver<LightControlResponse> responseObserver = new StreamObserver<LightControlResponse>() {

            @Override
            public void onNext(LightControlResponse response) {
                System.out.println("Received response from server: " + response.getStatus());
            }

            @Override
            public void onError(Throwable t) {
                // Handle errors
                System.err.println("Error in realTimeControl: " + t);
            }

            @Override
            public void onCompleted() {
                // Server has completed sending responses
                System.out.println("Real-time control completed");
            }

        };

        // Start the stream by sending requests to the server
        StreamObserver<LightControlRequest> requestObserver = lightStub.realTimeControl(responseObserver);

        // Send some sample requests to the server
        requestObserver.onNext(LightControlRequest.newBuilder()
                .setDeviceId("1")
                .setBrightness(80)
                .setPower(true)
                .build());

        // You can send more requests if needed

        // Mark the end of requests
        requestObserver.onCompleted();
    }

    public void lightChannelShutdown() {
        try {
            lightChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Error while shutting down client: " + e.getMessage());
        }
    }

    public void monitorLighting() {

    }

    public static void main(String[] args) {
        // Instantiate SmartHotel with appropriate hosts and ports
        SmartHotel smartHotel = new SmartHotel("localhost", 8081, "localhost", 8080, "localhost", 8082);

        // smartHotel.toggleCurtain("open");
        smartHotel.StreamTemperatureRequest();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Press 'Q' to quit");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("Q")) {
                smartHotel.temperatureChannelShutdown();
                break;
            }
        }

        smartHotel.realTimeControl();

        while (true) {
            System.out.println("Press 'A' to quit");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("a")) {
                smartHotel.lightChannelShutdown();
                break;
            }
        }

    }
}
