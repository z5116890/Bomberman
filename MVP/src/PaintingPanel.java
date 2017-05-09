import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

public class PaintingPanel extends JPanel {
	//Not entirely sure what this is for, but apparently its needed
	private static final long serialVersionUID = 42L;
	
	public static final int DEFAULT_WINDOW_SIZE = 640;
	
	
	private ArrayList<GameObject> renderList = new ArrayList<GameObject>();
	
	public PaintingPanel(){
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}
	public Dimension getPreferredSize(){
		return new Dimension(DEFAULT_WINDOW_SIZE,DEFAULT_WINDOW_SIZE);
	}
	int i = 0;
	public void paintComponent(Graphics g){//Might need to come back to this.
		super.paintComponent(g);
		g.setColor(new Color(100-2*i/3,100,100+i++));
		i%=150;
		g.fillRect(0, 0, DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
		
		for(GameObject go:renderList){
			g.drawImage(go.getImage(),go.getRealX(),go.getRealY(),null);
		}
	}
	public void addGameObject(GameObject obj){
		if(renderList.contains(obj))return;
		renderList.add(obj);
	}
	public void removeGameObject(GameObject obj){
		renderList.remove(obj);
	}
	public void addGameObjects(ArrayList objs){
		for(Object obj:objs){
			if(obj instanceof GameObject)
			addGameObject((GameObject)obj);
		}
	}
	public ArrayList<GameObject> getRenderList(){
		return renderList;
	}
}
