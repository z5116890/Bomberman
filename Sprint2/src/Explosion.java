import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Explosion extends GameObject{
	public static final int DURATION = 32;
	private BufferedImage[] images = new BufferedImage[4];
	int timer = 0;
	public Explosion(int x,int y,int dir,boolean end){
		super(x,y);
		String type = "";
		switch(dir){
		case UP:
			type = "U";
			break;
		case DOWN:
			type = "D";
			break;
		case LEFT:
			type = "L";
			break;
		case RIGHT:
			type = "R";
			break;
		default:
			type = "C";	
			break;
		}
		if(end && dir != 0)type += "E";
		type +="_";
		try{
			for(int i = 0;i < 4; i++){
            images[i] = ImageIO.read(new File("Explosion_"+type+i+".png"));
			}
        }catch(IOException e){
            e.printStackTrace();
        }
	}
	public void act(){
		super.act();
		ArrayList<GameObject> objects = GameManager.getGameManager().getObjectsAtLocation(gridX, gridY);
		for(GameObject obj:objects){
			if(obj instanceof Box){
				GameManager.getGameManager().removeObject(this);
			}
		}
		if(++timer > DURATION){
			GameManager.getGameManager().removeObject(this);
		}
	}
	@Override
	public boolean interact(int direction){
		return true;
	}
	@Override
	public BufferedImage getImage(){
		int t = (timer%4);
		if(timer%8 >= 4)t = 3 - t;
		return images[t];
	}
}
