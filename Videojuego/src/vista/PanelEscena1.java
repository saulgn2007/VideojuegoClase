package vista;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PanelEscena1 extends JPanel implements KeyListener{

	private static final long serialVersionUID = 1L;
	private Image imgSuelo, jugador, imgPared, imgExplosion, imgBomba;
	private final int TAMANO_CELDA = 70;
	private int[][] mapa; // Mapa de valores 0 y 1
	private int x = 1;//editado
	private int y = 1;//editado
	private final int ANCHO = 50;
	private final int ALTO = 50;
	private int velocidad = 1;

	// VARIABLES BOMBA
	private boolean bombaActiva = false;
	private boolean explosion = false;
	private int bombaX;
	private int bombaY;
	private boolean muerto = false;
	
	public PanelEscena1() {

		mapa = new int[11][17];
		for (int i = 0; i < mapa.length; i++) {
			for (int j = 0; j < mapa[i].length; j++) {
				mapa[i][j] = (int) Math.round(Math.random());
			}
		}
		mapa[y][x] = 0;

		//Imagen del fondo del juego

		jugador = new ImageIcon(getClass().getResource("/utils/vecna.png")).getImage();
		imgPared = new ImageIcon(getClass().getResource("/utils/suelopiedrasombra.png")).getImage();
		imgSuelo = new ImageIcon(getClass().getResource("/utils/arenasuelo.png")).getImage();
		imgExplosion = new ImageIcon(getClass().getResource("/utils/explosion.png")).getImage();
		imgBomba = new ImageIcon(getClass().getResource("/utils/bomba.png")).getImage();


		setFocusable(true); //Muy importante ya que si no lo ponemos, no reacciona a las pulsaciones del teclado
		addKeyListener(this);
	}


	@Override
	public void addNotify() {
		super.addNotify();
		SwingUtilities.invokeLater(() -> requestFocusInWindow());
	}



	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (int i = 0; i < mapa.length; i++) {
			for (int j = 0; j < mapa[i].length; j++) {
				if (mapa[i][j] == 1) {
					g.fillRect(j * TAMANO_CELDA, i * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA);
					g.drawImage(imgPared, j * TAMANO_CELDA, i * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);
				} else {
					g.drawImage(imgSuelo, j * TAMANO_CELDA, i * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);

				}
			}
		} 

		g.drawImage(jugador, x * TAMANO_CELDA, y * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);

		// DIBUJAR BOMBA
		if (bombaActiva && !explosion) {
			g.drawImage(imgBomba, bombaX * TAMANO_CELDA, bombaY * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);
			
		}

		// DIBUJAR EXPLOSION
		// DIBUJAR EXPLOSION TIPO BOMBERMAN
		if (explosion) {

			// centro
			g.drawImage(imgExplosion, bombaX * TAMANO_CELDA, bombaY * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);

			// derecha
			g.drawImage(imgExplosion, (bombaX + 1) * TAMANO_CELDA, bombaY * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);

			// izquierda
			g.drawImage(imgExplosion, (bombaX - 1) * TAMANO_CELDA, bombaY * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);

			// abajo
			g.drawImage(imgExplosion, bombaX * TAMANO_CELDA, (bombaY + 1) * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);

			// arriba
			g.drawImage(imgExplosion, bombaX * TAMANO_CELDA, (bombaY - 1) * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);
		}
		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		int newX = x;
		int newY = y;

		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			newY--;
			break;
		case KeyEvent.VK_DOWN:
			newY++;
			break;
		case KeyEvent.VK_LEFT:
			newX--;
			break;
		case KeyEvent.VK_RIGHT:
			newX++;
			break;
		case KeyEvent.VK_SPACE:

			if (!bombaActiva) {
				bombaActiva = true;
				bombaX = x;
				bombaY = y;

				repaint();

				new Thread(() -> {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ex) {}

					explosion = true;

					// ROMPER BLOQUES
					romperBloques();

					repaint();

					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {}

					explosion = false;
					bombaActiva = false;
					repaint();

				}).start();
			}

			break;
		}

		if (newX >= 0 && newX < mapa[0].length && newY >= 0 && newY < mapa.length && mapa[newY][newX] == 0) {
			x = newX;
			y = newY;
			repaint();
		}
	}

	// METODO PARA ROMPER BLOQUES
	private void romperBloques() {

		int[][] direcciones = {
				{0,0},
				{1,0},
				{-1,0},
				{0,1},
				{0,-1}
		};

		for (int[] d : direcciones) {

			int nx = bombaX + d[0];
			int ny = bombaY + d[1];

			if (nx >= 0 && nx < mapa[0].length && ny >= 0 && ny < mapa.length) {

				if (mapa[ny][nx] == 1) {
					mapa[ny][nx] = 0;
				}

			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

}