import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Wall extends GameObject{
	private boolean destructible;


	public Wall (int x, int y, boolean destructible, char type){

		super("Wall"+(destructible?"_breakable":"")+".png",x,y);
		this.destructible = destructible;

		if (type == 'R'){
			this.setImage("Wall_right.png");
		}
		if (type == 'C'){
			this.setImage("Wall_middle.png");
		}
		if (type == 'L'){
			this.setImage("Wall_left.png");
		}

	}


	public boolean isDestructable() {
		return destructible;
	}


	public void setDestructable(boolean destructible) {
		this.destructible = destructible;
	}

	public void destroy(){
		//if bomb explosion radius includes wall position then destroy wall
		if(this.destructible){
			GameManager.getGameManager().removeObject(this);
		}

	}

}
