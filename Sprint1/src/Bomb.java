import java.util.ArrayList;

public class Bomb extends GameObject{
	private int timer = 120;
	private boolean detonated = false;
	private Player owner = null;
	public Bomb(int x, int y, Player owner) {
		super("bomb_small_alt.png", x, y);
		this.owner = owner;
	}
	
	public void act(){
		super.act();
		if(--timer<=0){
			explode();
			GameManager.getGameManager().removeObject(this);
		}
	}
	public void explode(){
		if(detonated)return;
		detonated = true;
		int size = owner.getExplosionSize();
		placeExplosion(gridX,gridY,0,0);
		placeExplosion(gridX,gridY-1,UP,size-1);
		placeExplosion(gridX,gridY+1,DOWN,size-1);
		placeExplosion(gridX-1,gridY,LEFT,size-1);
		placeExplosion(gridX+1,gridY,RIGHT,size-1);
		owner.decreasePlacedBombs();
		GameManager.getGameManager().removeObject(this);
	}
	public void explode(int dir){
		if(detonated)return;
		detonated = true;
		int size = owner.getExplosionSize();
		placeExplosion(gridX,gridY,0,0);
		if(dir != DOWN)placeExplosion(gridX,gridY-1,UP,size-1);
		if(dir != UP)placeExplosion(gridX,gridY+1,DOWN,size-1);
		if(dir != RIGHT)placeExplosion(gridX-1,gridY,LEFT,size-1);
		if(dir != LEFT)placeExplosion(gridX+1,gridY,RIGHT,size-1);
		owner.decreasePlacedBombs();
		GameManager.getGameManager().removeObject(this);
	}
	public boolean placeExplosion(int x,int y,int dir,int size){
		boolean placeExplosion = true;
		ArrayList<GameObject> objects = GameManager.getGameManager().getObjectsAtLocation(x,y);
		for(GameObject obj : objects){
			if(obj instanceof Wall){
				if(((Wall)obj).isDestructable()){
					((Wall)obj).destroy();
					//boolean end = (dir != 0 && size == 0);
					Explosion explosion = new Explosion(x,y,dir,true);
					GameManager.getGameManager().addObject(explosion);
					return true;
				}else return false;	
			}
			else if(obj instanceof Box){
				return false;
			}
			else if(obj instanceof Bomb && dir != 0){
				((Bomb)obj).explode(dir);
				return true;
			}else if(obj instanceof Explosion){
				placeExplosion = false;
			}
		}
		boolean continueExplosion = false;
		if(size > 0){
			switch(dir){
				case UP:
					continueExplosion = placeExplosion(x,y-1,UP,size-1);
					break;
				case DOWN:
					continueExplosion = placeExplosion(x,y+1,DOWN,size-1);
					break;
				case LEFT:
					continueExplosion = placeExplosion(x-1,y,LEFT,size-1);
					break;
				case RIGHT:
					continueExplosion = placeExplosion(x+1,y,RIGHT,size-1);
					break;
			}
		}
		if(!placeExplosion)return true;
		boolean end = (dir != 0 && !continueExplosion);
		Explosion explosion = new Explosion(x,y,dir,end);
		GameManager.getGameManager().addObject(explosion);
		return true;
	}

}
