import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import com.fazecast.jSerialComm.*;
import com.studiohartman.jamepad.ControllerManager;
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
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(240, 248, 255));

        JLabel lblTitulo = new JLabel("Control de Robot con Mando Xbox", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitulo.setBounds(150, 30, 900, 40);
        panel.add(lblTitulo);

        JTextArea instrucciones = new JTextArea(getInstruccionesPorTipoControl());
        instrucciones.setFont(new Font("Arial", Font.PLAIN, 18));
        instrucciones.setEditable(false);
        instrucciones.setOpaque(false);
        instrucciones.setBounds(100, 100, 1000, 500);
        panel.add(instrucciones);

        JButton btnIniciar = new JButton("INICIAR");
        btnIniciar.setBounds(500, 620, 200, 50);
        btnIniciar.setBackground(new Color(40, 167, 69));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFont(new Font("Arial", Font.BOLD, 16));
        btnIniciar.setFocusPainted(false);
        btnIniciar.addActionListener(e -> iniciarJuego());
        panel.add(btnIniciar);
        
        // Listener para que el panel pueda obtener foco y recibir eventos de teclado
        panel.setFocusable(true);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                panel.requestFocusInWindow();
            }
        });

        return panel;
    }

    /**
     * Obtiene las instrucciones de control para el mando de Xbox.
     * @return String con las instrucciones.
     */
    private String getInstruccionesPorTipoControl() {
        return "¡Bienvenido al Control por Mando Xbox!\n\n" +
                "Asegúrate de tener tu control Xbox conectado y el Arduino enchufado.\n" +
                "La aplicación intentará conectarse automáticamente al iniciar.\n\n" +
                "--- Controles de Servomotores ---\n" +
                "• Stick Izquierdo (Horizontal): Mueve el Servo 1 (Base)\n" +
                "• Stick Izquierdo (Vertical):   Mueve el Servo 2 (Hombro)\n" +
                "• Stick Derecho (Vertical):     Mueve el Servo 3 (Codo)\n" +
                "• Botones LB / RB:              Mueve el Servo 4 (Garra)\n\n" +
                "--- Controles de Motores a Pasos ---\n" +
                "• Gatillo Izquierdo (LT):       Activa el motor a pasos (Dirección 1)\n" +
                "• Gatillo Derecho (RT):         Activa el motor a pasos (Dirección 2)\n" +
                "  (Los motores se detienen al soltar el gatillo)\n\n" +
                "--- Controles Especiales ---\n" +
                "• Botón START: Centrar todos los servos a 90°\n" +
                "• Botón BACK:   Volver al menú principal\n\n" +
                "Presiona ENTER o el botón INICIAR para comenzar.";
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
     * Método principal para ejecutar la aplicación.
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        // Establecer Look and Feel del sistema para una mejor apariencia
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Jugar().setVisible(true));
    }

    /**
     * Panel principal del juego, gestiona la lógica de control y la visualización de estado.
     */
    class JuegoPanel extends JPanel {
        private JLabel lblEstado = new JLabel("Inicializando...");
        private JLabel[] lblServos = new JLabel[4]; // Muestra los ángulos de los servos.

        private int[] angulos = {90, 90, 90, 90}; // Ángulos para la visualización

        private ControlArduino controlArduino;
        private Timer gamepadTimer;
        private boolean salir = false;

        public JuegoPanel() {
            setLayout(null);
            setBackground(new Color(230, 255, 230));
            configurarInterfaz();

            // Configurar etiquetas para mostrar el estado de los servos
            for (int i = 0; i < 4; i++) {
                lblServos[i] = new JLabel("Servo " + (i + 1) + ": 90°");
                lblServos[i].setFont(new Font("Arial", Font.PLAIN, 16));
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
            lblEstado.setFont(new Font("Arial", Font.BOLD, 18));
            lblEstado.setBounds(300, 680, 600, 40);
            lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
            add(lblEstado);

            JButton btnSalir = new JButton("Volver al Menú"); // Cambiado el texto del botón
            btnSalir.setBounds(20, 20, 150, 35); // Ajuste de tamaño para el nuevo texto
            btnSalir.setBackground(new Color(220, 53, 69));
            btnSalir.setForeground(Color.WHITE);
            btnSalir.setFocusPainted(false);
            btnSalir.addActionListener(e -> salirAlMenu());
            add(btnSalir);

            JButton btnCentrar = new JButton("Centrar Servos");
            btnCentrar.setBounds(180, 20, 150, 35); // Ajuste de posición debido al cambio de tamaño del botón Salir
            btnCentrar.setBackground(new Color(70, 130, 180));
            btnCentrar.setForeground(Color.WHITE);
            btnCentrar.setFocusPainted(false);
            btnCentrar.addActionListener(e -> centrarServos());
            add(btnCentrar);
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
            for (int i = 0; i < 4; i++) {
                angulos[i] = 90;
            }
            // Envía un comando especial a Arduino para centrar todo.
            // Asumimos que 'c' es el carácter que el código de Arduino usa para centrar.
            if(controlArduino != null && controlArduino.arduino.getisOpen()) {
                controlArduino.arduino.enviarDatos("c"); 
            }
            actualizarLabelsServos();
            repaint();
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
            
            // Regresa al panel de instrucciones. El método iniciarJuego()
            // que se llama desde el panel de instrucciones se encarga de llamar
            // a reset() en JuegoPanel, lo que reinicia el estado.
            Jugar.this.mostrarInstrucciones();
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
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centroX = getWidth() / 2; // Centrar horizontalmente
            int centroY = getHeight() / 2 + 150; // Posicionar más arriba para que el robot esté de pie

            // Dibujar base del robot
            g2d.setColor(new Color(60, 70, 80)); // Gris oscuro para la base
            g2d.fillOval(centroX - 80, centroY - 20, 160, 40); // Elipse de la base
            g2d.fillRect(centroX - 70, centroY - 40, 140, 40); // Rectángulo de la base
            g2d.setColor(new Color(80, 90, 100)); // Gris más claro para la parte superior de la base
            g2d.fillOval(centroX - 70, centroY - 50, 140, 40); // Elipse superior de la base
            
            // Guardar transformación actual
            AffineTransform oldTransform = g2d.getTransform();
            
            // Mover el origen al centro de la base del brazo
            g2d.translate(centroX, centroY - 50);

            // --- Brazo 1 (Hombro) ---
            double l1 = 150; // Longitud del primer segmento del brazo
            double a1 = -Math.toRadians(angulos[1]); // Ángulo del hombro (invertido para el dibujo vertical)
            double rotBase = Math.toRadians(angulos[0] - 90); // Ángulo de la base (relativo a 90 para estar de pie)

            AffineTransform t1 = new AffineTransform();
            t1.rotate(rotBase); // Rotar según la base
            t1.rotate(a1);       // Rotar según el hombro
            g2d.transform(t1);

            g2d.setColor(new Color(180, 70, 70)); // Color rojizo para el segmento del brazo
            g2d.fillRect(0, -15, (int) l1, 30); // Rectángulo del segmento del brazo
            g2d.setColor(Color.DARK_GRAY); // Color de la articulación
            g2d.fillOval(-15, -20, 40, 40); // Articulación del hombro
            g2d.fillOval((int)l1-25, -20, 40, 40); // Articulación del codo
            g2d.setColor(Color.YELLOW); // Acento en las articulaciones
            g2d.fillOval((int)l1-15, -10, 20, 20);

            // --- Brazo 2 (Codo) ---
            double l2 = 120; // Longitud del segundo segmento del brazo
            double a2 = -Math.toRadians(angulos[2] - (180 - angulos[1])); // Ángulo relativo
            g2d.translate(l1, 0); // Mover al final del brazo 1
            g2d.rotate(a2);

            g2d.setColor(new Color(70, 180, 70)); // Color verdoso para el segmento del brazo
            g2d.fillRect(0, -12, (int) l2, 24); // Rectángulo del segmento del brazo
            g2d.setColor(Color.DARK_GRAY); // Color de la articulación
            g2d.fillOval(-18, -18, 36, 36); // Articulación interna del codo
            g2d.fillOval((int)l2-18, -18, 36, 36); // Articulación de la muñeca
            g2d.setColor(Color.YELLOW); // Acento en las articulaciones
            g2d.fillOval((int)l2-8, -8, 16, 16);

            // --- Garra (Servo 3) ---
            double a3 = Math.toRadians(angulos[3] - 90) / 2.0; // Ángulo de apertura de la garra
            g2d.translate(l2, 0); // Mover al final del brazo 2
            
            // Garra superior
            AffineTransform garraT = g2d.getTransform();
            g2d.rotate(a3);
            g2d.setColor(new Color(80, 80, 180)); // Color azulado para la garra
            g2d.fillRect(0, -6, 50, 12); // Segmento de la garra
            g2d.setTransform(garraT);

            // Garra inferior
            g2d.rotate(-a3);
            g2d.setColor(new Color(80, 80, 180)); // Color azulado para la garra
            g2d.fillRect(0, -6, 50, 12); // Segmento de la garra
            g2d.setTransform(garraT);
            
            // Restaurar transformación original
            g2d.setTransform(oldTransform);
        }
    }
    
    // =====================================================================================
    // CLASE PuertoSerial 
    // =====================================================================================
    static class PuertoSerial {
        private SerialPort port;
        private InputStream inpstr;
        private OutputStream outstrm;
        private BufferedReader bufferedInput;

        public PuertoSerial() {
            System.out.println("Iniciando conexion Serial ...");
        }

        public void setPort(SerialPort port) { this.port = port; }
        public String getPort() { return this.port.getSystemPortName(); }

        public boolean detectarArduino() {
            for (SerialPort p : SerialPort.getCommPorts()) {
                if (p.getDescriptivePortName().toLowerCase().contains("arduino")) {
                    this.port = p;
                    System.out.println("Arduino detectado en: " + p.getSystemPortName());
                    return true;
                }
            }
            System.out.println("Arduino no detectado");
            return false;
        }
        
        public void listarPuertos() {
            SerialPort[] ports = SerialPort.getCommPorts();
            if (ports.length == 0) {
                System.out.println("No hay puertos disponibles");
                return;
            }
            for (int i = 0; i < ports.length; i++) {
                System.out.println(i + ".- " + ports[i].getSystemPortName());
            }
        }
        
        public boolean seleccionarPuertoPorLista(int index) {
            SerialPort[] ports = SerialPort.getCommPorts();
            if (index < 0 || index >= ports.length) {
                throw new IllegalArgumentException("El index seleccionado no es valido");
            }
            this.port = ports[index];
            System.out.println("Puerto seleccionado: " + this.port.getDescriptivePortName());
            return true;
        }

        public boolean getisOpen() { return this.port != null && this.port.isOpen(); }
        public String getPortName() { return (this.port != null) ? this.port.getSystemPortName() : "No hay puerto seleccionado"; }

        public void configurarPuerto(int bauds) {
            if (this.port == null) {
                throw new IllegalStateException("Puerto no seleccionado");
            }
            this.port.setComPortParameters(bauds, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        }
        
        public boolean abrirPuerto() {
            if (this.port == null) throw new IllegalStateException("Puerto no seleccionado");
            if (this.port.openPort()) {
                System.out.println("Puerto abierto");
                this.port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
                try {
                    this.inpstr = this.port.getInputStream();
                    this.outstrm = this.port.getOutputStream();
                    this.bufferedInput = new BufferedReader(new InputStreamReader(this.inpstr, "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                System.out.println("No se pudo abrir el puerto");
                return false;
            }
        }

        public void cerrarPuerto() {
            try {
                if (this.bufferedInput != null) this.bufferedInput.close();
                if (this.inpstr != null) this.inpstr.close();
                if (this.outstrm != null) this.outstrm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (this.port != null && this.port.isOpen()) {
                this.port.closePort();
                System.out.println("Puerto cerrado");
            }
        }
        
        public void enviarDatos(String message) {
            if (this.outstrm == null) {
                System.out.println("Error: el puerto no está abierto para enviar datos.");
                return;
            }
            try {
                this.outstrm.write((message.trim() + "\n").getBytes());
                this.outstrm.flush();
                // System.out.println("Mensaje enviado: " + message); // Descomentar para depuración
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String recibirDatos() {
            try {
                if (this.bufferedInput != null && this.bufferedInput.ready()) {
                    String dato = this.bufferedInput.readLine();
                    return dato != null ? dato.trim() : "NO_DATA";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "SYNTAX_ERROR";
            }
            return "NO_DATA";
        }
    }

    // =====================================================================================
    // CLASE ControlArduino
    // =====================================================================================
    static class ControlArduino {
        protected ControlXbox controlXbox;
        protected PuertoSerial arduino;
        private ControllerState estadoAnterior;

        public ControlArduino() {
            this.controlXbox = new ControlXbox();
            this.arduino = new PuertoSerial();
        }
        
        public void mandarDatos() {
            this.controlXbox.actualizarEstado();
            ControllerState estadoActual = this.controlXbox.getState();
            
            if (estadoActual == null || !this.controlXbox.isConnected() || !this.arduino.getisOpen()) {
                return; // Validaciones manejadas en el panel principal
            }

            // Si es el primer ciclo, inicializa estadoAnterior y sale.
            if (estadoAnterior == null) {
                estadoAnterior = estadoActual;
                return;
            }

            // --- LÓGICA DE ENVÍO DE MOTORES A PASO ---
            processTriggerOnChange("L", "S", estadoAnterior.leftTrigger, estadoActual.leftTrigger);
            processTriggerOnChange("R", "S", estadoAnterior.rightTrigger, estadoActual.rightTrigger);

            // --- LÓGICA PARA SERVOS CON JOYSTICKS Y BUMPERS ---
            final float DEADZONE = 0.25f;

            // SERVO 0: Joystick Izquierdo (Eje X)
            if (estadoActual.leftStickX > DEADZONE) arduino.enviarDatos("k"); // Derecha
            else if (estadoActual.leftStickX < -DEADZONE) arduino.enviarDatos("j"); // Izquierda

            // SERVO 1: Joystick Izquierdo (Eje Y)
            if (estadoActual.leftStickY < -DEADZONE) arduino.enviarDatos("i"); // Arriba
            else if (estadoActual.leftStickY > DEADZONE) arduino.enviarDatos("m"); // Abajo

            // SERVO 2: Joystick Derecho (Eje Y)
            if (estadoActual.rightStickY < -DEADZONE) arduino.enviarDatos("o"); // Arriba
            else if (estadoActual.rightStickY > DEADZONE) arduino.enviarDatos("p"); // Abajo

            // SERVO 3: Botones LB y RB (se envían continuamente si se mantienen presionados)
            if(estadoActual.lb) arduino.enviarDatos("q");
            if(estadoActual.rb) arduino.enviarDatos("w");

            // Al final, actualizamos el estado anterior para el próximo ciclo
            estadoAnterior = estadoActual;
        }

        private void processTriggerOnChange(String cmdPresionar, String cmdSoltar, float valorAnterior, float valorActual) {
            boolean estabaPresionado = valorAnterior > 0.1f;
            boolean estaPresionado = valorActual > 0.1f;

            if (!estabaPresionado && estaPresionado) {
                arduino.enviarDatos(cmdPresionar);
            } else if (estabaPresionado && !estaPresionado) {
                arduino.enviarDatos(cmdSoltar);
            }
        }
    }

    // =====================================================================================
    // CLASE ControlXbox
    // =====================================================================================
    static class ControlXbox {
        private ControllerManager controllerManager;
        private ControllerState state;
        private int selectedController;

        public enum Boton {
            A, B, X, Y, START, BACK, LB, RB,
            DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT,
            LEFT_STICK_CLICK, RIGHT_STICK_CLICK
        }

        public ControlXbox() {
            controllerManager = new ControllerManager();
            controllerManager.initSDLGamepad();
            selectedController = -1;
        }

        public void listarControles() {
            int count = controllerManager.getNumControllers();
            System.out.println("Controles conectados: " + count);
            for (int i = 0; i < count; i++) {
                ControllerState tempState = controllerManager.getState(i);
                System.out.println("ID " + i + ": " + (tempState.isConnected ? "Conectado" : "Desconectado"));
            }
        }

        public boolean seleccionarControlPorLista(int index) {
            if (index >= 0 && index < controllerManager.getNumControllers()) {
                ControllerState tempState = controllerManager.getState(index);
                if (tempState.isConnected) {
                    selectedController = index;
                    System.out.println("Control seleccionado: " + index);
                    return true;
                }
            }
            System.out.println("Control no válido o desconectado");
            return false;
        }

        public boolean isConnected() {
            actualizarEstado();
            return state != null && state.isConnected;
        }

        public ControllerState getState() {
            if (selectedController < 0 || selectedController >= controllerManager.getNumControllers()) {
                // Intenta encontrar un control conectado si no hay ninguno seleccionado
                for (int i = 0; i < controllerManager.getNumControllers(); i++) {
                    if(controllerManager.getState(i).isConnected) {
                        selectedController = i;
                        System.out.println("Control autoseleccionado: " + i);
                        break;
                    }
                }
                 if (selectedController < 0) return null;
            }
            return controllerManager.getState(selectedController);
        }

        public void actualizarEstado() {
            this.state = getState();
        }

        public boolean estaPresionado(Boton boton) {
            if (state == null) return false;
            return switch (boton) {
                case A -> state.a;
                case B -> state.b;
                case X -> state.x;
                case Y -> state.y;
                case START -> state.start;
                case BACK -> state.back;
                case LB -> state.lb;
                case RB -> state.rb;
                case LEFT_STICK_CLICK -> state.leftStickClick;
                case RIGHT_STICK_CLICK -> state.rightStickClick;
                case DPAD_UP -> state.dpadUp;
                case DPAD_DOWN -> state.dpadDown;
                case DPAD_LEFT -> state.dpadLeft;
                case DPAD_RIGHT -> state.dpadRight;
            };
        }
        
        public void shutdown() {
            controllerManager.quitSDLGamepad();
        }
    }
}
