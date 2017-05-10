import java.awt.image.BufferedImage;

public class GameObject {
	public static final int UP = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;

	protected BufferedImage image;
	protected int gridX;
	protected int gridY;
	protected int realX;
	protected int realY;
	protected int speed;

	//Getters

	public int getX(){
		return this.gridX;
	}
	public int getY(){
		return this.gridY;
	}
	public int getRealX(){
		return this.realX;
	}
	public int getRealY(){
		return this.realY;
	}

	public BufferedImage getImage(){
		return this.image;
	}

	//Setters

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public void setGridX(int gridX) {
		this.gridX = gridX;
	}

	public void setGridY(int gridY) {
		this.gridY = gridY;
	}

	public void setRealX(int realX) {
		this.realX = realX;
	}

	public void setRealY(int realY) {
		this.realY = realY;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}


	//Other functions

	public boolean interact(int direction){
		return false;
	}

	public void act(){
		move();
	}

	protected void move(){
		
	}
	
}
