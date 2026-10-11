package com.biblioteca.test;

import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.model.Libro;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== INICIANDO PRUEBA DE CONCURRENCIA Y CONTROL DE STOCK ===");

        LibroDAO libroDAO = new LibroDAO();
        PrestamoDAO prestamoDAO = new PrestamoDAO();

        // 1. Obtener o crear un libro de prueba con stock limitado (ej. stock = 3)
        Libro libro = libroDAO.obtenerPorId(1);
        if (libro == null) {
            System.err.println("No se encontró el libro ID 1 para la prueba.");
            return;
        }

        int stockInicial = libro.getStock();
        System.out.println("Libro de prueba: '" + libro.getTitulo() + "' - Stock inicial: " + stockInicial);

        // 2. Disparar 10 hilos concurrentes intentando pedir prestado el mismo libro al mismo tiempo
        int numHilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        AtomicInteger prestamosExitosos = new AtomicInteger(0);
        AtomicInteger prestamosFallidos = new AtomicInteger(0);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date fechaDevolucion = cal.getTime();

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= numHilos; i++) {
            final int estudianteId = (i % 5) + 1; // Estudiantes del 1 al 5
            final int threadId = i;
            executor.submit(() -> {
                boolean exito = prestamoDAO.registrarPrestamoTransaccional(estudianteId, libro.getId(), fechaDevolucion);
                if (exito) {
                    prestamosExitosos.incrementAndGet();
                    System.out.println("Hilo #" + threadId + ": Préstamo concedido con éxito.");
                } else {
                    prestamosFallidos.incrementAndGet();
                    System.out.println("Hilo #" + threadId + ": Préstamo RECHAZADO (Stock agotado / Bloqueado por concurrencia).");
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();

        // 3. Verificar stock final tras la ejecución concurrente
        Libro libroFinal = libroDAO.obtenerPorId(1);
        int stockFinal = libroFinal != null ? libroFinal.getStock() : -1;

        System.out.println("\n=== RESULTADOS DE LA PRUEBA DE CONCURRENCIA ===");
        System.out.println("Tiempo de ejecución: " + (endTime - startTime) + " ms");
        System.out.println("Préstamos Exitosos: " + prestamosExitosos.get());
        System.out.println("Préstamos Rechazados por falta de stock: " + prestamosFallidos.get());
        System.out.println("Stock Inicial: " + stockInicial);
        System.out.println("Stock Final en DB: " + stockFinal);

        // Validación de consistencia: Stock final nunca debe ser negativo y la suma debe ser exacta
        if (stockFinal >= 0 && (stockInicial - prestamosExitosos.get()) == stockFinal) {
            System.out.println("✅ PRUEBA DE CONCURRENCIA EXITOSA: El stock se mantuvo 100% consistente y libre de condiciones de carrera.");
        } else {
            System.err.println("❌ ERROR DE CONSISTENCIA: Se detectó desincronización o condición de carrera en el stock.");
        }
    }
}
