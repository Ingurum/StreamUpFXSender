package de.javaakademie.streamup.client.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * StreamUp-Test-Client.
 * 
 * @author Guido.Oelmann
 */
public class MainApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/streamView.fxml"));
		primaryStage.getProperties().put("hostServices", getHostServices());
		primaryStage.setTitle("StreamUp-Test-Client");
		primaryStage.setScene(new Scene(root));
		primaryStage.getScene().getStylesheets().add("/layout.css");
		primaryStage.sizeToScene();
		primaryStage.show();
	}
	
}