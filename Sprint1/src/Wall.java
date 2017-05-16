import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Wall extends GameObject{
	private boolean destructable;
	
	
    public Wall (int x, int y){
    	
    	super("Wall.png",x,y);
    	
    }


	public boolean isDestructable() {
		return destructable;
	}


	public void setDestructable(boolean destructable) {
		this.destructable = destructable;
	}
	
	public void destroy(){
		//if bomb explosion radius includes wall position then destroy wall
		if(this.destructable == true){
			//if(explosion includes wall position then destroy wall){
			
			//}
		}
		
	}

}
