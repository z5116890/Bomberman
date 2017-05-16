
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class LeaderBoard {
	ArrayList<String> highScores; //store the strings
	File file; //store the file itself
	
	public LeaderBoard() {
		checkIfExists();
		readFile();
	}
	
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
	
	public void checkIfExists(){
		File scoreFile = new File("highscores.dat");
		if (!scoreFile.exists()) {
			try {
				scoreFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		file = scoreFile;
	}
		
	public void checkScores(int score) {
		int pos = 0;
		for (String highScore: highScores) {
			if (score > Integer.parseInt((highScore.split(":")[1]))) {
				String name = JOptionPane.showInputDialog("You set a new high score. What is your name?");
				String newHighScore = name + ":" + score;
				highScores.add(pos, newHighScore);
				highScores.remove(highScores.size()-1);
			}
			pos++;
		}
	}
	
	public void writeScores() { //rewrite the whole file
		FileWriter write = null;
		BufferedWriter writer = null;
		try {
			write = new FileWriter(file);
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
	
}
