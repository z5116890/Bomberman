import java.util.ArrayList;

public class Player extends GameObject{
	
	public static final int BOMB = 5;
	private int nextAction = 0;
	private int bombCount = 1;
	private int placedBombs = 0;
	private int explosionSize = 1;
	private int deathTimer = 3;
	private boolean dead = false;
	public Player(int x, int y) {
		super("Player_small.png", x, y);
		speed = 4;
	}
	/**
	 * Whenever the player wants to move this method will check if something's in the way, if something is in the way then it will try to push it,
	 * if it can push the object or there's an empty space, the player will move to the desired location.
	 * @param direction 
	 * @return This function returns true if the player is moved, and false if it isn't
	 */
	public boolean interact(int direction){
		//see if anything is in the way ie box, wall
		if(direction == DOWN){
			GameObject obj = GameManager.getGameManager().getObjectAtLocation(gridX, gridY + 1);
			if(obj == null || obj.interact(direction) || obj instanceof Enemy){
				this.gridY++;
				return true;
			}
		}else if(direction == UP){
			GameObject obj = GameManager.getGameManager().getObjectAtLocation(gridX, gridY - 1);
			if(obj == null || obj.interact(direction) || obj instanceof Enemy){
				this.gridY--;
				return true;
			}
		}else if(direction == LEFT){
			GameObject obj = GameManager.getGameManager().getObjectAtLocation(gridX - 1, gridY);
			if(obj == null || obj.interact(direction) || obj instanceof Enemy){
				this.gridX--;
				return true;
			}
		}else if(direction == RIGHT){
			GameObject obj = GameManager.getGameManager().getObjectAtLocation(gridX + 1, gridY);
			if(obj == null || obj.interact(direction) || obj instanceof Enemy){
				this.gridX++;
				return true;
			}
		}
				//object exists in that location so return false
		return false;
	}
	@Override
	
	/**
	 * The frame-by-frame actions of the Player is are done in act()
	 * The Player first checks if its touching any items or explosions and acts accordingly, 
	 * then if the player decides to move or place a bomb, the action will be done.
	 */
	public void act(){
		if(dead){
			if(deathTimer--<0)die();
		}
		ArrayList<GameObject> objects = GameManager.getGameManager().getObjectsAtLocation(gridX, gridY);
		for(GameObject obj:objects){
			if(obj instanceof Explosion || obj instanceof Enemy){
				dead = true;
			}else if(obj instanceof Item){
				switch(((Item)obj).getType()){
				case Item.BOMB:
					bombCount++;
					break;
				case Item.EXPLOSION:
					explosionSize++;
					break;
				case Item.TIME:
					GameManager.getGameManager().delay();
					break;
				}
				GameManager.getGameManager().removeObject(obj);
			}
		} 
		if(nextAction != 0 && !dead){
			switch(nextAction){
			case UP:case DOWN:case LEFT:case RIGHT:
				interact(nextAction);
				break;
			case BOMB:
				placeBomb();
				break;
			}
			nextAction = 0;
		}
		super.act();
	}
	/**
	 * Upon death the player tells the GameManager to reset the map.
	 */
	private void die(){
		GameManager.getGameManager().reset();
	}
	/**
	 * The GameManager will call this method whenever the player presses a key to move or place a bomb, This method decides
	 * if it can do the action, and then sets that as the next action to be done in the next act()
	 * @param action the action the Player wants to do. 
	 */
	public void setAction(int action){
		switch(action){
		case UP:case DOWN: case LEFT: case RIGHT:
			int xDisp = realX - gridX*GameManager.CELL_SIZE;
			int yDisp = realY - gridY*GameManager.CELL_SIZE;
			if( (xDisp<0?-xDisp:xDisp)<= speed && (yDisp<0?-yDisp:yDisp) <= speed)
			nextAction = action;
			break;
		case BOMB:
			if(placedBombs < bombCount){
				nextAction = action;
			}
			break;
		}
	}
	public int getBombCount(){
		return bombCount;
	}
	public int getExplosionSize(){
		return explosionSize;
	}
	public void decreasePlacedBombs(){
		placedBombs--;
	}
	/**
	 * If there isn't a bomb in the player's current position it will place a bomb
	 */
	private void placeBomb(){
		for(GameObject obj:GameManager.getGameManager().getObjectsAtLocation(gridX, gridY)){
			if(obj instanceof Bomb)return;
		}
		Bomb bomb = new Bomb(gridX,gridY,this);
		GameManager.getGameManager().addObject(bomb);
		placedBombs++;
	}
	
	/**
	 * @return the amount of remaining bombs
	 */
	public int bombsLeft(){
		return (bombCount - placedBombs);
	}
	
}
