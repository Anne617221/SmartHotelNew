package SmartHotelClient;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.ecwid.consul.v1.health.model.HealthService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;
public class SmartHotelClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    public static void main(String[] args) {
        // Create a channel to the server
        ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
                .usePlaintext()
                .build();

        private final TemperaturecontrolserviceGrpc.TemperatureControlBlockingStub temperatureControlStub;
        private final LightControlserviceGrpc.LightControlBlockingStub lightControlStub;
        private final CurtainControlserviceGrpc.CurtainControlBlockingStub curtainControlStub;
//
//    public SmartHotelClient(String host, int temperaturePort, int lightPort, int curtainPort) {
//
//
//    }
}