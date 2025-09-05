import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.fazecast.jSerialComm.*;

/**
 * Clase principal para la configuración del Arduino
 * Esta ventana permite al usuario configurar la conexión con Arduino
 * usando únicamente Control Xbox
 */
public class Configuracion extends JFrame {

    // Componentes de la interfaz gráfica
    private JComboBox<String> puertoCombo;        // Lista desplegable para seleccionar puerto COM
    private JComboBox<String> arduinoCombo;      // Lista para seleccionar modelo de Arduino
    private JComboBox<String> baudRateCombo;     // Lista para seleccionar velocidad de comunicación
    private JLabel estadoLabel;                   // Etiqueta para mostrar estado
    private JTextField estadoConexionField;      // Campo que muestra si hay conexión o no
    private JTextField estadoControlField;       // Campo que muestra disponibilidad del control
    
    // Variable estática que guarda el tipo de control (siempre Xbox)
    private static String tipoControlSeleccionado = "Control Xbox"; 

    /**
     * Regresa al menú principal sin guardar las configuraciones
     * Útil si el usuario quiere cancelar los cambios
     */
    private void regresarAlMenu() {
        // Preguntar al usuario si está seguro de salir sin guardar
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que quieres regresar sin guardar?\nSe perderán los cambios realizados.",
            "Confirmar salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        // Si el usuario confirma, regresar al menú
        if (respuesta == JOptionPane.YES_OPTION) {
            new MenuGUI().setVisible(true);
            dispose(); // Cerrar esta ventana
        }
        // Si dice "No", no hacer nada (seguir en la ventana actual)
    }

    /**
     * Constructor de la ventana de configuración
     * Aquí se crean todos los elementos visuales y se configuran
     */
    public Configuracion() {
        // Configuración básica de la ventana
        setTitle("Configuración del Arduino - Control Xbox");
        setSize(500, 550);                    // Tamaño de la ventana (más pequeña)
        setLocationRelativeTo(null);          // Centrar la ventana en pantalla
        setLayout(null);                      // Layout manual (posiciones absolutas)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar programa al cerrar ventana

        // ===== TÍTULO PRINCIPAL =====
        JLabel lblTitulo = new JLabel("Configuración del Arduino", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setBounds(50, 20, 400, 40);  // x, y, ancho, alto
        add(lblTitulo);

        // ===== SUBTÍTULO =====
        JLabel lblSubtitulo = new JLabel("Control Xbox", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Arial", Font.ITALIC, 16));
        lblSubtitulo.setForeground(new Color(108, 117, 125));
        lblSubtitulo.setBounds(50, 60, 400, 25);
        add(lblSubtitulo);

        // ===== SELECCIÓN DE PUERTO COM =====
        JLabel lblPuerto = new JLabel("Puerto COM:");
        lblPuerto.setBounds(50, 110, 150, 25);
        add(lblPuerto);

        // Lista desplegable que se llena automáticamente con los puertos disponibles
        puertoCombo = new JComboBox<>();
        detectarPuertosSerial();  // Busca y agrega los puertos COM disponibles
        puertoCombo.setBounds(200, 110, 200, 25);
        add(puertoCombo);

        // ===== SELECCIÓN DEL MODELO DE ARDUINO =====
        JLabel lblModelo = new JLabel("Modelo Arduino:");
        lblModelo.setBounds(50, 160, 150, 25);
        add(lblModelo);

        // Lista con los modelos más comunes de Arduino
        arduinoCombo = new JComboBox<>(new String[] {
            "UNO", "Mega", "Nano", "Leonardo", "Due"
        });
        arduinoCombo.setBounds(200, 160, 200, 25);
        add(arduinoCombo);

        // ===== SELECCIÓN DE VELOCIDAD DE COMUNICACIÓN =====
        JLabel lblBaud = new JLabel("Velocidad (baud):");
        lblBaud.setBounds(50, 210, 150, 25);
        add(lblBaud);

        // Velocidades estándar para comunicación serial
        baudRateCombo = new JComboBox<>(new String[] {
            "9600", "19200", "38400", "57600", "115200"
        });
        baudRateCombo.setBounds(200, 210, 200, 25);
        add(baudRateCombo);

        // ===== INFORMACIÓN DEL CONTROL XBOX =====
        JTextArea infoControlArea = new JTextArea();
        infoControlArea.setEditable(false);                    // Solo lectura
        infoControlArea.setBackground(new Color(245, 245, 245)); // Fondo gris claro
        infoControlArea.setBorder(BorderFactory.createTitledBorder("Información del Control Xbox"));
        infoControlArea.setFont(new Font("Arial", Font.PLAIN, 12));
        infoControlArea.setText("Control Xbox:\n• Joysticks analógicos para movimiento suave\n• Botones programables\n• Requiere driver Xbox en el sistema\n• Detección automática del control");
        infoControlArea.setBounds(50, 250, 400, 80);
        add(infoControlArea);

        // ===== ESTADO DEL CONTROL XBOX =====
        JLabel lblEstadoControl = new JLabel("Estado Control Xbox:");
        lblEstadoControl.setBounds(50, 340, 150, 25);
        add(lblEstadoControl);

        // Campo que indica si el control Xbox está disponible
        estadoControlField = new JTextField();
        estadoControlField.setEditable(false);
        estadoControlField.setBounds(200, 340, 150, 25);
        add(estadoControlField);

        // ===== BOTÓN PARA DETECTAR CONTROL XBOX =====
        JButton btnDetectarXbox = new JButton("Detectar Xbox");
        btnDetectarXbox.setBounds(360, 340, 90, 25);
        btnDetectarXbox.setBackground(new Color(255, 140, 0));  // Naranja
        btnDetectarXbox.setForeground(Color.WHITE);
        btnDetectarXbox.setFocusPainted(false);
        btnDetectarXbox.addActionListener(e -> verificarDisponibilidadControl());
        add(btnDetectarXbox);

        // ===== BOTÓN PARA PROBAR CONEXIÓN =====
        JButton btnProbar = new JButton("Probar conexión Arduino");
        btnProbar.setBounds(50, 380, 200, 35);
        btnProbar.setBackground(new Color(51, 153, 255));  // Azul
        btnProbar.setForeground(Color.WHITE);              // Texto blanco
        btnProbar.setFocusPainted(false);                  // Sin borde de enfoque
        btnProbar.addActionListener(e -> probarConexionReal()); // Acción al hacer clic
        add(btnProbar);

        // ===== ESTADO DE LA CONEXIÓN =====
        estadoLabel = new JLabel("Estado Arduino:");
        estadoLabel.setBounds(260, 380, 100, 25);
        add(estadoLabel);

        // Campo que muestra el resultado de la prueba de conexión
        estadoConexionField = new JTextField("Sin conexión");
        estadoConexionField.setEditable(false);
        estadoConexionField.setBounds(260, 400, 190, 25);
        estadoConexionField.setForeground(Color.RED); // Rojo para "sin conexión"
        add(estadoConexionField);

        // ===== BOTÓN PARA GUARDAR Y CONTINUAR =====
        JButton btnGuardar = new JButton("Guardar configuraciones");
        btnGuardar.setBounds(50, 450, 180, 40);
        btnGuardar.setBackground(new Color(0, 204, 102));  // Verde
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> guardarYVolver()); // Guarda config y va al menú
        add(btnGuardar);

        // ===== BOTÓN PARA REGRESAR AL MENÚ SIN GUARDAR =====
        JButton btnRegresar = new JButton("Regresar al Menú");
        btnRegresar.setBounds(250, 450, 180, 40);
        btnRegresar.setBackground(new Color(108, 117, 125));  // Gris
        btnRegresar.setForeground(Color.WHITE);
        btnRegresar.setFocusPainted(false);
        btnRegresar.addActionListener(e -> regresarAlMenu()); // Regresa sin guardar
        add(btnRegresar);

        // Inicializar el estado del control Xbox (inicialmente no detectado)
        estadoControlField.setText("No detectado");
        estadoControlField.setForeground(Color.RED);
    }

    /**
     * Verifica si hay un control Xbox conectado al sistema
     * Corrige el bug inicial mostrando el estado real
     */
    private void verificarDisponibilidadControl() {
        if (detectarControlXbox()) {
            estadoControlField.setText("Xbox Detectado");
            estadoControlField.setForeground(new Color(0, 153, 0)); // Verde
        } else {
            estadoControlField.setText("Xbox No Detectado");
            estadoControlField.setForeground(Color.RED); // Rojo
        }
    }

    /**
     * Detecta si hay un control Xbox conectado al sistema
     * Mejorada para una detección más precisa
     */
    private boolean detectarControlXbox() {
        try {
            // Verificar dispositivos USB que sean controles Xbox
            ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", 
                "Get-PnpDevice | Where-Object {$_.FriendlyName -like '*Xbox*' -and $_.Status -eq 'OK'}");
            Process process = pb.start();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            
            String line;
            boolean xboxFound = false;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("xbox") && line.toLowerCase().contains("ok")) {
                    xboxFound = true;
                    break;
                }
            }
            
            process.waitFor();
            reader.close();
            
            // También verificar si existe la librería de Xbox
            if (!xboxFound) {
                java.io.File xinputDll = new java.io.File("C:\\Windows\\System32\\xinput1_4.dll");
                if (xinputDll.exists()) {
                    // Verificar si hay algún proceso relacionado con Xbox
                    pb = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq XboxStat.exe");
                    process = pb.start();
                    process.waitFor();
                    // Si no hay errores, podría haber un Xbox conectado
                }
            }
            
            return xboxFound;
            
        } catch (Exception e) {
            System.out.println("Error detectando Xbox: " + e.getMessage());
            return false;
        }
    }

    /**
     * Detecta automáticamente todos los puertos COM (Serial) disponibles
     * y los agrega a la lista desplegable
     */
    private void detectarPuertosSerial() {
        SerialPort[] ports = SerialPort.getCommPorts(); // Obtener todos los puertos disponibles
        
        // Agregar cada puerto encontrado a la lista
        for (SerialPort port : ports) {
            puertoCombo.addItem(port.getSystemPortName());
        }
        
        // Si no se encontraron puertos, mostrar mensaje y deshabilitar
        if (puertoCombo.getItemCount() == 0) {
            puertoCombo.addItem("No hay puertos");
            puertoCombo.setEnabled(false);
        }
    }

    /**
     * Prueba la conexión real con el Arduino
     */
    private void probarConexionReal() {
        String puertoSeleccionado = (String) puertoCombo.getSelectedItem();
        
        // Verificar que hay un puerto seleccionado
        if (puertoSeleccionado == null || puertoSeleccionado.contains("No hay puertos")) {
            estadoConexionField.setText("No hay puertos");
            estadoConexionField.setForeground(Color.RED);
            return;
        }

        // Intentar conectar al Arduino con los parámetros seleccionados
        int baudRate = Integer.parseInt((String) baudRateCombo.getSelectedItem());
        SerialPort serialPort = SerialPort.getCommPort(puertoSeleccionado);
        serialPort.setBaudRate(baudRate);

        // Probar la conexión
        if (serialPort.openPort()) {
            // ¡Conexión exitosa!
            estadoConexionField.setText("¡Conectado!");
            estadoConexionField.setForeground(new Color(0, 153, 0)); // Verde
            serialPort.closePort(); // Cerrar inmediatamente después de probar
        } else {
            // Error al conectar
            estadoConexionField.setText("Error al conectar");
            estadoConexionField.setForeground(Color.RED); // Rojo
        }
    }

    /**
     * Guarda todas las configuraciones seleccionadas y regresa al menú principal
     * Verifica que tanto el Xbox como el Arduino estén funcionando
     */
    private void guardarYVolver() {
        // Verificar que el control Xbox esté detectado
        if (!estadoControlField.getText().equals("Xbox Detectado")) {
            JOptionPane.showMessageDialog(this,
                "No se puede detectar el Control Xbox.\nVerifica que esté conectado y los drivers instalados.\nUsa el botón 'Detectar Xbox' para verificar.",
                "Error de Control Xbox", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar que haya conexión con Arduino
        if (!estadoConexionField.getText().equals("¡Conectado!")) {
            JOptionPane.showMessageDialog(this,
                "No puedes guardar. Verifica que la conexión con Arduino esté funcionando.\nUsa el botón 'Probar conexión Arduino' para verificar.",
                "Error de conexión Arduino", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Si llegamos aquí, todo está bien. Guardar la configuración
        String puerto = (String) puertoCombo.getSelectedItem();
        String modelo = (String) arduinoCombo.getSelectedItem();
        String baudRate = (String) baudRateCombo.getSelectedItem();
        
        // Mostrar resumen de lo que se guardó
        String mensaje = String.format(
            "Configuración guardada:\n" +
            "Puerto: %s\n" +
            "Modelo: %s\n" +
            "Baud Rate: %s\n" +
            "Tipo de Control: Control Xbox",
            puerto, modelo, baudRate
        );
        
        JOptionPane.showMessageDialog(this, mensaje, "Guardado", JOptionPane.INFORMATION_MESSAGE);

        // Regresar al menú principal
        new MenuGUI().setVisible(true);
        dispose(); // Cerrar esta ventana
    }

    /**
     * Método estático para obtener el tipo de control (siempre Xbox)
     * Otras clases pueden usar este método para saber qué control usar
     */
    public static String getTipoControlSeleccionado() {
        return tipoControlSeleccionado;
    }

    /**
     * Método de instancia para obtener el tipo de control actual
     * (para compatibilidad con versiones anteriores del código)
     */
    public String getTipoControl() {
        return "Control Xbox";
    }
}