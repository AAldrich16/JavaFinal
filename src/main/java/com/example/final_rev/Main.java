package com.example.final_rev;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;

public class Main extends Application {
    private Database database;
    private UI ui;

    private File selectedFile;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the application
     */
    @Override
    public void start(Stage primaryStage) {
        // Show FileChooser to select the database file on the JavaFX Application Thread
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Java Final");
            selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    // Create Database and UI instances
                    database = new Database(String.valueOf(selectedFile));
                    ui = new UI(primaryStage, database, this);

                    // Load the selected database file into the Database class
                    database.connect(String.valueOf(selectedFile));

                    // Show the main UI with the loaded database
                    ui.showMainUI();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                // Show an error message to the user
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No database file selected");
                alert.setContentText("Please select a database file and try again.");
                alert.showAndWait();

                // Terminate the application
                Platform.exit();
            }
        });
    }

    /**
     * Refreshes the UI after updating a row in the database.
     * This method is called from the EditRow class after saving the changes.
     */
    public void refreshUIAfterUpdate() {
        try {
            ui.refreshTableData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
