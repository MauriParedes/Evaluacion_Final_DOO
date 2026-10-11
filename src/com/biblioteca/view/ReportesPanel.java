package com.biblioteca.view;

import com.biblioteca.controller.EstudianteController;
import com.biblioteca.controller.ReporteController;
import com.biblioteca.model.Estudiante;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.ReporteItem;
import com.biblioteca.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;

/**
 * Panel de Generación de Reportes del Sistema.
 * Incluye:
 * 1. Libros más prestados.
 * 2. Historial por estudiante.
 * 3. Listado de libros actualmente en préstamo.
 */
public class ReportesPanel extends JPanel {

    private final ReporteController reporteController;
    private final EstudianteController estudianteController;
    private final Usuario usuarioActual;

    private JTabbedPane tabbedPane;

    // Tabla Reporte 1
    private DefaultTableModel modelMasPrestados;
    private JTable tableMasPrestados;

    // Tabla Reporte 2
    private DefaultTableModel modelHistorialEstudiante;
    private JTable tableHistorialEstudiante;
    private JComboBox<Estudiante> comboEstudiantesReporte;

    // Tabla Reporte 3
    private DefaultTableModel modelActivos;
    private JTable tableActivos;

    public ReportesPanel(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.reporteController = new ReporteController();
        this.estudianteController = new EstudianteController();

        initUI();
        cargarReportes();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernUIUtils.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerCard = ModernUIUtils.createCardPanel();
        headerCard.setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Informes y Reportes Estadísticos");
        lblTitulo.setFont(ModernUIUtils.FONT_TITLE);
        lblTitulo.setForeground(ModernUIUtils.COLOR_TEXT_MAIN);

        JLabel lblSub = new JLabel("Módulo de toma de decisiones y control de inventario de biblioteca");
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerCard.add(lblTitulo, BorderLayout.NORTH);
        headerCard.add(lblSub, BorderLayout.SOUTH);

        // Tabbed Pane de Reportes
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernUIUtils.FONT_BOLD);
        tabbedPane.setBackground(ModernUIUtils.COLOR_SURFACE);

        tabbedPane.addTab("📊 Libros Más Prestados", crearPanelMasPrestados());
        tabbedPane.addTab("👤 Historial por Estudiante", crearPanelHistorialEstudiante());
        tabbedPane.addTab("📖 Libros Activos en Préstamo", crearPanelPrestamosActivos());

        add(headerCard, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel crearPanelMasPrestados() {
        JPanel panel = ModernUIUtils.createCardPanel();
        panel.setLayout(new BorderLayout(0, 12));

        JLabel lblTitle = new JLabel("Ranking de Libros con Mayor Demanda de Préstamo");
        lblTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY);

        String[] cols = {"Posición", "Título del Libro", "Autor", "Categoría", "Total de Préstamos Realizados"};
        modelMasPrestados = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableMasPrestados = new JTable(modelMasPrestados);
        ModernUIUtils.styleTable(tableMasPrestados);

        tableMasPrestados.getColumnModel().getColumn(0).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableMasPrestados.getColumnModel().getColumn(4).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());

        tableMasPrestados.getColumnModel().getColumn(0).setPreferredWidth(70);
        tableMasPrestados.getColumnModel().getColumn(1).setPreferredWidth(260);
        tableMasPrestados.getColumnModel().getColumn(2).setPreferredWidth(180);
        tableMasPrestados.getColumnModel().getColumn(3).setPreferredWidth(140);
        tableMasPrestados.getColumnModel().getColumn(4).setPreferredWidth(180);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableMasPrestados), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelHistorialEstudiante() {
        JPanel panel = ModernUIUtils.createCardPanel();
        panel.setLayout(new BorderLayout(0, 12));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterBar.setOpaque(false);

        JLabel lblEst = new JLabel("Seleccionar Estudiante:");
        lblEst.setFont(ModernUIUtils.FONT_BOLD);

        comboEstudiantesReporte = new JComboBox<>();
        comboEstudiantesReporte.setFont(ModernUIUtils.FONT_REGULAR);
        comboEstudiantesReporte.setPreferredSize(new Dimension(280, 34));

        JButton btnConsultar = ModernUIUtils.createStyledButton("Consultar Historial", ModernUIUtils.COLOR_PRIMARY, Color.WHITE);

        filterBar.add(lblEst);
        filterBar.add(comboEstudiantesReporte);
        filterBar.add(btnConsultar);

        String[] cols = {"ID Préstamo", "Libro Prestado", "Autor", "Fecha Préstamo", "Fecha Devolución", "Estado"};
        modelHistorialEstudiante = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableHistorialEstudiante = new JTable(modelHistorialEstudiante);
        ModernUIUtils.styleTable(tableHistorialEstudiante);

        tableHistorialEstudiante.getColumnModel().getColumn(0).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableHistorialEstudiante.getColumnModel().getColumn(3).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableHistorialEstudiante.getColumnModel().getColumn(4).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableHistorialEstudiante.getColumnModel().getColumn(5).setCellRenderer(new ModernUIUtils.StatusBadgeRenderer());

        panel.add(filterBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableHistorialEstudiante), BorderLayout.CENTER);

        btnConsultar.addActionListener(e -> cargarHistorialEstudianteSeleccionado());
        return panel;
    }

    private JPanel crearPanelPrestamosActivos() {
        JPanel panel = ModernUIUtils.createCardPanel();
        panel.setLayout(new BorderLayout(0, 12));

        JLabel lblTitle = new JLabel("Listado de Libros Actualmente Prestados (Sin Devolver)");
        lblTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY);

        String[] cols = {"ID Préstamo", "Estudiante", "RUT Estudiante", "Libro Prestado", "Fecha Préstamo", "Fecha Vencimiento", "Estado de Entrega"};
        modelActivos = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableActivos = new JTable(modelActivos);
        ModernUIUtils.styleTable(tableActivos);

        tableActivos.getColumnModel().getColumn(0).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableActivos.getColumnModel().getColumn(2).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableActivos.getColumnModel().getColumn(4).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableActivos.getColumnModel().getColumn(5).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableActivos.getColumnModel().getColumn(6).setCellRenderer(new ModernUIUtils.StatusBadgeRenderer());

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableActivos), BorderLayout.CENTER);
        return panel;
    }

    public void cargarReportes() {
        // Cargar Reporte 1: Libros más prestados
        modelMasPrestados.setRowCount(0);
        List<ReporteItem> masPrestados = reporteController.obtenerLibrosMasPrestados();
        int pos = 1;
        for (ReporteItem r : masPrestados) {
            modelMasPrestados.addRow(new Object[]{
                pos++,
                r.getTitulo(),
                r.getAutor(),
                r.getCategoria() != null ? r.getCategoria() : "Sin Categoría",
                r.getCantidadPrestamos()
            });
        }

        // Cargar combo estudiantes para Reporte 2
        comboEstudiantesReporte.removeAllItems();
        List<Estudiante> estudiantes = estudianteController.listarTodos();
        for (Estudiante est : estudiantes) {
            comboEstudiantesReporte.addItem(est);
        }
        if (!estudiantes.isEmpty()) {
            cargarHistorialEstudianteSeleccionado();
        }

        // Cargar Reporte 3: Préstamos activos
        modelActivos.setRowCount(0);
        List<Prestamo> activos = reporteController.obtenerLibrosEnPrestamoActivo();
        for (Prestamo p : activos) {
            String estado = p.esAtrasado() ? "Atrasado" : "En Préstamo";
            modelActivos.addRow(new Object[]{
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

    private void cargarHistorialEstudianteSeleccionado() {
        modelHistorialEstudiante.setRowCount(0);
        Estudiante est = (Estudiante) comboEstudiantesReporte.getSelectedItem();
        if (est == null) return;

        List<Prestamo> historial = reporteController.obtenerHistorialEstudiante(est.getId());
        for (Prestamo p : historial) {
            String estado = p.isDevuelto() ? "Devuelto" : (p.esAtrasado() ? "Atrasado" : "En Préstamo");
            modelHistorialEstudiante.addRow(new Object[]{
                p.getId(),
                p.getTituloLibro(),
                p.getAutorLibro(),
                p.getFechaPrestamo() != null ? p.getFechaPrestamo().toString() : "-",
                p.getFechaDevolucion() != null ? p.getFechaDevolucion().toString() : "-",
                estado
            });
        }
    }
}
