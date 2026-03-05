package vista;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PanelEscena1 extends JPanel implements Runnable{

	private static final long serialVersionUID = 1L;
	private Image imagen, jugador;

	private int x = 1;//editado
	private int y = 1;//editado
	private final int ANCHO = 50;
	private final int ALTO = 50;
	private int velocidad = 5;


	private Thread hilo;

	//Variables para el movimiento
	private boolean arriba, abajo, izquierda, derecha;	

	public PanelEscena1() {

		// Imagen del fondo del juego
		imagen = new ImageIcon(getClass().getResource("/utils/escenario.png")).getImage();
		jugador = new ImageIcon(getClass().getResource("/utils/barcelona.png")).getImage();

		setFocusable(true); //Muy importante ya que si no lo ponemos, no reacciona a las pulsaciones del teclado

		// Listener de teclado
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_RIGHT -> derecha = true;
				case KeyEvent.VK_LEFT  -> izquierda = true;
				case KeyEvent.VK_UP    -> arriba = true;
				case KeyEvent.VK_DOWN  -> abajo = true;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_RIGHT -> derecha = false;
				case KeyEvent.VK_LEFT  -> izquierda = false;
				case KeyEvent.VK_UP    -> arriba = false;
				case KeyEvent.VK_DOWN  -> abajo = false;
				}
			}
		});

		// Iniciar hilo del juego
		hilo = new Thread(this);
		hilo.start();
	}

	@Override
	public void addNotify() {
		super.addNotify();
		SwingUtilities.invokeLater(() -> requestFocusInWindow());
	}



	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
		g.drawImage(jugador, x, y, 100, 100, this); //posición x, y, tamaño del personaje

	}

	@Override
	public void run() {
		while (true) {
			actualizarPosicion();
			repaint();

			try {
				Thread.sleep(16); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void actualizarPosicion() {
		if (derecha && x + ANCHO < getWidth()) x += velocidad;
		if (izquierda && x > 0) x -= velocidad;
		if (arriba && y > 0) y -= velocidad;
		if (abajo && y + ALTO < getHeight()) y += velocidad;
	}

}





