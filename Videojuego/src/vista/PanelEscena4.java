package vista;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import modelo.Personaje;

// --- CLASE DEL RIVAL ---
class Enemigo4 {
    double x, y;
    double vel = 3; // Misma velocidad que los enemigos de la E3
   
    String direccion = "Abajo";
    boolean vivo = true;
    int frameActual = 0;
    long ultimoCambioFrame = 0;
    
    // Atributos de bomba del rival
    boolean bombaActiva = false;
    int bX, bY;
    boolean explosionActiva = false;

    public Enemigo4(int gridX, int gridY) { 
        this.x = gridX * 70; 
        this.y = gridY * 70; 
    }
}

public class PanelEscena4 extends JPanel implements KeyListener {

    private Personaje jugadorVida = new Personaje(3, 1);
    private Image imgParedIrrompible, imgSuelo, imgPared, imgExplosion, imgBomba, imgVida, imgVidaVacia;
    private Image[] animArriba = new Image[9], animAbajo = new Image[9], animIzquierda = new Image[9];
    private Image[] calaArriba = new Image[9], calaAbajo = new Image[9], calaIzquierda = new Image[9];

    private int frameActualLuffy = 0; // Sincronizado con E3
    private long ultimoCambioFrameLuffy = 0;
    private final int TAMANO_CELDA = 70;
    private int[][] mapa; 

    private double posX = 70, posY = 70; 
    private double velocidad = 7.0; // Misma velocidad que E3
    private String direccionActual = "Abajo";
    private boolean moviendose = false;

    private Enemigo4 rival; 
    private boolean bombaActiva = false, explosion = false;
    private int bombaX, bombaY; 
    private boolean jugando = true, invulnerable = false;

    public PanelEscena4() {
        mapa = new int[11][17]; 
        Random rand = new Random();
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 17; j++) {
                if (i == 0 || i == 10 || j == 0 || j == 16) mapa[i][j] = 2;
                else mapa[i][j] = (rand.nextFloat() < 0.2) ? 1 : 0;
            }
        }
        mapa[1][1] = 0; 
        cargarRecursos(); 
        rival = new Enemigo4(15, 9); 

        setFocusable(true);
        addKeyListener(this);

        // --- MOTOR DEL JUEGO (Igual a Escena 3) ---
        Timer gameLoop = new Timer(16, e -> {
            if (jugando) {
                actualizarMovimiento(); 
                if (rival.vivo) {
                    actualizarMovimientoEnemigo(rival);
                    decidirAccionEnemigo();
                }
                verificarGolpeEnemigos();
                repaint(); 
            }
        });
        gameLoop.start();
    }

    private void decidirAccionEnemigo() {
        Random r = new Random();
        
        // Movimiento aleatorio del enemigo (igual a E3)
        if (!rival.bombaActiva && r.nextInt(100) < 2) {
            int rx = (int)Math.round(rival.x / 70);
            int ry = (int)Math.round(rival.y / 70);
            
            // El enemigo solo suelta bomba si el jugador está cerca (distancia en celdas <= 2)
            if (rx >= 0 && rx < 17 && ry >= 0 && ry < 11 && mapa[ry][rx] == 0) {
                soltarBombaEnemigo(rx, ry);
            }
        }
    }

    private void soltarBombaEnemigo(int bx, int by) {
        rival.bombaActiva = true;
        rival.bX = bx; rival.bY = by;
        new Thread(() -> {
            reproducirSonido("tictacsonido.wav");
            try { Thread.sleep(600); } catch (Exception e) {} // Tiempo igual a E3
            rival.explosionActiva = true;
            reproducirSonido("sonidoexplosion.wav");
            verificarDanoBombaRival();
            repaint();
            try { Thread.sleep(400); } catch (Exception e) {}
            rival.explosionActiva = false;
            rival.bombaActiva = false;
        }).start();
    }

    private void actualizarMovimiento() {
        if (!moviendose) return;
        double nx = posX, ny = posY;
        if (direccionActual.equals("Arriba")) ny -= velocidad;
        else if (direccionActual.equals("Abajo")) ny += velocidad;
        else if (direccionActual.equals("Izquierda")) nx -= velocidad;
        else if (direccionActual.equals("Derecha")) nx += velocidad;

        // Hitbox exacto de la Escena 3 (m=14, t=42)
        int m = 14, t = 42; 
        if (esLibre(nx+m, ny+m) && esLibre(nx+m+t, ny+m) && esLibre(nx+m, ny+m+t) && esLibre(nx+m+t, ny+m+t)) {
            posX = nx; posY = ny;
            if (System.currentTimeMillis() - ultimoCambioFrameLuffy > 80) {
                frameActualLuffy = (frameActualLuffy + 1) % 9;
                ultimoCambioFrameLuffy = System.currentTimeMillis();
            }
        }
    }

    private void actualizarMovimientoEnemigo(Enemigo4 en) {
        double nx = en.x, ny = en.y;
        if (en.direccion.equals("Arriba")) ny -= en.vel;
        else if (en.direccion.equals("Abajo")) ny += en.vel;
        else if (en.direccion.equals("Izquierda")) nx -= en.vel;
        else if (en.direccion.equals("Derecha")) nx += en.vel;

        // Hitbox de enemigo exacto de la Escena 3 (m=10, t=50)
        int m = 10, t = 50; 
        if (esLibre(nx+m, ny+m) && esLibre(nx+m+t, ny+m) && esLibre(nx+m, ny+m+t) && esLibre(nx+m+t, ny+m+t)) {
            en.x = nx; en.y = ny;
        } else {
            String[] ds = {"Arriba", "Abajo", "Izquierda", "Derecha"};
            en.direccion = ds[new Random().nextInt(4)];
        }
    }

    private boolean esLibre(double px, double py) {
        int cx = (int)(px/70), cy = (int)(py/70);
        return (cx>=0 && cx<17 && cy>=0 && cy<11) ? mapa[cy][cx]==0 : false;
    }

    private void ejecutarBomba() {
        new Thread(() -> {
            reproducirSonido("tictacsonido.wav");
            try { Thread.sleep(800); } catch (Exception e) {} // Tiempo igual a E3
            explosion = true;
            reproducirSonido("sonidoexplosion.wav");
            verificarDanoBombaJugador();
            repaint();
            try { Thread.sleep(500); } catch (Exception e) {}
            explosion = false; bombaActiva = false;
            verificarVictoria();
            repaint();
        }).start();
    }

    private void verificarDanoBombaRival() {
        // Daño al jugador (Igual a E3)
        int cx = (int)Math.round(posX/70), cy = (int)Math.round(posY/70);
        if (Math.abs(cx - rival.bX) <= 1 && cy == rival.bY || Math.abs(cy - rival.bY) <= 1 && cx == rival.bX) {
            if(!invulnerable) { 
                jugadorVida.recibirDano(1); 
                reproducirSonido("golpeenemigo.wav");
                aplicarInvulnerabilidad(); 
            }
        }
        romperBloques(rival.bX, rival.bY);
    }

    private void romperBloques(int bx, int by) {
        int[][] ds = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
        for(int[] d : ds) {
            int nx = bx+d[0], ny = by+d[1];
            if(nx>=0 && nx<17 && ny>=0 && ny<11 && mapa[ny][nx] == 1) mapa[ny][nx] = 0;
        }
    }

    private void verificarGolpeEnemigos() {
        if (invulnerable || !rival.vivo) return;
        // Distancia en píxeles igual a E3 (< 40)
        if (Math.abs(rival.x - posX) < 50 && Math.abs(rival.y - posY) < 50) {
            jugadorVida.recibirDano(1); 
            reproducirSonido("golpeenemigo.wav");
            aplicarInvulnerabilidad();
        }
    }

    private Clip reproducirSonido(String n) {
        try {
            URL u = getClass().getResource("/utils/" + n);
            if (u == null) return null;
            AudioInputStream a = AudioSystem.getAudioInputStream(u);
            Clip c = AudioSystem.getClip(); c.open(a);
            // Volumen unificado a -15.0f como en Escena 3
            if (c.isControlSupported(FloatControl.Type.MASTER_GAIN)) 
                ((FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-15.0f);
            c.start(); return c;
        } catch (Exception e) { return null; }
    }

    private void aplicarInvulnerabilidad() {
        invulnerable = true;
        Timer t = new Timer(1500, e -> { invulnerable = false; repaint(); });
        t.setRepeats(false); t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 17; j++) {
                Image f = (mapa[i][j] == 2) ? imgParedIrrompible : (mapa[i][j] == 1 ? imgPared : imgSuelo);
                g.drawImage(f, j*70, i*70, 70, 70, this);
            }
        }

        if (bombaActiva && !explosion) g.drawImage(imgBomba, bombaX*70, bombaY*70, 70, 70, this);
        if (explosion) dibujarExplosion(g, bombaX, bombaY);
        if (rival.bombaActiva && !rival.explosionActiva) g.drawImage(imgBomba, rival.bX*70, rival.bY*70, 70, 70, this);
        if (rival.explosionActiva) dibujarExplosion(g, rival.bX, rival.bY);

        if (jugadorVida.estaVivo() && !(invulnerable && System.currentTimeMillis() % 200 < 100)) dibujarLuffy(g);
        if (rival.vivo) dibujarRival(g);

        for (int i = 0; i < 3; i++) {
            g.drawImage((i < jugadorVida.getVidaBueno()) ? imgVida : imgVidaVacia, 20 + (i * 40), 10, 70, 35, this);
        }

        if (!jugadorVida.estaVivo()) {
            g.setColor(new Color(0,0,0,200)); g.fillRect(0,0,getWidth(),getHeight());
            g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 70));
            g.drawString("SUSPENSO", getWidth()/2-200, getHeight()/2);
        }
    }

    private void dibujarLuffy(Graphics g) {
        Image img; int ix = (int)posX, iy = (int)posY;
        switch(direccionActual) {
            case "Arriba": img = moviendose ? animArriba[frameActualLuffy] : animArriba[0]; break;
            case "Izquierda": img = moviendose ? animIzquierda[frameActualLuffy] : animIzquierda[0]; break;
            case "Derecha": 
                img = moviendose ? animIzquierda[frameActualLuffy] : animIzquierda[0];
                g.drawImage(img, ix+10, iy+10, ix+10+50, iy+10+50, img.getWidth(this), 0, 0, img.getHeight(this), this);
                return;
            default: img = moviendose ? animAbajo[frameActualLuffy] : animAbajo[0]; break;
        }
        g.drawImage(img, ix + 10, iy + 10, 50, 50, this);
    }

    private void dibujarRival(Graphics g) {
        if (System.currentTimeMillis() - rival.ultimoCambioFrame > 100) {
            rival.frameActual = (rival.frameActual + 1) % 9;
            rival.ultimoCambioFrame = System.currentTimeMillis();
        }
        Image img;
        
        int bossSize = 80; // Tamaño del rival (80x80)
        int offset = -5; // Ajuste para centrar la imagen del rival
        
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

    private void dibujarExplosion(Graphics g, int bx, int by) {
        int[][] ds = {{0,0},{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : ds) {
            int ex = bx+d[0], ey = by+d[1];
            if (ex>=0 && ex<17 && ey>=0 && ey<11) g.drawImage(imgExplosion, ex*70, ey*70, 70, 70, this);
        }
    }

    private void cargarRecursos() {
        imgParedIrrompible = cargarImagen("/utils/suelopiedra2.png");
        imgPared = cargarImagen("/utils/glowstone.jpg");
        imgSuelo = cargarImagen("/utils/suelopiedra3.png");
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
        if (Math.abs(rx - bombaX) <= 1 && ry == bombaY || Math.abs(ry - bombaY) <= 1 && rx == bombaX) {
            rival.vivo = false;
        }
        romperBloques(bombaX, bombaY);
    }

    private void verificarVictoria() {
        if (!rival.vivo) {
            jugando = false;
            // Aquí puedes llamar a tu siguiente escena o ventana de victoria
        }
    }

    @Override public void keyPressed(KeyEvent e) {
        if (!jugando) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: 	direccionActual="Arriba"; moviendose=true; break;
            case KeyEvent.VK_W: 	direccionActual="Arriba"; moviendose=true; break;
            case KeyEvent.VK_DOWN: 	direccionActual="Abajo"; moviendose=true; break;
            case KeyEvent.VK_S: 	direccionActual="Abajo"; moviendose=true; break;
            case KeyEvent.VK_LEFT: 	direccionActual="Izquierda"; moviendose=true; break;
            case KeyEvent.VK_A: 	direccionActual="Izquierda"; moviendose=true; break;
            case KeyEvent.VK_RIGHT: direccionActual="Derecha"; moviendose=true; break;
            case KeyEvent.VK_D: 	direccionActual="Derecha"; moviendose=true; break;
            case KeyEvent.VK_SPACE: if(!bombaActiva){bombaActiva=true; bombaX=(int)Math.round(posX/70); bombaY=(int)Math.round(posY/70); ejecutarBomba();} break;
        }
    }
    @Override public void keyReleased(KeyEvent e) { 
        int k = e.getKeyCode();
        if ((k==KeyEvent.VK_UP && direccionActual.equals("Arriba")) || (k==KeyEvent.VK_DOWN && direccionActual.equals("Abajo")) ||
            (k==KeyEvent.VK_LEFT && direccionActual.equals("Izquierda")) || (k==KeyEvent.VK_RIGHT && direccionActual.equals("Derecha")))
            moviendose = false; 
    }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void addNotify() { super.addNotify(); requestFocusInWindow(); }
}