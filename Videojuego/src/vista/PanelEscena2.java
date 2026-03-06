package vista;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import modelo.Personaje;

public class PanelEscena2 extends JPanel implements KeyListener {

	private Personaje jugadorVida = new Personaje(3, 1);
	private static final long serialVersionUID = 1L;
	private Image imgSuelo, jugador, imgPared, imgExplosion, imgBomba, imgEnemigo;
	private final int TAMANO_CELDA = 70;
	private int[][] mapa;
	private int x = 1, y = 1;

	// Lista de enemigos
	private ArrayList<Enemigo> enemigos = new ArrayList<>();

	// VARIABLES BOMBA
	private boolean bombaActiva = false;
	private boolean explosion = false;
	private int bombaX, bombaY;

	//Control del juego
	private boolean jugando = true;

	public PanelEscena2() {
		mapa = new int[11][17];
		Random rand = new Random();
		for (int i = 0; i < mapa.length; i++) {
			for (int j = 0; j < mapa[i].length; j++) {
				mapa[i][j] = (int) Math.round(Math.random());
			}
		}
		mapa[y][x] = 0;


		jugador = new ImageIcon(getClass().getResource("/utils/luffy.png")).getImage();
		imgPared = new ImageIcon(getClass().getResource("/utils/suelopiedra4.png")).getImage();
		imgSuelo = new ImageIcon(getClass().getResource("/utils/suelopiedra5.jpg")).getImage();
		imgExplosion = new ImageIcon(getClass().getResource("/utils/explosion.png")).getImage();
		imgBomba = new ImageIcon(getClass().getResource("/utils/bomba.png")).getImage();
		imgEnemigo = new ImageIcon(getClass().getResource("/utils/bot2.png")).getImage();

		// Generar enemigos
		int cantidadEnemigos = 1;
		while(enemigos.size() < cantidadEnemigos) {
			int ex = rand.nextInt(mapa[0].length);
			int ey = rand.nextInt(mapa.length);
			if (mapa[ey][ex] == 0 && (ex != x || ey != y)) {
				boolean ocupado = false;
				for(Enemigo e : enemigos) {
					if(e.x == ex && e.y == ey) {
						ocupado = true;
						break;
					}
				}
				if(!ocupado) enemigos.add(new Enemigo(ex, ey));
			}
		}

		setFocusable(true);
		addKeyListener(this);

		// Timer para mover enemigos
		Timer timerEnemigo = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!jugando) return;
				for(Enemigo en : enemigos) {
					if(en.vivo) movimientoEnemigo(en);
				}
				verificarGolpeEnemigos();
			}
		});
		timerEnemigo.start();
	}

	@Override
	public void addNotify() {
		super.addNotify();
		SwingUtilities.invokeLater(() -> requestFocusInWindow());
	}


	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Dibujar mapa
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

		// Dibujar jugador solo si sigue vivo
		if(jugadorVida.estaVivo()) {
			g.drawImage(jugador, x * TAMANO_CELDA, y * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);
		}

		// Dibujar enemigos
		for(Enemigo en : enemigos) {
			if(en.vivo) g.drawImage(imgEnemigo, en.x * TAMANO_CELDA, en.y * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);
		}

		// Dibujar bomba
		if(bombaActiva && !explosion) {
			g.drawImage(imgBomba, bombaX * TAMANO_CELDA, bombaY * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);
		}

		// Dibujar explosion
		if(explosion) {
			int[][] direcciones = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
			for(int[] d : direcciones) {
				int ex = bombaX + d[0];
				int ey = bombaY + d[1];
				if(ex >= 0 && ex < mapa[0].length && ey >= 0 && ey < mapa.length) {
					g.drawImage(imgExplosion, ex * TAMANO_CELDA, ey * TAMANO_CELDA, TAMANO_CELDA, TAMANO_CELDA, this);
				}
			}
		}

		// Mostrar vida
		g.setColor(Color.GREEN);
		g.setFont(g.getFont().deriveFont(Font.BOLD, 40f));
		g.drawString("Vida: " + jugadorVida.getVidaBueno(), 20, 50);

		// Pantalla GAME OVER
		if(!jugadorVida.estaVivo()) {
			jugando = false; // bloquea movimiento y bombas
			g.setColor(new Color(0,0,0,200));
			g.fillRect(0,0,getWidth(),getHeight());
			g.setColor(Color.RED);
			g.setFont(g.getFont().deriveFont(Font.BOLD, 70f));
			g.drawString("GAME OVER", getWidth()/2 - 200, getHeight()/2);

			g.setColor(Color.WHITE);
			g.setFont(g.getFont().deriveFont(Font.BOLD, 30f));
			g.drawString("Presiona R para reiniciar", getWidth()/2 - 180, getHeight()/2 + 80);
		}
	}
	
	
	private Clip reproducirSonido(String tictacsonido) {
		try {
			// Busca el archivo en la carpeta de recursos
			URL url = getClass().getResource("/utils/" + tictacsonido);
			if (url != null) {
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
				Clip clip = AudioSystem.getClip();
				clip.open(audioIn);
				clip.start(); // Reproduce el sonido
				return clip; // Devuelve el clip para control futuro.
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	
	private void movimientoEnemigo(Enemigo en) {
		Random rand = new Random();
		int newX = en.x;
		int newY = en.y;
		int direction = rand.nextInt(4);
		switch(direction) {
		case 0: newY--; break;
		case 1: newY++; break;
		case 2: newX--; break;
		case 3: newX++; break;
		}
		if(newX >= 0 && newX < mapa[0].length && newY >= 0 && newY < mapa.length && mapa[newY][newX] == 0) {
			boolean ocupado = false;
			for(Enemigo e : enemigos) {
				if(e != en && e.vivo && e.x == newX && e.y == newY) { ocupado = true; break; }
			}
			if(!ocupado) { en.x = newX; en.y = newY; }
		}
		repaint();
	}

	private void verificarGolpeEnemigos() {
		for(Enemigo en : enemigos) {
			if(en.vivo && en.x == x && en.y == y) {
				jugadorVida.recibirDano(1);
			}
		}
	}

	private void verificarGolpeEnemigosBomba() {
		for(Enemigo en : enemigos) {
			if(!en.vivo) continue;
			int[][] direcciones = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
			for(int[] d : direcciones) {
				int ex = bombaX + d[0];
				int ey = bombaY + d[1];
				if(en.x == ex && en.y == ey) {
					en.vivo = false;
				}
			}
		}
	}

	
	@Override
	public void keyPressed(KeyEvent e) {
		if(!jugando && e.getKeyCode() == KeyEvent.VK_R) {
			reiniciarJuego();
			return;
		}

		if(!jugando) return; // no puede moverse ni poner bombas

		int newX = x;
		int newY = y;

		switch(e.getKeyCode()) {
		case KeyEvent.VK_UP: newY--; break;
		case KeyEvent.VK_DOWN: newY++; break;
		case KeyEvent.VK_LEFT: newX--; break;
		case KeyEvent.VK_RIGHT: newX++; break;
		case KeyEvent.VK_SPACE:
			if(!bombaActiva) {
				bombaActiva = true;
				bombaX = x;
				bombaY = y;
				repaint();

				new Thread(() -> {

					//Inicia el sonido del tictac de la bomba guardando la referencia en tictac para poder detenerlo después
					Clip tictac = reproducirSonido("tictacsonido.wav");

					try { 
						Thread.sleep(800); 
					} catch(InterruptedException ex){}

					if (tictac != null && tictac.isRunning()) {
						tictac.stop(); // Detiene el sonido del tictac
						tictac.close(); // Libera recursos del clip
					}
					//Empieza la explosión
					explosion = true;

					//Llamamos al sonido de la explosión
					reproducirSonido("sonidoexplosion.wav");

					verificarGolpe();
					verificarGolpeEnemigosBomba();
					romperBloques();
					repaint();

					
					try { 
						Thread.sleep(500); 
					} catch(InterruptedException ex){}

					explosion = false;
					bombaActiva = false;
					repaint();
				}).start();
			}
			break;
		}

		if(newX >= 0 && newX < mapa[0].length && newY >= 0 && newY < mapa.length && mapa[newY][newX] == 0) {
			x = newX;
			y = newY;
			repaint();
		}
	}

	private void romperBloques() {
		int[][] direcciones = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
		for(int[] d : direcciones) {
			int nx = bombaX + d[0];
			int ny = bombaY + d[1];
			if(nx >= 0 && nx < mapa[0].length && ny >= 0 && ny < mapa.length) {
				if(mapa[ny][nx] == 1) mapa[ny][nx] = 0;
			}
		}
	}

	private void verificarGolpe() {
		int[][] direcciones = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
		for(int[] d : direcciones) {
			int ex = bombaX + d[0];
			int ey = bombaY + d[1];
			if(x == ex && y == ey) {
				jugadorVida.recibirDano(1);
			}
		}
	}
	
	private void reiniciarJuego() {
		jugadorVida = new Personaje(3,1);
		x = 1; y = 1;
		bombaActiva = false;
		explosion = false;
		enemigos.clear();
		Random rand = new Random();
		int cantidadEnemigos = 3;
		while(enemigos.size() < cantidadEnemigos) {
			int ex = rand.nextInt(mapa[0].length);
			int ey = rand.nextInt(mapa.length);
			if (mapa[ey][ex] == 0 && (ex != x || ey != y)) {
				boolean ocupado = false;
				for(Enemigo e : enemigos) {
					if(e.x == ex && e.y == ey) { ocupado = true; break; }
				}
				if(!ocupado) enemigos.add(new Enemigo(ex, ey));
			}
		}
		jugando = true;
		repaint();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}