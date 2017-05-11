
public class Player extends GameObject{
	private int nextAction = 0;
	public Player(int x, int y) {
		super("Player_small.png", x, y);
		speed = 4;
	}
	//things that interact with player
	//nothing interacts with player?
	//player cannot walk in direction if something is blocking the way
	//UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
	public boolean interact(int direction){
		//see if anything is in the way ie box, wall
		if(direction == DOWN){
			GameObject obj = GameManager.getGameManager().getObjectAtLocation(gridX, gridY + 1);
			if(obj == null || obj.interact(direction)){
				this.gridY++;
				return true;
			}
		}else if(direction == UP){
			GameObject obj = GameManager.getGameManager().getObjectAtLocation(gridX, gridY - 1);
			if(obj == null || obj.interact(direction)){
				this.gridY--;
				return true;
			}
		}else if(direction == LEFT){
			GameObject obj = GameManager.getGameManager().getObjectAtLocation(gridX - 1, gridY);
			if(obj == null || obj.interact(direction)){
				this.gridX--;
				return true;
			}
		}else if(direction == RIGHT){
			GameObject obj = GameManager.getGameManager().getObjectAtLocation(gridX + 1, gridY);
			if(obj == null || obj.interact(direction)){
				this.gridX++;
				return true;
			}
		}
				//object exists in that location so return false
		return false;
	}
	@Override
	
	
	public void act(){
		if(nextAction != 0){
			switch(nextAction){
			case UP:case DOWN:case LEFT:case RIGHT:
				interact(nextAction);
				break;
			}
			nextAction = 0;
		}
		super.act();
	}

	/*
	public boolean movable(int x, int y){
		GameObject obj = GameManager.getGameManager().getObjectAtLocation(x, y);
		//if there is something there
		if(obj != null){
			//if it is not physical than return false
			if(!(obj instanceof EndZone)) return false;
		}
		//else return true
		return true;
	}
	*/
	public void setAction(int action){
		switch(action){
		case UP:case DOWN: case LEFT: case RIGHT:
			int xDisp = realX - gridX*GameManager.CELL_SIZE;
			int yDisp = realY - gridY*GameManager.CELL_SIZE;
			if( (xDisp<0?-xDisp:xDisp)<= speed && (yDisp<0?-yDisp:yDisp) <= speed)
			nextAction = action;
			break;
		}
	}
}
