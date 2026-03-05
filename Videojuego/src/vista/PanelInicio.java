package vista;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelInicio extends JPanel {

	private static final long serialVersionUID = 1L;
	private Image imagen;

	public PanelInicio () {

		setLayout(null); // Necesario para posicionar el botón manualmente

		imagen = new ImageIcon(getClass().getResource("/utils/inicio.png")).getImage();

		// Crear botón
		JButton botonJugar = new JButton("EMPEZAR JUEGO");
		botonJugar.setBounds(525, 500, 150, 40); // posición y tamaño
		add(botonJugar);

		JButton botonPersonaje = new JButton("ELEGIR PERSONAJE");
		botonPersonaje.setBounds(520, 400, 160, 40);
		add(botonPersonaje);
		
		// Acción del botón
		botonJugar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Abrir ventana del juego
				PanelEscena1 escena = new PanelEscena1 ();
				VideoJuego ventana = (VideoJuego) SwingUtilities.getWindowAncestor(PanelInicio.this);

				ventana.setContentPane(escena);
				ventana.revalidate();
				ventana.repaint();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
	}
}


