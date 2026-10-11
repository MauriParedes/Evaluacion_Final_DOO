package com.biblioteca.dao;

import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.ReporteItem;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class PrestamoDAO {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public synchronized boolean registrarPrestamoTransaccional(int idEstudiante, int idLibro, java.util.Date fechaDevolucion) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Verificar stock actual y bloquear la fila (FOR UPDATE)
            String stockSql = "SELECT stock FROM libros WHERE id = ? FOR UPDATE";
            int currentStock = 0;
            try (PreparedStatement stockStmt = conn.prepareStatement(stockSql)) {
                stockStmt.setInt(1, idLibro);
                try (ResultSet rs = stockStmt.executeQuery()) {
                    if (rs.next()) {
                        currentStock = rs.getInt("stock");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            if (currentStock <= 0) {
                conn.rollback();
                return false;
            }

            // 2. Decrementar stock
            String updateStockSql = "UPDATE libros SET stock = stock - 1 WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                updateStmt.setInt(1, idLibro);
                updateStmt.executeUpdate();
            }

            // 3. Insertar registro de préstamo
            String insertSql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) " +
                               "VALUES (?, ?, ?, ?, FALSE)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                java.sql.Date fechaActual = new java.sql.Date(System.currentTimeMillis());
                java.sql.Date fechaDev = new java.sql.Date(fechaDevolucion.getTime());

                insertStmt.setInt(1, idEstudiante);
                insertStmt.setInt(2, idLibro);
                insertStmt.setDate(3, fechaActual);
                insertStmt.setDate(4, fechaDev);
                insertStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error en transacción registrarPrestamo: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error en rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                System.err.println("Error al restaurar autoCommit: " + e.getMessage());
            }
        }
    }

    // Registra la devolución de un libro de forma transaccional.
    public synchronized boolean registrarDevolucion(int idPrestamo) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            //  Obtener id_libro del préstamo
            int idLibro = -1;
            boolean yaDevuelto = true;
            String findSql = "SELECT id_libro, devuelto FROM prestamos WHERE id = ? FOR UPDATE";
            try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                findStmt.setInt(1, idPrestamo);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        idLibro = rs.getInt("id_libro");
                        yaDevuelto = rs.getBoolean("devuelto");
                    }
                }
            }

            if (idLibro == -1 || yaDevuelto) {
                conn.rollback();
                return false;
            }

            //  Marcar préstamo como devuelto
            String updatePrestamoSql = "UPDATE prestamos SET devuelto = TRUE WHERE id = ?";
            try (PreparedStatement upStmt = conn.prepareStatement(updatePrestamoSql)) {
                upStmt.setInt(1, idPrestamo);
                upStmt.executeUpdate();
            }

            //  Incrementar stock del libro
            String updateStockSql = "UPDATE libros SET stock = stock + 1 WHERE id = ?";
            try (PreparedStatement upStockStmt = conn.prepareStatement(updateStockSql)) {
                upStockStmt.setInt(1, idLibro);
                upStockStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar devolución: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error en rollback de devolución: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                System.err.println("Error al restaurar autoCommit: " + e.getMessage());
            }
        }
    }

    public List<Prestamo> obtenerTodos() {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT p.*, e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, l.titulo AS libro_titulo, l.autor AS libro_autor " +
                     "FROM prestamos p " +
                     "JOIN estudiantes e ON p.id_estudiante = e.id " +
                     "JOIN libros l ON p.id_libro = l.id " +
                     "ORDER BY p.id DESC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToPrestamo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener prestamos: " + e.getMessage());
        }
        return lista;
    }

    public List<Prestamo> obtenerActivos() {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT p.*, e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, l.titulo AS libro_titulo, l.autor AS libro_autor " +
                     "FROM prestamos p " +
                     "JOIN estudiantes e ON p.id_estudiante = e.id " +
                     "JOIN libros l ON p.id_libro = l.id " +
                     "WHERE p.devuelto = FALSE " +
                     "ORDER BY p.fecha_devolucion ASC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToPrestamo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener prestamos activos: " + e.getMessage());
        }
        return lista;
    }

    public List<Prestamo> obtenerHistorialEstudiante(int idEstudiante) {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT p.*, e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, l.titulo AS libro_titulo, l.autor AS libro_autor " +
                     "FROM prestamos p " +
                     "JOIN estudiantes e ON p.id_estudiante = e.id " +
                     "JOIN libros l ON p.id_libro = l.id " +
                     "WHERE p.id_estudiante = ? " +
                     "ORDER BY p.fecha_prestamo DESC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idEstudiante);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener historial del estudiante: " + e.getMessage());
        }
        return lista;
    }

    //Reporte: Libros más prestados (con conteo total de préstamos).


    public List<ReporteItem> obtenerLibrosMasPrestados() {
        List<ReporteItem> lista = new ArrayList<>();
        String sql = "SELECT l.titulo, l.autor, c.nombre AS categoria, COUNT(p.id) AS total_prestamos " +
                     "FROM libros l " +
                     "LEFT JOIN categorias c ON l.id_categoria = c.id " +
                     "LEFT JOIN prestamos p ON l.id = p.id_libro " +
                     "GROUP BY l.id, l.titulo, l.autor, c.nombre " +
                     "ORDER BY total_prestamos DESC, l.titulo ASC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ReporteItem item = new ReporteItem();
                item.setTitulo(rs.getString("titulo"));
                item.setAutor(rs.getString("autor"));
                item.setCategoria(rs.getString("categoria"));
                item.setCantidadPrestamos(rs.getInt("total_prestamos"));
                lista.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error en reporte libros más prestados: " + e.getMessage());
        }
        return lista;
    }

    private Prestamo mapResultSetToPrestamo(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo(
            rs.getInt("id"),
            rs.getInt("id_estudiante"),
            rs.getInt("id_libro"),
            rs.getDate("fecha_prestamo"),
            rs.getDate("fecha_devolucion"),
            rs.getBoolean("devuelto")
        );
        p.setNombreEstudiante(rs.getString("estudiante_nombre"));
        p.setRutEstudiante(rs.getString("estudiante_rut"));
        p.setTituloLibro(rs.getString("libro_titulo"));
        p.setAutorLibro(rs.getString("libro_autor"));
        return p;
    }
}
