package vista;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class PanelEscena1 extends JPanel {

	private static final long serialVersionUID = 1L;
	private Image imagen;
	
	public PanelEscena1() {

        // Imagen del fondo del juego
		
		
        imagen = new ImageIcon(getClass().getResource("/utils/escenario.png")).getImage();
    }

	
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
    }
}
	
