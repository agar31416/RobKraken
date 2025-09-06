import javax.swing.*;
import java.awt.*;

public class Configuracion extends JFrame {
    private final JLabel statusXbox = new JLabel("Sin detectar");
    private final JLabel statusArduino = new JLabel("Sin detectar");
    // Tiempo en milisegundos que tarda la animación de búsqueda
    public static final int DETECTION_DELAY = 1200;

    public Configuracion() {
        setTitle("Configuraciones");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(main);

        // Sección Control Xbox
        JPanel xboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblXbox = new JLabel("Control Xbox:");
        lblXbox.setFont(UIUtils.LABEL_FONT);
        xboxPanel.add(lblXbox);
        statusXbox.setForeground(Color.RED);
        statusXbox.setFont(UIUtils.LABEL_FONT);
        xboxPanel.add(statusXbox);
        JButton btnDetectXbox = UIUtils.createButton("Detectar", new Color(70, 130, 180));
        xboxPanel.add(btnDetectXbox);
        main.add(xboxPanel);

        // Sección Arduino
        JPanel arduinoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblArduino = new JLabel("Arduino:");
        lblArduino.setFont(UIUtils.LABEL_FONT);
        arduinoPanel.add(lblArduino);
        statusArduino.setForeground(Color.RED);
        statusArduino.setFont(UIUtils.LABEL_FONT);
        arduinoPanel.add(statusArduino);
        JButton btnDetectArduino = UIUtils.createButton("Detectar", new Color(70, 130, 180));
        arduinoPanel.add(btnDetectArduino);
        main.add(arduinoPanel);

        // Panel inferior con botones de acción
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = UIUtils.createButton("Guardar", new Color(34, 139, 34));
        JButton btnRegresar = UIUtils.createButton("Regresar al menú", new Color(178, 34, 34));
        bottom.add(btnGuardar);
        bottom.add(btnRegresar);
        main.add(Box.createVerticalGlue());
        main.add(bottom);

        // Listeners
        btnDetectXbox.addActionListener(e -> detectar(this::simularDeteccion, statusXbox));
        btnDetectArduino.addActionListener(e -> detectar(this::simularDeteccion, statusArduino));

        btnGuardar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Configuraciones guardadas");
            dispose();
        });

        btnRegresar.addActionListener(e -> dispose());
    }

    // Simulación de detección real. Ajustar según implementación real
    private boolean simularDeteccion() {
        return Math.random() > 0.5; // Devuelve true/false aleatoriamente
    }

    // Ejecuta detección en segundo plano con animación
    private void detectar(java.util.concurrent.Callable<Boolean> tarea, JLabel destino) {
        destino.setText("Buscando...");
        destino.setForeground(Color.YELLOW);

        JDialog dialog = new JDialog(this, "Buscando...", true);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        dialog.add(bar);
        dialog.setSize(200, 75);
        dialog.setLocationRelativeTo(this);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Thread.sleep(DETECTION_DELAY); // Simula retraso
                return tarea.call();
            }

            @Override
            protected void done() {
                dialog.dispose();
                boolean ok = false;
                try {
                    ok = get();
                } catch (Exception ignored) {}
                destino.setText(ok ? "Detectado" : "No detectado");
                destino.setForeground(ok ? Color.GREEN : Color.RED);
            }
        };
        worker.execute();
        dialog.setVisible(true);
    }
}
