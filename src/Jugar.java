import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingWorker;
import com.studiohartman.jamepad.ControllerState;

/**
 * Clase principal que gestiona el control de un robot con 4 servomotores
 * y un motor a pasos, utilizando un mando de Xbox como único método de control.
 * Se comunica con Arduino a través del puerto serie.
 *
 * Versión refactorizada para integrar las clases PuertoSerial, ControlArduino y ControlXbox.
 * Se ha eliminado el temporizador, pero se ha restaurado el dibujo del robot en posición vertical.
 */
public class Jugar extends JFrame {
    // Componentes principales de la interfaz
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private JuegoPanel juegoPanel;
    private JPanel instruccionesPanel;

    /**
     * Constructor principal - Inicializa la ventana y sus componentes.
     */
    public Jugar() {
        // Configuración básica de la ventana
        setTitle("Control de Robot con Mando Xbox");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Crear y configurar los paneles principales
        instruccionesPanel = crearPanelInstrucciones();
        mainPanel.add(instruccionesPanel, "instrucciones");

        juegoPanel = new JuegoPanel();
        mainPanel.add(juegoPanel, "juego");

        add(mainPanel);

        // Configurar captura global de teclas para iniciar el juego con ENTER
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (mainPanel.getComponent(0) == instruccionesPanel && instruccionesPanel.isShowing()) {
                    SwingUtilities.invokeLater(this::iniciarJuego);
                    return true; // Consume el evento
                }
            }
            return false; // No consume el evento
        });

        mostrarInstrucciones();
    }

    /**
     * Muestra el panel de instrucciones.
     */
    private void mostrarInstrucciones() {
        cardLayout.show(mainPanel, "instrucciones");
        SwingUtilities.invokeLater(() -> {
            instruccionesPanel.setFocusable(true);
            instruccionesPanel.requestFocusInWindow();
        });
    }

    /**
     * Crea y devuelve el panel de instrucciones.
     * @return JPanel con las instrucciones.
     */
    private JPanel crearPanelInstrucciones() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(224, 255, 255));

        JLabel lblTitulo = new JLabel("Controles del Mando Xbox", SwingConstants.CENTER);
        lblTitulo.setFont(UIUtils.TITLE_FONT);
        panel.add(lblTitulo, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(0, 1, 10, 10));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));
        cards.add(crearTarjeta("Stick Izq", "Hombro/Codo"));
        cards.add(crearTarjeta("Stick Der", "Muñeca/Rotación"));
        cards.add(crearTarjeta("Gatillos (LT/RT)", "Abrir/Cerrar pinza"));
        cards.add(crearTarjeta("D-Pad", "Velocidad ±"));
        cards.add(crearTarjeta("A / B", "Preset 1 / Preset 2"));
        cards.add(crearTarjeta("Start / Back", "Pausa / E-Stop"));
        panel.add(cards, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        JButton btnMenu = UIUtils.createButton("Regresar al menú", new Color(244, 67, 54));
        JButton btnIniciar = UIUtils.createButton("Jugar", new Color(76, 175, 80));
        btnMenu.addActionListener(e -> {
            Jugar.this.dispose();
            new MenuGUI().setVisible(true);
        });
        btnIniciar.addActionListener(e -> iniciarJuego());
        botones.add(btnMenu);
        botones.add(btnIniciar);
        panel.add(botones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearTarjeta(String titulo, String desc) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JLabel t = new JLabel(titulo);
        t.setFont(UIUtils.LABEL_FONT.deriveFont(Font.BOLD));
        t.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel d = new JLabel(desc);
        d.setFont(UIUtils.LABEL_FONT);
        d.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        card.add(t, BorderLayout.NORTH);
        card.add(d, BorderLayout.CENTER);
        return card;
    }

    /**
     * Inicia el panel del juego, reseteando las conexiones.
     */
    private void iniciarJuego() {
        cardLayout.show(mainPanel, "juego");
        juegoPanel.reset(); // Llama al reset para que inicie conexiones

        SwingUtilities.invokeLater(() -> {
            juegoPanel.setFocusable(true);
            juegoPanel.requestFocusInWindow();
        });
    }

    /**
     * Panel principal del juego, gestiona la lógica de control y la visualización de estado.
     */
    class JuegoPanel extends JPanel {
        private JLabel lblEstado = new JLabel("Inicializando...");
        private JLabel[] lblServos = new JLabel[4];
        private int[] angulos = {90, 90, 90, 90};
        private ControlArduino controlArduino;
        private Timer gamepadTimer;
        private boolean salir = false;
        private JProgressBar progreso = new JProgressBar(0, 100);
        private JLabel progresoLabel = new JLabel();
        private JButton btnCentrar;

        public JuegoPanel() {
            setLayout(null);
            setBackground(new Color(240, 255, 240));
            configurarInterfaz();

            // Configurar etiquetas para mostrar el estado de los servos
            for (int i = 0; i < 4; i++) {
                lblServos[i] = new JLabel("Servo " + (i + 1) + ": 90°");
                lblServos[i].setFont(UIUtils.LABEL_FONT);
                lblServos[i].setBounds(850, 100 + i * 30, 200, 25);
                add(lblServos[i]);
            }

            setFocusable(true);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    requestFocusInWindow();
                }
            });
        }
        
        /**
         * Configura los componentes de la interfaz de usuario en el panel del juego.
         */
        private void configurarInterfaz() {
            lblEstado.setFont(UIUtils.LABEL_FONT.deriveFont(Font.BOLD, 18f));
            lblEstado.setBounds(300, 680, 600, 40);
            lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
            add(lblEstado);

            JButton btnSalir = UIUtils.createButton("Volver al Menú", new Color(244, 67, 54));
            btnSalir.setBounds(20, 20, 150, 35);
            btnSalir.addActionListener(e -> salirAlMenu());
            add(btnSalir);

            btnCentrar = UIUtils.createButton("Centrar Servos", new Color(33, 150, 243));
            btnCentrar.setBounds(180, 20, 170, 35);
            btnCentrar.addActionListener(e -> centrarServos());
            add(btnCentrar);

            progresoLabel.setBounds(300, 620, 600, 25);
            progresoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            progresoLabel.setFont(UIUtils.LABEL_FONT);
            progresoLabel.setVisible(false);
            add(progresoLabel);

            progreso.setBounds(300, 650, 600, 20);
            progreso.setVisible(false);
            add(progreso);
        }
        
        /**
         * Inicializa las conexiones con el hardware (Arduino y Mando Xbox).
         */
        private void inicializarConexiones() {
            try {
                controlArduino = new ControlArduino();

                // 1. Conectar al control de Xbox
                controlArduino.controlXbox.listarControles();
                if (controlArduino.controlXbox.seleccionarControlPorLista(0)) {
                    lblEstado.setText("Control Xbox conectado. ");
                } else {
                    lblEstado.setText("Error: No se encontró control Xbox. ");
                    return; // Detener si no hay control
                }

                // 2. Conectar al Arduino
                if (controlArduino.arduino.detectarArduino()) {
                    controlArduino.arduino.configurarPuerto(9600);
                    if (controlArduino.arduino.abrirPuerto()) {
                        lblEstado.setText(lblEstado.getText() + "Arduino conectado en " + controlArduino.arduino.getPortName());
                         // Espera para que Arduino se inicialice
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    } else {
                         lblEstado.setText(lblEstado.getText() + "Error al abrir puerto Arduino.");
                         return; // Detener si no se abre el puerto
                    }
                } else {
                    lblEstado.setText(lblEstado.getText() + "Error: Arduino no detectado.");
                    return; // Detener si no hay Arduino
                }

                // 3. Iniciar el bucle de lectura del mando
                iniciarBucleDeControl();

            } catch (Exception e) {
                lblEstado.setText("Error fatal en inicialización: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        /**
         * Inicia el Timer que lee el estado del control y envía los datos.
         */
        private void iniciarBucleDeControl() {
            if (gamepadTimer != null) gamepadTimer.cancel();
            
            gamepadTimer = new Timer();
            gamepadTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (salir) {
                        this.cancel();
                        return;
                    }
                    try {
                        actualizarYEnviarComandos();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Opcional: mostrar error en lblEstado
                    }
                }
            }, 100, 50); // Lee el estado del control cada 50ms
        }
        
        /**
         * Lógica principal que se ejecuta en cada ciclo del timer del gamepad.
         * Lee el mando, actualiza los ángulos para la GUI y envía comandos al Arduino.
         */
        private void actualizarYEnviarComandos() {
            if (controlArduino == null || !controlArduino.controlXbox.isConnected() || !controlArduino.arduino.getisOpen()) {
                SwingUtilities.invokeLater(() -> lblEstado.setText("Hardware desconectado. Intentando reconectar..."));
                // Opcional: podrías intentar llamar a inicializarConexiones() de nuevo aquí.
                return;
            }

            // Actualiza el estado del control
            controlArduino.controlXbox.actualizarEstado();
            ControllerState estado = controlArduino.controlXbox.getState();
            if (estado == null) return;

            final float DEADZONE = 0.25f;
            boolean anguloCambiado = false;

            // Lógica para actualizar los ángulos VISUALES (para el dibujo)
            // Servo 0: Base (Stick Izquierdo X)
            if (estado.leftStickX > DEADZONE) { angulos[0] = Math.min(180, angulos[0] + 2); anguloCambiado = true; }
            else if (estado.leftStickX < -DEADZONE) { angulos[0] = Math.max(0, angulos[0] - 2); anguloCambiado = true; }
            // Servo 1: Hombro (Stick Izquierdo Y)
            if (estado.leftStickY < -DEADZONE) { angulos[1] = Math.min(180, angulos[1] + 2); anguloCambiado = true; }
            else if (estado.leftStickY > DEADZONE) { angulos[1] = Math.max(0, angulos[1] - 2); anguloCambiado = true; }
            // Servo 2: Codo (Stick Derecho Y)
            if (estado.rightStickY < -DEADZONE) { angulos[2] = Math.min(180, angulos[2] + 2); anguloCambiado = true; }
            else if (estado.rightStickY > DEADZONE) { angulos[2] = Math.max(0, angulos[2] - 2); anguloCambiado = true; }
            
            // Envía los comandos de un carácter al Arduino
            controlArduino.mandarDatos();
            
            // La lógica para el Servo 3 y los motores a pasos ya está en controlArduino.mandarDatos().
            // Pero necesitamos simular el cambio del servo 3 para la GUI.
            if (estado.rb) { angulos[3] = Math.min(180, angulos[3] + 5); anguloCambiado = true; }
            else if (estado.lb) { angulos[3] = Math.max(0, angulos[3] - 5); anguloCambiado = true; }

            // Botón START para centrar
            if (estado.start) {
                SwingUtilities.invokeLater(this::centrarServos);
            }
            
            // Si algún ángulo cambió, actualizamos la GUI
            if (anguloCambiado) {
                 SwingUtilities.invokeLater(() -> {
                     actualizarLabelsServos();
                     repaint(); // Redibuja el brazo robótico
                 });
            }
        }
        
        /**
         * Actualiza las etiquetas que muestran los ángulos de los servos.
         */
        private void actualizarLabelsServos() {
             for(int i = 0; i < 4; i++) {
                 lblServos[i].setText(String.format("Servo %d: %d°", i + 1, angulos[i]));
             }
        }
        
        /**
         * Envía el comando para centrar todos los servos y actualiza la GUI.
         */
        private void centrarServos() {
            btnCentrar.setEnabled(false);
            progreso.setValue(0);
            progreso.setVisible(true);
            progresoLabel.setText("Regresando a origen...");
            progresoLabel.setForeground(new Color(33, 150, 243));
            progresoLabel.setVisible(true);

            if (controlArduino != null && controlArduino.arduino.getisOpen()) {
                controlArduino.arduino.enviarDatos("c");
            }

            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    int pasos = 50;
                    for (int s = 0; s <= pasos; s++) {
                        for (int i = 0; i < 4; i++) {
                            if (angulos[i] < 90) angulos[i]++;
                            else if (angulos[i] > 90) angulos[i]--;
                        }
                        publish(s * 100 / pasos);
                        Thread.sleep(40);
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<Integer> chunks) {
                    int v = chunks.get(chunks.size() - 1);
                    progreso.setValue(v);
                    actualizarLabelsServos();
                    repaint();
                }

                @Override
                protected void done() {
                    progresoLabel.setText("Origen alcanzado");
                    progresoLabel.setForeground(new Color(76, 175, 80));
                    btnCentrar.setEnabled(true);
                    Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                progreso.setVisible(false);
                                progresoLabel.setVisible(false);
                            });
                        }
                    }, 1000);
                }
            };
            worker.execute();
        }

        /**
         * Cierra conexiones (si están abiertas) y regresa al panel de instrucciones.
         */
        private void salirAlMenu() {
            salir = true;
            if (gamepadTimer != null) gamepadTimer.cancel();
            
            if (controlArduino != null) {
                if(controlArduino.arduino != null) controlArduino.arduino.cerrarPuerto();
                if(controlArduino.controlXbox != null) controlArduino.controlXbox.shutdown();
            }
            
            Jugar.this.dispose();
            SwingUtilities.invokeLater(() -> new MenuGUI().setVisible(true));
        }

        /**
         * Resetea el panel del juego para un nuevo inicio.
         */
        public void reset() {
            salir = false;
            // Al volver al menú y luego iniciar el juego, los servos se centran
            // y las conexiones se re-establecen.
            centrarServos(); 
            lblEstado.setText("Intentando conectar con hardware...");
            setFocusable(true);
            requestFocusInWindow();
            
            inicializarConexiones();
            
            repaint();
        }

        /**
         * Método llamado al finalizar una operación (anteriormente por tiempo).
         * Ahora solo se usa para regresar a las instrucciones después de un mensaje.
         */
        public void finJuego() { // Se eliminó el parámetro 'ganado'
            if (gamepadTimer != null) gamepadTimer.cancel();
            
            // Mensaje genérico de finalización.
            lblEstado.setText("Operación finalizada. Volviendo a instrucciones...");
            
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                    SwingUtilities.invokeLater(() -> mostrarInstrucciones());
                }
            }, 3000); // Espera 3 segundos antes de volver a las instrucciones
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            dibujarBrazoRobotico(g);
        }

        /**
         * Dibuja el brazo robótico en el panel.
         * @param g Objeto Graphics para dibujar.
         */
        private void dibujarBrazoRobotico(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2 + 100;

            GradientPaint baseGp = new GradientPaint(cx - 60, cy - 20, new Color(200, 200, 210), cx - 60, cy + 20, new Color(120, 120, 130));
            g2.setPaint(baseGp);
            g2.fillOval(cx - 60, cy - 20, 120, 40);
            g2.setPaint(null);

            double base = Math.toRadians(angulos[0] - 90);
            double hombro = Math.toRadians(angulos[1] - 90);
            double codo = Math.toRadians(angulos[2] - 90);
            double l1 = 100, l2 = 100;

            double x1 = l1 * Math.cos(hombro) * Math.cos(base);
            double y1 = l1 * Math.sin(hombro);
            double z1 = l1 * Math.cos(hombro) * Math.sin(base);

            double hombroTotal = hombro + codo;
            double x2 = x1 + l2 * Math.cos(hombroTotal) * Math.cos(base);
            double y2 = y1 + l2 * Math.sin(hombroTotal);
            double z2 = z1 + l2 * Math.cos(hombroTotal) * Math.sin(base);

            Point p0 = project(0, 0, 0, cx, cy);
            Point p1 = project(x1, y1, z1, cx, cy);
            Point p2 = project(x2, y2, z2, cx, cy);

            g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            GradientPaint seg1 = new GradientPaint(p0.x, p0.y, new Color(255, 138, 128), p1.x, p1.y, new Color(244, 67, 54));
            g2.setPaint(seg1);
            g2.drawLine(p0.x, p0.y, p1.x, p1.y);
            GradientPaint seg2 = new GradientPaint(p1.x, p1.y, new Color(165, 214, 167), p2.x, p2.y, new Color(76, 175, 80));
            g2.setPaint(seg2);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

            double garra = Math.toRadians(angulos[3] - 90) / 2.0;
            int claw = 30;
            Point g1 = project(
                    x2 + claw * Math.cos(hombroTotal + garra) * Math.cos(base),
                    y2 + claw * Math.sin(hombroTotal + garra),
                    z2 + claw * Math.cos(hombroTotal + garra) * Math.sin(base), cx, cy);
            Point g2p = project(
                    x2 + claw * Math.cos(hombroTotal - garra) * Math.cos(base),
                    y2 + claw * Math.sin(hombroTotal - garra),
                    z2 + claw * Math.cos(hombroTotal - garra) * Math.sin(base), cx, cy);
            GradientPaint clawPaint = new GradientPaint(p2.x, p2.y, new Color(144, 202, 249), g1.x, g1.y, new Color(33, 150, 243));
            g2.setPaint(clawPaint);
            g2.drawLine(p2.x, p2.y, g1.x, g1.y);
            g2.drawLine(p2.x, p2.y, g2p.x, g2p.y);
            g2.setPaint(null);

            g2.setColor(Color.DARK_GRAY);
            g2.fillOval(p0.x - 6, p0.y - 6, 12, 12);
            g2.fillOval(p1.x - 6, p1.y - 6, 12, 12);
            g2.fillOval(p2.x - 6, p2.y - 6, 12, 12);
        }

        private Point project(double x, double y, double z, int cx, int cy) {
            double isoX = (x - z) * 0.707;
            double isoY = y + (x + z) * 0.408;
            return new Point((int) (cx + isoX), (int) (cy - isoY));
        }
    }
}
