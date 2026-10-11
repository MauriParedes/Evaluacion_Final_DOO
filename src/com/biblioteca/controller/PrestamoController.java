package com.biblioteca.controller;

import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.model.Prestamo;

import javax.swing.SwingWorker;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class PrestamoController {

    private final PrestamoDAO prestamoDAO;
    private final LibroDAO libroDAO;

    public PrestamoController() {
        this.prestamoDAO = new PrestamoDAO();
        this.libroDAO = new LibroDAO();
    }

    //Interfaz de callback para notificar el resultado de operaciones asíncronas en la GUI.

    public interface Callback {
        void onResultado(boolean exito, String mensaje);
    }

    /**
     * Ejecuta en un hilo independiente la operación de registrar un préstamo,
     * actualizar el stock del libro y registrar el historial del estudiante.
     * Evita el bloqueo de la interfaz gráfica durante la operación.
     */
    public void registrarPrestamoAsync(int idEstudiante, int idLibro, Date fechaDevolucion, Callback callback) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String mensajeError = "";

            @Override
            protected Boolean doInBackground() {
                // Se ejecuta en un hilo secundario fuera de la EDT
                try {
                    // Simular un brevísimo retardo si fuera necesario para verificar fluidez UI
                    Thread.sleep(100);

                    // Operación sincronizada a nivel de DAO y Base de Datos
                    boolean exito = prestamoDAO.registrarPrestamoTransaccional(idEstudiante, idLibro, fechaDevolucion);
                    if (!exito) {
                        mensajeError = "No se pudo realizar el préstamo. Verifique disponibilidad de stock.";
                    }
                    return exito;
                } catch (Exception e) {
                    mensajeError = "Error al procesar préstamo: " + e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (callback != null) {
                        if (exito) {
                            callback.onResultado(true, "¡Préstamo registrado exitosamente!");
                        } else {
                            callback.onResultado(false, mensajeError);
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onResultado(false, "Error inesperado en hilo de préstamo: " + e.getMessage());
                    }
                }
            }
        };
        worker.execute();
    }

    // Ejecuta en segundo plano la devolución de un libro.

    public void registrarDevolucionAsync(int idPrestamo, Callback callback) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String mensajeError = "";

            @Override
            protected Boolean doInBackground() {
                try {
                    Thread.sleep(100);
                    boolean exito = prestamoDAO.registrarDevolucion(idPrestamo);
                    if (!exito) {
                        mensajeError = "No se pudo registrar la devolución del préstamo.";
                    }
                    return exito;
                } catch (Exception e) {
                    mensajeError = "Error en devolución: " + e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (callback != null) {
                        if (exito) {
                            callback.onResultado(true, "¡Devolución registrada correctamente! Stock actualizado.");
                        } else {
                            callback.onResultado(false, mensajeError);
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onResultado(false, "Error inesperado al devolver libro: " + e.getMessage());
                    }
                }
            }
        };
        worker.execute();
    }

    public List<Prestamo> listarTodos() {
        return prestamoDAO.obtenerTodos();
    }

    public List<Prestamo> listarActivos() {
        return prestamoDAO.obtenerActivos();
    }

    public List<Prestamo> listarHistorialEstudiante(int idEstudiante) {
        return prestamoDAO.obtenerHistorialEstudiante(idEstudiante);
    }
}
