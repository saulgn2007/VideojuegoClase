package vista;

import java.awt.*;
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

    public PanelInicio() {
        Dimension size = new Dimension(1190, 770); 
        this.setPreferredSize(size);
        this.setMinimumSize(size);
        this.setMaximumSize(size);
        
        // Configuración base
        setLayout(new BorderLayout());
        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // Cargar imagen de fondo
        imagenFondo = new ImageIcon(getClass().getResource("/utils/fondo.png")).getImage();

        // 1. Capa de Fondo
        panelCapaFondo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
            }
        };
        layeredPane.add(panelCapaFondo, Integer.valueOf(0));

        // 2. Capa de Contenido
        panelCapaContenido = new JPanel(new GridBagLayout());
        panelCapaContenido.setOpaque(false); 
        layeredPane.add(panelCapaContenido, Integer.valueOf(1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.CENTER;

        // --- AREA DE INSTRUCCIONES ACTUALIZADA ---
        areaTextoInstrucciones = new JTextPane();
        areaTextoInstrucciones.setEditable(false); 
        areaTextoInstrucciones.setFocusable(false);    // <--- EVITA EL FOCO
        areaTextoInstrucciones.setHighlighter(null);  // <--- EVITA LA SELECCIÓN AZUL
        areaTextoInstrucciones.setOpaque(false); 
        areaTextoInstrucciones.setContentType("text/html");
        areaTextoInstrucciones.setText(generarTextoInstruccionesHTML());
        areaTextoInstrucciones.setVisible(false);
        
        gbc.gridy = 0;
        panelCapaContenido.add(areaTextoInstrucciones, gbc);

        // --- BOTONES ---
        Dimension tamBoton = new Dimension(220, 45);

        botonJugar = new BotonesRetro("EMPEZAR JUEGO");
        botonJugar.setPreferredSize(tamBoton);
        gbc.gridy = 1;
        panelCapaContenido.add(botonJugar, gbc);

        botonInstrucciones = new BotonesRetro("INSTRUCCIONES");
        botonInstrucciones.setPreferredSize(tamBoton);
        gbc.gridy = 2;
        panelCapaContenido.add(botonInstrucciones, gbc);

        botonVolver = new BotonesRetro("VOLVER");
        botonVolver.setPreferredSize(new Dimension(150, 40));
        botonVolver.setVisible(false);
        gbc.gridy = 3;
        panelCapaContenido.add(botonVolver, gbc);

        configurarAcciones();
        musicaFondo("/utils/musicaFondo.wav");

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
        // Ocultar/Mostrar elementos
        botonJugar.setVisible(!verInstrucciones);
        botonInstrucciones.setVisible(!verInstrucciones);
        areaTextoInstrucciones.setVisible(verInstrucciones);
        botonVolver.setVisible(verInstrucciones);

        // Cambiar el fondo del cuadro
        if (verInstrucciones) {
            panelCapaContenido.setOpaque(true);
            panelCapaContenido.setBackground(new Color(20, 30, 85, 210)); 
        } else {
            panelCapaContenido.setOpaque(false);
        }

        panelCapaContenido.revalidate();
        panelCapaContenido.repaint();
        this.repaint(); // Refresco extra para limpiar basura visual
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
            if (audioSrc == null) return;
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