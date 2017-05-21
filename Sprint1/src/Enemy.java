import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

public class Enemy extends GameObject {
	private static final int HARD_TIMER = 30;
	private static final int NORMAL_TIMER = 50;
	private static final int EASY_TIMER = 80;
	private BufferedImage[] images = new BufferedImage[8];
	private int animState = 0;
	private boolean animIncrement = true;
	private int timer = 0;
	private int animCap = 3;
	private int difficulty = 2;
	private int animDelay = 10;
	private int attackTimer = 15;
	private Random rand;
	private int deathTimer = 3;
	private boolean dead = false;
	public Enemy(int x, int y) {
		super(x, y);
		rand = new Random();
		animDelay = rand.nextInt(15);
		try{
			for(int i = 1;i <= 8; i++){
				images[i-1] = ImageIO.read(new File("Enemy_small_"+i+".png"));
			}
        }catch(IOException e){
            e.printStackTrace();
        }
		difficulty = GameManager.getGameManager().getDifficulty();
		switch(difficulty){
		case 1:
			timer = EASY_TIMER;
			break;
		case 2:
			timer = NORMAL_TIMER;
			break;
		case 3:
			timer = HARD_TIMER;
			break;
		}
	}
	public void act(){
		super.act();
		if(dead){
			if(deathTimer--<0)die();
			return;
		}
		ArrayList<GameObject> objects = GameManager.getGameManager().getObjectsAtLocation(gridX, gridY);
		for(GameObject obj:objects){
			if(obj instanceof Explosion){
				dead = true;
			}
		}
		if(timer%3==0)animate();
		timer--;
		if(timer <=0){
			attackTimer--;
			if(attackTimer<=0){
				int prob = 1 + rand.nextInt(100);
				int chance = 25;
				if(GameManager.getGameManager().getDifficulty()==1)chance = 10;
				if(GameManager.getGameManager().getDifficulty()==3)chance = 50;
				if(prob <= chance)attack();
				attackTimer = 3;
			}
			if(rand.nextInt(100)<=10)animCap = 5 + rand.nextInt(3);
			if(attackTimer%2 == 0)animDelay = 25;
			tryMove();
			switch(difficulty){
			case 1:
				timer = EASY_TIMER;
				break;
			case 2:
				timer = NORMAL_TIMER;
				break;
			case 3:
				timer = HARD_TIMER;
				break;
			}
			timer += rand.nextInt(HARD_TIMER);
		}
	}
	private void die(){
		//more will be added in a future sprint
		GameManager.getGameManager().removeObject(this);
	}
	private void attack(){
		GameManager gm = GameManager.getGameManager();
		ArrayList<GameObject> objects = gm.getObjectsAtLocation(gridX, gridY);
		
		for(GameObject obj:objects){
			if(obj instanceof Wall)return;
		}
		int obstructionCount = 0;
		ArrayList<GameObject> neighbours = gm.getObjectsAtLocation(gridX-1,gridY);
		for(GameObject obj:neighbours){
			if(obj instanceof Wall || obj instanceof Bomb || obj instanceof Enemy || obj instanceof Box){
				obstructionCount++;
				break;
			}
		}
		neighbours = gm.getObjectsAtLocation(gridX+1,gridY);
		for(GameObject obj:neighbours){
			if(obj instanceof Wall || obj instanceof Bomb || obj instanceof Enemy || obj instanceof Box){
				obstructionCount++;
				break;
			}
		}
		neighbours = gm.getObjectsAtLocation(gridX,gridY+1);
		for(GameObject obj:neighbours){
			if(obj instanceof Wall || obj instanceof Bomb || obj instanceof Enemy || obj instanceof Box){
				obstructionCount++;
				break;
			}
		}
		neighbours = gm.getObjectsAtLocation(gridX,gridY-1);
		for(GameObject obj:neighbours){
			if(obj instanceof Wall || obj instanceof Bomb || obj instanceof Enemy || obj instanceof Box){
				obstructionCount++;
				break;
			}
		}
		if(1+rand.nextInt(4)<=obstructionCount)return;
		animCap = 7;
		GameManager.getGameManager().addObject(new Wall(gridX,gridY,true));
	}
	private void tryMove(){
		int r = rand.nextInt(4);
		int dx = 0;
		int dy = 0;
		switch(r){
		case 0:
			dy = -1;
			break;
		case 1:
			dy = 1;
			break;
		case 2:
			dx = -1;
			break;
		case 3:
			dx = 1;
			break;
		}
		ArrayList<GameObject> objects = GameManager.getGameManager().getObjectsAtLocation(gridX + dx, gridY + dy);
		for(GameObject obj:objects ){
			if(obj instanceof Wall)return;
			if(obj instanceof Bomb)return;
			if(obj instanceof Enemy)return;
			if(obj instanceof Box)return;
		}
		gridX +=dx;
		gridY += dy;
	}
	private void animate(){
		if(animState==0 && animDelay>0){
			animDelay--;
			return;
		}
		if(animIncrement){
			animState++;
			if(animState >= animCap){
				animIncrement = false;
				animCap = 3;
			}
		}else{
			animState--;
			if(animState <= 0)animIncrement = true;

		}
	}
	public BufferedImage getImage(){
		return images[animState];
	}

}