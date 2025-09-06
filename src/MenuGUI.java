import javax.swing.*;
import java.awt.*;

public class MenuGUI extends JFrame {
    public MenuGUI() {
        setTitle("BRAZO KRAKEN");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        GradientPanel main = new GradientPanel();
        main.setLayout(new BorderLayout());
        setContentPane(main);

        JLabel title = new JLabel("BRAZO KRAKEN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        main.add(title, BorderLayout.NORTH);

        JLabel logo = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("logo_universidad.png");
            Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(scaled));
        } catch (Exception ex) {
            // Si no se carga el logo, simplemente no se muestra
        }
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setOpaque(false);
        logoPanel.add(logo);
        main.add(logoPanel, BorderLayout.WEST);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(BorderFactory.createEmptyBorder(100, 20, 100, 40));

        JButton jugar = UIUtils.createButton("Jugar", new Color(76, 175, 80));
        JButton config = UIUtils.createButton("Configuraciones", new Color(255, 193, 7));
        JButton salir = UIUtils.createButton("Salir", new Color(244, 67, 54));

        jugar.setAlignmentX(Component.CENTER_ALIGNMENT);
        config.setAlignmentX(Component.CENTER_ALIGNMENT);
        salir.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttons.add(jugar);
        buttons.add(Box.createVerticalStrut(30));
        buttons.add(config);
        buttons.add(Box.createVerticalStrut(30));
        buttons.add(salir);
        main.add(buttons, BorderLayout.EAST);

        jugar.addActionListener(e -> {
            new Jugar().setVisible(true);
            dispose();
        });

        config.addActionListener(e -> new Configuracion().setVisible(true));

        salir.addActionListener(e -> System.exit(0));
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, new Color(33, 150, 243), 0, getHeight(), new Color(3, 30, 180));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public static void main(String[] args) {
        UIUtils.setSystemLookAndFeel();
        SwingUtilities.invokeLater(() -> new MenuGUI().setVisible(true));
    }
}
