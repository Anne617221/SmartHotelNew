package SmartHotelServer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.temperaturecontrol.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class TemperatureControlServer {
    private static final int PORT = 8082;
    // private static final int Temperature_check_interval=15*60*1000;

    Server server;

    private void start() throws IOException {
        // Start gRPC server
        server = ServerBuilder.forPort(PORT)
                .addService(new TemperatureControlImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + PORT);

        // Register server to Consul
        registerToConsul();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            TemperatureControlServer.this.stop();
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

    // Registers the temperature control server to consul service registry
    private void registerToConsul() {
        System.out.println("Registering Temperature control server to Consul...");

        // Load Consul configuration from consul.properties file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/temperaturecontrol.properties")) {
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
        System.out.println("temperature control Server registered to Consul successfully. Host: " + hostAddress);
    }

    // Main method to start
    public static void main(String[] args) throws IOException, InterruptedException {
        final TemperatureControlServer server = new TemperatureControlServer();
        server.start();
        server.blockUntilShutdown();
    }

    // Implementation of the temperature control server
    static class TemperatureControlImpl extends TemperatureControlServiceGrpc.TemperatureControlServiceImplBase {
        @Override
        public void temperatureStream(StreamTemperatureRequest request,
                StreamObserver<TemperatureData> responseObserver) {
            try {
                // Generate an array of temperatures to stream
                double[] temperatures = generateTemperatures();

                // Stream each temperature to the client
                for (double temperature : temperatures) {
                    TemperatureData temperatureData = TemperatureData.newBuilder()
                            .setTemperature(temperature)
                            .build();
                    responseObserver.onNext(temperatureData);

                    // Simulate some delay between each temperature
                    Thread.sleep(1000); // 1 second delay
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                responseObserver.onError(e);
            } finally {
                responseObserver.onCompleted();
            }
        }

        // Method to generate an array of temperatures
        private double[] generateTemperatures() {

            int numTemperatures = 30; // Number of temperatures to generate
            double[] temperatures = new double[numTemperatures];
            for (int i = 0; i < numTemperatures; i++) {
                temperatures[i] = 20 + Math.random() * 10; // Generate temperatures between 20 to 30 degrees Celsius
            }
            return temperatures;
        }
    }

}
