package vista;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import java.net.URL;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.SwingUtilities; 
import modelo.Personaje;

class Enemigo4 {
	double x, y;
	double vel = 2.5; 
	String direccion = "Abajo";
	boolean vivo = true;
	int vida = 6; 
	boolean herido = false; 
	int frameActual = 0;
	long ultimoCambioFrame = 0;
	boolean bombaActiva = false;
	int bX, bY;
	boolean explosionActiva = false;
	private JPanel panel;

	public Enemigo4(int gridX, int gridY, JPanel panel) { 
		this.x = gridX * 70; 
		this.y = gridY * 70; 
		this.panel = panel;
	}

	public void recibirDano() {
		if (!herido && vivo) {
			vida--;
			herido = true;
			if (vida <= 0) vivo = false;
			if(panel != null) panel.repaint();
			Timer t = new Timer(800, e -> {
				herido = false;
				if(panel != null) panel.repaint();
			});
			t.setRepeats(false);
			t.start();
		}
	}
}

public class PanelEscena4 extends JPanel implements KeyListener {

	private Personaje jugadorVida = new Personaje(3, 1);
	private Image imgParedIrrompible, imgSuelo, imgExplosion, imgBomba, imgVida, imgVidaVacia;
	private Image[] animArriba = new Image[9], animAbajo = new Image[9], animIzquierda = new Image[9];
	private Image[] calaArriba = new Image[9], calaAbajo = new Image[9], calaIzquierda = new Image[9];

	private int frameActualLuffy = 0; 
	private long ultimoCambioFrameLuffy = 0;
	private int[][] mapa; 

	private double posX = 70, posY = 70; 
	private double velocidad = 7.0; 
	private String direccionActual = "Abajo";
	private boolean moviendose = false;

	private boolean teclaArriba, teclaAbajo, teclaIzquierda, teclaDerecha;

	private Enemigo4 rival; 
	private boolean bombaActiva = false, explosion = false;
	private int bombaX, bombaY; 
	private boolean jugando = true, invulnerable = false;
	private int reintentosRestantes = 1; 

	public PanelEscena4() {
		mapa = new int[11][17]; 
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 17; j++) {
				if (i == 0 || i == 10 || j == 0 || j == 16) mapa[i][j] = 2; 
				else mapa[i][j] = 0; 
			}
		}

		cargarRecursos(); 
		rival = new Enemigo4(15, 9, this); 

		setFocusable(true);
		addKeyListener(this);

		Timer gameLoop = new Timer(16, e -> {
			if (jugando) {
				actualizarMovimiento(); // Actualiza el movimiento del jugador basado en las teclas presionadas
				if (rival.vivo) { // Solo actualiza al enemigo si sigue vivo
					actualizarMovimientoEnemigo(rival); // Movimiento básico hacia el jugador
					decidirAccionEnemigo(); // Decide si el enemigo suelta una bomba
				}
				verificarGolpeEnemigos(); // Verifica si el enemigo ha golpeado al jugador
				repaint(); // Redibuja el panel para reflejar los cambios
			}
		});
		gameLoop.start();
	}

	/**
	 * MÉTODO CLAVE: Detiene absolutamente todos los sonidos que estén sonando en el sistema.
	 * Esto evita que la música del panel anterior se mezcle con la del nuevo.
	 */
	private void detenerTodoElAudio() {
		try {
			for (Mixer.Info info : AudioSystem.getMixerInfo()) { // Recorremos todos los mixers disponibles
				Mixer mixer = AudioSystem.getMixer(info); // Obtenemos el mixer
				for (Line line : mixer.getSourceLines()) { // Recorremos todas las líneas de audio que el mixer puede reproducir
					if (line instanceof Clip) { // Si la línea es un Clip (un tipo común de línea que reproduce sonidos cortos)
						Clip clip = (Clip) line; // Intentamos detener y cerrar el clip
						if (clip.isRunning()) clip.stop(); // Detenemos el clip si está sonando
						clip.close(); // Cerramos el clip para liberar recursos
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void reiniciarNivel() {
		jugadorVida = new Personaje(3, 1);
		posX = 70; posY = 70;
		teclaArriba = teclaAbajo = teclaIzquierda = teclaDerecha = false;
		direccionActual = "Abajo";
		moviendose = false;
		bombaActiva = false;
		explosion = false;
		invulnerable = false;
		rival = new Enemigo4(15, 9, this);
		jugando = true;
		repaint();
	}

	private void volverAlInicio() {
		// Limpiamos cualquier sonido antes de crear el nuevo panel
		detenerTodoElAudio();

		//Cambiamos al PanelInicio en el hilo de eventos para evitar problemas de concurrencia
		SwingUtilities.invokeLater(() -> {
			Window ventana = SwingUtilities.getWindowAncestor(this);
			if (ventana instanceof JFrame) {
				JFrame frame = (JFrame) ventana;
				frame.getContentPane().removeAll();

				//Al instanciarse, PanelInicio llamará a su música y será la única sonando
				PanelInicio menuPrincipal = new PanelInicio();

				frame.setContentPane(menuPrincipal);
				frame.setResizable(true);
				frame.pack(); 
				frame.setLocationRelativeTo(null);
				frame.setResizable(false); 
				frame.revalidate();
				frame.repaint();

				menuPrincipal.requestFocusInWindow();
			}
		});
	}

	
	//Manejo de teclas para controlar el movimiento del jugador y otras acciones
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_R && !jugando) {
			if (!jugadorVida.estaVivo() && reintentosRestantes > 0) {
				reintentosRestantes--;
				reiniciarNivel();
			} else {
				volverAlInicio();
			}
			return;
		}

		if (!jugando) return;

		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP: case KeyEvent.VK_W:    teclaArriba = true; break;
		case KeyEvent.VK_DOWN: case KeyEvent.VK_S:  teclaAbajo = true; break;
		case KeyEvent.VK_LEFT: case KeyEvent.VK_A:  teclaIzquierda = true; break;
		case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: teclaDerecha = true; break;
		case KeyEvent.VK_SPACE: 
			if (!bombaActiva) {
				bombaActiva = true;
				bombaX = (int) Math.round(posX / 70); bombaY = (int) Math.round(posY / 70);
				ejecutarBomba();
			}
			break;
		}
	}

	
	//Al soltar las teclas, actualizamos el estado para que el personaje deje de moverse en esa dirección
	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP: case KeyEvent.VK_W:    teclaArriba = false; break;
		case KeyEvent.VK_DOWN: case KeyEvent.VK_S:  teclaAbajo = false; break;
		case KeyEvent.VK_LEFT: case KeyEvent.VK_A:  teclaIzquierda = false; break;
		case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: teclaDerecha = false; break;
		}
	}

	
	//Este método actualiza la posición del jugador basado en las teclas presionadas y verifica colisiones con el mapa
	private void actualizarMovimiento() {
		double nx = posX, ny = posY;
		boolean huboMovimiento = false;

		//Calculamos la nueva posición basada en las teclas presionadas
		if (teclaArriba) { ny -= velocidad; direccionActual = "Arriba"; huboMovimiento = true; }
		else if (teclaAbajo) { ny += velocidad; direccionActual = "Abajo"; huboMovimiento = true; }

		
		//Si el jugador se mueve horizontalmente, actualizamos la dirección y marcamos que hubo movimiento
		if (teclaIzquierda) { nx -= velocidad; direccionActual = "Izquierda"; huboMovimiento = true; }
		else if (teclaDerecha) { nx += velocidad; direccionActual = "Derecha"; huboMovimiento = true; }

		moviendose = huboMovimiento;

		
		//Verificamos que la nueva posición no colisione con paredes irrompibles usando un margen para el personaje
		int m = 14, t = 42; 
		if (esLibre(nx+m, ny+m) && esLibre(nx+m+t, ny+m) && esLibre(nx+m, ny+m+t) && esLibre(nx+m+t, ny+m+t)) {
			posX = nx; posY = ny;
			if (moviendose && System.currentTimeMillis() - ultimoCambioFrameLuffy > 80) {
				frameActualLuffy = (frameActualLuffy + 1) % 9;
				ultimoCambioFrameLuffy = System.currentTimeMillis();
			}
		}
	}

	
	//Este método se encarga de dibujar todo el escenario, personajes, bombas, explosiones y HUD (vida) en el panel
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 17; j++) {
				g.drawImage((mapa[i][j] == 2) ? imgParedIrrompible : imgSuelo, j*70, i*70, 70, 70, this);
			}
		}

		if (bombaActiva && !explosion) g.drawImage(imgBomba, bombaX*70, bombaY*70, 70, 70, this);
		if (explosion) dibujarExplosionCruz(g, bombaX, bombaY); 

		if (rival.bombaActiva && !rival.explosionActiva) g.drawImage(imgBomba, rival.bX*70, rival.bY*70, 70, 70, this);
		if (rival.explosionActiva) dibujarExplosionArea(g, rival.bX, rival.bY);

		if (jugadorVida.estaVivo() && !(invulnerable && System.currentTimeMillis() % 200 < 100)) dibujarLuffy(g);
		if (rival.vivo) dibujarRival(g);

		for (int i = 0; i < 3; i++) {
			g.drawImage((i < jugadorVida.getVidaBueno()) ? imgVida : imgVidaVacia, 20 + (i * 40), 10, 70, 35, this);
		}

		if (rival.vivo) {
			g.setColor(Color.BLACK); g.fillRect(getWidth() - 250, 15, 200, 20);
			g.setColor(Color.RED); g.fillRect(getWidth() - 250, 15, (int)(rival.vida * (200.0/6.0)), 20);
			g.setColor(Color.WHITE); g.drawRect(getWidth() - 250, 15, 200, 20);
			g.drawString("BOSS CALABAZA", getWidth() - 250, 12);
		}

		if (!jugadorVida.estaVivo()) {
			jugando = false;
			g.setColor(new Color(0,0,0,200)); g.fillRect(0,0,getWidth(),getHeight());
			g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 70));
			g.drawString("HAS PERDIDO", getWidth()/2-230, getHeight()/2);
			g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 25));
			if (reintentosRestantes > 0) g.drawString("Presiona 'R' para Reintentar (Queda " + reintentosRestantes + ")", getWidth()/2-230, getHeight()/2 + 80);
			else g.drawString("Presiona 'R' para volver al Inicio", getWidth()/2-180, getHeight()/2 + 80);
		} else if (!rival.vivo) {
			jugando = false;
			g.setColor(new Color(0,0,0,200)); g.fillRect(0,0,getWidth(),getHeight());
			g.setColor(Color.GREEN); g.setFont(new Font("Arial", Font.BOLD, 70));
			g.drawString("¡VICTORIA!", getWidth()/2-180, getHeight()/2);
			g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 25));
			g.drawString("Presiona 'R' para volver al Inicio", getWidth()/2-180, getHeight()/2 + 80);
		}
	}

	
	//Este método decide si el enemigo suelta una bomba basado en la proximidad al jugador y una probabilidad aleatoria
	private void decidirAccionEnemigo() {
		Random r = new Random();
		if (!rival.bombaActiva && rival.vivo) {
			int rx = (int)Math.round(rival.x / 70);
			int ry = (int)Math.round(rival.y / 70);
			int cx = (int)Math.round(posX / 70);
			int cy = (int)Math.round(posY / 70);

			if (Math.abs(rx - cx) <= 4 && Math.abs(ry - cy) <= 4) {
				if (r.nextInt(100) < 15) soltarBombaEnemigo(rx, ry);
			}
		}
	}

	
	//Este método maneja la lógica de la bomba del enemigo, incluyendo el temporizador, la explosión y el daño al jugador
	private void soltarBombaEnemigo(int bx, int by) {
		rival.bombaActiva = true; rival.bX = bx; rival.bY = by;
		new Thread(() -> {
			Clip tictacRival = reproducirSonido("tictacsonido.wav");
			try { Thread.sleep(800); } catch (Exception e) {}
			if (tictacRival != null) {
				tictacRival.stop();
				tictacRival.close();
			}
			rival.explosionActiva = true;
			reproducirSonido("sonidoexplosion.wav");
			verificarDanoBombaRival();
			repaint();
			try { Thread.sleep(800); } catch (Exception e) {} 
			rival.explosionActiva = false; rival.bombaActiva = false;
		}).start();
	}

	private void actualizarMovimientoEnemigo(Enemigo4 en) {
		double dx = posX - en.x; double dy = posY - en.y;
		if (Math.abs(dx) > Math.abs(dy)) en.direccion = (dx > 0) ? "Derecha" : "Izquierda";
		else en.direccion = (dy > 0) ? "Abajo" : "Arriba";
		double nx = en.x, ny = en.y;
		if (en.direccion.equals("Arriba")) ny -= en.vel;
		else if (en.direccion.equals("Abajo")) ny += en.vel;
		else if (en.direccion.equals("Izquierda")) nx -= en.vel;
		else if (en.direccion.equals("Derecha")) nx += en.vel;
		int m = 10, t = 50; 
		if (esLibre(nx+m, ny+m) && esLibre(nx+m+t, ny+m) && esLibre(nx+m, ny+m+t) && esLibre(nx+m+t, ny+m+t)) {
			en.x = nx; en.y = ny;
		}
	}

	private boolean esLibre(double px, double py) {
		int cx = (int)(px/70), cy = (int)(py/70);
		return (cx>=0 && cx<17 && cy>=0 && cy<11) ? mapa[cy][cx]==0 : false;
	}

	private void ejecutarBomba() {
		new Thread(() -> {
			Clip sonidoTictac = reproducirSonido("tictacsonido.wav");
			try { Thread.sleep(800); } catch (Exception e) {} 
			if (sonidoTictac != null) {
				sonidoTictac.stop();
				sonidoTictac.close();
			}
			explosion = true;
			reproducirSonido("sonidoexplosion.wav");
			verificarDanoBombaJugador();
			repaint();
			try { Thread.sleep(500); } catch (Exception e) {} 
			explosion = false; 
			bombaActiva = false;
		}).start();
	}

	private void verificarDanoBombaRival() {
		int cx = (int)Math.round(posX/70);
		int cy = (int)Math.round(posY/70);
		if (Math.abs(cx - rival.bX) <= 2 && Math.abs(cy - rival.bY) <= 2 && !invulnerable) {
			jugadorVida.recibirDano(1); 
			reproducirSonido("golpeenemigo.wav"); 
			aplicarInvulnerabilidad(); 
		}
	}

	private void verificarGolpeEnemigos() {
		if (invulnerable || !rival.vivo) return;
		if (Math.abs(rival.x - posX) < 80 && Math.abs(rival.y - posY) < 80) {
			jugadorVida.recibirDano(1); 
			reproducirSonido("golpeenemigo.wav"); 
			aplicarInvulnerabilidad();
		}
	}

	private void aplicarInvulnerabilidad() {
		invulnerable = true;
		Timer t = new Timer(1500, e -> { invulnerable = false; repaint(); });
		t.setRepeats(false); t.start();
	}

	private void dibujarLuffy(Graphics g) {
		Image img; int ix = (int)posX, iy = (int)posY;
		switch(direccionActual) {
		case "Arriba": img = moviendose ? animArriba[frameActualLuffy] : animArriba[0]; break;
		case "Izquierda": img = moviendose ? animIzquierda[frameActualLuffy] : animIzquierda[0]; break;
		case "Derecha": 
			img = moviendose ? animIzquierda[frameActualLuffy] : animIzquierda[0];
			g.drawImage(img, ix+10+50, iy+10, -50, 50, this); return;
		default: img = moviendose ? animAbajo[frameActualLuffy] : animAbajo[0]; break;
		}
		g.drawImage(img, ix + 10, iy + 10, 50, 50, this);
	}

	private void dibujarRival(Graphics g) {
		if (System.currentTimeMillis() - rival.ultimoCambioFrame > 100) {
			rival.frameActual = (rival.frameActual + 1) % 9;
			rival.ultimoCambioFrame = System.currentTimeMillis();
		}
		if (rival.herido && (System.currentTimeMillis() % 200 < 100)) return; 
		
		Image img; 
		int bossSize = 130;
		int offset = -30;
		
		switch(rival.direccion) {
		case "Arriba": img = calaArriba[rival.frameActual]; break;
		case "Izquierda": img = calaIzquierda[rival.frameActual]; break;
		case "Derecha": 
			img = calaIzquierda[rival.frameActual]; 
			g.drawImage(img, (int)rival.x + bossSize + offset, (int)rival.y + offset, -bossSize, bossSize, this); 
			return;
		default: img = calaAbajo[rival.frameActual]; break;
		}
		g.drawImage(img, (int)rival.x + offset, (int)rival.y + offset, bossSize, bossSize, this);
	}

	private void dibujarExplosionCruz(Graphics g, int bx, int by) {
		int[][] ds = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
		for (int[] d : ds) {
			int ex = bx + d[0], ey = by + d[1];
			if (ex >= 0 && ex < 17 && ey >= 0 && ey < 11 && mapa[ey][ex] != 2) g.drawImage(imgExplosion, ex * 70, ey * 70, 70, 70, this);
		}
	}

	private void dibujarExplosionArea(Graphics g, int bx, int by) {
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				int ex = bx + i, ey = by + j;
				if (ex >= 0 && ex < 17 && ey >= 0 && ey < 11 && mapa[ey][ex] != 2) {
					g.drawImage(imgExplosion, ex * 70, ey * 70, 70, 70, this);
				}
			}
		}
	}

	private void cargarRecursos() {
		imgParedIrrompible = cargarImagen("/utils/suelopiedra2.png");
		imgSuelo = cargarImagen("/utils/suelopiedra7.png");
		imgExplosion = cargarImagen("/utils/explosion.png");
		imgBomba = cargarImagen("/utils/bomba.png");
		imgVida = cargarImagen("/utils/corazonlleno.png");
		imgVidaVacia = cargarImagen("/utils/corazonvacio.png");
		for (int i = 1; i <= 9; i++) {
			animAbajo[i-1] = cargarImagen("/utils/Abajo/Abajo" + i + ".PNG");
			animArriba[i-1] = cargarImagen("/utils/Arriba/Arriba" + i + ".PNG");
			animIzquierda[i-1] = cargarImagen("/utils/Izquierda/Izquierda" + i + ".PNG");
			calaAbajo[i-1] = cargarImagen("/utils/abajo_calabaza/abajo" + i + ".PNG");
			calaArriba[i-1] = cargarImagen("/utils/arriba_calabaza/arriba" + i + ".PNG");
			calaIzquierda[i-1] = cargarImagen("/utils/izq_calabaza/izq" + i + ".PNG");
		}
	}

	private Image cargarImagen(String r) {
		URL u = getClass().getResource(r);
		return (u != null) ? new ImageIcon(u).getImage() : null;
	}

	private void verificarDanoBombaJugador() {
		int rx = (int)Math.round(rival.x/70), ry = (int)Math.round(rival.y/70); 
		if ((rx == bombaX && Math.abs(ry - bombaY) <= 1) || (ry == bombaY && Math.abs(rx - bombaX) <= 1)) {
			rival.recibirDano(); 
			reproducirSonido("golpeenemigo.wav");
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

	@Override public void keyTyped(KeyEvent e) {}
	@Override public void addNotify() { super.addNotify(); requestFocusInWindow(); }
}