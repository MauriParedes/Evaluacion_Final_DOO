package com.biblioteca.controller;

import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.ReporteItem;

import java.util.List;

//Controlador para la generación y consolidación de reportes del sistema.

public class ReporteController {

    private final PrestamoDAO prestamoDAO;

    public ReporteController() {
        this.prestamoDAO = new PrestamoDAO();
    }

    // Reporte 1: Libros más prestados.

    public List<ReporteItem> obtenerLibrosMasPrestados() {
        return prestamoDAO.obtenerLibrosMasPrestados();
    }

    //Reporte 2: Historial por estudiante.

    public List<Prestamo> obtenerHistorialEstudiante(int idEstudiante) {
        return prestamoDAO.obtenerHistorialEstudiante(idEstudiante);
    }

    //Reporte 3: Listado de libros actualmente en préstamo (sin devolver).

    public List<Prestamo> obtenerLibrosEnPrestamoActivo() {
        return prestamoDAO.obtenerActivos();
    }
}
