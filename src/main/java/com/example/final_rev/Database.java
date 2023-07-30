package com.example.final_rev;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Database {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    /**
     * Constructor to connect to the SQLite database.
     *
     * absolutePath Absolute path to the SQLite database file.
     */
    public Database(String absolutePath) {
        connect(absolutePath);
        createTables();
    }

    /**
     * Connects to the SQLite database using the given absolute path.
     *
     * absolutePath Absolute path to the SQLite database file.
     */
    void connect(String absolutePath) {
        try {
            String url = "jdbc:sqlite:" + absolutePath;
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates necessary tables in the database.
     * Call this method to create tables when initializing the database instance.
     */
    private void createTables() {
        // Add code here to create necessary tables in the database
    }

    /**
     * Retrieves the names of all tables in the database.
     *
     * @return A list of table names.
     */
    public List<String> getTables() {
        List<String> tableNames = new ArrayList<>();

        try {
            ResultSet resultSet = search("SELECT name FROM sqlite_master WHERE type='table'");
            while (resultSet.next()) {
                String tableName = resultSet.getString("name");
                tableNames.add(tableName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }

        return tableNames;
    }

    /**
     * Checks if a primary key value exists in the given table.
     *
     * tableName      The name of the table to search for the primary key value.
     * primaryKeyColumn The name of the primary key column.
     * primaryKeyValue The value of the primary key to check for existence.
     */
    public boolean isPrimaryKeyExists(String tableName, String primaryKeyColumn, String primaryKeyValue) {
        try {
            String query = "SELECT 1 FROM " + tableName + " WHERE " + primaryKeyColumn + " = ? LIMIT 1;";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, primaryKeyValue);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves the names of primary key columns for the given table.
     *
     * tableName The name of the table to retrieve primary key columns from.
     * @return A list of primary key column names.
     */
    public List<String> getPrimaryKeyColumns(String tableName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();

        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getPrimaryKeys(null, null, tableName);

        while (rs.next()) {
            String primaryKeyColumnName = rs.getString("COLUMN_NAME");
            primaryKeys.add(primaryKeyColumnName);
        }

        return primaryKeys;
    }

    /**
     * Retrieves the column names of the given table.
     *
     * tableName The name of the table to retrieve column names from.
     * @return A list of column names.
     */
    public List<String> getColumns(String tableName) {
        List<String> columnNames = new ArrayList<>();

        try {
            String query = "PRAGMA table_info(" + tableName + ");";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String columnName = resultSet.getString("name");
                columnNames.add(columnName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }

        return columnNames;
    }

    /**
     * Executes the given SQL query and returns the result set.
     *
     * query The SQL query to execute.
     * @return The result set of the query.
     */
    public ResultSet search(String query) {
        ResultSet tableRows = null;
        try {
            preparedStatement = connection.prepareStatement(query);
            tableRows = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tableRows;
    }

    /**
     * Retrieves all rows from the given table.
     *
     * tableName The name of the table to retrieve rows from.
     * @return The result set containing all rows of the table.
     */
    public ResultSet getTableRows(String tableName) {
        ResultSet tableRows = null;
        try {
            String query = "SELECT * FROM " + tableName + ";";
            preparedStatement = connection.prepareStatement(query);
            tableRows = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tableRows;
    }

    /**
     * Inserts a new row into the given table with the specified column values.
     *
     * tableName   The name of the table to insert the row.
     * columnNames The list of column names.
     * columnValues The map containing column names and their corresponding values.
     */
    public void insertRow(String tableName, List<String> columnNames, Map<String, String> columnValues) {
        try {
            StringBuilder placeholders = new StringBuilder();

            for (int i = 0; i < columnNames.size(); i++) {
                placeholders.append("?, ");
            }

            placeholders.delete(placeholders.length() - 2, placeholders.length());

            String query = "INSERT INTO " + tableName + " (" + String.join(", ", columnNames) + ") VALUES (" + placeholders + ");";
            preparedStatement = connection.prepareStatement(query);

            int index = 1;
            for (String columnName : columnNames) {
                preparedStatement.setString(index, columnValues.get(columnName));
                index++;
            }

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a row in the given table with the specified column values.
     *
     * tableName      The name of the table to update the row.
     * primaryKeyColumn The name of the primary key column.
     * primaryKeyValue The value of the primary key of the row to be updated.
     * editedValues   The map containing column names and their updated values.
     */
    public void updateRow(String tableName, String primaryKeyColumn, String primaryKeyValue, Map<String, String> editedValues) {
        try {
            List<String> columns = getColumns(tableName);
            StringBuilder setClause = new StringBuilder();

            for (String columnName : columns) {
                if (!columnName.equals(primaryKeyColumn)) {
                    setClause.append(columnName).append(" = ?, ");
                }
            }
            setClause.delete(setClause.length() - 2, setClause.length());

            String query = "UPDATE " + tableName + " SET " + setClause + " WHERE " + primaryKeyColumn + " = ?";
            preparedStatement = connection.prepareStatement(query);

            int index = 1;
            for (String columnName : columns) {
                if (!columnName.equals(primaryKeyColumn)) {
                    preparedStatement.setString(index++, editedValues.get(columnName));
                }
            }
            preparedStatement.setString(index, primaryKeyValue);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    /**
     * Closes the resources used for database operations.
     */
    private void closeResources() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
