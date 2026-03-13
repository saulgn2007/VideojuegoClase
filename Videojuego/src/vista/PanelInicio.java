package vista;


import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PanelInicio extends JPanel {

	private static final long serialVersionUID = 1L;
	private Image imagen;
	private Clip clip;	
	public PanelInicio () {

		setLayout(null); // Necesario para posicionar el botón manualmente

		imagen = new ImageIcon(getClass().getResource("/utils/fondo.png")).getImage();
		
		//Llamada al método de mñusca
		musicaFondo("/utils/musicaFondo.wav");
		
		// Crear botón
		BotonesRetro botonJugar = new BotonesRetro("EMPEZAR JUEGO");
		botonJugar.setBounds(380, 500, 150, 40); // Posición y tamaño del botón
		add(botonJugar);

		BotonesRetro botonInstrucciones = new BotonesRetro("INSTRUCCIONES");
		botonInstrucciones.setBounds(610, 500, 200, 40);
		add(botonInstrucciones);

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
		
		botonInstrucciones.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Abrir ventana de instrucciones
				Instrucciones instrucciones = new Instrucciones();
				VideoJuego ventana = (VideoJuego) SwingUtilities.getWindowAncestor(PanelInicio.this);

				ventana.setContentPane(instrucciones);
				ventana.revalidate();
				ventana.repaint();
			}
		});
	}

	private void musicaFondo(String ruta) {
		try {
			InputStream audioSrc = getClass().getResourceAsStream(ruta);
			InputStream bufferedIn = new BufferedInputStream(audioSrc);
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
			
			clip = AudioSystem.getClip();
			clip.open(audioStream);
			
			//CONTROL DEL VOLUMEN
			 FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			 gainControl.setValue(-20.0f); // Reducir el volumen en 10 decibelios	
			clip.loop(Clip.LOOP_CONTINUOUSLY); // Reproducir en bucle
			
			
			clip.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
	}
}


