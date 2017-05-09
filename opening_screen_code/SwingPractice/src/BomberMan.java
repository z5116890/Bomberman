import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import static java.awt.Image.SCALE_SMOOTH;

//sound
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class BomberMan extends JFrame {

	private JFrame frmBomberman;
	private JTextField txtName;
	private final ButtonGroup radios = new ButtonGroup();
	JLabel lblRadio = new JLabel("radio1");

	public BomberMan(){
		initialize();
		this.setLocationRelativeTo(null);
	}

	/**
	 * Launch the application
	 * @param args
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BomberMan window = new BomberMan();
					window.frmBomberman.setVisible(true);

					window.playMusic();

				} catch (Exception e) {
					e.printStackTrace();
				}
			} //run
		});
	}

	class RadioListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// to give us the source object that triggered the action.
			//In other words, the radio button that was clicked.
			JRadioButton btn = (JRadioButton) e.getSource();
			lblRadio.setText(btn.getText());

		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		//Initial JFrame
		frmBomberman = new JFrame();
		frmBomberman.setTitle("bomberman");
		frmBomberman.setBounds(100, 100, 1100, 648);
		frmBomberman.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmBomberman.getContentPane().setLayout(new BorderLayout());

		//Read the picture as a BufferedImage
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("bombparade_sml.jpg"));
			System.out.println("reading image");
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Resize the BufferedImage to background size
		Image dimg = img.getScaledInstance(1100, 648, SCALE_SMOOTH);

		ImageIcon imageIcon = new ImageIcon(dimg);

		JLabel background2=new JLabel(imageIcon);

		background2.setVisible(true);


		background2.setSize(1100, 648);
		//background2.setLayout(new GridLayout(3,3));

		frmBomberman.add(background2);

		JButton btnButtontest = new JButton("Play Bomberman");
		btnButtontest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnButtontest.setForeground(new Color(000, 000, 255));
				//this is where we should start the game

			}
		});
		btnButtontest.setFont(new Font("Impact", Font.PLAIN, 16));
		btnButtontest.setForeground(new Color(208, 17, 8));

		btnButtontest.setSize(new Dimension(100, 50));

		btnButtontest.setBounds(450, 300, 200, 80);


		//frmBomberman.getContentPane().add(btnButtontest);
		background2.add(btnButtontest);


	} //initialise

	public void playMusic(){

		//music - DPRK anthem
		try{
			AudioInputStream audioInputStream =AudioSystem.getAudioInputStream(new File("anthem2.wav"));
			//(this.getClass().getResource("anthem.mp3"));

			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}
}
