package vista;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class VentanaJuegoInicio extends JFrame {


		public VentanaJuegoInicio () {
			setTitle("Juego");
			setSize(500, 400);
			setLocationRelativeTo(null);
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	        JLabel label = new JLabel("¡El juego ha comenzado!");
	        label.setFont(new Font("Arial", Font.BOLD, 20));
	        label.setHorizontalAlignment(JLabel.CENTER);

	        add(label);
		}
	}
