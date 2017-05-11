
public class Box extends GameObject{
	
	public Box(int x,int y){
		super("Box.png",x,y);

	}
	
	@Override
	public boolean interact(int direction){ //simply check if anything is in next grid spot
		int dx = 0;
		int dy = 0;
		if (direction == UP) { 
			dy = -1;
		} else if (direction == DOWN) {
			dy = 1;
		} else if (direction == LEFT) {
			dx = -1;
		} else if (direction == RIGHT) {
			dx = 1;
		}
		GameObject obj = GameManager.getGameManager().getObjectAtLocation(gridX + dx, gridY + dy);
		if (obj == null || obj instanceof EndZone) {
			gridX += dx;
			gridY += dy;
			return true;
		} else return false;
	}	
		
	@Override
	public void act(){
		super.act();
	}
}
