package com.biblioteca.controller;

import com.biblioteca.dao.EstudianteDAO;
import com.biblioteca.model.Estudiante;

import java.util.List;

public class EstudianteController {

    private final EstudianteDAO estudianteDAO;

    public EstudianteController() {
        this.estudianteDAO = new EstudianteDAO();
    }

    public List<Estudiante> listarTodos() {
        return estudianteDAO.obtenerTodos();
    }

    public List<Estudiante> buscar(String criterio) {
        return estudianteDAO.buscar(criterio);
    }

    public Estudiante obtenerPorId(int id) {
        return estudianteDAO.obtenerPorId(id);
    }

    public Estudiante obtenerPorRut(String rut) {
        return estudianteDAO.obtenerPorRut(rut);
    }

    public boolean guardarEstudiante(Estudiante e) {
        if (e.getId() > 0) {
            return estudianteDAO.actualizar(e);
        } else {
            return estudianteDAO.insertar(e);
        }
    }

    public boolean eliminarEstudiante(int id) {
        return estudianteDAO.eliminar(id);
    }
}
