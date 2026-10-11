package com.biblioteca.test;

import com.biblioteca.config.DatabaseConnection;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;

public class ResetDatabase {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();

            // Eliminar tablas previas en orden de clave foránea
            stmt.execute("DROP TABLE IF EXISTS prestamos");
            stmt.execute("DROP TABLE IF EXISTS libros");
            stmt.execute("DROP TABLE IF EXISTS categorias");
            stmt.execute("DROP TABLE IF EXISTS estudiantes");
            stmt.execute("DROP TABLE IF EXISTS usuarios");

            String createSql = Files.readString(new File("sql_scripts/PRY2203_EFT_S9_Script_crea_tablas_biblioteca.sql").toPath());
            for (String sql : createSql.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }

            String populateSql = Files.readString(new File("sql_scripts/PRY2203_EFT_S9_Script_poblado_tablas_biblioteca.sql").toPath());
            for (String sql : populateSql.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }

            System.out.println("✅ Base de datos reiniciada con éxito con los datos de prueba oficiales.");
        } catch (Exception e) {
            System.err.println("Error al reiniciar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
