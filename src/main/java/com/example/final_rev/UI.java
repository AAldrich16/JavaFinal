package com.example.final_rev;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UI {
    private Stage primaryStage;
    private Database database;

    private Main main;
    private AddRow addRow;
    private EditRow editRow;
    private TableView<String[]> tableView;
    private String selectedTableName;

    // Constructor for UI class
    public UI(Stage primaryStage, Database database, Main main) {
        this.primaryStage = primaryStage;
        this.database = database;
        this.main = main;
    }

    // Show the main user interface
    public void showMainUI() throws SQLException {
        primaryStage.setTitle("Java Final");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        ComboBox<String> tableDropdown = new ComboBox<>();
        tableDropdown.setPromptText("Select a Table");
        tableDropdown.setOnAction(e -> {
            try {
                handleTableSelection(tableDropdown.getValue());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        layout.getChildren().add(tableDropdown);

        tableView = new TableView<>();
        layout.getChildren().add(tableView);

        List<String> tables = database.getTables();
        if (!tables.isEmpty()) {
            selectedTableName = tables.get(0);
            tableDropdown.setValue(selectedTableName);
            tableDropdown.setItems(FXCollections.observableArrayList(tables));
            showTableRows(selectedTableName);
        }

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> {
            AddRow addRowInstance = new AddRow(database, selectedTableName, main);
            addRowInstance.show();
        });

        layout.getChildren().add(0, addButton);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Handle the selection of a table from the drop-down
    private void handleTableSelection(String tableName) throws SQLException {
        selectedTableName = tableName;
        showTableRows(selectedTableName);
    }

    // Show the rows of the selected table in the TableView
    private void showTableRows(String tableName) throws SQLException {
        ResultSet tableRows = database.getTableRows(tableName);

        try {
            tableView.getColumns().clear();
            tableView.getItems().clear();

            if (tableRows != null) {
                ResultSetMetaData metaData = tableRows.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    TableColumn<String[], String> column = new TableColumn<>(metaData.getColumnName(i));
                    final int columnIndex = i;
                    column.setCellValueFactory(cellData -> {
                        String[] row = cellData.getValue();
                        if (row.length >= columnIndex) {
                            return new SimpleStringProperty(row[columnIndex - 1]);
                        }
                        return new SimpleStringProperty("");
                    });
                    tableView.getColumns().add(column);
                }

                List<String[]> rows = new ArrayList<>();
                while (tableRows.next()) {
                    String[] row = new String[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = tableRows.getString(i + 1);
                    }
                    rows.add(row);
                }

                tableRows.close();

                tableView.getItems().addAll(rows);

                TableColumn<String[], Void> editColumn = new TableColumn<>("Edit");
                editColumn.setCellFactory(param -> new TableCell<>() {
                    private final Button editButton = new Button("Edit");

                    {
                        editButton.setOnAction(event -> {
                            String[] rowData = getTableView().getItems().get(getIndex());
                            editRow = new EditRow(database, rowData, tableName, main);
                            editRow.show();
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(editButton);
                        }
                    }
                });

                tableView.getColumns().add(0, editColumn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update the row in the TableView after editing it
    public void updateRowInTable(String[] updatedRowData) {
        for (int i = 0; i < tableView.getItems().size(); i++) {
            if (tableView.getItems().get(i)[0].equals(updatedRowData[0])) {
                tableView.getItems().set(i, updatedRowData);
                break;
            }
        }
    }

    // Add a new row to the TableView after inserting it into the database
    public void addRowToTable(String[] newRowData) {
        tableView.getItems().add(newRowData);
    }

    // Refresh the data in the TableView
    public void refreshTableData() throws SQLException {
        showTableRows(selectedTableName);
    }
}
