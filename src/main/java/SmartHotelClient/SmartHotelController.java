package SmartHotelClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.curtaincontrol.*;

public class SmartHotelController {

    @FXML
    public TextField nameTextField;

    @FXML
    public Button submitButton;

    @FXML
    void submitButtonClickOnAction(ActionEvent event) {
        System.out.println("SubmitButtonClickOnAction clicked");
        String name = nameTextField.getText();
        System.out.println("nameTextField: " + name);

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        // 创建 gRPC stub
        curtaincontrolserviceGrpc.curtaincontrolserviceStub stub = curtaincontrolserviceGrpc.newStub(channel);

        // 构建请求
        SimpleCurtaincontrolRequest request = SimpleCurtaincontrolRequest.newBuilder()
                .setMessage("open")
                .build();
        stub.curtaincontrol(request, new StreamObserver<SimpleCurtaincontrolResponse>() {
            @Override
            public void onNext(SimpleCurtaincontrolResponse response) {
                System.out.println("Response received: " + response.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error occurred: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Request completed");
            }
        });
    }



}
