package SmartHotelServer;


import org.example.curtaincontrol.curtaincontrolservice.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.lightcontrol.lightcontrolservice.LightcontrolRequest;


import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class CurtainControlServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(PORT)
                .addService(new CurtainControlImpl())
                .build();

        server.start();
        System.out.println("Server started, listening on port " + PORT);

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server");
            try {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }));

        server.awaitTermination();
    }


    static class CurtainControlImpl extends curtaincontrolserviceGrpc.curtaincontrolserviceImplBase {
        @Override
        public void curtaincontrol(CurtaincontrolRequest request, StreamObserver<CurtaincontrolResponse> responseObserver) {

            System.out.println("Received message from client: " + request.getMessage());

            CurtaincontrolResponse response = CurtaincontrolResponse.newBuilder()
                    .setMessage("Curtain controlled successfully ")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }


        @Override
        public void onError(Throwable t) {
            System.err.println("Error from client: " + t.getMessage());
        }
    }
}






