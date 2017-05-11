
public class EndZone extends GameObject{
	private boolean activated;
	
	public EndZone(int x, int y) {
		super("Endzone.png",x,y);
		activated = false;
	}
	
	@Override
	public void act(){
		if (GameManager.getGameManager().getObjectAtLocation(gridX, gridY) instanceof Box)
		activated = true;
		else activated = false;
	}
	
	@Override
	public boolean interact(int direction){
		return true;
	}
	
	public boolean getActive(){
		return activated;
	}
}
