package vista;

import javax.swing.JButton;
import java.awt.*;

public class BotonesRetro extends JButton {

	public BotonesRetro(String texto) {
		super(texto);
		setContentAreaFilled(false); // Quita el fondo por defecto
		setFocusPainted(false);      // Quita el borde de enfoque
		setBorderPainted(false);     // Quita el borde original
		setForeground(Color.BLACK);  // Color del texto

		// Usamos Monospaced que viene en Java y parece de consola
		setFont(new Font("Monospaced", Font.BOLD, 14)); 
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		// Suavizado de bordes para que no se vea "borroso"
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		// 1. Dibujar sombra (desplazada 2px)
		g2.setColor(new Color(0, 0, 0, 100)); // Sombra semitransparente
		g2.fillRect(4, 4, getWidth() - 4, getHeight() - 4); // Sombra del botón

		// 2. Dibujar fondo del botón
		if (getModel().isPressed()) {
			g2.setColor(new Color(200, 200, 200)); // Color cuando se pulsa
		} else {
			g2.setColor(Color.GRAY); // Color normal
		}
		g2.fillRect(0, 0, getWidth() - 4, getHeight() - 4); // Fondo del botón

		// 3. Dibujar borde "Pixel" grueso
		g2.setColor(Color.BLACK); // Color del borde
		g2.setStroke(new BasicStroke(3)); // Grosor del borde
		g2.drawRect(1, 1, getWidth() - 6, getHeight() - 6); // Borde grueso

		super.paintComponent(g); 
	}


}
