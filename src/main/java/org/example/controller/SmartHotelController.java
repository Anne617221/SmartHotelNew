package org.example.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;
import javafx.scene.shape.Circle;
import org.example.curtaincontrol.*;
import org.example.curtaincontrol.curtaincontrolserviceGrpc;

public class SmartHotelController {
    public ToggleButton toggleCurtainButton;
    public Circle curtainLight;

    private ManagedChannel channel;
    private curtaincontrolserviceGrpc.curtaincontrolserviceStub stub;

    public void initialize() {
        // Create gRPC channel
        channel = ManagedChannelBuilder.forAddress("localhost", 8081).usePlaintext().build();
        // Create gRPC stub
        stub = curtaincontrolserviceGrpc.newStub(channel);
    }


    public void toggleCurtainAction(ActionEvent actionEvent) {
        // Get the current state of the toggle button
        boolean isCurtainOpen = toggleCurtainButton.isSelected();

        // Create a request
        SimpleCurtaincontrolRequest request = SimpleCurtaincontrolRequest.newBuilder()
                .setMessage(isCurtainOpen ? "open" : "close")
                .build();

        // Send the request asynchronously
        stub.curtaincontrol(request, new StreamObserver<SimpleCurtaincontrolResponse>() {
            @Override
            public void onNext(SimpleCurtaincontrolResponse response) {
                System.out.println("Response received: " + response.getMessage());
                // Update UI based on the response
                javafx.application.Platform.runLater(() -> {
                    if (isCurtainOpen) {
                        curtainLight.setFill(javafx.scene.paint.Color.GREEN);
                        toggleCurtainButton.setText("Close");
                    } else {
                        curtainLight.setFill(javafx.scene.paint.Color.RED);
                        toggleCurtainButton.setText("Open");
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error occurred: " + throwable.getMessage());
                // Handle error if needed
            }

            @Override
            public void onCompleted() {
                System.out.println("Request completed");
                // Handle completion if needed
            }
        });
    }

    // Close the gRPC channel when the controller is destroyed
    public void onDestroy() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}
