import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

package SmartHotelServer;

public class LightControlServer {
    private static final int PORT=8080;
    private Server server;

    private static void main(String[]args) throws IOException, InterruptedException{
        LightControlServer lightServer=new LightControlServer();
        lightServer.start();

        private void start()throws IOException,InterruptedException{
            server = ServerBuilder.forPort(PORT)
                    .addServer(new LightControlimpl())
                    .build()
                    .start();

            System.out.println("Server started, listening on port"+PORT);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("Shutting down gRPC server");
            try{
                server.shutdown().awaitTermination(60,TimeUnit.SECONDS);
            }catch(InterruptedException e){
                e.printStackTrace(System.err);
            }
        }));
        server.awaitTermination();
    }

    public class LightControlImpl extends LightControlGrpc.LightControlImplBase {
        @Override
        public StreamObserver<LightRequest>turnOnLight(StreamObserver<LightResponse>responseObserver) {
            return new StreamObserver<LightRequest>(){
                @Override
                public void onNext(LightRequest request){
                    System.out.println("Received message from client:"+rquest.getMessage());
                    for(int i=0;i<5;i++){
                        LightResponse response = LightResponse.newBuilder()
                                .setStatus("Light is turned on")
                                .build();

                        responseObserver.onNext(response);

                    }
                }
                @Override
                public void onError(Throwable t){
                    System.err.println("Error from client:"+t.getMessage()):}

                @Override
                public void onCompleted(){
                    System.out.println("Client stream completed");
                    responseObserver.onCompleted();
                }
            };
        }
    }
}

