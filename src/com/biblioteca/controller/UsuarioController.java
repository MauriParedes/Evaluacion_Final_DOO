package com.biblioteca.controller;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.model.Usuario;

import java.util.List;

// Controlador de Autenticación y gestión de Usuarios del sistema.
public class UsuarioController {

    private final UsuarioDAO usuarioDAO;

    public UsuarioController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public Usuario autenticar(String rutOCorreo, String contraseña) {
        if (rutOCorreo == null || rutOCorreo.trim().isEmpty() || contraseña == null || contraseña.trim().isEmpty()) {
            return null;
        }
        return usuarioDAO.autenticar(rutOCorreo.trim(), contraseña.trim());
    }

    public List<Usuario> listarTodos() {
        return usuarioDAO.obtenerTodos();
    }

    public boolean guardarUsuario(Usuario u) {
        if (u.getId() > 0) {
            return usuarioDAO.actualizar(u);
        } else {
            return usuarioDAO.insertar(u);
        }
    }

    public boolean eliminarUsuario(int id) {
        return usuarioDAO.eliminar(id);
    }
}
