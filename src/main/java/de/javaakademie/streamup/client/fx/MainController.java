package de.javaakademie.streamup.client.fx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * UI-Controller.
 * 
 * @author Guido.Oelmann
 */
public class MainController implements Initializable {

	private static final String DEFAULT_SERVER_ADDRESS = "http://localhost:8080/streamup/rest/video/";
	
	@FXML
	private ListView<Path> chunkList;
	@FXML
	private Button chunkSelectButton;
	@FXML
	private ImageView arrowImageView;
	@FXML
	private TextField serverField;
	@FXML
	private Button testConnectionButton;
	@FXML
	private MediaView mediaView;
	@FXML
	private Button sendButton;
	@FXML
	private CheckBox loopCheckBox;
	@FXML
	private Label statusLabel;
	@FXML
	private AnchorPane contentView;

	private StreamUpService streamUpService = new StreamUpService();

	boolean sendStream = false;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		chunkSelectButton.setOnAction(e -> selectChunkFolder());
		testConnectionButton.setOnAction(e -> testConnection());
		sendButton.setOnAction(e -> sendStream());
		loopCheckBox.setOnAction(e -> toggleLoop());

		serverField.setText(DEFAULT_SERVER_ADDRESS);
		
		String resourcePath = getClass().getResource("/").toExternalForm();
		Image image = new Image(resourcePath + "arrow.png");
		arrowImageView.setImage(image);
		VBox.setVgrow(arrowImageView, Priority.ALWAYS);
		mediaView.setStyle("-fx-background-color: black");
		statusLabel.setText("");
		Platform.runLater(() -> chunkSelectButton.requestFocus());

	}

	private void selectChunkFolder() {
		Stage stage = (Stage) contentView.getScene().getWindow();
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(stage);
		if (selectedDirectory != null) {
			// chunkList.getItems().addAll(selectedDirectory.listFiles());
			chunkList.getItems().clear();
			Path dir = selectedDirectory.toPath();
			int depth = 1;
			try {
				Files.find(dir, depth, (path, attributes) -> correctSuffix(path)).forEach(chunkList.getItems()::add);
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(null);
				alert.setContentText("Error while reading the files.");
				alert.showAndWait();
			}
		}
	}

	private boolean correctSuffix(Path path) {
		String filename = path.getFileName().toString().toLowerCase();
		if (filename.endsWith("mp4") || filename.endsWith("m4s")) {
			return true;
		} else {
			return false;
		}
	}

	private void testConnection() {
		String server = serverField.getText().trim();
		if (streamUpService.testConnection(server)) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Successful");
			alert.setHeaderText(null);
			alert.setContentText("Connection tested successful.");
			alert.showAndWait();
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Connecting to server failed.");
			alert.showAndWait();
		}
	}

	private void sendStream() {
		// server address set?
		String server = serverField.getText().trim();
		if (server.length() == 0) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText(null);
			alert.setContentText("Server address missing.");
			alert.showAndWait();
		} else if (!streamUpService.testConnection(server)) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Connecting to server failed.");
			alert.showAndWait();
		} else if (chunkList.getItems().size() > 0) {
			if (sendStream) {
				// cancel stream sending
				sendStream = false;
				sendButton.getStyleClass().remove("stopButton");
				sendButton.setText("Send Stream");
			} else {
				// start sending and set cancel button
				sendStream = true;
				sendButton.setText("Stop Sending");
				sendButton.getStyleClass().add("stopButton");

				CompletableFuture<Void> future = CompletableFuture.supplyAsync(taskCreatePost);
				future.thenAcceptAsync(dbl -> Platform.runLater(() -> resetSendButton()));
			}
		}
	}

	private void resetSendButton() {
		sendButton.getStyleClass().remove("stopButton");
		sendButton.setText("Send Stream");
	}

	public Supplier<Void> taskCreatePost = () -> {
		boolean endReached = false;
		int totalChunks = chunkList.getItems().size();
		int index = 0;
		while (sendStream && !endReached) {
			try {
				System.out.println("send chunk: " + chunkList.getItems().get(index));
				streamUpService.sendChunk(serverField.getText(), chunkList.getItems().get(index));
				index++;
				if (index == totalChunks) {
					if (loopCheckBox.isSelected() && sendStream) {
						// restart stream
						index = 0;
					} else {
						sendStream = false;
					}
				}
				TimeUnit.MILLISECONDS.sleep(4);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	};

	/**
	 * TODO: Video-Loop not implemented yet!
	 */
	private void toggleLoop() {
		if (loopCheckBox.isSelected()) {
			System.out.println("select");
		} else {
			System.out.println("deselect");
		}
	}

	@FXML
	private void handleAbout() {
		FlowPane fp = new FlowPane();
	    Label lbl = new Label("Guido Oelmann - ");
		Hyperlink link = new Hyperlink();
		link.setText("http://JavaAkademie.de");
		link.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Stage stage = (Stage) contentView.getScene().getWindow();
				HostServices hostServices = (HostServices)stage.getProperties().get("hostServices");
				hostServices.showDocument(link.getText());
			}
		});
	    fp.getChildren().addAll( lbl, link);
		
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("Test Client for the StreamUp plattform!");
		alert.getDialogPane().contentProperty().set(fp);
		alert.showAndWait();
	}

	@FXML
	private void handleExit() {
		System.exit(0);
	}
}