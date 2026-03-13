package vista;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import modelo.Personaje;

/**
 * Clase Enemigo: Actualizada para movimiento fluido.
 * DAM TIP: Usamos 'double' para las coordenadas x e y para que al sumar 
 * la velocidad (píxeles), el movimiento sea suave.
 */
class Enemigo {
	double x, y; // Coordenadas en píxeles
	double vel = 2.5; // Velocidad de desplazamiento fluido
	String direccion = "Abajo";
	boolean vivo = true;

	public Enemigo(int gridX, int gridY) { 
		// Convertimos la posición de la cuadrícula a píxeles iniciales
		this.x = gridX * 70; 
		this.y = gridY * 70; 
	}
}

public class PanelEscena1 extends JPanel implements KeyListener {

	private Personaje jugadorVida = new Personaje(3, 1);
	private static final long serialVersionUID = 1L;
	private Image imgParedIrrompible, imgSuelo, imgPared, imgExplosion, imgBomba, imgEnemigo, imgVida, imgVidaVacia;

	private Image[] animArriba = new Image[9], animAbajo = new Image[9], animIzquierda = new Image[9];
	private int frameActual = 0;
	private long ultimoCambioFrame = 0;

	private final int TAMANO_CELDA = 70;
	private int[][] mapa; 

	private double posX = 70, posY = 70; 
	private double velocidad = 7.0; 
	private String direccionActual = "Abajo";
	private boolean moviendose = false;

	private ArrayList<Enemigo> enemigos = new ArrayList<>();
	private boolean bombaActiva = false, explosion = false;
	private int bombaX, bombaY; 

	private boolean jugando = true, invulnerable = false;

	public PanelEscena1() {
		mapa = new int[11][17]; 
		Random rand = new Random();
		
		for (int i = 0; i < mapa.length; i++) {
			for (int j = 0; j < mapa[i].length; j++) {
				if (i == 0 || i == 10 || j == 0 || j == 16) {
					mapa[i][j] = 2;
				} else {
					mapa[i][j] = (rand.nextFloat() < 0.3) ? 1 : 0;
				}
			}
		}
		mapa[1][1] = 0; 

		cargarRecursos(); 
		generarEnemigos(); 

		setFocusable(true);
		addKeyListener(this);

		// --- MOTOR DEL JUEGO (Game Loop) ---
		// Ahora los enemigos también se mueven aquí para que sea fluido
		Timer gameLoop = new Timer(16, e -> {
			if (jugando) {
				actualizarMovimiento(); // Movimiento Luffy
				
				// Movimiento fluido de cada enemigo
				for (Enemigo en : enemigos) {
					if (en.vivo) actualizarMovimientoEnemigo(en);
				}
				
				verificarGolpeEnemigos(); // Colisión píxel a píxel
				repaint(); 
			}
		});
		gameLoop.start();

		// Hilo para que los enemigos decidan cambiar de dirección al azar
		Timer timerIA = new Timer(800, e -> {
			if (!jugando) return;
			Random r = new Random();
			for (Enemigo en : enemigos) {
				if (r.nextBoolean()) {
					String[] dirs = {"Arriba", "Abajo", "Izquierda", "Derecha"};
					en.direccion = dirs[r.nextInt(4)];
				}
			}
		});
		timerIA.start();
	}

	/**
	 * Lógica de movimiento píxel a píxel para el enemigo.
	 * Detecta colisiones con muros usando un hitbox.
	 */
	private void actualizarMovimientoEnemigo(Enemigo en) {
		double nx = en.x, ny = en.y;

		if (en.direccion.equals("Arriba")) ny -= en.vel;
		else if (en.direccion.equals("Abajo")) ny += en.vel;
		else if (en.direccion.equals("Izquierda")) nx -= en.vel;
		else if (en.direccion.equals("Derecha")) nx += en.vel;

		// Hitbox del enemigo (un poco más grande que el de Luffy para dificultad)
		int m = 10, t = 50; 
		if (esLibre(nx+m, ny+m) && esLibre(nx+m+t, ny+m) && esLibre(nx+m, ny+m+t) && esLibre(nx+m+t, ny+m+t)) {
			en.x = nx;
			en.y = ny;
		} else {
			// Si choca contra un muro, elige otra dirección al azar inmediatamente
			String[] dirs = {"Arriba", "Abajo", "Izquierda", "Derecha"};
			en.direccion = dirs[new Random().nextInt(4)];
		}
	}

	private void cargarRecursos() {
		imgParedIrrompible = cargarImagen("/utils/suelopiedra2.png");
		imgPared = cargarImagen("/utils/suelopiedrasombra.png");
		imgSuelo = cargarImagen("/utils/arenasuelo.png");
		imgExplosion = cargarImagen("/utils/explosion.png");
		imgBomba = cargarImagen("/utils/bomba.png");
		imgEnemigo = cargarImagen("/utils/bot1.png");
		imgVida = cargarImagen("/utils/corazonlleno.png");
		imgVidaVacia = cargarImagen("/utils/corazonvacio.png");

		for (int i = 1; i <= 9; i++) {
			animAbajo[i - 1] = cargarImagen("/utils/Abajo/Abajo" + i + ".PNG");
			animArriba[i - 1] = cargarImagen("/utils/Arriba/Arriba" + i + ".PNG");
			animIzquierda[i - 1] = cargarImagen("/utils/Izquierda/Izquierda" + i + ".PNG");
		}
	}

	private Image cargarImagen(String ruta) {
		URL url = getClass().getResource(ruta);
		if (url == null) url = getClass().getResource(ruta.replace(".PNG", ".png"));
		return (url != null) ? new ImageIcon(url).getImage() : null;
	}

	private void actualizarMovimiento() {
		if (!moviendose) return;
		double nx = posX, ny = posY;

		if (direccionActual.equals("Arriba")) ny -= velocidad;
		else if (direccionActual.equals("Abajo")) ny += velocidad;
		else if (direccionActual.equals("Izquierda")) nx -= velocidad;
		else if (direccionActual.equals("Derecha")) nx += velocidad;

		if (nx < 0) nx = 0;
		if (nx > (17 * TAMANO_CELDA) - TAMANO_CELDA) nx = (17 * TAMANO_CELDA) - TAMANO_CELDA;
		if (ny < 0) ny = 0;
		if (ny > (11 * TAMANO_CELDA) - TAMANO_CELDA) ny = (11 * TAMANO_CELDA) - TAMANO_CELDA;

		int m = 14, t = 42; 
		if (esLibre(nx+m, ny+m) && esLibre(nx+m+t, ny+m) && esLibre(nx+m, ny+m+t) && esLibre(nx+m+t, ny+m+t)) {
			posX = nx;
			posY = ny;

			if (System.currentTimeMillis() - ultimoCambioFrame > 80) {
				frameActual = (frameActual + 1) % 9;
				ultimoCambioFrame = System.currentTimeMillis();
			}
		}
	}

	private boolean esLibre(double px, double py) {
		int cx = (int)(px/TAMANO_CELDA), cy = (int)(py/TAMANO_CELDA);
		return (cx>=0 && cx<17 && cy>=0 && cy<11) ? mapa[cy][cx]==0 : false;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 17; j++) {
				Image imgFondo = (mapa[i][j] == 2) ? imgParedIrrompible : (mapa[i][j] == 1 ? imgPared : imgSuelo);
				g.drawImage(imgFondo, j*70, i*70, 70, 70, this);
			}
		}

		if (bombaActiva && !explosion) g.drawImage(imgBomba, bombaX*70, bombaY*70, 70, 70, this);

		if (explosion) {
			int[][] ds = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
			for (int[] d : ds) {
				int ex = bombaX+d[0], ey = bombaY+d[1];
				if (ex>=0 && ex<17 && ey>=0 && ey<11) g.drawImage(imgExplosion, ex*70, ey*70, 70, 70, this);
			}
		}

		if (jugadorVida.estaVivo()) {
			if (!(invulnerable && System.currentTimeMillis() % 200 < 100))
				dibujarLuffy(g);
		}

		// Dibujar Enemigos con posición double (convertida a int para drawImage)
		for (Enemigo en : enemigos) {
			if (en.vivo) g.drawImage(imgEnemigo, (int)en.x, (int)en.y, 85, 85, this);
		}

		for (int i = 0; i < 3; i++) {
			Image imgV = (i < jugadorVida.getVidaBueno()) ? imgVida : imgVidaVacia;
			g.drawImage(imgV, 20 + (i * 40), 10, 70, 35, this);
		}

		if (!jugadorVida.estaVivo()) {
			jugando = false;
			g.setColor(new Color(0,0,0,200)); g.fillRect(0,0,getWidth(),getHeight());
			g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 70));
			g.drawString("SUSPENSO", getWidth()/2-200, getHeight()/2);
			g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 30));
			g.drawString("Presiona R para reiniciar", getWidth()/2-180, getHeight()/2 + 80);
		}
	}

	private void dibujarLuffy(Graphics g) {
		Image img;
		int ix = (int)posX, iy = (int)posY;
		switch(direccionActual) {
		case "Arriba": 
			img = moviendose ? animArriba[frameActual] : animArriba[0];
			g.drawImage(img, ix + 10, iy + 10, 50, 50, this);
			break;
		case "Izquierda":
			img = moviendose ? animIzquierda[frameActual] : animIzquierda[0];
			g.drawImage(img, ix + 10, iy + 10, 40, 50, this);
			break;
		case "Derecha":
			img = moviendose ? animIzquierda[frameActual] : animIzquierda[0];
			g.drawImage(img, ix + 10, iy + 10, ix+10+40, iy+10+50, img.getWidth(this), 0, 0, img.getHeight(this), this);
			break;
		default:
			img = moviendose ? animAbajo[frameActual] : animAbajo[0];
			g.drawImage(img, ix + 10, iy + 10, 50, 50, this);
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_R && !jugando) { reiniciarJuego(); return; }
		if (!jugando) return;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:    direccionActual = "Arriba"; moviendose = true; break;
		case KeyEvent.VK_W:     direccionActual = "Arriba"; moviendose = true; break;
		case KeyEvent.VK_DOWN:  direccionActual = "Abajo";  moviendose = true; break;
		case KeyEvent.VK_S:     direccionActual = "Abajo";  moviendose = true; break;
		case KeyEvent.VK_LEFT:  direccionActual = "Izquierda"; moviendose = true; break;
		case KeyEvent.VK_A:     direccionActual = "Izquierda"; moviendose = true; break;
		case KeyEvent.VK_RIGHT: direccionActual = "Derecha"; moviendose = true; break;
		case KeyEvent.VK_D:     direccionActual = "Derecha"; moviendose = true; break;
		case KeyEvent.VK_SPACE: 
			if (!bombaActiva) {
				bombaActiva = true;
				bombaX = (int) Math.round(posX / 70); bombaY = (int) Math.round(posY / 70);
				ejecutarBomba();
			}
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int k = e.getKeyCode();
		if ((k==KeyEvent.VK_UP && direccionActual.equals("Arriba")) || (k==KeyEvent.VK_DOWN && direccionActual.equals("Abajo")) ||
				(k==KeyEvent.VK_LEFT && direccionActual.equals("Izquierda")) || (k==KeyEvent.VK_RIGHT && direccionActual.equals("Derecha")))
			moviendose = false;
	}

	private void ejecutarBomba() {
		new Thread(() -> {
			Clip tictac = reproducirSonido("tictacsonido.wav");
			try { Thread.sleep(800); } catch (InterruptedException ex) {}
			if (tictac != null) { tictac.stop(); tictac.close(); }

			explosion = true; 
			reproducirSonido("sonidoexplosion.wav");
			verificarGolpeBomba(); 
			romperBloques(); 
			verificarGolpeEnemigosBomba(); 
			repaint();
			verificarVictoria(); 

			try { Thread.sleep(500); } catch (InterruptedException ex) {}
			explosion = false; bombaActiva = false;
			repaint();
		}).start();
	}

	private void verificarGolpeBomba() {
		if (invulnerable) return;
		int cx = (int)Math.round(posX/70), cy = (int)Math.round(posY/70);
		if (Math.abs(cx-bombaX)<=1 && cy==bombaY || Math.abs(cy-bombaY)<=1 && cx==bombaX) {
			jugadorVida.recibirDano(1); reproducirSonido("golpeenemigo.wav"); aplicarInvulnerabilidad();
		}
	}

	/**
	 * Verificación de golpe por contacto con enemigos.
	 * Ahora usa distancias en píxeles para mayor precisión.
	 */
	private void verificarGolpeEnemigos() {
		if (invulnerable) return;
		for (Enemigo en : enemigos) {
			if (en.vivo) {
				// Si la distancia entre centros es menor a 40 píxeles, hay golpe
				if (Math.abs(en.x - posX) < 40 && Math.abs(en.y - posY) < 40) {
					jugadorVida.recibirDano(1); 
					reproducirSonido("golpeenemigo.wav"); 
					aplicarInvulnerabilidad(); 
					break;
				}
			}
		}
	}

	private void aplicarInvulnerabilidad() {
		invulnerable = true;
		Timer t = new Timer(1500, e -> { invulnerable = false; repaint(); });
		t.setRepeats(false); t.start();
	}

	private void romperBloques() {
		int[][] ds = {{1,0},{-1,0},{0,1},{0,-1}};
		for(int[] d : ds) {
			int nx = bombaX+d[0], ny = bombaY+d[1];
			if(nx>=0 && nx<17 && ny>=0 && ny<11 && mapa[ny][nx] == 1) {
				mapa[ny][nx] = 0;
			}
		}
	}

	private void verificarGolpeEnemigosBomba() {
		for(Enemigo en : enemigos) {
			if(!en.vivo) continue;
			// Convertimos la posición de píxeles del enemigo a celda para ver si le da la bomba
			int ex = (int)Math.round(en.x / 70);
			int ey = (int)Math.round(en.y / 70);
			if(Math.abs(ex-bombaX)<=1 && ey==bombaY || Math.abs(ey-bombaY)<=1 && ex==bombaX) en.vivo = false;
		}
	}

	private void verificarVictoria() {
		boolean vivos = false;
		for(Enemigo e : enemigos) if(e.vivo) vivos = true;
		if(!vivos) {
			jugando = false;
			SwingUtilities.invokeLater(() -> {
				VideoJuego v = (VideoJuego) SwingUtilities.getWindowAncestor(this);
				PanelEscena2 e2 = new PanelEscena2(); v.setContentPane(e2); v.revalidate(); e2.requestFocusInWindow();
			});
		}
	}

	private void generarEnemigos() {
		Random r = new Random(); enemigos.clear();
		while(enemigos.size()<1) {
			int ex=r.nextInt(17), ey=r.nextInt(11);
			if(mapa[ey][ex]==0 && (ex!=1 || ey!=1)) enemigos.add(new Enemigo(ex, ey));
		}
	}

	private Clip reproducirSonido(String n) {
		try {
			URL u = getClass().getResource("/utils/" + n);
			if (u == null) return null;
			AudioInputStream a = AudioSystem.getAudioInputStream(u);
			Clip c = AudioSystem.getClip(); c.open(a);
			if (c.isControlSupported(FloatControl.Type.MASTER_GAIN)) 
				((FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-15.0f);
			c.start(); return c;
		} catch (Exception e) { return null; }
	}

	private void reiniciarJuego() { 
		mapa = new int[11][17];
		Random rand = new Random();
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 17; j++) {
				if (i == 0 || i == 10 || j == 0 || j == 16) mapa[i][j] = 2;
				else mapa[i][j] = (rand.nextFloat() < 0.3) ? 1 : 0;
			}
		}
		mapa[1][1] = 0;
		posX = 70; posY = 70; jugadorVida = new Personaje(3,1); invulnerable = false;
		generarEnemigos(); jugando = true; moviendose = false; direccionActual = "Abajo"; repaint(); 
	}

	@Override public void keyTyped(KeyEvent e) {}
	@Override public void addNotify() { super.addNotify(); requestFocusInWindow(); }
}