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
	protected float speed = 0.1f;


	//No constructor for GameObject?
//	//Constructor
//	public GameObject(int x, int y){
//
//		this.gridX = x;
//		this.gridY = y;
//
//		//Assuming realX/realY are the pixel positions, of the upper left corner
//		this.realX = x*GameManager.CELL_SIZE;
//		this.realY = y*GameManager.CELL_SIZE;
//	}


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

		//if the screen is 640x640px, and there are 20x20cells, the images should be 32x32px I'm guessing

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

	//this is "be pushed" in the case of boxes etc, false unless overridden in subclass?
	public boolean interact(int direction){
		return false;
	}

	public void act(){
		move();
	}

	protected void move(){



	}

}
