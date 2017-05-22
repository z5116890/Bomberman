
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class LeaderBoard {
	private ArrayList<String> highScores; //store the strings
	private File file; //store the file itself
	private static final int MAX_SCORES = 5;
	
	public LeaderBoard() {
		checkIfExists();
		readFile();
	}
	
	/**
	 * checks if the leaderboard data file exists
	 * 		if it doesnt, creates one
	 */
	public void checkIfExists(){
		File highScoresFile = new File("leaderboard.dat");
		if (!highScoresFile.exists()) {
			try {
				highScoresFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		file = highScoresFile;
	}
	
	/**
	 * reads the leaderboard file and stores each highscore in an array
	 */
	public void readFile() {
		FileReader read = null;
		BufferedReader reader = null;
		try {
			read = new FileReader(file);
			reader = new BufferedReader(read);

			while (reader.readLine() != null) {
				String score = reader.readLine();
				highScores.add(score);
			}
		} catch (Exception e){
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * compares the current score to the high scores
	 * 		if current score is a high score, puts it in its respective position
	 * 			if the leaderboard is filled, removes the last score
	 * 			break out of loop
	 * @param score
	 */
	public void checkScores(int score) {
		int pos = 0;
		for (String highScore: highScores) {
			if (score > Integer.parseInt((highScore.split(":")[1]))) {
				String name = JOptionPane.showInputDialog("You set a new high score. What is your name?");
				String newHighScore = name + ":" + score;
				highScores.add(pos, newHighScore);
				if (highScores.size() > MAX_SCORES) {
					highScores.remove(highScores.size()-1);
				}
				break;
			}
			pos++;
		}
	}
	
	/**
	 * rewrites the leaderboard file
	 */
	public void writeScores() { //rewrite the whole file
		FileWriter write = null;
		BufferedWriter writer = null;
		try {
			write = new FileWriter(file, false);
			writer = new BufferedWriter(write);
			for (String score: highScores) {
				writer.write(score);
				writer.newLine();
			}
		} catch (Exception e) {
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public ArrayList<String> getLeaderBoard() {
		return this.highScores;
	}
	
}
