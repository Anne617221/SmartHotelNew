import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

package SmartHotelClient;

public class SmartHotelClient {
    private final ManagedChannel channel;
    private final SmartHotelClientblockingStub.blockingStub;
    private final StreamingClientGrpc.StreamingServerServiceStub streamingStub;
    public SmartHotelClient(String host,int port){
        this.channel=ManagedChannelBuilder.forAddress(host,port)
                .usePlaintext()
                .build();
        this.blockingStub=SmartHotelBlockingStub(channel);
        this.streamingStub=streamingCilentGrpc.newStub(channel);
    }

    public void shutdown() {
        try {
            awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruputedException e) {
            System.err.println("Error while shutting down client:" + e.getMessage());
        }
    }
    public void turnOnLight(){
        LightRequest request=LightRequest.newBuilder().build();
        LightResponse response=lightControlStub.turnOnLight(request);
        System.out.println("Light turned on:"+response);
    }

    public void stremServerRequest(){
        StreamObserver<StreamServerResponse>responseStreamObserver=new StreamObserver<StreamServerResponse>() {
            @Override
            public void onNext(StreamServerResponse streamServerResponse) {
                System.out.println("Server message:"+response.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error in server streaming:"+t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Server streaming completed");
            }
        };
        streamingStub.streamServerRequest(StreamServerRequest.newBuilder().setServerName("Server01").build(),responseObserver);
    }

    public static void main(String[] args){
        SmartHotelClient client=new SmartHotelClient("localhost", 50051);
        client.turnOnLight();
        client.streamServerRequest();

        Scanner scanner=new Scanner(System.in);
        while(ture){
            System.out.println("Press 'Q' to quit");
            String input=scanner.nextLine();
            if(input.equalsIgnoreCase("Q")){
                client.shutdown();
                break;
            }
        }
    }
}
}
