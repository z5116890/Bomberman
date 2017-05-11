
public class Player extends GameObject{
	

	
	//things that interact with player
	//nothing interacts with player?
	//player cannot walk in direction if something is blocking the way
	//UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
	public boolean interact(int direction){
		//see if anything is in the way ie box, wall
		if(direction == UP){
			if(this.movable(this.gridX, this.gridY + 1)){
				this.gridY++;
				return true;
			}
		}else if(direction == DOWN){
			if(this.movable(this.gridX, this.gridY - 1)){
				this.gridY--;
				return true;
			}
		}else if(direction == LEFT){
			if(this.movable(this.gridX - 1, this.gridY)){
				this.gridX--;
				return true;
			}
		}else if(direction == RIGHT){
			if(this.movable(this.gridX + 1, this.gridY)){
				this.gridX++;
				return true;
			}
		}
				//object exists in that location so return false
		return false;
	}
	@Override
	
	
	public void act(){
		super.act();
	}

	
	public boolean movable(int x, int y){
		if(GameManager.getGameManager().getObjectAtLocation(x, y) != null) return false;
		return true;
	}
}
