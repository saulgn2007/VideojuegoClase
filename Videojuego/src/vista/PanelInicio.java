package vista;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelInicio extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private Image imagen;

    public PanelInicio () {

        setLayout(null); // Necesario para posicionar el botón manualmente

        imagen = new ImageIcon(getClass().getResource("/utils/inicio.png")).getImage();

        // Crear botón
        JButton botonJugar = new JButton("EMPEZAR JUEGO");
        botonJugar.setBounds(525, 600, 150, 40); // posición y tamaño
        add(botonJugar);

        // Acción del botón
        botonJugar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Abrir ventana del juego
                PanelEscena1 juego = new PanelEscena1 ();
                juego.setVisible(true);

                // Cerrar ventana actual
                javax.swing.SwingUtilities.getWindowAncestor(PanelInicio.this).dispose();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
    }
}


