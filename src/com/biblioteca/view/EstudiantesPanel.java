package com.biblioteca.view;

import com.biblioteca.controller.EstudianteController;
import com.biblioteca.controller.PrestamoController;
import com.biblioteca.model.Estudiante;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;

// Permite registrar, editar, eliminar y visualizar el historial de cada estudiante.
public class EstudiantesPanel extends JPanel {

    private final EstudianteController estudianteController;
    private final PrestamoController prestamoController;
    private final Usuario usuarioActual;

    private JTable tableEstudiantes;
    private DefaultTableModel tableModel;
    private JTextField txtBuscar;

    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnVerHistorial;
    private JButton btnRefrescar;

    public EstudiantesPanel(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.estudianteController = new EstudianteController();
        this.prestamoController = new PrestamoController();
        initUI();
        cargarEstudiantes();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernUIUtils.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Toolbar
        JPanel topPanel = ModernUIUtils.createCardPanel();
        topPanel.setLayout(new BorderLayout(16, 0));

        JLabel lblTitulo = new JLabel("Gestión de Estudiantes");
        lblTitulo.setFont(ModernUIUtils.FONT_TITLE);
        lblTitulo.setForeground(ModernUIUtils.COLOR_TEXT_MAIN);
        topPanel.add(lblTitulo, BorderLayout.NORTH);

        // Panel de filtro
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterPanel.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar Estudiante:");
        lblBuscar.setFont(ModernUIUtils.FONT_BOLD);
        txtBuscar = new JTextField(20);
        txtBuscar.setFont(ModernUIUtils.FONT_REGULAR);
        txtBuscar.setPreferredSize(new Dimension(220, 34));

        JButton btnBuscar = ModernUIUtils.createStyledButton("Buscar", ModernUIUtils.COLOR_PRIMARY, Color.BLACK);
        btnRefrescar = ModernUIUtils.createStyledButton("Refrescar", ModernUIUtils.COLOR_SECONDARY, Color.BLACK);

        filterPanel.add(lblBuscar);
        filterPanel.add(txtBuscar);
        filterPanel.add(btnBuscar);
        filterPanel.add(btnRefrescar);

        // Panel de acciones
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionsPanel.setOpaque(false);

        btnVerHistorial = ModernUIUtils.createStyledButton("Ver Historial", new Color(147, 51, 234), Color.BLACK);
        actionsPanel.add(btnVerHistorial);

        if (usuarioActual != null && usuarioActual.esBibliotecario()) {
            btnNuevo = ModernUIUtils.createStyledButton("+ Nuevo Estudiante", ModernUIUtils.COLOR_PRIMARY, Color.BLACK);
            btnEditar = ModernUIUtils.createStyledButton("Editar", new Color(16, 185, 129), Color.BLACK);
            btnEliminar = ModernUIUtils.createStyledButton("Eliminar", ModernUIUtils.COLOR_DANGER_TEXT, Color.BLACK);

            actionsPanel.add(btnNuevo);
            actionsPanel.add(btnEditar);
            actionsPanel.add(btnEliminar);
        }

        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(actionsPanel, BorderLayout.SOUTH);

        // Tabla
        String[] columnNames = {"ID", "Nombre Completo", "RUT", "Curso", "Correo Electrónico"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableEstudiantes = new JTable(tableModel);
        ModernUIUtils.styleTable(tableEstudiantes);

        tableEstudiantes.getColumnModel().getColumn(0).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableEstudiantes.getColumnModel().getColumn(2).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableEstudiantes.getColumnModel().getColumn(3).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());

        tableEstudiantes.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableEstudiantes.getColumnModel().getColumn(1).setPreferredWidth(220);
        tableEstudiantes.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableEstudiantes.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableEstudiantes.getColumnModel().getColumn(4).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(tableEstudiantes);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUIUtils.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Listeners
        btnBuscar.addActionListener(e -> filtrarEstudiantes());
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarEstudiantes();
        });
        btnVerHistorial.addActionListener(e -> mostrarHistorialSeleccionado());

        if (usuarioActual != null && usuarioActual.esBibliotecario()) {
            btnNuevo.addActionListener(e -> abrirModalFormulario(null));
            btnEditar.addActionListener(e -> editarEstudianteSeleccionado());
            btnEliminar.addActionListener(e -> eliminarEstudianteSeleccionado());
        }
    }

    public void cargarEstudiantes() {
        tableModel.setRowCount(0);
        List<Estudiante> lista = estudianteController.listarTodos();
        for (Estudiante e : lista) {
            tableModel.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()});
        }
    }

    private void filtrarEstudiantes() {
        tableModel.setRowCount(0);
        String criterio = txtBuscar.getText().trim();
        List<Estudiante> lista = estudianteController.buscar(criterio);
        for (Estudiante e : lista) {
            tableModel.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()});
        }
    }

    private void editarEstudianteSeleccionado() {
        int row = tableEstudiantes.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un estudiante de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Estudiante e = estudianteController.obtenerPorId(id);
        if (e != null) {
            abrirModalFormulario(e);
        }
    }

    private void eliminarEstudianteSeleccionado() {
        int row = tableEstudiantes.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un estudiante para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String nombre = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al estudiante '" + nombre + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (estudianteController.eliminarEstudiante(id)) {
                JOptionPane.showMessageDialog(this, "Estudiante eliminado con éxito.");
                cargarEstudiantes();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el estudiante (posee préstamos asociados).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarHistorialSeleccionado() {
        int row = tableEstudiantes.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un estudiante para consultar su historial.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idEstudiante = (int) tableModel.getValueAt(row, 0);
        String nombreEstudiante = (String) tableModel.getValueAt(row, 1);

        List<Prestamo> historial = prestamoController.listarHistorialEstudiante(idEstudiante);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Historial de Préstamos - " + nombreEstudiante, true);
        dialog.setSize(680, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(ModernUIUtils.COLOR_SURFACE);

        JLabel lblTitle = new JLabel("Historial de Libros solicitados por " + nombreEstudiante);
        lblTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY);

        String[] cols = {"ID Préstamo", "Libro", "Fecha Préstamo", "Fecha Vencimiento", "Estado"};
        DefaultTableModel modelHistorial = new DefaultTableModel(cols, 0);
        for (Prestamo p : historial) {
            String estado = p.isDevuelto() ? "Devuelto" : (p.esAtrasado() ? "Atrasado" : "En Préstamo");
            modelHistorial.addRow(new Object[]{
                p.getId(),
                p.getTituloLibro(),
                p.getFechaPrestamo() != null ? p.getFechaPrestamo().toString() : "-",
                p.getFechaDevolucion() != null ? p.getFechaDevolucion().toString() : "-",
                estado
            });
        }

        JTable tableHist = new JTable(modelHistorial);
        ModernUIUtils.styleTable(tableHist);
        tableHist.getColumnModel().getColumn(4).setCellRenderer(new ModernUIUtils.StatusBadgeRenderer());

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableHist), BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void abrirModalFormulario(Estudiante estudianteExistente) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                estudianteExistente == null ? "Nuevo Estudiante" : "Editar Estudiante", true);
        dialog.setSize(400, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ModernUIUtils.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JTextField txtNombre = new JTextField(estudianteExistente != null ? estudianteExistente.getNombre() : "", 20);
        JTextField txtRut = new JTextField(estudianteExistente != null ? estudianteExistente.getRut() : "", 20);
        JTextField txtCurso = new JTextField(estudianteExistente != null ? estudianteExistente.getCurso() : "", 20);
        JTextField txtCorreo = new JTextField(estudianteExistente != null ? estudianteExistente.getCorreo() : "", 20);

        agregarCampoForm(panel, gbc, 0, "Nombre Completo:", txtNombre);
        agregarCampoForm(panel, gbc, 2, "RUT (ej: 12345678-9):", txtRut);
        agregarCampoForm(panel, gbc, 4, "Curso (ej: 3ro Medio A):", txtCurso);
        agregarCampoForm(panel, gbc, 6, "Correo Electrónico:", txtCorreo);

        gbc.gridy = 8;
        JButton btnGuardar = ModernUIUtils.createStyledButton("Guardar Estudiante", ModernUIUtils.COLOR_PRIMARY, Color.WHITE);
        panel.add(btnGuardar, gbc);

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String rut = txtRut.getText().trim();
            String curso = txtCurso.getText().trim();
            String correo = txtCorreo.getText().trim();

            if (nombre.isEmpty() || rut.isEmpty() || curso.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nombre, RUT y Curso son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Estudiante est = estudianteExistente != null ? estudianteExistente : new Estudiante();
            est.setNombre(nombre);
            est.setRut(rut);
            est.setCurso(curso);
            est.setCorreo(correo);

            if (estudianteController.guardarEstudiante(est)) {
                JOptionPane.showMessageDialog(dialog, "Estudiante guardado correctamente.");
                dialog.dispose();
                cargarEstudiantes();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al guardar el estudiante. Verifique si el RUT ya está registrado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void agregarCampoForm(JPanel panel, GridBagConstraints gbc, int gridy, String labelText, Component comp) {
        gbc.gridy = gridy;
        JLabel label = new JLabel(labelText);
        label.setFont(ModernUIUtils.FONT_BOLD);
        panel.add(label, gbc);

        gbc.gridy = gridy + 1;
        comp.setFont(ModernUIUtils.FONT_REGULAR);
        panel.add(comp, gbc);
    }
}
