package vista;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class VideoJuego extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	
	public VideoJuego() {
		setDefaultCloseOperation(VideoJuego.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 800);
		setResizable(false);
		setLocationRelativeTo(null);
		contentPane = new PanelInicio();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

	}



}
