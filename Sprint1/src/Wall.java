import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Wall extends GameObject{
	private boolean destructable;
	
	
    public Wall (int x, int y/*, boolean destructable*/){
    	
    	super("Wall.png",x,y);
    	//this.destructable = destructable;
    	//different image for destructable wall?
    	
    	
    }


	public boolean isDestructable() {
		return destructable;
	}


	public void setDestructable(boolean destructable) {
		this.destructable = destructable;
	}
	
	public void destroy(){
		//if bomb explosion radius includes wall position then destroy wall
		if(this.destructable){
			//GameManager.getGameManager().removeObject(this);
		}
		
	}

}
