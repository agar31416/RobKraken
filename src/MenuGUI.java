import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File; // Importado para verificar la existencia del archivo de imagen

public class MenuGUI extends JFrame {

    public MenuGUI() {
        // --- Configuración de la Ventana Principal ---
        setTitle("Brazo Kraken - Menú Principal");
        setSize(900, 600); // Aumentamos un poco el tamaño para acomodar mejor los elementos
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        // Usamos un JPanel principal con BorderLayout para la estructura general
        // Y un JPanel para el contenido central con GridBagLayout para los botones
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 40)); // Fondo oscuro para un tema "Kraken"

        // --- Panel Superior para Logo y Título del Proyecto ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS)); // Acomodo vertical
        headerPanel.setBackground(new Color(30, 30, 40));
        headerPanel.setBorder(new EmptyBorder(20, 0, 10, 0)); // Margen superior

        // 1. Logo de la Universidad
        ImageIcon originalIcon = null;
        try {
            // Verifica si el archivo existe antes de cargarlo
            File logoFile = new File("logo_universidad.png");
            if (logoFile.exists()) {
                originalIcon = new ImageIcon("logo_universidad.png");
                // Escalar la imagen si es demasiado grande
                Image originalImage = originalIcon.getImage();
                Image scaledImage = originalImage.getScaledInstance(120, 120, Image.SCALE_SMOOTH); // Ajusta el tamaño
                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                JLabel logoLabel = new JLabel(scaledIcon);
                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar horizontalmente
                headerPanel.add(logoLabel);
                headerPanel.add(Box.createVerticalStrut(10)); // Espacio entre logo y nombre del proyecto
            } else {
                System.out.println("Advertencia: El archivo 'logo_universidad.png' no se encontró en la ruta especificada.");
                // Opcional: Puedes agregar un placeholder o un mensaje si el logo no se carga
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen 'logo_universidad.png': " + e.getMessage());
            // Opcional: Puedes agregar un placeholder o un mensaje si el logo no se carga
        }

        // 2. Nombre del Proyecto "Brazo Kraken"
        JLabel projectNameLabel = new JLabel("Brazo Kraken", SwingConstants.CENTER);
        projectNameLabel.setFont(new Font("Impact", Font.BOLD, 48)); // Fuente más impactante
        // Colores que evocan un kraken: Azul oscuro casi morado o verde oscuro
        projectNameLabel.setForeground(new Color(138, 43, 226)); // Azul violeta
        projectNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar horizontalmente
        headerPanel.add(projectNameLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Panel Central para los Botones (Acomodo Dinámico) ---
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(new Color(30, 30, 40)); // Mismo fondo que el mainPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0); // Espaciado entre botones
        gbc.fill = GridBagConstraints.HORIZONTAL; // Hacer que los botones se expandan horizontalmente
        gbc.ipadx = 80; // Padding interno para hacer los botones más anchos
        gbc.ipady = 25; // Padding interno para hacer los botones más altos

        // 3. Botones con Estilo
        Font buttonFont = new Font("Arial", Font.BOLD, 22); // Fuente para los botones

        // Botón Jugar
        JButton btnJugar = createStyledButton("Jugar", new Color(34, 139, 34), Color.WHITE, buttonFont); // Verde bosque
        gbc.gridy = 0; // Fila 0
        buttonPanel.add(btnJugar, gbc);
        btnJugar.addActionListener(e -> {
            try {
                // Asume que 'Jugar' es una clase JFrame
                Class<?> jugarClass = Class.forName("Jugar");
                JFrame jugarFrame = (JFrame) jugarClass.getDeclaredConstructor().newInstance();
                jugarFrame.setVisible(true);
                dispose(); // Cierra el menú principal al abrir el juego
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "La clase 'Jugar' no se encontró. Asegúrate de que el archivo .java esté compilado y en la ruta de clases.", "Error de Clase", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al iniciar el juego: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Botón Configuraciones
        JButton btnConfig = createStyledButton("Configuraciones", new Color(255, 140, 0), Color.WHITE, buttonFont); // Naranja oscuro
        gbc.gridy = 1; // Fila 1
        buttonPanel.add(btnConfig, gbc);
        btnConfig.addActionListener(e -> {
            try {
                // Asume que 'Configuracion' es una clase JFrame
                Class<?> configClass = Class.forName("Configuracion");
                JFrame configFrame = (JFrame) configClass.getDeclaredConstructor().newInstance();
                configFrame.setVisible(true);
                // No se cierra el menú principal
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "La clase 'Configuracion' no se encontró. Asegúrate de que el archivo .java esté compilado y en la ruta de clases.", "Error de Clase", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir configuraciones: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Botón Salir
        JButton btnSalir = createStyledButton("Salir", new Color(178, 34, 34), Color.WHITE, buttonFont); // Rojo fuego
        gbc.gridy = 2; // Fila 2
        buttonPanel.add(btnSalir, gbc);
        btnSalir.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres salir?", "Confirmar Salida", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0); // Termina la aplicación
            }
        });

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // --- Botón de Información (¿?) en la esquina superior derecha ---
        JButton btnInfo = createStyledButton("?", new Color(65, 105, 225), Color.WHITE, new Font("Arial", Font.BOLD, 20)); // Azul rey
        btnInfo.setPreferredSize(new Dimension(50, 50)); // Tamaño fijo para el botón de info
        btnInfo.setMinimumSize(new Dimension(50, 50));
        btnInfo.setMaximumSize(new Dimension(50, 50));
        btnInfo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true)); // Borde redondeado

        // Usamos un JPanel auxiliar para posicionar el botón de info en la esquina
        JPanel infoButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)); // Margen a la derecha y arriba
        infoButtonPanel.setBackground(new Color(30, 30, 40));
        infoButtonPanel.add(btnInfo);
        mainPanel.add(infoButtonPanel, BorderLayout.EAST); // Lo colocamos en la parte ESTE del BorderLayout

        // Ajustamos la posición del botón de info si queremos que esté 'flotando' en la esquina
        // Para esto, necesitaríamos un JLayeredPane o un Custom Layout, pero para simplicidad
        // lo dejaremos en el BorderLayout.EAST, que lo alinea a la derecha.
        // Si el usuario insiste en una posición absoluta (como antes con setBounds),
        // tendríamos que volver a setLayout(null) para el mainPanel, lo cual no es recomendable
        // para un diseño dinámico.

        btnInfo.addActionListener(e -> {
            new AcercaDe(this).setVisible(true); // Ventana de información
        });

        add(mainPanel); // Agrega el panel principal a la ventana
    }

    /**
     * Método auxiliar para crear botones con un estilo consistente.
     */
    private JButton createStyledButton(String text, Color bgColor, Color fgColor, Font font) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false); // Quita el recuadro de enfoque
        button.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 90), 3, true)); // Borde con color y redondeado
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano al pasar por encima
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter()); // Aclara el color al pasar el mouse
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor); // Vuelve al color original
            }
        });
        return button;
    }

    public static void main(String[] args) {
        // Asegura que la GUI se ejecute en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new MenuGUI().setVisible(true));
    }
}

// Ventana de Acerca De - Mejoras de estilo
class AcercaDe extends JFrame {
    public AcercaDe(JFrame parent) {
        setTitle("Acerca del Proyecto Brazo Kraken");
        setSize(550, 350); // Ajusta el tamaño
        setLocationRelativeTo(parent);
        setResizable(false); // No redimensionable

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(50, 50, 60)); // Fondo oscuro
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextArea textArea = new JTextArea(
                "Proyecto: Brazo Kraken v11.0\n\n" +
                        "Este proyecto simula el funcionamiento de un brazo robótico, " +
                        "inspirado en la agilidad y fuerza de los tentáculos de un kraken.\n\n" +
                        "Desarrollado con Java Swing, esta aplicación permite a los usuarios " +
                        "interactuar con el brazo, configurar sus movimientos y visualizar " +
                        "su comportamiento en un entorno gráfico.\n\n" +
                        "Características principales:\n" +
                        " - Control de articulaciones en tiempo real.\n" +
                        " - Interfaz de usuario intuitiva.\n\n" +
                        "Equipo de Desarrollo:\n" +
                        " - Angel Gael Aguilar Reyes\n" +
                        " - Erwin Michelle Cortés Salinas\n" +
                        " - Melisa Yamilee Flores Vargas\n" +
                        " - Isaac Rodriguez Colima\n" +
                        " - Universidad Autonoma de Aguascalientes.\n\n" +
                        "Versión: 11.0 (Marzo-Junio 2025)\n" +
                        "Agradecimientos especiales a nuestros profesores y mentores.\n\n" +
                        "Mensiones Onorificas:\n" +
                        " - Agradecemos a ChatGpt, Claude, Gemini por su ayuda "
        );
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setForeground(Color.LIGHT_GRAY); // Texto claro
        textArea.setBackground(new Color(50, 50, 60)); // Fondo del área de texto
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setCaretPosition(0); // Scroll al inicio

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 90), 1)); // Borde para el scrollpane
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Cerrar");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(100, 149, 237)); // Azul cornflower
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 90), 2, true));
        closeButton.addActionListener(e -> dispose()); // Cierra solo esta ventana

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(50, 50, 60));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Espacio superior
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel);
    }
}