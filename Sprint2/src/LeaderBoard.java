
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
		highScores = new ArrayList<String>();
		checkIfExists();
		readFile();
	}
	
	/**
	 * checks if the leaderboard data file exists
	 * 		if it doesnt, creates one
	 */
	public void checkIfExists(){
		File highScoresFile = new File("leaderboard.txt");
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
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				highScores.add(line);
			}
		} catch (Exception e){
			e.printStackTrace();
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
	public boolean checkScores(String name, int score, int difficulty) {
		int pos = 0;
		String stringDifficulty = difficultyToString(difficulty);
		for (String highScore: highScores) {
			if (score > Integer.parseInt(highScore.split(":")[1])) {
				String newHighScore = name + ":" + score + ":" + stringDifficulty;
				if (highScores.size() < MAX_SCORES) { //if arraylist has less than 5 scores
					highScores.add(pos, newHighScore); //simply add
				} else { //else it has 5 or more
					highScores.add(pos, newHighScore); //add
					highScores.remove(highScores.size()-1); //we must remove the last score
				}
				return true;
			}
			pos++;
		}
		if (highScores.size() < MAX_SCORES) { //if less than 5 scores and also not higher than any of scores
			String newHighScore = name + ":" + score + ":" + stringDifficulty; //we can still add! to end
			highScores.add(pos, newHighScore);
			return true;
		}
		return false;
	}
	
	public String difficultyToString(int difficulty) {
		String stringDifficulty = null;
		if (difficulty == 1) {
			stringDifficulty = "Easy";
		} else if (difficulty == 2) {
			stringDifficulty = "Medium";
		} else {
			stringDifficulty = "Hard";
		}
		return stringDifficulty;
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
