package com.biblioteca.controller;

import com.biblioteca.dao.CategoriaDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.model.Categoria;
import com.biblioteca.model.Libro;

import java.util.List;

public class LibroController {

    private final LibroDAO libroDAO;
    private final CategoriaDAO categoriaDAO;

    public LibroController() {
        this.libroDAO = new LibroDAO();
        this.categoriaDAO = new CategoriaDAO();
    }

    public List<Libro> listarTodos() {
        return libroDAO.obtenerTodos();
    }

    public List<Libro> buscar(String criterio, Integer idCategoria) {
        return libroDAO.buscar(criterio, idCategoria);
    }

    public boolean guardarLibro(Libro libro) {
        if (libro.getId() > 0) {
            return libroDAO.actualizar(libro);
        } else {
            return libroDAO.insertar(libro);
        }
    }

    public boolean eliminarLibro(int id) {
        return libroDAO.eliminar(id);
    }

    public List<Categoria> listarCategorias() {
        return categoriaDAO.obtenerTodas();
    }

    public boolean guardarCategoria(Categoria categoria) {
        if (categoria.getId() > 0) {
            return categoriaDAO.actualizar(categoria);
        } else {
            return categoriaDAO.insertar(categoria);
        }
    }
}
