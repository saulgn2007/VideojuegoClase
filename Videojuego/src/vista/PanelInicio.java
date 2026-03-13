package vista;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.*;
import javax.swing.*;

public class PanelInicio extends JPanel {

    private static final long serialVersionUID = 1L;
    private Image imagenFondo;
    private Clip clipMusica;

    private JLayeredPane layeredPane;
    private JPanel panelCapaFondo;
    private JPanel panelCapaContenido;

    private BotonesRetro botonJugar;
    private BotonesRetro botonInstrucciones;
    private BotonesRetro botonVolver;
    private JTextPane areaTextoInstrucciones;

    private boolean mostrandoInstrucciones = false;

    public PanelInicio() {
    	
    	Dimension size = new Dimension(1190, 770); 
        this.setPreferredSize(size);
        this.setMinimumSize(size);
        this.setMaximumSize(size);
        this.setLayout(null); // Si usas posicionamiento absoluto
    	
        // Configuracion base
        setLayout(new BorderLayout());
        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // Cargar imagen de fondo
        imagenFondo = new ImageIcon(getClass().getResource("/utils/fondo.png")).getImage();

        // 1. Capa de Fondo (Imagen)
        panelCapaFondo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
            }
        };
        layeredPane.add(panelCapaFondo, Integer.valueOf(0));

        // 2. Capa de Contenido (Centrado y Transparencia)
        panelCapaContenido = new JPanel(new GridBagLayout());
        panelCapaContenido.setOpaque(false); 
        layeredPane.add(panelCapaContenido, Integer.valueOf(1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.CENTER;

        // --- AREA DE INSTRUCCIONES (Oculta al inicio) ---
        areaTextoInstrucciones = new JTextPane();
        areaTextoInstrucciones.setEditable(false);
        areaTextoInstrucciones.setOpaque(false);
        areaTextoInstrucciones.setContentType("text/html");
        areaTextoInstrucciones.setText(generarTextoInstruccionesHTML());
        areaTextoInstrucciones.setVisible(false);
        gbc.gridy = 0; // Ahora ocupa la primera fila disponible
        panelCapaContenido.add(areaTextoInstrucciones, gbc);

        // --- TAMAÑO DE BOTONES ---
        Dimension tamBoton = new Dimension(220, 45);

        // Boton Jugar
        botonJugar = new BotonesRetro("EMPEZAR JUEGO");
        botonJugar.setPreferredSize(tamBoton);
        gbc.gridy = 1;
        panelCapaContenido.add(botonJugar, gbc);

        // Boton Instrucciones
        botonInstrucciones = new BotonesRetro("INSTRUCCIONES");
        botonInstrucciones.setPreferredSize(tamBoton);
        gbc.gridy = 2;
        panelCapaContenido.add(botonInstrucciones, gbc);

        // Boton Volver
        botonVolver = new BotonesRetro("VOLVER");
        botonVolver.setPreferredSize(new Dimension(150, 40));
        botonVolver.setVisible(false);
        gbc.gridy = 3;
        panelCapaContenido.add(botonVolver, gbc);

        configurarAcciones();
        musicaFondo("/utils/musicaFondo.wav");

        // Ajustar capas cuando cambie el tamaño de la ventana
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                actualizarDimensiones();
            }
        });
    }

    private void configurarAcciones() {
        botonJugar.addActionListener(e -> {
            PanelEscena1 escena = new PanelEscena1();
            VideoJuego ventana = (VideoJuego) SwingUtilities.getWindowAncestor(this);
            ventana.setContentPane(escena);
            ventana.revalidate();
        });

        botonInstrucciones.addActionListener(e -> alternarVista(true));
        botonVolver.addActionListener(e -> alternarVista(false));
    }

    private void alternarVista(boolean verInstrucciones) {
        mostrandoInstrucciones = verInstrucciones;

        // Ocultar/Mostrar elementos
        botonJugar.setVisible(!verInstrucciones);
        botonInstrucciones.setVisible(!verInstrucciones);

        areaTextoInstrucciones.setVisible(verInstrucciones);
        botonVolver.setVisible(verInstrucciones);

        // Cambiar el fondo del cuadro (Azul transparente)
        if (verInstrucciones) {
            panelCapaContenido.setOpaque(true);
            panelCapaContenido.setBackground(new Color(20, 30, 85, 210)); 
        } else {
            panelCapaContenido.setOpaque(false);
        }

        revalidate();
        repaint();
    }

    private void actualizarDimensiones() {
        Rectangle r = new Rectangle(0, 0, getWidth(), getHeight());
        layeredPane.setBounds(r);
        panelCapaFondo.setBounds(r);
        panelCapaContenido.setBounds(r);
    }

    private String generarTextoInstruccionesHTML() {
        return "<html><body style='text-align:center; color:white; font-family:Sans-Serif; padding:20px;'>" +
               "<h1 style='font-size:30px;'>INSTRUCCIONES</h1>" +
               "<table width='100%'>" +
               "<tr>" +
               "<td style='padding:10px; text-align:left;'>" +
               "<b>CONTROLES</b><br>W - Arriba<br>S - Abajo<br>A - Izquierda<br>D - Derecha<br>FLECHAS TAMBIÉN" +
               "</td>" +
               "<td style='padding:10px; text-align:left;'>" +
               "<b>BOMBAS</b><br>ESPACIO - Colocar<br>Explota y destruye<br>enemigos y bloques." +
               "</td>" +
               "</tr>" +
               "</table>" +
               "<br><b>OBJETIVO</b><br>Derrota a todos los enemigos.<br>Evita explosiones y contacto." +
               "</body></html>";
    }

    private void musicaFondo(String ruta) {
        try {
            InputStream audioSrc = getClass().getResourceAsStream(ruta);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            clipMusica = AudioSystem.getClip();
            clipMusica.open(audioStream);
            FloatControl gainControl = (FloatControl) clipMusica.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-20.0f);
            clipMusica.loop(Clip.LOOP_CONTINUOUSLY);
            clipMusica.start();
        } catch (Exception e) {
            System.err.println("Error al cargar música: " + e.getMessage());
        }
    }
}