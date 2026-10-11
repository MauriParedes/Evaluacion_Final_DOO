import com.biblioteca.config.DatabaseConnection;
import com.biblioteca.model.Usuario;
import com.biblioteca.view.LoginDialog;
import com.biblioteca.view.MainFrame;

import javax.swing.*;

/**
 * Punto de entrada principal del Sistema de Gestión de Biblioteca Escolar.
 */
public class Main {
    public static void main(String[] args) {
        // Establecer Look & Feel nativo/moderno del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar Look and Feel nativo: " + e.getMessage());
        }

        // Inicializar conexión a Base de Datos (Singleton)
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConnection.getInstance().getConnection();
            } catch (Exception e) {
                System.err.println("Advertencia inicializando base de datos: " + e.getMessage());
            }

            // Iniciar pantalla de Autenticación (Login)
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);

            Usuario usuario = loginDialog.getUsuarioAutenticado();
            if (usuario != null) {
                // Abrir ventana principal con el usuario autenticado
                MainFrame mainFrame = new MainFrame(usuario);
                mainFrame.setVisible(true);
            } else {
                System.out.println("Aplicación finalizada por el usuario.");
                System.exit(0);
            }
        });
    }
}
