package com.biblioteca.view;

import com.biblioteca.controller.UsuarioController;
import com.biblioteca.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

//Pantalla de Autenticación de Usuarios (Login).
public class LoginDialog extends JDialog {

    private JTextField txtRutOCorreo;
    private JPasswordField txtContraseña;
    private JButton btnIngresar;
    private JButton btnQuickBibliotecario;
    private JButton btnQuickEstudiante;
    private JLabel lblMensajeError;

    private final UsuarioController usuarioController;
    private Usuario usuarioAutenticado;

    public LoginDialog(Frame owner) {
        super(owner, "Sistema de Biblioteca Escolar - Autenticación", true);
        this.usuarioController = new UsuarioController();
        initUI();
    }

    private void initUI() {
        setSize(480, 520);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernUIUtils.COLOR_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Biblioteca Escolar", SwingConstants.CENTER);
        lblTitulo.setFont(ModernUIUtils.FONT_TITLE);
        lblTitulo.setForeground(ModernUIUtils.COLOR_PRIMARY);

        JLabel lblSubtitulo = new JLabel("Ingrese sus credenciales para continuar", SwingConstants.CENTER);
        lblSubtitulo.setFont(ModernUIUtils.FONT_REGULAR);
        lblSubtitulo.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerPanel.add(lblTitulo);
        headerPanel.add(lblSubtitulo);

        // Form Panel (Card Container)
        JPanel formCard = ModernUIUtils.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Campo RUT / Correo
        gbc.gridy = 0;
        JLabel lblUser = new JLabel("RUT o Correo Electrónico:");
        lblUser.setFont(ModernUIUtils.FONT_BOLD);
        lblUser.setForeground(ModernUIUtils.COLOR_TEXT_MAIN);
        formCard.add(lblUser, gbc);

        gbc.gridy = 1;
        txtRutOCorreo = new JTextField(20);
        txtRutOCorreo.setFont(ModernUIUtils.FONT_REGULAR);
        txtRutOCorreo.setPreferredSize(new Dimension(300, 36));
        formCard.add(txtRutOCorreo, gbc);

        // Campo Contraseña
        gbc.gridy = 2;
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(ModernUIUtils.FONT_BOLD);
        lblPass.setForeground(ModernUIUtils.COLOR_TEXT_MAIN);
        formCard.add(lblPass, gbc);

        gbc.gridy = 3;
        txtContraseña = new JPasswordField(20);
        txtContraseña.setFont(ModernUIUtils.FONT_REGULAR);
        txtContraseña.setPreferredSize(new Dimension(300, 36));
        formCard.add(txtContraseña, gbc);

        // Mensaje de Error
        gbc.gridy = 4;
        lblMensajeError = new JLabel("", SwingConstants.CENTER);
        lblMensajeError.setFont(ModernUIUtils.FONT_SMALL);
        lblMensajeError.setForeground(ModernUIUtils.COLOR_DANGER_TEXT);
        formCard.add(lblMensajeError, gbc);

        // Botón Ingresar
        gbc.gridy = 5;
        btnIngresar = ModernUIUtils.createStyledButton("Iniciar Sesión", ModernUIUtils.COLOR_PRIMARY, Color.BLUE);
        btnIngresar.setPreferredSize(new Dimension(300, 42));
        formCard.add(btnIngresar, gbc);

        // Panel de Acceso Rápido (para pruebas y evaluación)
        JPanel quickAccessPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        quickAccessPanel.setOpaque(false);
        quickAccessPanel.setBorder(BorderFactory.createTitledBorder("Acceso Rápido (Demostración)"));

        btnQuickBibliotecario = new JButton("Ingresar como Bibliotecario (Antonia Pérez)");
        btnQuickBibliotecario.setFont(ModernUIUtils.FONT_SMALL);
        btnQuickBibliotecario.setFocusPainted(false);
        btnQuickBibliotecario.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnQuickEstudiante = new JButton("Ingresar como Estudiante (Carlos Ruiz)");
        btnQuickEstudiante.setFont(ModernUIUtils.FONT_SMALL);
        btnQuickEstudiante.setFocusPainted(false);
        btnQuickEstudiante.setCursor(new Cursor(Cursor.HAND_CURSOR));

        quickAccessPanel.add(btnQuickBibliotecario);
        quickAccessPanel.add(btnQuickEstudiante);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formCard, BorderLayout.CENTER);
        mainPanel.add(quickAccessPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Listeners
        btnIngresar.addActionListener(e -> realizarLogin());

        txtContraseña.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }
        });

        btnQuickBibliotecario.addActionListener(e -> {
            txtRutOCorreo.setText("12345678-9");
            txtContraseña.setText("clave123");
            realizarLogin();
        });

        btnQuickEstudiante.addActionListener(e -> {
            txtRutOCorreo.setText("98765432-1");
            txtContraseña.setText("clave123");
            realizarLogin();
        });
    }

    private void realizarLogin() {
        String input = txtRutOCorreo.getText().trim();
        String pass = new String(txtContraseña.getPassword()).trim();

        if (input.isEmpty() || pass.isEmpty()) {
            lblMensajeError.setText("Por favor complete todos los campos.");
            return;
        }

        Usuario usuario = usuarioController.autenticar(input, pass);
        if (usuario != null) {
            this.usuarioAutenticado = usuario;
            dispose();
        } else {
            lblMensajeError.setText("Usuario o contraseña incorrectos.");
        }
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}
