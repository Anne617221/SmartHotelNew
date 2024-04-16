package SmartHotelServer;


import org.example.lightcontrol.lightcontrolservice.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class CurtainControlServer{
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(PORT)
                .addService(new CurtianControlImpl())
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


    static class CurtainControlImpl extends curtiancontrolserviceGrpc.curtiancontrolserviceImplBase {

        @Override
        public StreamObserver<CurtiancontrolRequest> lightcontrolStream(StreamObserver<CurtiancontrolResponse> responseObserver) {
            return new StreamObserver <CurtiancontrolRequest>() {
                @Override
                public void onNext(CurtiancontrolRequest request) {
                    System.out.println("Received message from client: " + request.getMessage());

                    // Respond to the client's message with a stream
                    for (int i = 0; i < 5; i++) {
                        CurtiancontrolResponse response = CurtiancontrolResponse.newBuilder()
                                .setMessage("Response " + i)
                                .build();
                        responseObserver.onNext(response);
                    }
                }


                @Override
                public void onError(Throwable t) {
                    System.err.println("Error from client: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Client stream completed");
                    responseObserver.onCompleted(); // Complete the response stream
                }
            };
        }
    }
}


