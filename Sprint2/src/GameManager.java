import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.util.Random;
import java.util.Scanner;

public class GameManager{
	/*
	 * the GameManager is the main class in this application, it serves the role of storing the game state, controlling
	 * the flow of game logic, managing the window and deciding what is rendered.
	 */
	//Game Settings
	public static final int MAP_SIZE = 640;
	public static final int CELL_COUNT = 20;
	public static final int CELL_SIZE = MAP_SIZE/CELL_COUNT;//32
	public static final int TARGET_FRAME_RATE = 32;
	public static final int GAME_DURATION = 300;
	private static final long DELAY_DURATION = 15*1000000000L;

	//map creation IDs
	//These are used during map generation to represent a type of object
	public static final int EMPTY = 0;
	public static final int WALL = 1;
	public static final int BOX = 2;
	public static final int PLAYER = 3;
	public static final int ENDZONE = 4;
	public static final int BREAKABLE_WALL = 5;
	public static final int ENEMY = 6;
	public static final int WALL_LEFT = 7;
	public static final int WALL_RIGHT = 8;
	public static final int WALL_CENTRE = 9;

	//Screen IDs
	private static final int START_MENU = 1;
	private static final int RUN = 2;
	private static final int SCORES = 3;
	private static final int SETTINGS = 4;
	private static final int ENDSCREEN = 5;
	private static final int QUIT = 6;
	private static int instruction = START_MENU;

	//A static reference to the active game manager is stored so that any object can easily reference it without having to pass it around.
	private static GameManager gm;

	//Game State
	private boolean gameStarted = false;
	private static boolean gamePaused = false;
	private ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
	private ArrayList<EndZone> endZones = new ArrayList<EndZone>();
	private Player player;
	private ArrayList<GameObject> removeList = new ArrayList<GameObject>();
	private int difficulty = 2;
	private int[][] map = null;
	private boolean reset = false;
	private boolean quit = false;
	private long startTime = 0;
	private int score = 0;
	private float framerateMultiplier = 1;
	
	//Swing stuff
	private JFrame frame;
	private PaintingPanel panel;
	
	//leaderboard
	private LeaderBoard leaderBoard;
	/**
	 * Initializes GameManager by creating the frame, panel and leaderboard manager and sets it as the active GameManager.
	 * Precondition: There must not be an active GameManager
	 */
	private GameManager(){
		gm = this;
		//all images are drawn onto this
		panel = new PaintingPanel();
		//this accesses the leaderboard file and does all of the I/O
		leaderBoard = new LeaderBoard();

		//This is the window
		frame = new JFrame("Bomberman");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(panel.getPreferredSize());
		frame.setVisible(true);

	}
	/**
	 * Creates an instance of GameManager, changes the screen state and runs the game. 
	 * @param args This method takes no arguments.
	 */
	public static void main(String args[]){
		gm = new GameManager();
		while(instruction != QUIT){//until the user wants to close the application the game manager will open a screen based on the current instructions
			switch(instruction){
				case START_MENU:
					gm.startMenu();
					break;
				case RUN:
					gm.runGame();
					break;
				case ENDSCREEN:
					gm.endGameMenu();
					break;
			}
		}
	}
	
	//These methods are used for processes like rendering and map generation
	/**
	 *  updates the screen image.
	 */
	private void draw(){
		panel.repaint();
	}
	/**
	 * generates a map and instantiates all the objects and adds them to their corresponding lists
	 */
	private void createMap(){
		map = new int[20][20];//the map is being stored as a field for if the player wants to reset the map during the game
		for(int i = 0; i<20; i++){
			//creating the borders for the map
			map[0][i] = WALL;
			map[i][0] = WALL;
			map[19][i] = WALL;
			map[i][19] = WALL;
		}
		//walls that will have labels on them
		map [0][0] = WALL_LEFT;
		map [0][1] = WALL_CENTRE;
		map [0][2] = WALL_CENTRE;
		map [0][3] = WALL_CENTRE;
		map [0][4] = WALL_RIGHT;

		map [0][15] = WALL_LEFT;
		map [0][16] = WALL_CENTRE;
		map [0][17] = WALL_CENTRE;
		map [0][18] = WALL_CENTRE;
		map [0][19] = WALL_RIGHT;

		map [0][7] = WALL_LEFT;
		map [0][8] = WALL_CENTRE;
		map [0][9] = WALL_CENTRE;
		map [0][10] = WALL_CENTRE;
		map [0][11] = WALL_CENTRE;
		map [0][12] = WALL_RIGHT;

		map [19][0] = WALL_LEFT;
		map [19][1] = WALL_CENTRE;
		map [19][2] = WALL_RIGHT;

		map [19][17] = WALL_LEFT;
		map [19][18] = WALL_CENTRE;
		map [19][19] = WALL_RIGHT;
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
		/*
		 * Here a graph is being constructing such that each node represents a 6x6 partition of the map inside the borders.
		 * The selected edges of the graph represent paths in which a player can move a box. 
		 * The graph is set up like a 3x3 grid where each adjacent segment is connected.
		 * Once the graph is constructed, edges from each node are randomly selected until it is possible to create a subgraph
		 * containing all the nodes and selected edges such that a path between any two nodes can be found. 
		 * By at what edges are selected for each node, the shape of the segment can be determined, and the randomization for that
		 * segment is restricted such that a path in the desired shape will always form and connect to the linked segment.
		 * Then, by using a random number generator, the value for each index of a 6x6 array is decided while staying within the 
		 * shape restriction.
		 * The map array is then filled using the values in the 9 sub-arrays.
		 */
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
		
		/*
		graph[0].link(Segment.RIGHT);
		graph[1].link(Segment.LEFT);
		graph[0].link(Segment.DOWN);
		graph[3].link(Segment.UP);
		*/
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
		//deciding the positions for the boxes, endzones and player
		ArrayList<Integer> locations = new ArrayList<Integer>();
		Random rand = new Random();
		while(locations.size()<3+difficulty*2){//the amount of boxes/endzones is dependent on difficulty
			int id = rand.nextInt(9);
			if(!locations.contains(id))locations.add(id);
		}
		for(int i = 0;i<1+difficulty;i++)graph[locations.get(i)].setSpecialType(BOX);
		for(int i = 1+difficulty;i<(1+difficulty)*2;i++)graph[locations.get(i)].setSpecialType(ENDZONE);
		graph[locations.get((1+difficulty)*2)].setSpecialType(PLAYER);
		
		//Create array from graph
		int[][] buffer;
		
		for(int i = 0;i<9;i++){
			int x = i%3;
			int y = i/3;
			buffer = graph[i].getMapArray();
			insertIntoMap(buffer,1 + 6*x,1 + 6*y);
		}
		//Create map from array
		createMapFromArray();

	}
	/**
	 * This method uses the saved map array to instantiate all the GameObjects in the map and then adds them to their corresponding ArrayLists.
	 */
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
						walls.add(new Wall(x,y,false, 'N'));
						break;
					case WALL_LEFT:
						walls.add(new Wall(x,y,false, 'L'));
						break;
					case WALL_CENTRE:
						walls.add(new Wall(x,y,false, 'C'));
						break;
					case WALL_RIGHT:
						walls.add(new Wall(x,y,false, 'R'));
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
						walls.add(new Wall(x,y,true, 'N'));
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
	/**
	 * This method is used to insert the sub-arrays into the map for map generation.
	 * @param section This is the array being inserted into the map
	 * @param xStart this is the horizontal index of the top left corner of the section of the map where the array is being inserted.
	 * @param yStart this is the vertical index of the top left corner of the section of the map where the array is being inserted.
	 */
	private void insertIntoMap(int[][] section,int xStart,int yStart){
		for(int y = 0; y<section.length; y++){
			for(int x = 0; x<section[y].length;x++){
				map[yStart + y][xStart + x] = section[y][x];
			}
		}
	}
	
	
	
	
	//These methods are used for setting up the different screens and managing the execution of their corresponding features. 
	/**
	 * all of the actual game is run in here, the method doesn't end until the game is over
	 */
	public void runGame(){
		//The first part of this method sets up many parts of the Window and UI
		quit = false;
		createMap();
		//setting up the panel and key listeners for playing the game.
		panel.setVisible(true);
		frame.add(panel);
		frame.pack();
		KeyInputListener keyListener1 = new KeyInputListener();
		frame.addKeyListener(keyListener1);
		frame.setFocusable(true);
		frame.requestFocus();
		KeyInputListener keyListener2 = new KeyInputListener();
		panel.addKeyListener(keyListener2);
		panel.setFocusable(true);

		//counters to display at top
		JLabel statBox = new JLabel("");
		JLabel bombsLeft = new JLabel("");
		JLabel timer = new JLabel("");
		JLabel explosionSize = new JLabel("");
		
		//icons for counters
		BufferedImage bomb = null;
		BufferedImage nuclear = null;
		BufferedImage timerImg = null;
		try{
			bomb = ImageIO.read(new File("bombIcon.png"));
			nuclear = ImageIO.read(new File("blastIcon.png"));
			timerImg = ImageIO.read(new File("Item_3.png"));
		}catch(IOException e){
			e.printStackTrace();
		}
		
		ImageIcon iconBomb = new ImageIcon(bomb);
		JLabel bombLabel = new JLabel(iconBomb);
		bombLabel.setVisible(true);
		bombLabel.setBounds(5,-10,35,50);
		statBox.add(bombLabel);
		
		Image dTime = timerImg.getScaledInstance(30,25,10);
		ImageIcon imageIcon = new ImageIcon(dTime);
		JLabel timeIcon = new JLabel(imageIcon);
		timeIcon.setVisible(true);
		timeIcon.setBounds(10,-10,520,50);
		statBox.add(timeIcon);
	
		ImageIcon iconNuke = new ImageIcon(nuclear);
		JLabel nukeLabel = new JLabel(iconNuke);
		nukeLabel.setVisible(true);
		nukeLabel.setBounds(0,-10,1010,50);
		statBox.add(nukeLabel);
		
		//Exit button to display at bottom
		JButton btnEndGame = new JButton("Quit");
		btnEndGame.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				quit = true;
			}
		});
		btnEndGame.setVisible(false);
		btnEndGame.setFont(new Font("Impact", Font.PLAIN,15));
		btnEndGame.setForeground(new Color(208,17,8));

		//Pause button to display at bottom
		JButton btnPause = new JButton("Pause");
		btnPause.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				pauseGame();
			}
		});
		btnPause.setVisible(false);
		btnPause.setFont(new Font("Impact", Font.PLAIN,15));
		btnPause.setForeground(new Color(208,17,8));

		//The game starts here.
		long time = 0;
		boolean ended = false;
		startTime = System.nanoTime();
		while(!ended){

			//Only need pause and exit buttons when pause menu not showing
			if (!gamePaused) {
				this.addBoardButtons(btnEndGame, btnPause);
			} else {
				panel.remove(btnEndGame);
				panel.remove(btnPause);
			}

			time = System.nanoTime();//the time this 'frame' started is recorded so that the FPS (frames per second) can be controlled.

			if(quit){//this closes the game if the player decides to quit mid-game
				panel.remove(btnEndGame);
				panel.remove(btnPause);
				instruction = START_MENU;
				gameObjects.clear();
				panel.removeGameObjects(panel.getRenderList());
				endZones.clear();
				panel.setVisible(false);
				return;
			}
			this.displayLiveGameStats(statBox, bombsLeft, timer, explosionSize);//this updates the counters.
			

			if(!gamePaused){
				//In here the act method is called for each of the GameObjects, this is when all the frame-by-frame changes are calculated.				
				player.act();
				try{
					for(GameObject obj:gameObjects){
						obj.act();
					}
				}catch(Exception e){
					//This is for a concurrent modification exception which occurs whenever the list of GameObjects is changed mid-loop
					//no action needs to be taken since stopping and calculating the next frame is the correct response and 
					//natural course of action, which is why this is left blank. 
				}
			}
			//this is used to delete all GameObjects that were deleted this frame at the same time, it is done to make object removal look smoother. 
			gameObjects.removeAll(removeList);
			panel.removeGameObjects(removeList);
			removeList.clear();
			
			//Here it is checking if all the endzones have boxes on them or if the player has run out of time so that it can end the game. 
			ended = true;
			for(EndZone e:endZones){
				e.act();
				if(!e.getActive()) {
					ended = false;//if one endzone isn't activated, it means there isn't a box on it, so the game shouldn't ended.
					break;
				}
			}
			if(getTime()<=0)ended = true;
			
			if(reset)resetMap();
			
			draw();//this tells the painting panel to update its image. 
			
			//limits the frame rate, by waiting until the time since the start of the frame has reached the amount required for the desired FPS.
			while(System.nanoTime()-time<(long)(1000000000L/(TARGET_FRAME_RATE*framerateMultiplier)));
		}
		//At this point the game has ended.
		score = getTime()*difficulty;
		//Deleting all the GameObjects as they're no longer needed. 
		gameObjects.clear();
		panel.removeGameObjects(panel.getRenderList());
		panel.remove(btnEndGame);
		panel.remove(btnPause);
		endZones.clear();
		panel.setVisible(false);
		//setting the instruction to ENDSCREEN so that main knows what screen to open up next. 
		instruction = ENDSCREEN;
	}
	/**
	 * opens up the screen first seen when the player opens the application, this screen is used to go to different parts of the application.
	 * This method does not end until the player decides to play the game or close the application. 
	 */
	private void startMenu(){
		gameStarted = false;
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
		//scaling background to window size and putting it on the screen.
		int width = (int)(img.getWidth()*((float)PaintingPanel.DEFAULT_WINDOW_SIZE/img.getHeight()));
		Image dimg = img.getScaledInstance(width,PaintingPanel.DEFAULT_WINDOW_SIZE,Image.SCALE_SMOOTH);
		ImageIcon imageIcon = new ImageIcon(dimg);
		JLabel background = new JLabel(imageIcon);
		background.setVisible(true);
		background.setSize(PaintingPanel.DEFAULT_WINDOW_SIZE,PaintingPanel.DEFAULT_WINDOW_SIZE);

		frame.add(background);
		addStartMenuButtons(background);

		frame.pack();
		frame.setSize(640,640);
		while(!gameStarted)System.out.print("");
		frame.remove(background);
		clip.stop();
		clip.close();

	} 
	/**
	 * this method creates all the buttons for the start menu.
	 * @param background This is the JLabel onto which the buttons are added
	 */
	private void addStartMenuButtons(JLabel background){
		//These are the buttons on the start menu.
		JButton btnStart = new JButton("Start");
		JButton btnHighScores = new JButton("Glorious scores");
		JButton btnOptions = new JButton("Settings");
		JButton btnQuit = new JButton("Quit");
		JButton btnInstructions = new JButton("Instructions");

		//creating start button
		btnStart.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				instruction = RUN;
				GameManager.getGameManager().startGame();
				btnStart.setForeground(new Color(0,0,255));
			}
		});
		btnStart.setFont(new Font("Impact", Font.PLAIN,16));
		btnStart.setForeground(new Color(208,17,8));
		btnStart.setSize(new Dimension(100,500));
		btnStart.setBounds(220,200,200,50);

		background.add(btnStart);

		//creating high scores button
		btnHighScores.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				background.remove(btnHighScores);
				background.remove(btnStart);
				background.remove(btnOptions);
				background.remove(btnQuit);
				background.remove(btnInstructions);
				displayLeaderBoard(background);
				background.repaint();
			}
		});
		btnHighScores.setFont(new Font("Impact", Font.PLAIN,16));
		btnHighScores.setForeground(new Color(208,17,8));
		btnHighScores.setSize(new Dimension(100,500));
		btnHighScores.setBounds(220,260,200,50);

		background.add(btnHighScores);

		//creating options button
		btnOptions.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				background.remove(btnHighScores);
				background.remove(btnStart);
				background.remove(btnOptions);
				background.remove(btnQuit);
				background.remove(btnInstructions);
				addOptionsMenuButtons(background);
				background.repaint();
			}
		});
		btnOptions.setFont(new Font("Impact", Font.PLAIN,16));
		btnOptions.setForeground(new Color(208,17,8));
		btnOptions.setSize(new Dimension(100,500));
		btnOptions.setBounds(220,320,200,50);

		background.add(btnOptions);

		//creating instructions button
		btnInstructions.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				background.remove(btnHighScores);
				background.remove(btnStart);
				background.remove(btnOptions);
				background.remove(btnQuit);
				background.remove(btnInstructions);
				addInstructionsMenu(background);
				background.repaint();
			}
		});
		btnInstructions.setFont(new Font("Impact", Font.PLAIN,16));
		btnInstructions.setForeground(new Color(208,17,8));
		btnInstructions.setSize(new Dimension(100,500));
		btnInstructions.setBounds(220,380,200,50);

		background.add(btnInstructions);

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
				dialogQuit.setLocationRelativeTo(frame);
				dialogQuit.setVisible(true);
			}
		});
		btnQuit.setFont(new Font("Impact", Font.PLAIN,16));
		btnQuit.setForeground(new Color(208,17,8));
		btnQuit.setSize(new Dimension(100,500));
		btnQuit.setBounds(220,440,200,50);

		background.add(btnQuit);
	}
	/**
	 * Create the options menu buttons, add main settings buttons back on return
	 * Precondition: the settings buttons have already been removed and the user is in the main start menu
	 * @param background This is the JLabel onto which the buttons are added 
	 */
	private void addOptionsMenuButtons(JLabel background){

		//SPEED SLIDER
		//Slider to control game speed - parameters are max, min and default values
		JSlider speedSlider = new JSlider(JSlider.HORIZONTAL,
				1, 5, 3);

		speedSlider.setSize(new Dimension(100,500));
		speedSlider.setBounds(310,270,100,50);
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
		speedLabel.setBounds(220,283,200,50);

		//Add label and slider to background
		background.add(speedSlider);
		background.add(speedLabel);

		//DIFFICULTY SLIDER
		//Slider to control game difficulty - parameters are max, min and default values
		JSlider difficultySlider = new JSlider(JSlider.HORIZONTAL,
				1, 3, 2);

		difficultySlider.setSize(new Dimension(100,500));
		difficultySlider.setBounds(310,320,100,50);
		difficultySlider.setFont(font);
		difficultySlider.setForeground(Color.white);
		difficultySlider.setMajorTickSpacing(1);
		difficultySlider.setPaintLabels(true);
		difficultySlider.setSnapToTicks(true);

		difficultySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				int value = difficultySlider.getValue();
				GameManager.getGameManager().setDifficulty(value);
			}
		});

		JLabel difficultyLabel = new JLabel("Difficulty");
		difficultyLabel.setFont(font);
		difficultyLabel.setForeground(Color.white);
		difficultyLabel.setBounds(220,333,200,50);

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
		btnBack.setBounds(220,200,200,50);

		background.add(btnBack);
	}
	/**
	 * Creates the instruction menu
	 * @param background This is the JLabel onto which the buttons are added
	 */
	private void addInstructionsMenu(JLabel background){

		//TextArea to display instructions
		JTextArea helpTextArea = new JTextArea("");
		helpTextArea.setOpaque(true);
		helpTextArea.setLineWrap(true);
		helpTextArea.setWrapStyleWord(true);
		helpTextArea.setForeground(Color.black);
		helpTextArea.setBounds(140,210,360,200);
		helpTextArea.setEditable(false);
		helpTextArea.setBorder(new LineBorder(Color.white, 10, true));
		helpTextArea.setBackground(Color.white);
		//helpTextArea.setVisible(true);
		background.add(helpTextArea);

		JScrollPane scroll = new JScrollPane(helpTextArea);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(140,210,360,200);
		scroll.setVisible(true);
		background.add(scroll);

		//This label just makes the rounded border for the TextArea
		JLabel frameLabel = new JLabel("");
		frameLabel.setOpaque(false);
		frameLabel.setBounds(130,200,380,220);
		frameLabel.setBorder(new LineBorder(Color.white, 10, true));
		background.add(frameLabel);

		//Make a back button so user can go back to the normal options menu
		JButton btnBack = new JButton("Back to main menu");
		btnBack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				background.remove(btnBack);
				background.remove(helpTextArea);
				background.remove(frameLabel);
				background.remove(scroll);
				addStartMenuButtons(background);
				background.repaint();
			}
		});
		btnBack.setFont(new Font("Impact", Font.PLAIN,16));
		btnBack.setForeground(new Color(208,17,8));
		btnBack.setSize(new Dimension(100,500));
		btnBack.setBounds(220,440,200,50);

		background.add(btnBack);

		//read file containing instructions
		helpTextArea.setFont(new Font("Courier", Font.PLAIN,16));
		Scanner sc = null;
		try {
			sc = new Scanner(new FileReader("instructions.txt"));
			while (sc.hasNextLine()){
				String line = sc.nextLine();
				helpTextArea.append(line);
				helpTextArea.append("\n");
			}
		} catch (FileNotFoundException e){
			System.out.println("Help file not found");
			e.printStackTrace();
		}

		//Makes sure that the scrollbar starts at the top, not the bottom
		helpTextArea.setCaretPosition(0);
	}

	/**
	 * Displays the current leaderboard
	 * @param background This is the JLabel the leaderboard is displayed on
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
		rankJLabel.setBounds(90,200,200,50);
		background.add(rankJLabel);

		JLabel nameJLabel = new JLabel("Name");
		nameJLabel.setFont(new Font("Impact", Font.PLAIN,16));
		nameJLabel.setForeground(Color.white);
		nameJLabel.setSize(new Dimension(100,500));
		nameJLabel.setBounds(230,200,200,50);
		background.add(nameJLabel);

		JLabel scoreJLabel = new JLabel("Score (s)");
		scoreJLabel.setFont(new Font("Impact", Font.PLAIN,16));
		scoreJLabel.setForeground(Color.white);
		scoreJLabel.setSize(new Dimension(100,500));
		scoreJLabel.setBounds(370,200,200,50);
		background.add(scoreJLabel);

		JLabel difficultyJLabel = new JLabel("Difficulty");
		difficultyJLabel.setFont(new Font("Impact", Font.PLAIN,16));
		difficultyJLabel.setForeground(Color.white);
		difficultyJLabel.setSize(new Dimension(100,500));
		difficultyJLabel.setBounds(510,200,200,50);
		background.add(difficultyJLabel);
		
		int spacing = 50;
		int rank = 1;
		for (String nameScore: leaderBoard.getLeaderBoard()) {
			JLabel rankLabel = new JLabel(Integer.toString(rank));
			rankLabel.setFont(new Font("Impact", Font.PLAIN,16));
			rankLabel.setForeground(Color.white);
			rankLabel.setSize(new Dimension(100,500));
			rankLabel.setBounds(90,200+spacing,200,50);
			background.add(rankLabel);
			
			String name = nameScore.split(":")[0];
			JLabel nameLabel = new JLabel(name);
			nameLabel.setFont(new Font("Impact", Font.PLAIN,16));
			nameLabel.setForeground(Color.white);
			nameLabel.setSize(new Dimension(100,500));
			nameLabel.setBounds(230,200+spacing,200,50);
			background.add(nameLabel);

			int score = Integer.parseInt(nameScore.split(":")[1]);
			JLabel scoreLabel = new JLabel(Integer.toString(score));
			scoreLabel.setFont(new Font("Impact", Font.PLAIN,16));
			scoreLabel.setForeground(Color.white);
			scoreLabel.setSize(new Dimension(100,500));
			scoreLabel.setBounds(370,200+spacing,200,50);
			background.add(scoreLabel);
			
			String difficulty = nameScore.split(":")[2];
			JLabel difficultyLabel = new JLabel(difficulty);
			difficultyLabel.setFont(new Font("Impact", Font.PLAIN,16));
			difficultyLabel.setForeground(Color.white);
			difficultyLabel.setSize(new Dimension(100,500));
			difficultyLabel.setBounds(510,200+spacing,200,50);
			background.add(difficultyLabel);

			spacing += 50;
			rank += 1;
		}
	}
	/**
	 * This is used to create the pause window.
	 */
	private void pauseGame(){

		gamePaused = true;

		//Make semi-opaque black panel that covers screen to indicate game paused
		JPanel semiOpaquePanel = new JPanel();
		semiOpaquePanel.setBackground(new Color(000, 000, 000, 200));
		semiOpaquePanel.setVisible(true);
		semiOpaquePanel.setBounds(32, 32, 576, 576);
		semiOpaquePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 10));
		panel.add(semiOpaquePanel);
		panel.repaint();

		addPauseMenuButtons(semiOpaquePanel);
	}
	/**
	 * This adds the buttons to the pause window.
	 * @param semiOpaquePanel This is the JPanel onto which the buttons are added
	 */
	private void addPauseMenuButtons(JPanel semiOpaquePanel){

		//Make a label to let user know that the game is paused
		JLabel lblPause = new JLabel("GAME PAUSED");
		lblPause.setForeground(Color.white);
		lblPause.setFont(new Font("Impact", Font.PLAIN,30));
		lblPause.setBounds(208,123,200,50);
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
		btnResume.setBounds(188,225,200,50);

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
		btnRestart.setBounds(188,295,200,50);
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
		btnHelp.setBounds(188,365,200,50);
		semiOpaquePanel.add(btnHelp);

		//Quit button
		btnQuit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				quit = true;
				unPauseGame();
				panel.remove(semiOpaquePanel);
			}
		});
		btnQuit.setFont(new Font("Impact", Font.PLAIN,16));
		btnQuit.setForeground(new Color(208,17,8));
		btnQuit.setBounds(188,435,200,50);
		semiOpaquePanel.add(btnQuit);
	}
	/**
	 * This displays the help menu while in the pause window.
	 * @param semiOpaquePanel This is the JPanel the help menu is displayed on
	 */
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
		lblHelpText.setBounds(230,150,200,300);
		semiOpaquePanel.add(lblHelpText);

		//Make a button to go back to the pause menu
		JButton btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){

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
	/**
	 * This screen is displayed at the end of every game, and it tells the player if they've won or not and their score if they have.
	 * It also allows a user to enter their name and adds their score to the leaderboard if it's high enough. 
	 */
	private void endGameMenu(){
		//playing music if the player won.
		Clip clip = null;
		if(score>0){
			try{
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("anthem2.wav"));
				clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		//creating background
		BufferedImage img = null;
		try{
			String alt = "";
			if(score <= 0)alt = "2_";
			img = ImageIO.read(new File("end_square_"+alt+"640x640.png"));//the image used is determined by whether the player won or not.
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
		while(instruction == ENDSCREEN)System.out.print("");
		frame.remove(tempPanel);
		//stops the music if the player won. (won't be playing if they lost)
		if(score>0){
			clip.stop();
			clip.close();
		}
		//sets the instruction so that the start menu will open up. 
		instruction = START_MENU;

	} 
	/**
	 * This creates the buttons for the end game menu, it will create different buttons based on whether the player won or not.
	 * @param background This is the JLabel onto which the buttons are added
	 */
	private void addEndGameMenuButtons(JLabel background) {
		/*
		JLabel gameOverLabel = new JLabel("Game Over");
		gameOverLabel.setFont(new Font("Impact", Font.PLAIN,16));
		gameOverLabel.setForeground(Color.white);
		gameOverLabel.setBounds(220,50,200,50);
		background.add(gameOverLabel);
		*/
		if(score > 0){
			JLabel ScoreLabel = new JLabel("Score");
			ScoreLabel.setFont(new Font("Impact", Font.PLAIN,16));
			ScoreLabel.setForeground(Color.white);
			ScoreLabel.setBounds(220, 100, 200,50);
			background.add(ScoreLabel);
			//So not sure where to store the score but here is where it would go for this screen
			JLabel ScoreNumberLabel = new JLabel(""+score);
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
			
			JButton submitBtn = new JButton("Submit");
			submitBtn.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					//You can get the name from here for the scoreboard
					String name = textField.getText();
					
					JButton btnBack = new JButton("Back to main menu");
					btnBack.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e){
							instruction = START_MENU;
						}
					});
					btnBack.setFont(new Font("Impact", Font.PLAIN,16));
					btnBack.setForeground(new Color(208,17,8));
					btnBack.setSize(new Dimension(100,500));
					btnBack.setBounds(220,300,200,50);
					
					JLabel notifyPlayer = new JLabel();
					notifyPlayer.setFont(new Font("Impact", Font.PLAIN,24));
					notifyPlayer.setForeground(Color.white);
					notifyPlayer.setBounds(150,250,400,50);
					
					//check leaderboard scores
					
					if (name.isEmpty()) {
						background.add(errorLabel);
						background.repaint();
					} else if (leaderBoard.checkScores(name,score,difficulty)) {
						leaderBoard.writeScores();
						
						background.remove(btnBack);
						//background.remove(gameOverLabel);
						background.remove(ScoreLabel);
						background.remove(ScoreNumberLabel);
						background.remove(inputTextLabel);
						background.remove(textField);
						background.remove(errorLabel);
						background.remove(submitBtn);
						notifyPlayer.setText("Congratulations " + name + ", it is a high score!");					
						background.add(notifyPlayer);
						background.add(btnBack);
						background.repaint();
					} else {
						background.remove(btnBack);
						//background.remove(gameOverLabel);
						background.remove(ScoreLabel);
						background.remove(ScoreNumberLabel);
						background.remove(inputTextLabel);
						background.remove(textField);
						background.remove(errorLabel);
						background.remove(submitBtn);
						notifyPlayer.setText("Sorry " + name + ", it is not a high score");
						background.add(notifyPlayer);
						background.add(btnBack);
						background.repaint();
					}
				}
				
			});
			submitBtn.setFont(new Font("Impact", Font.PLAIN,16));
			submitBtn.setForeground(new Color(208,17,8));
			submitBtn.setSize(new Dimension(100,500));
			submitBtn.setBounds(220,350,200,50);
			
	
			background.add(submitBtn);
		}else{
				JButton btnReturn = new JButton("Back to main menu");
				btnReturn.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						instruction = START_MENU;
						background.remove(btnReturn);
					}
				});
		
		
				btnReturn.setFont(new Font("Impact", Font.PLAIN,16));
				btnReturn.setForeground(new Color(208,17,8));
				btnReturn.setSize(new Dimension(100,500));
				btnReturn.setBounds(220,300,200,50);
				background.add(btnReturn);
				background.repaint();				
			}
	}
	/**
	 * This is used to add the exit and pause buttons during the game.
	 * @param btnExit This is the exit button
	 * @param btnPause This is the pause button
	 */
	private void addBoardButtons(JButton btnExit, JButton btnPause){
		//Make a button to go back to the pause menu
		btnExit.setVisible(true);
		btnExit.setBounds(3,608,90,32);
		panel.add(btnExit);

		btnPause.setVisible(true);
		btnPause.setBounds(550,608,90,32);
		panel.add(btnPause);

		panel.repaint();
	}

	/**
	 * Used to display live game stats including bombs left, explosion size, timer.
	 * @param statBox This is the JLabel onto which the stats are written
	 * @param bombsLeft This is the JLabel which holds the bomb counter
	 * @param timer This is the JLabel that holds the time counter
	 * @param explosionSize This is the JLabel that holds the explosion size counter
	 */
	private void displayLiveGameStats(JLabel statBox, JLabel bombsLeft, JLabel timer, JLabel explosionSize){

		statBox.setBounds(0, 0, 750, 30);
		statBox.setBackground(Color.white);
		statBox.setOpaque(false);
		panel.add(statBox);
		

		bombsLeft.setText("Bombs Away: " + player.bombsLeft() + "/" + player.getBombCount());
		bombsLeft.setForeground(Color.yellow);
		bombsLeft.setFont(new Font("Impact", Font.PLAIN,15));
		bombsLeft.setBounds(40, -10, 150, 50);
		statBox.add(bombsLeft);
		
		String time = "";
		int t = getTime();
		if(t>=60){
			time = "" + t/60 + ":";
			if(t%60 < 10)time += "0";
			time += t%60 + "";
		}
		else time = "" + t;
		timer.setText("Time Left: "+time);
		timer.setForeground(Color.yellow);
		timer.setFont(new Font("Impact", Font.PLAIN,15));
		timer.setBounds(290, -10, 150, 50);
		statBox.add(timer);
		
		explosionSize.setText("Explosion Size: " + player.getExplosionSize());
		explosionSize.setForeground(Color.yellow);
		explosionSize.setFont(new Font("Impact", Font.PLAIN,15));
		explosionSize.setBounds(523, -10, 150, 50);
		statBox.add(explosionSize);
		
		//icons
		/*
		BufferedImage timerImg = null;
		BufferedImage bomb = null;
		BufferedImage nuclear = null;
		try{
			timerImg = ImageIO.read(new File("Item_3.png"));
			bomb = ImageIO.read(new File("Item_1.png"));
			nuclear = ImageIO.read(new File("Item_2.png"));
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Image dTime = timerImg.getScaledInstance(30,25,10);
		ImageIcon imageIcon = new ImageIcon(dTime);
		JLabel timeIcon = new JLabel(imageIcon);
		timeIcon.setVisible(true);
		timeIcon.setBounds(0,-10,520,50);
		statBox.add(timeIcon);
		
		
		
		Image dbomb = bomb.getScaledInstance(30,25,10);
		ImageIcon iconBomb = new ImageIcon(dbomb);
		JLabel bombLabel = new JLabel(iconBomb);
		bombLabel.setVisible(true);
		bombLabel.setBounds(0,-10,35,50);
		statBox.add(bombLabel);
	
		
		
		Image dnuke = nuclear.getScaledInstance(30,25,10);
		ImageIcon iconNuke = new ImageIcon(dnuke);
		JLabel nukeLabel = new JLabel(iconNuke);
		nukeLabel.setVisible(true);
		nukeLabel.setBounds(0,-10,1010,50);
		statBox.add(nukeLabel);*/

	}

	
	
	
	//These methods are used for communicating with other Objects so that they can get information about the game state and get the GameManager to change it. 
	/**
	 * Used to get the object at a specified grid location.
	 * @param x the grid location on the X axis
	 * @param y the grid location on the Y axis
	 * @return returns the object at the specified grid location, if multiple objects are in the same place, it returns the one rendered last (seen ontop)
	 */
	public GameObject getObjectAtLocation(int x,int y){
		GameObject output = null;
		for(GameObject obj:panel.getRenderList()){
			if(obj.getX() == x && obj.getY() == y)output = obj;
		}
		return output;
	}
	/**
	 * similar to getObjectAtLocation except it returns all the objects at the specified location.
	 * @param x the grid location on the X axis
	 * @param y the grid location on the Y axis
	 * @return returns a list of objects at a specified location, ordered from bottom to top.
	 */
	public ArrayList<GameObject> getObjectsAtLocation(int x,int y){
		ArrayList<GameObject> output = new ArrayList<GameObject>();
		for(GameObject obj:panel.getRenderList()){
			if(obj.getX() == x && obj.getY() == y)output.add(obj);
		}
		return output;
	}
	/**
	 * adds an object to the remove list to be deleted later.
	 * @param obj the object being removed
	 */
	public void removeObject(GameObject obj){
		removeList.add(obj);
	}
	/**
	 * adds a GameObject to the list.
	 * @param obj the object being added
	 */
	public void addObject(GameObject obj){
		gameObjects.add(obj);
		panel.addGameObject(obj);
	}
	/**
	 * used for other objects to get a reference to the active GameManager
	 * @return the active GameManager
	 */
	public static GameManager getGameManager(){
		return gm;
	}
	/**
	 * This is used to tell the GameManager that the game is ready to start. 
	 */
	public void startGame(){
		gameStarted = true;
	}
	/**
	 * This is used to tell the GameManager that the player has unpaused the game.
	 */
	public void unPauseGame() {
		gamePaused = false;
	}
	/**
	 * This is used by GameObjects to check if the game is paused.
	 * @return returns true if the game is paused, false if it isn't
	 */
	public static boolean isPaused() {
		return gamePaused;
	}
	/**
	 * This is used to get the remaining time for the game in seconds. 
	 * @return returns the amount of time until the game ends in seconds
	 */
	public int getTime(){
		return GAME_DURATION - (int)((System.nanoTime() - startTime)/1000000000);
	}
	/**
	 * This is used to tell the GameManager to add more time to the timer.
	 */
	public void delay(){
		startTime += DELAY_DURATION;
	}
	/**
	 * This is called by the KeyInputListener to tell the GameManager that a key has been pressed.
	 * @param keyCode the keycode for the key that was pressed
	 */
	public void keyPressed(int keyCode){
		switch(keyCode){
		//movement
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
		//pausing
			case KeyEvent.VK_P:case KeyEvent.VK_ESCAPE:
				if(instruction != RUN)return;
				if (gamePaused) {
					//unPauseGame();
					break;
				}
				else {
					pauseGame();
					break;
				}
		//reseting the map
			case KeyEvent.VK_R:
				reset = true;
				break;
		//placing a bomb
			case KeyEvent.VK_SPACE:
				if (!gamePaused) {
					player.setAction(Player.BOMB);
				}
				break;
		}
	}
	/**
	 * This is used to change the frame rate limit. 
	 * @param multiplier The default target frame rate is multiplied by this
	 */
	public void setSpeed(float multiplier){
		framerateMultiplier = multiplier;
	}
	/**
	 * This method clears all the lists of objects and then recreates the map.
	 */
	private void resetMap(){
		panel.removeGameObjects(panel.getRenderList());
		gameObjects.clear();
		endZones.clear();
		createMapFromArray();
		reset = false;
	}
	/**
	 * This is used by other objects to tell the GameManager that the map needs to be reset. 
	 */
	public void reset(){
		reset = true;
	}
	/**
	 * This is used by other objects to check the difficulty set by the player.
	 * @return returns the current value of the difficulty setting
	 */
	public int getDifficulty(){
		return difficulty;
	}
	/**
	 * This is used to set the difficulty.
	 * @param value the new difficulty setting
	 */
	public void setDifficulty(int value){
		difficulty = value;
	}

}