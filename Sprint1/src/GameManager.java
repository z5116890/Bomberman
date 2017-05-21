import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.util.Random;
import java.util.Scanner;

//import java.util.
public class GameManager{
	public static final int MAP_SIZE = 640;
	public static final int CELL_COUNT = 20;
	public static final int CELL_SIZE = MAP_SIZE/CELL_COUNT;//32
	public static final int TARGET_FRAME_RATE = 32;

	//map creation stuff
	public static final int EMPTY = 0;
	public static final int WALL = 1;
	public static final int BOX = 2;
	public static final int PLAYER = 3;
	public static final int ENDZONE = 4;
	public static final int BREAKABLE_WALL = 5;
	public static final int ENEMY = 6;





	private static GameManager gm;

	//Game State
	private boolean gameStarted = false;
	private static boolean gamePaused = false;
	private ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
	private ArrayList<EndZone> endZones = new ArrayList<EndZone>();
	private Player player;
	private ArrayList<GameObject> removeList = new ArrayList<GameObject>();
	private ScoreCounter scoreCounter;
	private int difficulty = 2;
	private int[][] map = null;
	private boolean reset = false;
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
		frame.setResizable(false);
		//frame.add(panel);
		//frame.pack();
		frame.setSize(panel.getPreferredSize());
		frame.setVisible(true);

		//createMap();
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
		map = new int[20][20];
		for(int i = 0; i<20; i++){
			map[0][i] = WALL;
			map[i][0] = WALL;
			map[19][i] = WALL;
			map[i][19] = WALL;
		}
		/*= {//Example/Test map
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
		};*/
		//constructing a graph
		Segment[] graph = new Segment[9];
		for(int i = 0;i<9;i++){
			graph[i] = new Segment();
		}
		graph[0].setConnections(null, graph[3], null, graph[1]);
		graph[1].setConnections(null, graph[4], graph[0], graph[2]);
		graph[2].setConnections(null, graph[5], graph[1], null);
		graph[3].setConnections(graph[0], graph[6], null, graph[4]);
		graph[4].setConnections(graph[1], graph[7], graph[3], graph[5]);
		graph[5].setConnections(graph[2], graph[8], graph[4], null);
		graph[6].setConnections(graph[3], null, null, graph[7]);
		graph[7].setConnections(graph[4], null, graph[6], graph[8]);
		graph[8].setConnections(graph[5], null, graph[7], null);

		graph[0].link(Segment.RIGHT);
		graph[1].link(Segment.LEFT);
		graph[0].link(Segment.DOWN);
		graph[3].link(Segment.UP);

		//randomly link segments until all segments are reachable from the start (graph[0])
		boolean finishedLinking = false;
		while(!finishedLinking){
			for(int i = 1;i < 9;i++){
				graph[i].randomLink();
			}
			ArrayList<Segment> found = new ArrayList<Segment>();
			ArrayList<Segment> toCheck = new ArrayList<Segment>();
			found.add(graph[0]);
			toCheck.add(graph[0]);
			while(toCheck.size()>0){
				Segment s = toCheck.get(0);
				toCheck.remove(0);
				ArrayList<Segment> links = s.getLinkedSegments();
				for(Segment l:links){
					if(!found.contains(l)){
						found.add(l);
						toCheck.add(l);
					}
				}
			}
			if(found.size()==9)finishedLinking = true;
		}
		//I'll make this random later
		ArrayList<Integer> boxLocations = new ArrayList<Integer>();
		Random rand = new Random();
		while(boxLocations.size()<4){
			int id = 1 + rand.nextInt(8);
			if(!boxLocations.contains(id))boxLocations.add(id);
		}
		for(int i = 0;i<4;i++)graph[boxLocations.get(i)].giveBox();

		//Create array from graph
		int[][] buffer = graph[0].getStartArray();
		insertIntoMap(buffer,1,1);
		for(int i = 1;i<9;i++){
			int x = i%3;
			int y = i/3;
			buffer = graph[i].getMapArray();
			insertIntoMap(buffer,1 + 6*x,1 + 6*y);
		}
		//Create map from array
		createMapFromArray();

	}
	private void createMapFromArray(){
		ArrayList<GameObject> walls = new ArrayList<GameObject>();
		ArrayList<EndZone> endzones = new ArrayList<EndZone>();
		ArrayList<GameObject> boxes = new ArrayList<GameObject>();
		ArrayList<Player> players = new ArrayList<Player>();
		ArrayList<Enemy> enemies = new ArrayList<Enemy>();

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
					case ENEMY:
						enemies.add(new Enemy(x,y));
						break;
				}
			}
		}

		gameObjects.addAll(walls);
		gameObjects.addAll(boxes);
		gameObjects.addAll(enemies);
		endZones.addAll(endzones);
		player = players.get(0);//there should be one and only one of these for now.

		panel.addGameObjects(walls);
		panel.addGameObjects(endZones);
		panel.addGameObjects(boxes);
		panel.addGameObjects(enemies);
		panel.addGameObject(player);
	}
	private void insertIntoMap(int[][] section,int xStart,int yStart){
		for(int y = 0; y<section.length; y++){
			for(int x = 0; x<section[y].length;x++){
				map[yStart + y][xStart + x] = section[y][x];
			}
		}
	}
	private void resetMap(){
		panel.removeGameObjects(panel.getRenderList());
		gameObjects.clear();
		endZones.clear();
		createMapFromArray();
		reset = false;
	}
	public void reset(){
		reset = true;
	}
	//all of the actual game is run in here, the method doesn't end until the game is over
	public void runGame(){
		createMap();
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
			if(reset)resetMap();
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
				GameManager.getGameManager().setDifficulty(value);
				//System.out.println("Setting difficulty to " + value);
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
		//back button
		JButton btnBack = new JButton("Back to main menu");
		btnBack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				background.removeAll();
				addStartMenuButtons(background);
				background.repaint();
			}
		});
		btnBack.setFont(new Font("Impact", Font.PLAIN,16));
		btnBack.setForeground(new Color(208,17,8));
		btnBack.setSize(new Dimension(100,500));
		btnBack.setBounds(220,150,200,50);
		background.add(btnBack);
		
		//"Rank" label and "Name" label and "Score" label
		JLabel rankJLabel = new JLabel("Rank");
		rankJLabel.setFont(new Font("Impact", Font.PLAIN,16));
		rankJLabel.setForeground(Color.white);
		rankJLabel.setSize(new Dimension(100,500));
		rankJLabel.setBounds(100,200,200,50);
		background.add(rankJLabel);
		
		JLabel nameJLabel = new JLabel("Name");
		nameJLabel.setFont(new Font("Impact", Font.PLAIN,16));
		nameJLabel.setForeground(Color.white);
		nameJLabel.setSize(new Dimension(100,500));
		nameJLabel.setBounds(300,200,200,50);
		background.add(nameJLabel);
		
		JLabel scoreJLabel = new JLabel("Score");
		scoreJLabel.setFont(new Font("Impact", Font.PLAIN,16));
		scoreJLabel.setForeground(Color.white);
		scoreJLabel.setSize(new Dimension(100,500));
		scoreJLabel.setBounds(500,200,200,50);
		background.add(scoreJLabel);
		
		int spacing = 50;
		int rank = 1;
		//display the names and the scores
//		for (String nameScore: leaderBoard.getLeaderBoard()) {
//			
//			JLabel rankLabel = new JLabel(rank);
//			rankLabel.setFont(new Font("Impact", Font.PLAIN,16));
//			rankLabel.setForeground(Color.white);
//			rankLabel.setSize(new Dimension(100,500));
//			rankLabel.setBounds(100,200+spacing,200,50);
//		
//			String name = nameScore.split(":")[0];
//			JLabel nameLabel = new JLabel(name);
//			nameLabel.setFont(new Font("Impact", Font.PLAIN,16));
//			nameLabel.setForeground(new Color(208,17,8));
//			nameLabel.setSize(new Dimension(100,500));
//			nameLabel.setBounds(300,200+spacing,200,50);
//			
//			int score = Integer.parseInt(nameScore.split(":")[1]);
//			JLabel scoreLabel = new JLabel(score);
//			scoreLabel.setFont(new Font("Impact", Font.PLAIN,16));
//			scoreLabel.setForeground(new Color(208,17,8));
//			scoreLabel.setSize(new Dimension(100,500));
//			scoreLabel.setBounds(500,200+spacing,200,50);
//			
//			spacing += 50;
//			rank += 1;
//		}
	}

	private void addStartMenuButtons(JLabel background){
		JButton btnStart = new JButton("Start");
		JButton btnHighScores = new JButton("Glorious scores");
		JButton btnOptions = new JButton("Settings");
		JButton btnQuit = new JButton("Quit");

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
				background.remove(btnQuit);
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

				dialogQuit.add( new JLabel ("Do you really want to leave the Motherland?"));
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
		semiOpaquePanel.setSize(new Dimension(640, 640));
		semiOpaquePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 640, 640));
		panel.add(semiOpaquePanel);
		panel.repaint();
		frame.pack();

		addPauseMenuButtons(semiOpaquePanel);

	}

	private void addPauseMenuButtons(JPanel semiOpaquePanel){

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
		JButton btnHelp = new JButton("Help");

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
				reset();
				unPauseGame();
				panel.remove(semiOpaquePanel);
			}
		});
		btnRestart.setFont(new Font("Impact", Font.PLAIN,16));
		btnRestart.setForeground(new Color(208,17,8));
		btnRestart.setBounds(220,295,200,50);
		semiOpaquePanel.add(btnRestart);

		//Help button - gives game instructions etc.
		btnHelp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){

				semiOpaquePanel.remove(btnResume);
				semiOpaquePanel.remove(btnQuit);
				semiOpaquePanel.remove(btnRestart);
				semiOpaquePanel.remove(btnHelp);
				semiOpaquePanel.remove(lblPause);
				addPauseHelpMenu(semiOpaquePanel);
			}
		});
		btnHelp.setFont(new Font("Impact", Font.PLAIN,16));
		btnHelp.setForeground(new Color(208,17,8));
		btnHelp.setBounds(220,365,200,50);
		semiOpaquePanel.add(btnHelp);

		//Quit button
		btnQuit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){

			}
		});
		btnQuit.setFont(new Font("Impact", Font.PLAIN,16));
		btnQuit.setForeground(new Color(208,17,8));
		btnQuit.setBounds(220,435,200,50);
		semiOpaquePanel.add(btnQuit);
	}

	private void addPauseHelpMenu(JPanel semiOpaquePanel){

		//Make a label to indicate user is in help menu
		JLabel lblPause = new JLabel("INSTRUCTIONS");
		lblPause.setForeground(Color.white);
		lblPause.setFont(new Font("Impact", Font.PLAIN,30));
		lblPause.setBounds(240,155,250,50);
		semiOpaquePanel.add(lblPause);

		//Read help.txt into JLabel
		JLabel lblHelpText = new JLabel();
		String helpString = new String();
		helpString+= "<html>";

		//read source file
		Scanner sc = null;
		try {
			sc = new Scanner(new FileReader("help.txt"));
			while (sc.hasNextLine()){
				String line = sc.nextLine();
				helpString+=line;
				helpString+="<br>";
			}
			helpString+="</html>";
			lblHelpText.setText(helpString);

		} catch (FileNotFoundException e){
			System.out.println("Help file not found");
			e.printStackTrace();
		}

		lblHelpText.setForeground(Color.white);
		lblHelpText.setFont(new Font("Impact", Font.PLAIN,16));
		//Why is it 150...
		lblHelpText.setBounds(230,150,200,300);
		semiOpaquePanel.add(lblHelpText);

		//Make a button to go back to the pause menu
		JButton btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){

				//btnBack.setForeground(new Color(0,0,255));
				semiOpaquePanel.remove(lblPause);
				semiOpaquePanel.remove(btnBack);
				semiOpaquePanel.remove(lblHelpText);
				addPauseMenuButtons(semiOpaquePanel);
			}
		});
		btnBack.setFont(new Font("Impact", Font.PLAIN,16));
		btnBack.setForeground(new Color(208,17,8));
		btnBack.setBounds(220,435,200,50);
		semiOpaquePanel.add(btnBack);
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
			case KeyEvent.VK_P:case KeyEvent.VK_ESCAPE:
				if (gamePaused) {
					//unPauseGame();
					break;
				}
				else {
					pauseGame();
					break;
				}
			case KeyEvent.VK_R:
				reset = true;
				break;
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

	public int getDifficulty(){
		return difficulty;
	}
	public void setDifficulty(int value){
		difficulty = value;
	}

}

