package main.java.zenit.terminal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import main.java.zenit.ui.DialogBoxes;

import java.util.Optional;

public class runTerminal extends Application {
	private DialogBoxes dialog;

	@Override
	public void start(Stage stage) {
		System.out.println("hello");
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/zenit/terminal/Terminal.fxml"));
			
			
			TerminalController controller = new TerminalController();
			loader.setController(controller);
			Parent root = loader.load();
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.setTitle("Zenit");

			stage.show();
		} catch (Exception e ) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
