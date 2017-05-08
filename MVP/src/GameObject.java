import java.awt.image.BufferedImage;

public class GameObject {
	public static final int UP = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	public boolean interact(int direction){
		return false;
	}
	public BufferedImage getImage(){
		return null;
	}
	public void act(){
		move();
	}
	public int getX(){
		return 0;
	}
	public int getY(){
		return 0;
	}
	public int getRealX(){
		return 0;
	}
	public int getRealY(){
		return 0;
	}
	protected void move(){
		
	}
}
