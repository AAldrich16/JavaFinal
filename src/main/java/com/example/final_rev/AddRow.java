package com.example.final_rev;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRow {
    private Database database;
    private String tableName;
    private Stage modalStage;
    private UI parentUI;
    private Main main;

    public AddRow(Database database, String tableName, Main main) {
        this.database = database;
        this.tableName = tableName;
        this.main = main;
    }

    public void show() {
        modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Add a Row");
        modalStage.setMinWidth(300);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        gridPane.setAlignment(Pos.CENTER);

        // Create text fields for each column
        List<TextField> textFields = new ArrayList<>();

        List columnNames = database.getColumns(tableName);

        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = (String) columnNames.get(i);
            Label label = new Label(columnName + ":");
            TextField textField = new TextField();
            textFields.add(textField);
            gridPane.addRow(i, label, textField);
        }

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> handleSave(columnNames, textFields));

        gridPane.add(saveButton, 0, columnNames.size());

        VBox layout = new VBox(10);
        layout.getChildren().add(gridPane);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    private void handleSave(List<String> columnNames, List<TextField> textFields) {
        // Get the column values from the text fields
        String[] columnValues = new String[textFields.size()];
        for (int i = 0; i < textFields.size(); i++) {
            columnValues[i] = textFields.get(i).getText();
        }

        // Create a map to store the mapping of column names to their edited values
        // The key is the column name, and the value is the edited value for that column
        Map<String, String> columnValueMap = new HashMap<>();
        for (int i = 0; i < columnNames.size(); i++) {
            columnValueMap.put(columnNames.get(i), columnValues[i]);
        }

        // Get the primary key columns for the table
        List<String> primaryKeys;
        try {
            primaryKeys = database.getPrimaryKeyColumns(tableName);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
            return; // Stop further processing if there's an error
        }

        // Check if any of the primary key columns exist in the column names
        for (String primaryKeyColumn : primaryKeys) {
            if (columnNames.contains(primaryKeyColumn)) {
                // Get the primary key value from the map
                String primaryKeyValue = columnValueMap.get(primaryKeyColumn);

                // Check if the primary key value is not empty and already exists in the table
                if (primaryKeyValue != null && database.isPrimaryKeyExists(tableName, primaryKeyColumn, primaryKeyValue)) {
                    // Show an error message to the user or handle the duplicate key scenario appropriately
                    // For example:
                    // showError("Primary key value already exists. Please enter a unique value.");
                    return; // Stop further processing since we cannot insert a duplicate primary key
                }
            }
        }



        // Insert the new row into the database
        database.insertRow(tableName, columnNames, columnValueMap);

        // Close the modal after saving the new row
        modalStage.close();

        // Refresh the table view in the parent UI
        main.refreshUIAfterUpdate();
    }

}
