
public class Player extends GameObject{
	
	private int posX;
	private int posY;
	
	//initial position of player depends on gameManager setup for map/player generation
	public Player(int x, int y){
		this.setPosX(x);
		this.setPosY(y);
	}
	
	//things that interact with player
	//nothing interacts with player?
	//player cannot walk in direction if something is blocking the way
	//UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
	public boolean interact(int direction){
		//see if anything is in the way ie box, wall
		if(direction == 1){
			if(this.movable(this.posX, this.posY + 1)) return true;
		}else if(direction == 2){
			if(this.movable(this.posX, this.posY - 1)) return true;
		}else if(direction == 3){
			if(this.movable(this.posX - 1, this.posY)) return true;
		}else if(direction == 4){
			if(this.movable(this.posX + 1, this.posY)) return true;
		}
				//object exists in that location so return false
		return false;
	}
	@Override
	
	
	public void act(){
		
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}
	
	public boolean movable(int x, int y){
		if(GameManager.getGameManager().getObjectAtLocation(x, y) != null) return false;
		return true;
	}
}
