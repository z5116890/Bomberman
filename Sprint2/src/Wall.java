import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

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
			Random rand = new Random();
			switch(GameManager.getGameManager().getDifficulty()){
			case 1:
				if(rand.nextFloat()<=0.25f)GameManager.getGameManager().addObject(new Item(gridX,gridY,1+rand.nextInt(3)));
				break;
			case 2:
				if(rand.nextFloat()<=0.15f)GameManager.getGameManager().addObject(new Item(gridX,gridY,1+rand.nextInt(3)));
				break;
			case 3:
				if(rand.nextFloat()<=0.05f)GameManager.getGameManager().addObject(new Item(gridX,gridY,1+rand.nextInt(3)));
				break;
			}
			GameManager.getGameManager().removeObject(this);
			GameManager.getGameManager().removeObject(this);
		}

	}

}
