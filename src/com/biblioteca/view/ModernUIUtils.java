package com.biblioteca.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import java.awt.*;

//Utilidades de diseño moderno para componentes Java Swing.

public class ModernUIUtils {

    // Paleta de colores Slate / Indigo
    public static final Color COLOR_PRIMARY = new Color(79, 70, 229);      // Indigo primary
    public static final Color COLOR_PRIMARY_HOVER = new Color(67, 56, 202);
    public static final Color COLOR_SECONDARY = new Color(71, 85, 105);    // Slate secondary
    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252);  // Light grey-blue bg
    public static final Color COLOR_SURFACE = Color.WHITE;                  // Card surface bg
    public static final Color COLOR_SIDEBAR = new Color(15, 23, 42);        // Dark slate sidebar
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    public static final Color COLOR_BORDER = new Color(226, 232, 240);

    // Colores de estado
    public static final Color COLOR_SUCCESS_BG = new Color(220, 252, 231);  // Light green
    public static final Color COLOR_SUCCESS_TEXT = new Color(22, 101, 52);  // Dark green
    public static final Color COLOR_DANGER_BG = new Color(254, 226, 226);   // Light red
    public static final Color COLOR_DANGER_TEXT = new Color(153, 27, 27);   // Dark red
    public static final Color COLOR_WARNING_BG = new Color(254, 243, 199);  // Light amber
    public static final Color COLOR_WARNING_TEXT = new Color(146, 64, 14);  // Dark amber

    // Fuentes
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);


    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(COLOR_PRIMARY_HOVER);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_MAIN);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(FONT_BOLD);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(4, 10, 4, 10));

            String text = value != null ? value.toString() : "";
            if ("Devuelto".equalsIgnoreCase(text) || "Disponible".equalsIgnoreCase(text) || text.startsWith("En Stock")) {
                label.setBackground(COLOR_SUCCESS_BG);
                label.setForeground(COLOR_SUCCESS_TEXT);
            } else if ("Atrasado".equalsIgnoreCase(text) || "Sin Stock".equalsIgnoreCase(text) || text.startsWith("Agotado")) {
                label.setBackground(COLOR_DANGER_BG);
                label.setForeground(COLOR_DANGER_TEXT);
            } else if ("En Préstamo".equalsIgnoreCase(text) || "Prestado".equalsIgnoreCase(text)) {
                label.setBackground(COLOR_WARNING_BG);
                label.setForeground(COLOR_WARNING_TEXT);
            } else {
                if (isSelected) {
                    label.setBackground(table.getSelectionBackground());
                    label.setForeground(table.getSelectionForeground());
                } else {
                    label.setBackground(table.getBackground());
                    label.setForeground(table.getForeground());
                }
            }
            return label;
        }
    }

    /**
     * Renderizador de celda para centrados de texto.
     */
    public static class CenterTableCellRenderer extends DefaultTableCellRenderer {
        public CenterTableCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    /**
     * Crea un panel contenedor estilo tarjeta con sombra suave / borde pulido.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }
}
