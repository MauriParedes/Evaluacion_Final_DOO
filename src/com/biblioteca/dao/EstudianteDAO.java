package com.biblioteca.dao;

import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.model.Estudiante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class EstudianteDAO {

    public List<Estudiante> obtenerTodos() {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY nombre ASC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToEstudiante(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar estudiantes: " + e.getMessage());
        }
        return lista;
    }

    public Estudiante obtenerPorId(int id) {
        String sql = "SELECT * FROM estudiantes WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEstudiante(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estudiante por ID: " + e.getMessage());
        }
        return null;
    }

    public Estudiante obtenerPorRut(String rut) {
        String sql = "SELECT * FROM estudiantes WHERE rut = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rut);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEstudiante(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estudiante por RUT: " + e.getMessage());
        }
        return null;
    }

    public List<Estudiante> buscar(String criterio) {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes WHERE nombre LIKE ? OR rut LIKE ? OR curso LIKE ? OR correo LIKE ? ORDER BY nombre ASC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + criterio + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToEstudiante(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar estudiantes: " + e.getMessage());
        }
        return lista;
    }

    public boolean insertar(Estudiante e) {
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, e.getNombre());
            pstmt.setString(2, e.getRut());
            pstmt.setString(3, e.getCurso());
            pstmt.setString(4, e.getCorreo());
            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        e.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error al insertar estudiante: " + ex.getMessage());
        }
        return false;
    }

    public boolean actualizar(Estudiante e) {
        String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.getNombre());
            pstmt.setString(2, e.getRut());
            pstmt.setString(3, e.getCurso());
            pstmt.setString(4, e.getCorreo());
            pstmt.setInt(5, e.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Error al actualizar estudiante: " + ex.getMessage());
        }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar estudiante: " + e.getMessage());
        }
        return false;
    }

    private Estudiante mapResultSetToEstudiante(ResultSet rs) throws SQLException {
        return new Estudiante(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("rut"),
            rs.getString("curso"),
            rs.getString("correo")
        );
    }
}
