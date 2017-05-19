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
	private static boolean gamePaused = false;
	private ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
	private ArrayList<EndZone> endZones = new ArrayList<EndZone>();
	private Player player;
	private ArrayList<GameObject> removeList = new ArrayList<GameObject>();
	private ScoreCounter scoreCounter;

	//Swing stuff
	private JFrame frame;
	private PaintingPanel panel;
	private float framerateMultiplier = 1;

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
		//gm.endGameMenu(); - so this works but otherwise loops or soemthing
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
		KeyInputListener keyListener1 = new KeyInputListener();
		frame.addKeyListener(keyListener1);
		frame.setFocusable(true);
		frame.requestFocus();
		KeyInputListener keyListener2 = new KeyInputListener();
		panel.addKeyListener(keyListener2);
		panel.setFocusable(true);
		
		//stats to display at top
		JLabel statBox = new JLabel("");
		JLabel bombsLeft = new JLabel("");
		JLabel timer = new JLabel("Time left: ");
		JLabel score = new JLabel("Score: " );
		
		//frame.setSize(panel.getPreferredSize());
		long time = 0;
		boolean ended = false;
		while(!ended){
			this.displayLiveGameStats(statBox, bombsLeft, timer, score);
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
		//panel.removeKeyListener(keyListener2);
		//frame.removeKeyListener(keyListener1);
		frame.remove(panel);
		GameManager.getGameManager().endGameMenu();
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
			img = ImageIO.read(new File("title_640x640.png"));
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

		//SPEED SLIDER
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
			}
		});

		JLabel speedLabel = new JLabel("Game speed");
		speedLabel.setFont(font);
		speedLabel.setForeground(Color.white);
		speedLabel.setBounds(220,308,200,50);

		//Add label and slider to background
		background.add(speedSlider);
		background.add(speedLabel);

		//DIFFICULTY SLIDER
		//Slider to control game difficulty - parameters are max, min and default values
		JSlider difficultySlider = new JSlider(JSlider.HORIZONTAL,
				1, 3, 2);

		difficultySlider.setSize(new Dimension(100,500));
		difficultySlider.setBounds(310,345,100,50);
		difficultySlider.setFont(font);
		difficultySlider.setForeground(Color.white);
		difficultySlider.setMajorTickSpacing(1);
		difficultySlider.setPaintLabels(true);
		difficultySlider.setSnapToTicks(true);

		difficultySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				int value = difficultySlider.getValue();

				System.out.println("Setting difficulty to " + value);
			}
		});

		JLabel difficultyLabel = new JLabel("Difficulty");
		difficultyLabel.setFont(font);
		difficultyLabel.setForeground(Color.white);
		difficultyLabel.setBounds(220,358,200,50);

		//Add label and slider to background
		background.add(difficultySlider);
		background.add(difficultyLabel);

		//make a back button so that we can go back to the normal options menu
		//creating start button
		JButton btnBack = new JButton("Back to main menu");
		btnBack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				background.remove(btnBack);
				background.remove(speedSlider);
				background.remove(speedLabel);
				background.remove(difficultyLabel);
				background.remove(difficultySlider);
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
		JButton btnStart = new JButton("Play");
		JButton btnHighScores = new JButton("Glorious scores");
		JButton btnOptions = new JButton("Settings");
		JButton btnQuit = new JButton("Quit");
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
				background.remove(btnQuit);
				addOptionsMenuButtons(background);
				background.repaint();
			}
		});
		btnOptions.setFont(new Font("Impact", Font.PLAIN,16));
		btnOptions.setForeground(new Color(208,17,8));
		btnOptions.setSize(new Dimension(100,500));
		btnOptions.setBounds(220,365,200,50);

		background.add(btnOptions);

		//creating quit button
		btnQuit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				JDialog dialogQuit = new JDialog(frame, "Confirm defection", true);
				dialogQuit.setLayout(new FlowLayout());
				JButton btnYes = new JButton ("OK");
				btnYes.addActionListener ( new ActionListener()
				{
					public void actionPerformed( ActionEvent e )
					{
						System.exit(0);
					}
				});
				JButton btnNo = new JButton ("Cancel");
				btnNo.addActionListener (new ActionListener()
				{
					public void actionPerformed( ActionEvent e )
					{
						dialogQuit.setVisible(false);
					}
				});

				dialogQuit.add( new JLabel ("Do you really want to quit the Motherland?"));
				dialogQuit.add(btnYes);
				dialogQuit.add(btnNo);
				dialogQuit.setBounds(170, 200, 300, 80);
				dialogQuit.setVisible(true);
			}
		});
		btnQuit.setFont(new Font("Impact", Font.PLAIN,16));
		btnQuit.setForeground(new Color(208,17,8));
		btnQuit.setSize(new Dimension(100,500));
		btnQuit.setBounds(220,435,200,50);

		background.add(btnQuit);
	}

	private void pauseGame(){

		gamePaused = true;

		//Make semi-opaque black panel that covers screen to indicate game paused
		JPanel semiOpaquePanel = new JPanel();
		semiOpaquePanel.setBackground(new Color(000, 000, 000, 200));
		semiOpaquePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 640, 640));
		panel.add(semiOpaquePanel);
		panel.repaint();
		frame.pack();

		//Make a label to let user know that the game is paused
		JLabel lblPause = new JLabel("GAME PAUSED");
		lblPause.setForeground(Color.white);
		lblPause.setFont(new Font("Impact", Font.PLAIN,30));
		lblPause.setBounds(240,155,200,50);
		semiOpaquePanel.add(lblPause);

		//Create buttons
		JButton btnResume = new JButton("Resume");
		JButton btnQuit = new JButton("Quit");
		JButton btnRestart = new JButton("Restart");

		//Resume button
		btnResume.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				unPauseGame();
				panel.remove(semiOpaquePanel);
			}
		});
		btnResume.setFont(new Font("Impact", Font.PLAIN,16));
		btnResume.setForeground(new Color(208,17,8));
		btnResume.setBounds(220,225,200,50);

		semiOpaquePanel.add(btnResume);

		//Restart button
		btnRestart.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){

				btnRestart.setForeground(new Color(0,0,255));
			}
		});
		btnRestart.setFont(new Font("Impact", Font.PLAIN,16));
		btnRestart.setForeground(new Color(208,17,8));
		//btnQuit.setSize(new Dimension(100,500));
		btnRestart.setBounds(220,295,200,50);

		semiOpaquePanel.add(btnRestart);

		//Quit button
		btnQuit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){

				btnQuit.setForeground(new Color(0,0,255));
			}
		});
		btnQuit.setFont(new Font("Impact", Font.PLAIN,16));
		btnQuit.setForeground(new Color(208,17,8));
		btnQuit.setBounds(220,365,200,50);
		semiOpaquePanel.add(btnQuit);
	}

	public void startGame(){
		gameStarted = true;
	}
	public void unPauseGame() {
		gamePaused = false;
	}
	public static boolean isPaused() {
		return gamePaused;
	}

	public void keyPressed(int keyCode){
		switch(keyCode){
			case KeyEvent.VK_UP:
				if (!gamePaused) {
					player.setAction(GameObject.UP);
				}
				break;
			case KeyEvent.VK_DOWN:
				if (!gamePaused) {
					player.setAction(GameObject.DOWN);
				}
				break;
			case KeyEvent.VK_LEFT:
				if (!gamePaused) {
					player.setAction(GameObject.LEFT);
				}
				break;
			case KeyEvent.VK_RIGHT:
				if (!gamePaused) {
					player.setAction(GameObject.RIGHT);
				}
				break;
			case KeyEvent.VK_P:
				if (gamePaused) {
					//unPauseGame();
					break;
				}
				else {
					pauseGame();
					break;
				}
			case KeyEvent.VK_SPACE:
				if (!gamePaused) {
					player.setAction(Player.BOMB);
				}
				break;
		}
	}
	public void setSpeed(float multiplier){
		framerateMultiplier = multiplier;
	}
	
	private void endGameMenu(){
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
			img = ImageIO.read(new File("title_640x640.png"));
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

		addEndGameMenuButtons(background);

		frame.pack();
		frame.setSize(640,640);
		
		while(!gameStarted)System.out.print("");
		frame.remove(tempPanel);
		clip.stop();
		clip.close();

	} //End Game Menu
	
	private void addEndGameMenuButtons(JLabel background) {
				JLabel gameOverLabel = new JLabel("Game Over");
				gameOverLabel.setFont(new Font("Impact", Font.PLAIN,16));
				gameOverLabel.setForeground(Color.white);
				gameOverLabel.setBounds(220,50,200,50);
				background.add(gameOverLabel);
				
				JLabel ScoreLabel = new JLabel("Score");
				ScoreLabel.setFont(new Font("Impact", Font.PLAIN,16));
				ScoreLabel.setForeground(Color.white);
				ScoreLabel.setBounds(220, 100, 200,50);
				background.add(ScoreLabel);
				//So not sure where to store the score but here is where it would go for this screen 
				JLabel ScoreNumberLabel = new JLabel("10000");
				ScoreNumberLabel.setFont(new Font("Impact", Font.PLAIN,16));
				ScoreNumberLabel.setForeground(Color.white);
				ScoreNumberLabel.setBounds(220, 150,200,50);
				background.add(ScoreNumberLabel);
				
				JLabel inputTextLabel = new JLabel("Input Name");
				inputTextLabel.setFont(new Font("Impact", Font.PLAIN,16));
				inputTextLabel.setForeground(Color.white);
				inputTextLabel.setBounds(220,200,200,50);
				background.add(inputTextLabel);

				JTextField textField = new JTextField();
				textField.setFont(new Font("Impact", Font.PLAIN,16));
				textField.setForeground(Color.black);
				textField.setBounds(220,250,200,50);
				background.add(textField);
				
				JLabel errorLabel = new JLabel("Must Enter Name");
				errorLabel.setFont(new Font("Impact", Font.PLAIN,16));
				errorLabel.setForeground(Color.red);
				errorLabel.setBounds(220,300,200,50);
				
				JButton btnBack = new JButton("Back to main menu");
				btnBack.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						//You can get the name from here for the scoreboard 
						String name = textField.getText();
						if (name.isEmpty()) {
							background.add(errorLabel);
							background.repaint();
						} else {
							background.remove(btnBack); 
							background.remove(gameOverLabel);
							background.remove(ScoreLabel);
							background.remove(ScoreNumberLabel);
							background.remove(inputTextLabel);
							background.remove(textField);
							background.remove(errorLabel);
							addStartMenuButtons(background);
							background.repaint();
						}
					}
				});
				btnBack.setFont(new Font("Impact", Font.PLAIN,16));
				btnBack.setForeground(new Color(208,17,8));
				btnBack.setSize(new Dimension(100,500));
				btnBack.setBounds(220,350,200,50);

				background.add(btnBack);

	}
	
	//function to display live game stats including bombs left, score, timer
	private void displayLiveGameStats(JLabel statBox, JLabel bombsLeft, JLabel timer, JLabel score){
		
		statBox.setBounds(0, 0, 750, 30);
		statBox.setBackground(Color.white);
		statBox.setOpaque(false);
		panel.add(statBox);
		//panel.repaint();
		
		//calls player function bombsLeft to update num of bombs left
		//doesnt show properly for some reason
		
		bombsLeft.setText("bombs away: " + player.bombsLeft());
		bombsLeft.setForeground(Color.yellow);
		bombsLeft.setFont(new Font("Impact", Font.PLAIN,15));
		bombsLeft.setBounds(10, -10, 100, 50);
		statBox.add(bombsLeft);
		
		
		
		
		timer.setForeground(Color.yellow);
		timer.setFont(new Font("Impact", Font.PLAIN,15));
		timer.setBounds(250, -10, 100, 50);
		statBox.add(timer);
		
		
		score.setForeground(Color.yellow);
		score.setFont(new Font("Impact", Font.PLAIN,15));
		score.setBounds(500, -10, 100, 50);
		statBox.add(score);
	}
	

	
}

