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
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public  class TemperatureControlServer {
    private static final int PORT = 8082;
    //private static final int Temperature_check_interval=15*60*1000;

    Server server;

    private void start() throws IOException {
        //Start gRPC server
        server = ServerBuilder.forPort(PORT)
                .addService(new TemperatureControlServer.TemperatureControlImpl())
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

    //Registers the temperature control server  to consul service registry
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

    //Main method to start
    public static void main(String[] args) throws IOException, InterruptedException {
        final TemperatureControlServer server = new TemperatureControlServer();
        server.start();
        server.blockUntilShutdown();
    }

    //Implementation of the temperature control server
    static class TemperatureControlImpl extends temperaturecontrolserviceGrpc.temperaturecontrolserviceImplBase {
        //Method to handle temperature control stream
        public void temperaturecontrolStream(TemperaturecontrolRequest request, StreamObserver<TemperaturecontrolResponse> responseObserver) {
            // Implement temperature control logic here
            // Example logic:
            double temperature = getCurrentTemperature();
            if (temperature < 17) {
                // Turn on
                TemperaturecontrolResponse response = TemperaturecontrolResponse.newBuilder().setMessage("Turning on").build();
                responseObserver.onNext(response);
            } else if (temperature > 25) {
                // Turn off
                TemperaturecontrolResponse response = TemperaturecontrolResponse.newBuilder().setMessage("Turning off").build();
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        }



        // Method to get current temperature
        private double getCurrentTemperature() {
            // Implement logic to get current temperature from sensor
            return 20.0; // Dummy value for demonstration
        }

        //Method to handle temperature stream
        @Override
        public StreamObserver<TemperatureStreamRequest> temperatureStream(StreamObserver<TemperatureStreamResponse> responseObserver) {
            return new StreamObserver<TemperatureStreamRequest>() {
                @Override
                public void onNext(TemperatureStreamRequest request) {
                    double temperature = request.getTemperature();
                    System.out.println("Received temperature data: " + temperature);

                    // Determine whether to turn on/off the light based on temperature
                    boolean turnOnLight = temperature > 30; // Example condition, adjust as needed

                    // Send the response to control light
                    TemperatureStreamResponse response = TemperatureStreamResponse.newBuilder()
                            .setTurnOnLight(turnOnLight)
                            .build();

                    responseObserver.onNext(response);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Temperature Control Stream Error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Temperature Control Stream completed.");
                    responseObserver.onCompleted();
                }
            };
        }
    }

    //Block the main thread until the gRPC server is terminated
    private void awaitTermination() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
    }
