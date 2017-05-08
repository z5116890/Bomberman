import java.util.ArrayList;
import javax.swing.*;
public class GameManager {
	private static GameManager gm;
	
	private ArrayList<GameObject> gameObjects;
	private ArrayList<EndZone> endzones;
	private Player player;
	private JFrame frame;
	private PaintingPanel panel;
	
	private GameManager(){
		gm = this;
	}
	public static void main(String args[]){
		
	}
	private void draw(){
		
	}
	private void createMap(){
		
	}
	private void runGame(){
		
	}
	public GameObject getObjectAtLocation(int x,int y){
		return null;
	}
	public static GameManager getGameManager(){
		return gm;
	}
}
