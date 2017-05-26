import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class GameObject {

	public static final int UP = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;

	protected BufferedImage image;
	protected int gridX;			//gridX / grid Y hold the grid position of the GameObject
	protected int gridY;			// i.e. where it is *on the way* to moving to (can = current position)
	protected int realX;			//realX / realY is what is seen on the screen, object in motion
	protected int realY;
	protected int speed = 4; //pixels to move per frame. Must be >0


	//Constructor
	public GameObject(String imageName,int x, int y){

		this.gridX = x;
		this.gridY = y;

		//Assuming realX/realY are the pixel positions, of the upper left corner
		this.realX = x*GameManager.CELL_SIZE;
		this.realY = y*GameManager.CELL_SIZE;

		try{
			image = ImageIO.read(new File(imageName));
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public GameObject(int x, int y){

		this.gridX = x;
		this.gridY = y;

		//Assuming realX/realY are the pixel positions, of the upper left corner
		this.realX = x*GameManager.CELL_SIZE;
		this.realY = y*GameManager.CELL_SIZE;
	}
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

	public void setImage(String filename) {

		//if the screen is 640x640px & there are 20x20cells, images should be 32x32px
		try{
			this.image = ImageIO.read(new File(filename));
		}catch(IOException e){
			e.printStackTrace();
		}
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
		if (!GameManager.isPaused()) {
			move();
		}
	}

	protected void move(){

		//this is the displacement in pixels
		//if the object is *left* of grid position, xDisplace is positive (gridX>realX)
		//if the object is *right* of grid position, xDisplace is negative (gridX<realX)
		int xDisplace = (GameManager.CELL_SIZE)*gridX - realX;

		//if xDisplace is zero, the GameObject has finished moving on the x-axis
		//i.e. its realX and gridX are equal, it is not "on the way" anywhere
		//in this case, do nothing
		if (xDisplace != 0)
		{
			//if the object is *left* of grid position, realX must be incremented
			//else if object is *right* of grid position, realX must be decremented
			int pixelsToMove = this.speed;

			//We don't want to "overshoot" the desired destination position
			if (pixelsToMove>Math.abs(xDisplace)){
				pixelsToMove = Math.abs(xDisplace);
			}

			//Move
			if (xDisplace>0){
				this.realX += pixelsToMove;

			} else {
				this.realX -= pixelsToMove;
			}
		}

		//displacement in pixels
		//if the object is *above* the grid position, yDisplace is positive (gridY>realY)
		//if the object is *below* the grid position, yDisplace is negative (gridY<realY)
		int yDisplace = (GameManager.CELL_SIZE)*gridY - realY;

		//if yDisplace is zero, the GameObject has finished moving on the y-axis
		//i.e. its realY and gridY are equal, it is not "on the way" anywhere
		//in this case, do nothing
		if (yDisplace != 0)
		{
			//if the object is *above* the grid position, realY must be incremented
			//else if object is *below* the grid position, realY must be decremented
			int pixelsToMove = this.speed;

			//We don't want to "overshoot" the desired destination position
			if (pixelsToMove>Math.abs(yDisplace)){
				pixelsToMove = Math.abs(yDisplace);
			}

			//Move
			if (yDisplace>0){
				this.realY += pixelsToMove;

			} else {
				this.realY -= pixelsToMove;
			}
		}
	}

}