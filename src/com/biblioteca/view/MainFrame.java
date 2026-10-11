package com.biblioteca.view;

import com.biblioteca.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

//Ventana Principal del Sistema de Gestión de Biblioteca Escolar.
public class MainFrame extends JFrame {

    private final Usuario usuarioActual;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    private LibrosPanel librosPanel;
    private EstudiantesPanel estudiantesPanel;
    private PrestamosPanel prestamosPanel;
    private ReportesPanel reportesPanel;

    private JLabel lblStatusConn;
    private JLabel lblUsuarioInfo;

    public MainFrame(Usuario usuario) {
        this.usuarioActual = usuario;
        initUI();
    }

    private void initUI() {
        setTitle("Sistema de Gestión de Biblioteca Escolar - [" + (usuarioActual != null ? usuarioActual.getRol().toUpperCase() : "INVITADO") + "]");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ModernUIUtils.COLOR_BACKGROUND);

        // 1. Header Superior
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(ModernUIUtils.COLOR_SIDEBAR);
        headerBar.setBorder(new EmptyBorder(12, 24, 12, 24));

        JLabel lblBrand = new JLabel("📚 BIBLIOTECA ESCOLAR DIGITAL");
        lblBrand.setFont(ModernUIUtils.FONT_TITLE);
        lblBrand.setForeground(Color.WHITE);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        String userDisplay = usuarioActual != null ? usuarioActual.getNombre() + " (" + usuarioActual.getRol() + ")" : "Invitado";
        lblUsuarioInfo = new JLabel("👤 " + userDisplay);
        lblUsuarioInfo.setFont(ModernUIUtils.FONT_BOLD);
        lblUsuarioInfo.setForeground(new Color(226, 232, 240));

        JButton btnLogout = ModernUIUtils.createStyledButton("Cerrar Sesión", ModernUIUtils.COLOR_DANGER_TEXT, Color.BLACK);
        btnLogout.setPreferredSize(new Dimension(120, 32));
        btnLogout.addActionListener(e -> cerrarSesion());

        userPanel.add(lblUsuarioInfo);
        userPanel.add(btnLogout);

        headerBar.add(lblBrand, BorderLayout.WEST);
        headerBar.add(userPanel, BorderLayout.EAST);

        // 2. Sidebar Lateral de Navegación
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ModernUIUtils.COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ModernUIUtils.COLOR_BACKGROUND);

        // Instanciar paneles de módulos
        librosPanel = new LibrosPanel(usuarioActual);
        estudiantesPanel = new EstudiantesPanel(usuarioActual);
        prestamosPanel = new PrestamosPanel(usuarioActual);
        reportesPanel = new ReportesPanel(usuarioActual);

        contentPanel.add(librosPanel, "LIBROS");
        contentPanel.add(estudiantesPanel, "ESTUDIANTES");
        contentPanel.add(prestamosPanel, "PRESTAMOS");
        contentPanel.add(reportesPanel, "REPORTES");

        // Construir Botones del Menú de Navegación según Rol
        sidebar.add(crearBotonNav("📖 Libros y Catálogo", "LIBROS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        if (usuarioActual != null && usuarioActual.esBibliotecario()) {
            sidebar.add(crearBotonNav("👥 Gestión Estudiantes", "ESTUDIANTES"));
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        sidebar.add(crearBotonNav("🔄 Préstamos y Devoluciones", "PRESTAMOS"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        if (usuarioActual != null && usuarioActual.esBibliotecario()) {
            sidebar.add(crearBotonNav("📊 Informes y Reportes", "REPORTES"));
        }

        // 3. Barra de Estado Inferior
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ModernUIUtils.COLOR_BORDER),
            new EmptyBorder(6, 16, 6, 16)
        ));

        lblStatusConn = new JLabel("🟢 Conexión a Base de Datos MySQL: ACTIVA");
        lblStatusConn.setFont(ModernUIUtils.FONT_SMALL);
        lblStatusConn.setForeground(new Color(22, 101, 52));

        JLabel lblCopyright = new JLabel("Sistema de Gestión Escolar v2.0 - MVC & DAO & Hilos");
        lblCopyright.setFont(ModernUIUtils.FONT_SMALL);
        lblCopyright.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        statusBar.add(lblStatusConn, BorderLayout.WEST);
        statusBar.add(lblCopyright, BorderLayout.EAST);

        rootPanel.add(headerBar, BorderLayout.NORTH);
        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(contentPanel, BorderLayout.CENTER);
        rootPanel.add(statusBar, BorderLayout.SOUTH);

        add(rootPanel);
        cardLayout.show(contentPanel, "LIBROS");
    }

    private JButton crearBotonNav(String texto, String cardName) {
        JButton btn = new JButton(texto);
        btn.setFont(ModernUIUtils.FONT_BOLD);
        btn.setForeground(Color.BLACK);
        btn.setBackground(new Color(30, 41, 59)); // Slate dark
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(216, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            // Refrescar vistas cuando se cambie a ellas
            if ("LIBROS".equals(cardName)) librosPanel.cargarLibros();
            if ("ESTUDIANTES".equals(cardName)) estudiantesPanel.cargarEstudiantes();
            if ("PRESTAMOS".equals(cardName)) {
                prestamosPanel.cargarCombos();
                prestamosPanel.cargarPrestamos();
            }
            if ("REPORTES".equals(cardName)) reportesPanel.cargarReportes();
        });

        return btn;
    }

    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Desea cerrar la sesión actual?",
                "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            // Re-abrir pantalla de Login
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            Usuario u = login.getUsuarioAutenticado();
            if (u != null) {
                MainFrame frame = new MainFrame(u);
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        }
    }
}
