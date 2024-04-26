import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class SmartHotelApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
//        Parent parent = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource("SmartHotel.fxml")));
        //Load the SmartHotel file
        Parent parent = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource("SmartHotel.fxml")));
        //Create a scene with the loaded parent and set the stage properties
        Scene scene = new Scene(parent, 800, 600);
        primaryStage.setTitle("Smart Hotel");
        primaryStage.centerOnScreen();
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //Main method to launch the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}