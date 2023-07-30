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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRow {
    private Database database;
    private Main main;
    private Stage modalStage;
    private String[] selectedRow;
    private String tableName;

    // Constructor for EditRow class
    public EditRow(Database database, String[] rowData, String tableName, Main main) {
        this.database = database;
        this.selectedRow = rowData;
        this.tableName = tableName;
        this.main = main;
    }

    // Show the modal dialog for editing a row
    public void show() {
        modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Edit Row");
        modalStage.setMinWidth(400);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        gridPane.setAlignment(Pos.CENTER);

        // Check if a row is selected
        if (selectedRow != null) {
            int columnCount = selectedRow.length;

            List<TextField> textFields = new ArrayList<>();
            List<String> columnNames = database.getColumns(tableName);
            // Create input fields for each column in the row
            for (int i = 0; i < columnCount; i++) {
                String columnName = columnNames.get(i);
                String columnValue = selectedRow[i];

                Label label = new Label(columnName + ":");
                TextField textField = new TextField(columnValue);
                textFields.add(textField);

                gridPane.addRow(i, label, textField);
            }

            // Create save button and handle the save action
            Button saveButton = new Button("Save");
            saveButton.setOnAction(e -> handleSave(textFields, columnCount));

            VBox layout = new VBox(10);
            layout.getChildren().addAll(gridPane, saveButton);
            layout.setAlignment(Pos.CENTER);

            Scene scene = new Scene(layout);
            modalStage.setScene(scene);
            modalStage.showAndWait();
        }
    }

    // Handle the save button action to update the row in the database
    private void handleSave(List<TextField> textFields, int columnCount) {
        String[] editedValues = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            editedValues[i] = textFields.get(i).getText();
        }

        List<String> columns = database.getColumns(tableName);

        // Assuming the primary key is the first column
        String primaryKeyColumn = columns.get(0);
        String primaryKeyValue = selectedRow[0]; // Use the first element as the primary key value

        // Create a map to store the mapping of column names to their edited values
        // The key is the column name, and the value is the edited value for that column
        Map<String, String> columnValueMap = new HashMap<>();
        for (int i = 0; i < columnCount; i++) {
            columnValueMap.put(columns.get(i), editedValues[i]);
        }

        // Update the row in the database with the edited values
        database.updateRow(tableName, primaryKeyColumn, primaryKeyValue, columnValueMap);

        // Close the modal after saving the changes
        modalStage.close();

        // Refresh the UI after updating the row
        main.refreshUIAfterUpdate();
    }
}
