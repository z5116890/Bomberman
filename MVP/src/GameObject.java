import java.awt.image.BufferedImage;

public class GameObject {

	public static final int UP = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	public static final float INCREMENT = 1f;

	protected BufferedImage image;
	protected int gridX;
	protected int gridY;
	protected float realX;
	protected float realY;
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
		return (int)this.realX;
	}
	public int getRealY(){
		return (int)this.realY;
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

	public void setRealX(float realX) {
		this.realX = realX;
	}

	public void setRealY(float realY) {
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

		//gridX / grid Y hold the grid position of the GameObject
		// i.e. where it is *on the way* to moving to (if it is stationary, it's moving to where it is)
		//realX / realY is what is seen on the screen, object in motion

		float xDisplace = (float)gridX - realX;

		//if xDisplace is zero, the GameObject has finished moving on the x-axis
		//i.e. its realX and gridX are equal, it is not "on the way" anywhere
		//in this case, do nothing
		if (xDisplace != 0)
		{
			this.realX += Math.abs(xDisplace) > this.speed ? this.speed * Math.signum(xDisplace) : xDisplace;

			//this is a translation of Daniel's C# version
			//I was just thinking: realX += speed*Math.signum(xDisplace); .....
		}

		//if yDisplace is zero, the GameObject has finished moving on the y-axis
		//i.e. its realY and gridY are equal, it is not "on the way" anywhere
		//in this case, do nothing
		float yDisplace = (float)gridY - realY;
		if (yDisplace != 0)
		{
			this.realY += Math.abs(xDisplace) > this.speed ? this.speed * Math.signum(xDisplace) : xDisplace;
		}

		//this is Unity stuff I think
		//transform.position = new Vector3(START + realX, transform.position.y, START + realY);



	}

}
