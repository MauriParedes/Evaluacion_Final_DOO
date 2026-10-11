package com.biblioteca.dao;

import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.model.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    public List<Libro> obtenerTodos() {
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT l.*, c.nombre AS categoria_nombre " +
                     "FROM libros l " +
                     "LEFT JOIN categorias c ON l.id_categoria = c.id " +
                     "ORDER BY l.titulo ASC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToLibro(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar libros: " + e.getMessage());
        }
        return lista;
    }

    public Libro obtenerPorId(int id) {
        String sql = "SELECT l.*, c.nombre AS categoria_nombre " +
                     "FROM libros l " +
                     "LEFT JOIN categorias c ON l.id_categoria = c.id " +
                     "WHERE l.id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLibro(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener libro por ID: " + e.getMessage());
        }
        return null;
    }

    public List<Libro> buscar(String criterio, Integer idCategoria) {
        List<Libro> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT l.*, c.nombre AS categoria_nombre " +
            "FROM libros l " +
            "LEFT JOIN categorias c ON l.id_categoria = c.id " +
            "WHERE 1=1 "
        );

        if (criterio != null && !criterio.trim().isEmpty()) {
            sql.append("AND (l.titulo LIKE ? OR l.autor LIKE ? OR l.isbn LIKE ? OR l.editorial LIKE ?) ");
        }
        if (idCategoria != null && idCategoria > 0) {
            sql.append("AND l.id_categoria = ? ");
        }
        sql.append("ORDER BY l.titulo ASC");

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (criterio != null && !criterio.trim().isEmpty()) {
                String pattern = "%" + criterio.trim() + "%";
                pstmt.setString(paramIdx++, pattern);
                pstmt.setString(paramIdx++, pattern);
                pstmt.setString(paramIdx++, pattern);
                pstmt.setString(paramIdx++, pattern);
            }
            if (idCategoria != null && idCategoria > 0) {
                pstmt.setInt(paramIdx++, idCategoria);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToLibro(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar libros: " + e.getMessage());
        }
        return lista;
    }

    public boolean insertar(Libro libro) {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, libro.getTitulo());
            pstmt.setString(2, libro.getAutor());
            pstmt.setString(3, libro.getIsbn());
            pstmt.setString(4, libro.getEditorial());
            pstmt.setInt(5, libro.getStock());
            pstmt.setInt(6, libro.getIdCategoria());
            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        libro.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar libro: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizar(Libro libro) {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, libro.getTitulo());
            pstmt.setString(2, libro.getAutor());
            pstmt.setString(3, libro.getIsbn());
            pstmt.setString(4, libro.getEditorial());
            pstmt.setInt(5, libro.getStock());
            pstmt.setInt(6, libro.getIdCategoria());
            pstmt.setInt(7, libro.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar libro: " + e.getMessage());
        }
        return false;
    }

    // sinchronized
    public synchronized boolean decrementarStock(int idLibro) {
        String checkSql = "SELECT stock FROM libros WHERE id = ?";
        String updateSql = "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, idLibro);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int currentStock = rs.getInt("stock");
                    if (currentStock <= 0) {
                        System.err.println("Sin stock disponible para el libro ID: " + idLibro);
                        return false;
                    }
                } else {
                    return false;
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, idLibro);
                return updateStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al decrementar stock (synchronized): " + e.getMessage());
        }
        return false;
    }

    /**
     * Aumenta de forma SINCRONIZADA el stock de un libro en 1 unidad tras una devolución.
     * @param idLibro ID del libro.
     * @return true si se incrementó con éxito.
     */
    public synchronized boolean incrementarStock(int idLibro) {
        String sql = "UPDATE libros SET stock = stock + 1 WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idLibro);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al incrementar stock (synchronized): " + e.getMessage());
        }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM libros WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar libro: " + e.getMessage());
        }
        return false;
    }

    private Libro mapResultSetToLibro(ResultSet rs) throws SQLException {
        return new Libro(
            rs.getInt("id"),
            rs.getString("titulo"),
            rs.getString("autor"),
            rs.getString("isbn"),
            rs.getString("editorial"),
            rs.getInt("stock"),
            rs.getInt("id_categoria"),
            rs.getString("categoria_nombre")
        );
    }
}
