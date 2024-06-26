package SmartHotelServer;


import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import org.example.curtaincontrol.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;


import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;


public class CurtainControlServer {
    private static final int PORT = 8081;
    Server server;

    public void start() throws IOException {
        //Start gRPC server
        server = ServerBuilder.forPort(PORT)
                .addService(new CurtainControlImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + PORT);

        // Register server to Consul
        registerToConsul();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            CurtainControlServer.this.stop();
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

    //Registers the curtain control server  to consul service registry
    private void registerToConsul() {
        System.out.println("Registering curtain control server to Consul...");

        // Load Consul configuration from consul.properties file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/curtaincontrol.properties")) {
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
        System.out.println("curtain control Server registered to Consul successfully. Host: " + hostAddress);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        //Start the curtain control server
        final CurtainControlServer server = new CurtainControlServer();
        server.start();
        server.blockUntilShutdown();
    }

    //Implementation of the curtain control server
    static class CurtainControlImpl extends curtaincontrolserviceGrpc.curtaincontrolserviceImplBase {
        @Override
        public void curtaincontrol(SimpleCurtaincontrolRequest request,
                                   io.grpc.stub.StreamObserver<SimpleCurtaincontrolResponse> responseObserver) {
           //Extract message from the request
            String message = request.getMessage();
            System.out.println("Received message: " + message);
            //Create response message indicating the status the curtain
            SimpleCurtaincontrolResponse response = SimpleCurtaincontrolResponse.newBuilder()
                    .setMessage("Curtain is " + message)
                    .build();
            //Send response to the client
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}






