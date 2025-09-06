import javax.swing.*;
import java.awt.*;

/**
 * Utilidades de interfaz para un estilo consistente.
 */
public class UIUtils {
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 18);

    private UIUtils() {
        // Evitar instanciación
    }

    /**
     * Configura el Look & Feel del sistema para una apariencia nativa.
     */
    public static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    /**
     * Crea un botón estilizado con colores y fuente coherentes.
     *
     * @param text Texto del botón
     * @param bg   Color de fondo
     * @return JButton estilizado
     */
    public static JButton createButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(BUTTON_FONT);
        b.setBackground(bg);
        int brightness = (int) ((bg.getRed() * 299 + bg.getGreen() * 587 + bg.getBlue() * 114) / 1000);
        b.setForeground(brightness > 186 ? Color.BLACK : Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        return b;
    }
}
