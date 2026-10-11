package com.biblioteca.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // Ajusta según la convención de tu script (biblioteca_escolar o biblioteca%20escolar)
    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca_escolar?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String[] PASSWORDS = {"admin", "root", "", "123456", "mysql"};

    private DatabaseConnection() {
        connect();
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC de MySQL no encontrado. Asegúrate de agregarlo a las dependencias: " + e.getMessage());
            return; // No intentamos conectar si falta la librería
        }

        boolean connected = false;
        for (String pass : PASSWORDS) {
            try {
                connection = DriverManager.getConnection(URL, USER, pass);
                connected = true;
                System.out.println("Conexión a la base de datos establecida con éxito.");
                break;
            } catch (SQLException e) {
                // Sigue probando el siguiente password del arreglo
            }
        }

        if (!connected) {
            System.err.println("No se pudo conectar a MySQL. Verifique que el servicio esté activo, las credenciales o el nombre de la BD.");
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                if (instance.connection == null || instance.connection.isClosed()) {
                    instance.connect();
                }
            } catch (SQLException e) {
                instance.connect();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexión a la base de datos cerrada.");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}