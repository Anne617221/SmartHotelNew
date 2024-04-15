import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.RandomUtils;

package SmartHotelServer;

public class TemperatureControlServer {
    private Server server;

    private void start() throws Exception {
        int port = 8080;
        server = ServerBuilder.forPort(port)
                .addService(new TemperatureControlImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + port);
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

    public static void main(String[] args) throws Exception {
        final TemperatureControlServer server = new TemperatureControlServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class TemperatureControlImpl extends TemperatureControlGrpc.TemperatureControlImplBase {
        @Override
        public void getTemperature(TemperatureRequest request, StreamObserver<TemperatureResponse> responseObserver) {
            int temperature = RandomUtils.nextInt(0, 40); // Simulate temperature reading
            TemperatureResponse response = TemperatureResponse.newBuilder()
                    .setTemperature(temperature)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
}
