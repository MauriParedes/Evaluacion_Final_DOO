package com.biblioteca.view;

import com.biblioteca.controller.LibroController;
import com.biblioteca.model.Categoria;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;

//Panel de Gestión e Inventario de Libros.

public class LibrosPanel extends JPanel {

    private final LibroController libroController;
    private final Usuario usuarioActual;

    private JTable tableLibros;
    private DefaultTableModel tableModel;
    private JTextField txtBuscar;
    private JComboBox<Categoria> comboCategoriasFiltro;

    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnRefrescar;

    public LibrosPanel(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.libroController = new LibroController();
        initUI();
        cargarCategoriasFiltro();
        cargarLibros();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernUIUtils.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Toolbar
        JPanel topPanel = ModernUIUtils.createCardPanel();
        topPanel.setLayout(new BorderLayout(16, 0));

        // Título de la vista
        JLabel lblTitulo = new JLabel("Catálogo e Inventario de Libros");
        lblTitulo.setFont(ModernUIUtils.FONT_TITLE);
        lblTitulo.setForeground(ModernUIUtils.COLOR_TEXT_MAIN);
        topPanel.add(lblTitulo, BorderLayout.NORTH);

        // Panel de búsqueda y filtros
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterPanel.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(ModernUIUtils.FONT_BOLD);
        txtBuscar = new JTextField(18);
        txtBuscar.setFont(ModernUIUtils.FONT_REGULAR);
        txtBuscar.setPreferredSize(new Dimension(200, 34));

        JLabel lblCat = new JLabel("Categoría:");
        lblCat.setFont(ModernUIUtils.FONT_BOLD);
        comboCategoriasFiltro = new JComboBox<>();
        comboCategoriasFiltro.setFont(ModernUIUtils.FONT_REGULAR);
        comboCategoriasFiltro.setPreferredSize(new Dimension(180, 34));

        JButton btnBuscar = ModernUIUtils.createStyledButton("Buscar", ModernUIUtils.COLOR_PRIMARY, Color.BLACK);

        filterPanel.add(lblBuscar);
        filterPanel.add(txtBuscar);
        filterPanel.add(lblCat);
        filterPanel.add(comboCategoriasFiltro);
        filterPanel.add(btnBuscar);

        // Panel de acciones (solo bibliotecario)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionsPanel.setOpaque(false);

        btnRefrescar = ModernUIUtils.createStyledButton("Refrescar", ModernUIUtils.COLOR_SECONDARY, Color.BLACK);
        actionsPanel.add(btnRefrescar);

        if (usuarioActual != null && usuarioActual.esBibliotecario()) {
            btnNuevo = ModernUIUtils.createStyledButton("+ Nuevo Libro", ModernUIUtils.COLOR_PRIMARY, Color.BLACK);
            btnEditar = ModernUIUtils.createStyledButton("Editar", new Color(16, 185, 129), Color.BLACK);
            btnEliminar = ModernUIUtils.createStyledButton("Eliminar", ModernUIUtils.COLOR_DANGER_TEXT, Color.BLACK);

            actionsPanel.add(btnNuevo);
            actionsPanel.add(btnEditar);
            actionsPanel.add(btnEliminar);
        }

        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(actionsPanel, BorderLayout.SOUTH);

        // Tabla de Libros
        String[] columnNames = {"ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock", "Estado"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableLibros = new JTable(tableModel);
        ModernUIUtils.styleTable(tableLibros);

        // Renderizadores de celda
        tableLibros.getColumnModel().getColumn(0).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableLibros.getColumnModel().getColumn(3).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableLibros.getColumnModel().getColumn(6).setCellRenderer(new ModernUIUtils.CenterTableCellRenderer());
        tableLibros.getColumnModel().getColumn(7).setCellRenderer(new ModernUIUtils.StatusBadgeRenderer());

        tableLibros.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableLibros.getColumnModel().getColumn(1).setPreferredWidth(220);
        tableLibros.getColumnModel().getColumn(2).setPreferredWidth(140);
        tableLibros.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableLibros.getColumnModel().getColumn(4).setPreferredWidth(120);
        tableLibros.getColumnModel().getColumn(5).setPreferredWidth(130);
        tableLibros.getColumnModel().getColumn(6).setPreferredWidth(60);
        tableLibros.getColumnModel().getColumn(7).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(tableLibros);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernUIUtils.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Listeners
        btnBuscar.addActionListener(e -> filtrarLibros());
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            comboCategoriasFiltro.setSelectedIndex(0);
            cargarLibros();
        });

        if (usuarioActual != null && usuarioActual.esBibliotecario()) {
            btnNuevo.addActionListener(e -> abrirModalFormulario(null));
            btnEditar.addActionListener(e -> editarLibroSeleccionado());
            btnEliminar.addActionListener(e -> eliminarLibroSeleccionado());
        }
    }

    private void cargarCategoriasFiltro() {
        comboCategoriasFiltro.removeAllItems();
        comboCategoriasFiltro.addItem(new Categoria(0, "-- Todas las Categorías --"));
        List<Categoria> categorias = libroController.listarCategorias();
        for (Categoria c : categorias) {
            comboCategoriasFiltro.addItem(c);
        }
    }

    public void cargarLibros() {
        tableModel.setRowCount(0);
        List<LibrosPanel.LibroTablaItem> items = obtenerItemsLibros(null, 0);
        for (LibrosPanel.LibroTablaItem item : items) {
            tableModel.addRow(item.toRowData());
        }
    }

    private void filtrarLibros() {
        tableModel.setRowCount(0);
        String criterio = txtBuscar.getText().trim();
        Categoria catSel = (Categoria) comboCategoriasFiltro.getSelectedItem();
        int idCat = catSel != null ? catSel.getId() : 0;

        List<LibrosPanel.LibroTablaItem> items = obtenerItemsLibros(criterio, idCat);
        for (LibrosPanel.LibroTablaItem item : items) {
            tableModel.addRow(item.toRowData());
        }
    }

    private List<LibroTablaItem> obtenerItemsLibros(String criterio, int idCategoria) {
        List<Libro> libros = libroController.buscar(criterio, idCategoria);
        return libros.stream().map(LibroTablaItem::new).toList();
    }

    private void editarLibroSeleccionado() {
        int selectedRow = tableLibros.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un libro de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idLibro = (int) tableModel.getValueAt(selectedRow, 0);
        Libro libro = libroController.buscar(null, 0).stream()
                .filter(l -> l.getId() == idLibro)
                .findFirst().orElse(null);

        if (libro != null) {
            abrirModalFormulario(libro);
        }
    }

    private void eliminarLibroSeleccionado() {
        int selectedRow = tableLibros.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un libro para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idLibro = (int) tableModel.getValueAt(selectedRow, 0);
        String titulo = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el libro '" + titulo + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (libroController.eliminarLibro(idLibro)) {
                JOptionPane.showMessageDialog(this, "Libro eliminado correctamente.");
                cargarLibros();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el libro. Es posible que tenga préstamos asociados.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirModalFormulario(Libro libroExistente) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                libroExistente == null ? "Nuevo Libro" : "Editar Libro", true);
        dialog.setSize(440, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ModernUIUtils.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JTextField txtTitulo = new JTextField(libroExistente != null ? libroExistente.getTitulo() : "", 20);
        JTextField txtAutor = new JTextField(libroExistente != null ? libroExistente.getAutor() : "", 20);
        JTextField txtIsbn = new JTextField(libroExistente != null ? libroExistente.getIsbn() : "", 20);
        JTextField txtEditorial = new JTextField(libroExistente != null ? libroExistente.getEditorial() : "", 20);
        JSpinner spinStock = new JSpinner(new SpinnerNumberModel(libroExistente != null ? libroExistente.getStock() : 5, 0, 999, 1));
        JComboBox<Categoria> comboCategorias = new JComboBox<>();

        List<Categoria> categorias = libroController.listarCategorias();
        for (Categoria c : categorias) {
            comboCategorias.addItem(c);
            if (libroExistente != null && c.getId() == libroExistente.getIdCategoria()) {
                comboCategorias.setSelectedItem(c);
            }
        }

        // Agregar campos
        agregarCampoForm(panel, gbc, 0, "Título:", txtTitulo);
        agregarCampoForm(panel, gbc, 2, "Autor:", txtAutor);
        agregarCampoForm(panel, gbc, 4, "ISBN:", txtIsbn);
        agregarCampoForm(panel, gbc, 6, "Editorial:", txtEditorial);
        agregarCampoForm(panel, gbc, 8, "Stock Inicial:", spinStock);
        agregarCampoForm(panel, gbc, 10, "Categoría:", comboCategorias);

        // Botón Guardar
        gbc.gridy = 12;
        JButton btnGuardar = ModernUIUtils.createStyledButton("Guardar Libro", ModernUIUtils.COLOR_PRIMARY, Color.WHITE);
        panel.add(btnGuardar, gbc);

        btnGuardar.addActionListener(e -> {
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            String isbn = txtIsbn.getText().trim();
            String editorial = txtEditorial.getText().trim();
            int stock = (int) spinStock.getValue();
            Categoria catSel = (Categoria) comboCategorias.getSelectedItem();

            if (titulo.isEmpty() || autor.isEmpty() || isbn.isEmpty() || catSel == null) {
                JOptionPane.showMessageDialog(dialog, "Complete todos los campos obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Libro l = libroExistente != null ? libroExistente : new Libro();
            l.setTitulo(titulo);
            l.setAutor(autor);
            l.setIsbn(isbn);
            l.setEditorial(editorial);
            l.setStock(stock);
            l.setIdCategoria(catSel.getId());

            if (libroController.guardarLibro(l)) {
                JOptionPane.showMessageDialog(dialog, "Libro guardado exitosamente.");
                dialog.dispose();
                cargarLibros();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al guardar el libro. Verifique si el ISBN ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
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

    private static class LibroTablaItem {
        private final Libro libro;

        public LibroTablaItem(Libro libro) {
            this.libro = libro;
        }

        public Object[] toRowData() {
            String estado = libro.getStock() > 0 ? "Disponible" : "Sin Stock";
            return new Object[]{
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getIsbn(),
                libro.getEditorial(),
                libro.getNombreCategoria() != null ? libro.getNombreCategoria() : "Sin Categoría",
                libro.getStock(),
                estado
            };
        }
    }
}
