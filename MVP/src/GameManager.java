import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
//import java.util.
public class GameManager {
	public static final int MAP_SIZE = 640;
	public static final int CELL_COUNT = 20;
	public static final int CELL_SIZE = MAP_SIZE/CELL_COUNT;//32
	public static final int TARGET_FRAME_RATE = 10;
	
	//map generation stuff
	private static final int EMPTY = 0;
	private static final int WALL = 1;
	private static final int BOX = 2;
	private static final int PLAYER = 3;
	private static final int ENDZONE = 4;
	
	
	private static GameManager gm;
		
	//Game State
	private boolean gameStarted = false;
	private ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
	private ArrayList<EndZone> endZones = new ArrayList<EndZone>();
	private Player player;
	
	//Swing stuff
	private JFrame frame;
	private PaintingPanel panel;
	//Start Menu stuff
	private JTextField txtName;
	private final ButtonGroup radios = new ButtonGroup();
	private JLabel lblRadio = new JLabel("radio1");
	
	private GameManager(){
		gm = this;
		panel = new PaintingPanel();
		
		frame = new JFrame("Bomberman");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.add(panel);
		//frame.pack();
		frame.setSize(panel.getPreferredSize());
		frame.setVisible(true);
		
		createMap();
	}
	//Creates an instance of GameManager, does start screen, runs the game
	public static void main(String args[]){
		gm = new GameManager();
		gm.startMenu();
		gm.runGame();
	}
	//updates the screen image
	private void draw(){
		panel.repaint();
	}
	//generates a map and instantiates all the objects and adds them to their corresponding lists
	private void createMap(){
		int[][] map = {
				{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
				{1,0,3,0,0,0,0,2,2,2,0,0,0,0,0,0,0,0,0,1},
				{1,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,1,0,0,4,0,0,1,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,1},
				{1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,1},
				{1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,1},
				{1,0,0,0,1,0,0,0,0,0,1,0,4,2,4,0,1,0,0,1},
				{1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,1},
				{1,0,0,0,1,0,0,0,0,0,1,1,1,1,1,1,1,0,0,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,4,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
		};
		
		//Create map from array
		ArrayList<GameObject> walls = new ArrayList<GameObject>();
		ArrayList<EndZone> endzones = new ArrayList<EndZone>();
		ArrayList<GameObject> boxes = new ArrayList<GameObject>();
		ArrayList<Player> players = new ArrayList<Player>();
		
		for(int y = 0;y<map.length;y++){
			for(int x = 0;x<map[y].length;x++){
				switch(map[y][x]){
					case EMPTY:
						break;
					case WALL:
						//walls.add(new Wall(x,y));
						break;
					case BOX:
						//boxes.add(new Box(x,y));
						break;
					case PLAYER:
						//players.add(new Player(x,y));
						break;
					case ENDZONE:
						//endzones.add(new EndZone(x,y));
						break;
				}
			}
		}
		/*
		gameObjects.addAll(walls);
		gameObjects.addAll(boxes);
		endZones.addAll(endzones);
		player = players.get(0);//there should be one and only one of these for now. 
		
		panel.addGameObjects(walls);
		panel.addGameObjects(endZones);
		panel.addGameObjects(boxes);
		panel.addGameObject(player);
		*/
	}
	//all of the actual game is run in here, the method doesn't end until the game is over
	public void runGame(){
		frame.add(panel);
		frame.pack();
		frame.setSize(panel.getPreferredSize());
		long time = 0;
		boolean ended = false;
		while(!ended){
			time = System.nanoTime();
			
			if(player!=null)//temporary if statement
			player.act();
			
			for(GameObject obj:gameObjects){
				obj.act();
			}
			
			//ended = true;
			for(EndZone e:endZones){
				e.act();
				if(!e.getActive())ended = false;
			}
			draw();
			//limits the frame rate
			while(System.nanoTime()-time<(long)(1000000000L/TARGET_FRAME_RATE));
		}
		
	}
	//returns the object at the specified grid location, if multiple objects are in the same place, it returns the one rendered last (seen ontop)
	public GameObject getObjectAtLocation(int x,int y){
		GameObject output = null;
		for(GameObject obj:panel.getRenderList()){
			if(obj.getX() == x && obj.getY() == y)output = obj;
		}
		return output;
	}
	//used for other objects to get a reference to the active GameManager
	public static GameManager getGameManager(){
		return gm;
	}
	private void startMenu(){
		//playing music
		Clip clip = null;
		try{
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("anthem2.wav"));
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		//creating background
		BufferedImage img = null;
		try{
			img = ImageIO.read(new File("missile_square_640x640.jpg"));
		}catch(IOException e){
			e.printStackTrace();
		}
		int width = (int)(img.getWidth()*((float)PaintingPanel.DEFAULT_WINDOW_SIZE/img.getHeight()));
		Image dimg = img.getScaledInstance(width,PaintingPanel.DEFAULT_WINDOW_SIZE,Image.SCALE_SMOOTH);
		ImageIcon imageIcon = new ImageIcon(dimg);
		JLabel background = new JLabel(imageIcon);
		background.setVisible(true);
		background.setSize(PaintingPanel.DEFAULT_WINDOW_SIZE,PaintingPanel.DEFAULT_WINDOW_SIZE);
		
		JPanel tempPanel = new JPanel();
		frame.add(tempPanel);
		tempPanel.add(background);
		background.setVisible(true);
		
		//creating start button
		JButton btnStart = new JButton("Play Bomberman");
		btnStart.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				GameManager.getGameManager().startGame();
				btnStart.setForeground(new Color(0,0,255));
			}
		});
		btnStart.setFont(new Font("Impact", Font.PLAIN,16));
		btnStart.setForeground(new Color(208,17,8));
		btnStart.setSize(new Dimension(100,500));
		btnStart.setBounds(450,300,200,80);
		
		background.add(btnStart);
		frame.pack();
		frame.setSize(640,640);
		while(!gameStarted)System.out.println("");
		frame.remove(tempPanel);
		clip.stop();
		clip.close();
		
	}
	public void startGame(){
		gameStarted = true;
	}
}

