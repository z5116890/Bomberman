
public class EndZone extends GameObject{
	private boolean activated;
	
	public EndZone() {
		activated = false;
	}
	
	@Override
	public void act(){
		if (GameManager.getGameManager().getObjectAtLocation(gridX, gridY) instanceof Box)
		activated = true;
	}
	
	@Override
	public boolean interact(int direction){
		return true;
	}
	
	public boolean getActive(){
		return activated;
	}
}
