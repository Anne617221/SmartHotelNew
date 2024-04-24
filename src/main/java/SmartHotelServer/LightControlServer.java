package SmartHotelServer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import  org.example.lightcontrol.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;


public class LightControlServer {
    private static final int PORT = 8080;
    Server server;

    private void start() throws IOException {

        server = ServerBuilder.forPort(PORT)
                .addService(new LightControlImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + PORT);

        // Register server to Consul
        registerToConsul();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            LightControlServer.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    private void registerToConsul() {
        System.out.println("Registering light control server to Consul...");

        // Load Consul configuration from consul.properties file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/lightcontrol.properties")) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Extract Consul configuration properties
        String consulHost = props.getProperty("consul.host");
        int consulPort = Integer.parseInt(props.getProperty("consul.port"));
        String serviceName = props.getProperty("consul.service.name");
        int servicePort = Integer.parseInt(props.getProperty("consul.service.port"));
        String healthCheckInterval = props.getProperty("consul.service.healthCheckInterval");

        // Get host address
        String hostAddress;
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        // Create a Consul client
        ConsulClient consulClient = new ConsulClient(consulHost, consulPort);

        // Define service details
        NewService newService = new NewService();
        newService.setName(serviceName);
        newService.setPort(servicePort);
        newService.setAddress(hostAddress); // Set host address

        // Register service with Consul
        consulClient.agentServiceRegister(newService);

        // Print registration success message
        System.out.println("light control Server registered to Consul successfully. Host: " + hostAddress);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final LightControlServer server = new LightControlServer();
        server.start();
        server.blockUntilShutdown();
    }



    static class LightControlImpl extends LightcontrolserviceGrpc.LightcontrolserviceImplBase {
        @Override
        public void changeLightColor(ColorRequest request, StreamObserver<LightcontrolRespons> responseObserver){
            String color = request.getColor();
            LightcontrolRespons response = LightcontrolRespons.newBuilder().setMessage("Changing light color to " + color).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            try{
                LightcontrolRespons message = LightcontrolRespons.newBuilder().setMessage("Turning on").build();
                responseObserver.onNext(message);

                responseObserver.onCompleted();
            }
            catch (Exception e) {
                responseObserver.onError(e);
            }
        }


    }
    public StreamObserver<ToggleRequest> toggleLights(final StreamObserver<LightcontrolRespons> responseObserver) {
        return new StreamObserver<ToggleRequest>() {
            @Override
            public void onNext(ToggleRequest request) {
                // Handle the request
                String lightId = request.getLightId();
                boolean turnOn = request.getTurnOn();

                // Simulate toggling the light
                String message;
                if (turnOn) {
                    message = "Turned on light " + lightId;
                } else {
                    message = "Turned off light " + lightId;
                }

                // Create and send the response
                LightcontrolRespons response = LightcontrolRespons.newBuilder().setMessage(message).build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                // Handle errors
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // Finish the response
                responseObserver.onCompleted();
            }
        };
    }


        public void changeLightColor(ColorRequest request, io.grpc.stub.StreamObserver<LightcontrolRespons> responseObserver) {
        // Implement changeLightColor functionality here
    }
}