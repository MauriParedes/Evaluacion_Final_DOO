package com.biblioteca.view;

import com.biblioteca.controller.EstudianteController;
import com.biblioteca.controller.LibroController;
import com.biblioteca.controller.PrestamoController;
import com.biblioteca.model.Estudiante;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//Panel de Gestión de Préstamos y Devoluciones.

public class PrestamosPanel extends JPanel {

    private final PrestamoController prestamoController;
    private final EstudianteController estudianteController;
    private final LibroController libroController;
    private final Usuario usuarioActual;

    private JTable tablePrestamos;
    private DefaultTableModel tableModel;

    private JComboBox<Estudiante> comboEstudiantes;
    private JComboBox<Libro> comboLibros;
    private JSpinner spinDiasPrestamo;
    private JButton btnRegistrarPrestamo;
    private JButton btnDevolver;
    private JButton btnRefrescar;
    private JProgressBar progressBar;
    private JLabel lblEstadoHilo;

    public PrestamosPanel(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.prestamoController = new PrestamoController();
        this.estudianteController = new EstudianteController();
        this.libroController = new LibroController();

        initUI();
        cargarCombos();
        cargarPrestamos();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernUIUtils.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header and Form Card (Nuevo Préstamo)
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        JLabel lblTitulo = new JLabel("Préstamos y Devoluciones de Libros");
        lblTitulo.setFont(ModernUIUtils.FONT_TITLE);
        lblTitulo.setForeground(ModernUIUtils.COLOR_TEXT_MAIN);
        topContainer.add(lblTitulo, BorderLayout.NORTH);

        // Card Formulario de Préstamo
        JPanel formCard = ModernUIUtils.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Estudiante
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblEstudiante = new JLabel("Seleccionar Estudiante:");
        lblEstudiante.setFont(ModernUIUtils.FONT_BOLD);
        formCard.add(lblEstudiante, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        comboEstudiantes = new JComboBox<>();
        comboEstudiantes.setFont(ModernUIUtils.FONT_REGULAR);
        comboEstudiantes.setPreferredSize(new Dimension(280, 34));
        formCard.add(comboEstudiantes, gbc);

        // Fila 1: Libro
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblLibro = new JLabel("Seleccionar Libro:");
        lblLibro.setFont(ModernUIUtils.FONT_BOLD);
        formCard.add(lblLibro, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        comboLibros = new JComboBox<>();
        comboLibros.setFont(ModernUIUtils.FONT_REGULAR);
        comboLibros.setPreferredSize(new Dimension(280, 34));
        formCard.add(comboLibros, gbc);

        // Fila 2: Días de préstamo (Cálculo automático de devolución)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblDias = new JLabel("Plazo de Préstamo (días):");
        lblDias.setFont(ModernUIUtils.FONT_BOLD);
        formCard.add(lblDias, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        JPanel diasPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        diasPanel.setOpaque(false);
        spinDiasPrestamo = new JSpinner(new SpinnerNumberModel(7, 1, 30, 1));
        spinDiasPrestamo.setFont(ModernUIUtils.FONT_REGULAR);
        spinDiasPrestamo.setPreferredSize(new Dimension(70, 32));
        JLabel lblCalcHint = new JLabel("(Se calcula automáticamente la fecha de vencimiento)");
        lblCalcHint.setFont(ModernUIUtils.FONT_SMALL);
        lblCalcHint.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);
        diasPanel.add(spinDiasPrestamo);
        diasPanel.add(lblCalcHint);
        formCard.add(diasPanel, gbc);

        // Fila 3: Botón Préstamo y Estado del Hilo
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel btnSubmitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        btnSubmitPanel.setOpaque(false);

        btnRegistrarPrestamo = ModernUIUtils.createStyledButton("Procesar Préstamo (Segundo Plano)", ModernUIUtils.COLOR_PRIMARY, Color.BLACK);
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(140, 20));

        lblEstadoHilo = new JLabel("");
        lblEstadoHilo.setFont(ModernUIUtils.FONT_BOLD);
        lblEstadoHilo.setForeground(ModernUIUtils.COLOR_PRIMARY_HOVER);

        btnSubmitPanel.add(btnRegistrarPrestamo);
        btnSubmitPanel.add(progressBar);
        btnSubmitPanel.add(lblEstadoHilo);

        formCard.add(btnSubmitPanel, gbc);

        topContainer.add(formCard, BorderLayout.CENTER);

        // Center Panel: Tabla de Préstamos Registrados
        JPanel tableContainer = ModernUIUtils.createCardPanel();
        tableContainer.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel lblSub = new JLabel("Registros de Préstamos Activos e Histórico");
        lblSub.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MAIN);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableActions.setOpaque(false);

        btnDevolver = ModernUIUtils.createStyledButton("Registrar Devolución", new Color(16, 185, 129), Color.BLACK);
        btnRefrescar = ModernUIUtils.createStyledButton("Refrescar", ModernUIUtils.COLOR_SECONDARY, Color.black);

        tableActions.add(btnDevolver);
        tableActions.add(btnRefrescar);

        tableHeader.add(lblSub, BorderLayout.WEST);
        tableHeader.add(tableActions, BorderLayout.EAST);

        String[] cols = {"ID", "Estudiante", "RUT Estudiante", "Libro", "Fecha Préstamo", "Fecha Vencimiento", "Estado"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablePrestamos = new JTable(tableModel);
        ModernUIUtils.styleTable(tablePrestamos);

        tablePrestamos.getColumnModel().getColumn(0).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tablePrestamos.getColumnModel().getColumn(2).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tablePrestamos.getColumnModel().getColumn(4).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tablePrestamos.getColumnModel().getColumn(5).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tablePrestamos.getColumnModel().getColumn(6).setCellRenderer(new ModernUIUtils.StatusBadgeRenderer());

        tablePrestamos.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablePrestamos.getColumnModel().getColumn(1).setPreferredWidth(160);
        tablePrestamos.getColumnModel().getColumn(2).setPreferredWidth(110);
        tablePrestamos.getColumnModel().getColumn(3).setPreferredWidth(200);
        tablePrestamos.getColumnModel().getColumn(4).setPreferredWidth(110);
        tablePrestamos.getColumnModel().getColumn(5).setPreferredWidth(110);
        tablePrestamos.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(tablePrestamos);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUIUtils.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableContainer.add(tableHeader, BorderLayout.NORTH);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);

        // Listeners
        btnRegistrarPrestamo.addActionListener(e -> ejecutarPrestamoEnSegundoPlano());
        btnDevolver.addActionListener(e -> ejecutarDevolucionEnSegundoPlano());
        btnRefrescar.addActionListener(e -> {
            cargarCombos();
            cargarPrestamos();
        });
    }

    public void cargarCombos() {
        comboEstudiantes.removeAllItems();
        List<Estudiante> estudiantes = estudianteController.listarTodos();
        for (Estudiante est : estudiantes) {
            comboEstudiantes.addItem(est);
        }

        // Si es rol estudiante, auto-seleccionar al estudiante autenticado
        if (usuarioActual != null && usuarioActual.esEstudiante()) {
            Estudiante eLogueado = estudianteController.obtenerPorRut(usuarioActual.getRut());
            if (eLogueado != null) {
                comboEstudiantes.setSelectedItem(eLogueado);
                comboEstudiantes.setEnabled(false); // Solo puede pedir para sí mismo
            }
        }

        comboLibros.removeAllItems();
        List<Libro> libros = libroController.listarTodos();
        for (Libro lib : libros) {
            comboLibros.addItem(lib);
        }
    }

    public void cargarPrestamos() {
        tableModel.setRowCount(0);
        List<Prestamo> lista;

        if (usuarioActual != null && usuarioActual.esEstudiante()) {
            Estudiante eLogueado = estudianteController.obtenerPorRut(usuarioActual.getRut());
            if (eLogueado != null) {
                lista = prestamoController.listarHistorialEstudiante(eLogueado.getId());
            } else {
                lista = List.of();
            }
        } else {
            lista = prestamoController.listarTodos();
        }

        for (Prestamo p : lista) {
            String estado;
            if (p.isDevuelto()) {
                estado = "Devuelto";
            } else if (p.esAtrasado()) {
                estado = "Atrasado";
            } else {
                estado = "En Préstamo";
            }

            tableModel.addRow(new Object[]{
                p.getId(),
                p.getNombreEstudiante(),
                p.getRutEstudiante(),
                p.getTituloLibro(),
                p.getFechaPrestamo() != null ? p.getFechaPrestamo().toString() : "-",
                p.getFechaDevolucion() != null ? p.getFechaDevolucion().toString() : "-",
                estado
            });
        }
    }

    /**
     * Inicia un hilo en segundo plano mediante PrestamoController
     * para procesar el préstamo de forma asíncrona sin congelar la GUI.
     */
    private void ejecutarPrestamoEnSegundoPlano() {
        Estudiante est = (Estudiante) comboEstudiantes.getSelectedItem();
        Libro libro = (Libro) comboLibros.getSelectedItem();
        int dias = (int) spinDiasPrestamo.getValue();

        if (est == null || libro == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un estudiante y un libro válido.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (libro.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, "El libro '" + libro.getTitulo() + "' no cuenta con stock disponible.", "Sin Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calcular fecha de devolución
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, dias);
        Date fechaDevolucion = cal.getTime();

        // Mostrar indicador de proceso multihilo
        btnRegistrarPrestamo.setEnabled(false);
        progressBar.setVisible(true);
        lblEstadoHilo.setText("Ejecutando proceso de préstamo en hilo secundario...");

        // Llamar al controlador asíncrono (que usa SwingWorker)
        prestamoController.registrarPrestamoAsync(est.getId(), libro.getId(), fechaDevolucion, (exito, mensaje) -> {
            // Se ejecuta de vuelta en la EDT tras finalizar el hilo de segundo plano
            btnRegistrarPrestamo.setEnabled(true);
            progressBar.setVisible(false);
            lblEstadoHilo.setText("");

            if (exito) {
                JOptionPane.showMessageDialog(PrestamosPanel.this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarCombos();
                cargarPrestamos();
            } else {
                JOptionPane.showMessageDialog(PrestamosPanel.this, mensaje, "Error en Préstamo", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void ejecutarDevolucionEnSegundoPlano() {
        int selectedRow = tablePrestamos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo de la tabla para registrar la devolución.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPrestamo = (int) tableModel.getValueAt(selectedRow, 0);
        String estadoActual = (String) tableModel.getValueAt(selectedRow, 6);

        if ("Devuelto".equalsIgnoreCase(estadoActual)) {
            JOptionPane.showMessageDialog(this, "Este préstamo ya ha sido devuelto anteriormente.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        btnDevolver.setEnabled(false);
        progressBar.setVisible(true);
        lblEstadoHilo.setText("Registrando devolución en segundo plano...");

        prestamoController.registrarDevolucionAsync(idPrestamo, (exito, mensaje) -> {
            btnDevolver.setEnabled(true);
            progressBar.setVisible(false);
            lblEstadoHilo.setText("");

            if (exito) {
                JOptionPane.showMessageDialog(PrestamosPanel.this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarCombos();
                cargarPrestamos();
            } else {
                JOptionPane.showMessageDialog(PrestamosPanel.this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
