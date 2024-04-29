package SmartHotelServer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import org.example.lightcontrol.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LightControlServer {
    private static final int PORT = 8080;
    Server server;

    public void start() throws IOException {
        // Start gRPC server
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

    // Registers the light control server to consul service registry
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
        // Start the light control server
        final LightControlServer server = new LightControlServer();
        server.start();
        server.blockUntilShutdown();
    }

    // Implementation of changeLightcolor gRPC method
    static class LightControlImpl extends LightControlServiceGrpc.LightControlServiceImplBase {
        private final Map<String, LightingStatus> lightingStatusMap = new HashMap<>();

        @Override
        public StreamObserver<LightControlRequest> realTimeControl(
                StreamObserver<LightControlResponse> responseObserver) {
            return new StreamObserver<LightControlRequest>() {
                @Override
                public void onNext(LightControlRequest request) {

                    LightControlResponse response = LightControlResponse.newBuilder()
                            .setDeviceId(request.getDeviceId())
                            .setStatus("Received request: " + request)
                            .build();
                    responseObserver.onNext(response);
                }

                @Override
                public void onError(Throwable t) {
                    // Handle errors
                    System.err.println("Error in RealTimeControl: " + t);
                }

                @Override
                public void onCompleted() {

                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public void monitorLighting(MonitorRequest request, StreamObserver<LightingStatus> responseObserver) {

            LightingStatus status = LightingStatus.newBuilder()
                    .setDeviceId(request.getDeviceId())
                    .setBrightness(50)
                    .setPower(true)
                    .build();
            responseObserver.onNext(status);
            responseObserver.onCompleted();
        }
    }

}
