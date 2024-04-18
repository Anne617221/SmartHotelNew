package SmartHotelServer;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.temperaturecontrol.temperaturecontrolservice.*;


import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public  class TemperatureControlServer {
    private static final int PORT = 8080;
    private static final int Temperature_check_interval=15*60*1000;

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(PORT)
                .addService(new TemperatureControlServer.TemperatureControlImpl())
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


    static class TemperatureControlImpl extends TemperaturecontrolserviceGrpc.TemperatureControlserviceImplBase {
        @Override
        public StreamObserver<TemperaturecontrolRequest> temperatureontrolStream(StreamObserver<TemperaturecontrolResponse> responseObserver) {
            return new StreamObserver<TemperaturecontrolRequest>() {
                @Override
                public void onNext(TemperaturecontrolRequest request) {
                    int temperature=getTemperature();
                    if(temperature<15){
                        TemperaturecontrolResponse response=TemperaturecontrolResponse.newBuilder()
                                .setMessage("Switch on")
                                .build();
                        responseObserver.onNext(response);
                    }else if(temperature>25){
                        TemperaturecontrolResponse response=TemperaturecontrolResponse.newBuilder()
                                .setMessage("seitch off")
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
        private int getTemperature(){
            return 25;
        }
    }
    static class TemperatureMOnitoringTask extends TimerTask{
        public void run(){
            System.out.println("Temperature monitoring task executed");
        }
    }
}