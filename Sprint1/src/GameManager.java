import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
//import java.util.
public class GameManager{
	public static final int MAP_SIZE = 640;
	public static final int CELL_COUNT = 20;
	public static final int CELL_SIZE = MAP_SIZE/CELL_COUNT;//32
	public static final int TARGET_FRAME_RATE = 32;

	//map generation stuff
	private static final int EMPTY = 0;
	private static final int WALL = 1;
	private static final int BOX = 2;
	private static final int PLAYER = 3;
	private static final int ENDZONE = 4;
	private static final int BREAKABLE_WALL = 5;


	private static GameManager gm; 

	//Game State
	private boolean gameStarted = false;
	private ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
	private ArrayList<EndZone> endZones = new ArrayList<EndZone>();
	private Player player;
	private ArrayList<GameObject> removeList = new ArrayList<GameObject>();

	//Swing stuff
	private JFrame frame;
	private PaintingPanel panel;
	private float framerateMultiplier = 1;
	//Start Menu stuff
	private JTextField txtName;
	private final ButtonGroup radios = new ButtonGroup();
	private JLabel lblRadio = new JLabel("radio1");
	//leaderboard
	private LeaderBoard leaderBoard;

	private GameManager(){
		gm = this;
		panel = new PaintingPanel();
		leaderBoard = new LeaderBoard();

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
				{1,0,0,0,0,0,0,5,5,5,0,0,0,0,0,0,0,0,0,1},
				{1,0,3,0,5,5,0,2,2,2,0,0,0,0,0,5,5,0,0,1},
				{1,0,2,0,0,0,0,5,5,5,0,0,0,0,0,0,5,0,5,1},
				{1,0,5,0,0,0,0,5,0,5,0,0,0,0,5,5,5,5,5,1},
				{1,0,0,0,1,1,1,1,1,1,1,0,0,0,5,0,5,0,0,1},
				{1,0,0,0,1,0,0,5,0,0,1,0,0,0,0,0,5,5,0,1},
				{1,0,0,0,1,0,5,4,5,0,1,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,1,5,0,0,0,5,1,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,1,0,5,0,5,0,1,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,1,0,0,5,0,0,1,0,0,0,0,0,1,0,0,1},
				{1,0,0,0,1,0,5,0,5,0,1,5,5,0,5,5,1,0,0,1},
				{1,0,0,0,1,5,0,0,0,5,1,5,5,5,5,5,1,0,0,1},
				{1,0,0,0,1,0,5,0,5,0,1,0,4,2,4,0,1,0,0,1},
				{1,0,0,0,1,0,0,5,0,0,1,0,0,5,0,0,1,5,0,1},
				{1,5,5,5,1,5,5,5,5,5,1,1,1,1,1,1,1,5,5,1},
				{1,0,0,0,5,0,0,5,0,5,0,0,0,0,0,5,1,0,5,1},
				{1,0,0,5,0,0,5,0,5,0,5,0,0,0,0,5,5,1,4,1},
				{1,0,5,0,0,5,0,5,0,0,0,5,0,0,0,5,5,4,1,1},
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
						walls.add(new Wall(x,y,false));
						break;
					case BOX:
						boxes.add(new Box(x,y));
						break;
					case PLAYER:
						players.add(new Player(x,y));
						break;
					case ENDZONE:
						endzones.add(new EndZone(x,y));
						break;
					case BREAKABLE_WALL:
						walls.add(new Wall(x,y,true));
						break;
				}
			}
		}

		gameObjects.addAll(walls);
		gameObjects.addAll(boxes);
		endZones.addAll(endzones);
		player = players.get(0);//there should be one and only one of these for now.

		panel.addGameObjects(walls);
		panel.addGameObjects(endZones);
		panel.addGameObjects(boxes);
		panel.addGameObject(player);

	}
	//all of the actual game is run in here, the method doesn't end until the game is over
	public void runGame(){
		frame.add(panel);
		frame.pack();
		frame.addKeyListener(new KeyInputListener());
		frame.setFocusable(true);
		frame.requestFocus();
		panel.addKeyListener(new KeyInputListener());
		panel.setFocusable(true);
		//frame.setSize(panel.getPreferredSize());
		long time = 0;
		boolean ended = false;
		while(!ended){
			time = System.nanoTime();

			if(player!=null)//temporary if statement
				player.act();
			try{
				for(GameObject obj:gameObjects){
					obj.act();	
				}
			}catch(Exception e){
				//this is responsible exception handling.
			}
			gameObjects.removeAll(removeList);
			panel.removeGameObjects(removeList);
			removeList.clear();
			ended = true;
			for(EndZone e:endZones){
				e.act();
				if(!e.getActive())ended = false;
			}
			draw();
			//limits the frame rate
			while(System.nanoTime()-time<(long)(1000000000L/(TARGET_FRAME_RATE*framerateMultiplier)));
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
	//returns a list of objects at a specified location
	public ArrayList<GameObject> getObjectsAtLocation(int x,int y){
		ArrayList<GameObject> output = new ArrayList<GameObject>();
		for(GameObject obj:panel.getRenderList()){
			if(obj.getX() == x && obj.getY() == y)output.add(obj);
		}
		return output;
	}
	public void removeObject(GameObject obj){
		removeList.add(obj);
		//gameObjects.remove(obj);
		//panel.removeGameObject(obj);
	}
	public void addObject(GameObject obj){
		gameObjects.add(obj);
		panel.addGameObject(obj);
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

		addStartMenuButtons(background);




		frame.pack();
		frame.setSize(640,640);
		while(!gameStarted)System.out.print("");
		frame.remove(tempPanel);
		clip.stop();
		clip.close();

	} //start menu

	/**
	 * Create the options menu buttons, add main settings buttons back on return
	 * Precondition: the settings buttons have already been removed
	 * @param background - the main image in the menu
	 */
	private void addOptionsMenuButtons(JLabel background){

		//At this point we're already in the start menu, there's no other way to get here

		//Slider to control game speed - parameters are max, min and default values
		JSlider speedSlider = new JSlider(JSlider.HORIZONTAL,
				1, 5, 3);

		speedSlider.setSize(new Dimension(100,500));
		speedSlider.setBounds(310,295,100,50);
		Font font = new Font("Impact", Font.PLAIN, 15);
		speedSlider.setFont(font);
		speedSlider.setForeground(Color.white);
		speedSlider.setMajorTickSpacing(1);
		speedSlider.setPaintLabels(true);
		speedSlider.setSnapToTicks(true);

		speedSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				int value = speedSlider.getValue();
				GameManager.getGameManager().setSpeed(0.1f + (value*value)/10f);
				//System.out.println("Setting speed to " + value);
			}
		});

		JLabel speedLabel = new JLabel("Game speed");
		speedLabel.setFont(font);
		speedLabel.setForeground(Color.white);
		//Might be nice if this wasn't so hard coded...
		speedLabel.setBounds(220,308,200,50);

		//Add label and slider to background
		background.add(speedSlider);
		background.add(speedLabel);

		//make a back button so that we can go back to the normal options menu
		//creating start button
		JButton btnBack = new JButton("Back to main menu");
		btnBack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				background.remove(btnBack);
				background.remove(speedSlider);
				background.remove(speedLabel);
				addStartMenuButtons(background);
				background.repaint();
			}
		});
		btnBack.setFont(new Font("Impact", Font.PLAIN,16));
		btnBack.setForeground(new Color(208,17,8));
		btnBack.setSize(new Dimension(100,500));
		btnBack.setBounds(220,225,200,50);

		background.add(btnBack);

	}
	
	/**
	 * Displays the current leaderboard
	 * @param background
	 */
	private void displayLeaderBoard(JLabel background) {
		int spacing = 50;
		for (String score: leaderBoard.getLeaderBoard()) {
			
		}
	}

	private void addStartMenuButtons(JLabel background){
		JButton btnStart = new JButton("Play Bomberman");
		JButton btnHighScores = new JButton("Glorious scores");
		JButton btnOptions = new JButton("Options");
		//moved the constructors here to avoid error - GRANT
		
		//creating start button
		btnStart.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				GameManager.getGameManager().startGame();
				btnStart.setForeground(new Color(0,0,255));
			}
		});
		btnStart.setFont(new Font("Impact", Font.PLAIN,16));
		btnStart.setForeground(new Color(208,17,8));
		btnStart.setSize(new Dimension(100,500));
		btnStart.setBounds(220,225,200,50);

		background.add(btnStart);

		//creating high scores button
		btnHighScores.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				background.remove(btnHighScores);
				background.remove(btnStart);
				background.remove(btnOptions);
				displayLeaderBoard(background);
				//btnHighScores.setForeground(new Color(0,0,255));
				background.repaint();
			}
		});
		btnHighScores.setFont(new Font("Impact", Font.PLAIN,16));
		btnHighScores.setForeground(new Color(208,17,8));
		btnHighScores.setSize(new Dimension(100,500));
		btnHighScores.setBounds(220,295,200,50);

		background.add(btnHighScores);

		//creating options button
		btnOptions.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				background.remove(btnHighScores);
				background.remove(btnStart);
				background.remove(btnOptions);
				addOptionsMenuButtons(background);
				background.repaint();
			}
		});
		btnOptions.setFont(new Font("Impact", Font.PLAIN,16));
		btnOptions.setForeground(new Color(208,17,8));
		btnOptions.setSize(new Dimension(100,500));
		btnOptions.setBounds(220,365,200,50);

		background.add(btnOptions);


	}




	public void startGame(){
		gameStarted = true;
	}
	public void keyPressed(int keyCode){
		switch(keyCode){
			case KeyEvent.VK_UP:
				player.setAction(GameObject.UP);
				break;
			case KeyEvent.VK_DOWN:
				player.setAction(GameObject.DOWN);
				break;
			case KeyEvent.VK_LEFT:
				player.setAction(GameObject.LEFT);
				break;
			case KeyEvent.VK_RIGHT:
				player.setAction(GameObject.RIGHT);
				break;
			case KeyEvent.VK_P:
				//paused = true;
				break;
			case KeyEvent.VK_SPACE:
				player.setAction(Player.BOMB);
				break;
		}
	}
	public void setSpeed(float multiplier){
		framerateMultiplier = multiplier;
	}
}

