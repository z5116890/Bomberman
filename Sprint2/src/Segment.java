import java.util.Random;
import java.util.ArrayList;
public class Segment {
	public static final int UP = 1;
	public static final int DOWN = -UP;
	public static final int RIGHT = 2;
	public static final int LEFT = -RIGHT;
	//map generation stuff, each of these numbers are 3 letter words to improve readability
	private static final int EMP = GameManager.EMPTY;
	private static final int WAL = GameManager.WALL;
	private static final int PLR = GameManager.PLAYER;
	private static final int END = GameManager.ENDZONE;
	private static final int BWL = GameManager.BREAKABLE_WALL;
	private static final int ENM = GameManager.ENEMY;
	private static final int AWL = -1; //Unbreakable or Breakable wall or Empty
	private static final int SWL = -2; //Unbreakable or Breakable wall
	private static final int BWE = -3; //Breakable wall or Empty
	private static final int ANY = -4; //Breakable or Unbreakable wall or Enemy or Empty
	private static final int BOE = -5; //Box or Endzone
	/*
	 * This is an empty segment
	 		{
				{EMP, EMP, EMP, EMP, EMP, EMP},
				{EMP, EMP, EMP, EMP, EMP, EMP},
				{EMP, EMP, EMP, EMP, EMP, EMP},
				{EMP, EMP, EMP, EMP, EMP, EMP},
				{EMP, EMP, EMP, EMP, EMP, EMP},
				{EMP, EMP, EMP, EMP, EMP, EMP}
			}
	 */
	// the map is broken down into 3x3 partitions with each cell being a 6x6 cell square
	/*
	private static final int[][] startSection = {
			{EMP, EMP, EMP, BWL, EMP, WAL},
			{EMP, PLR, WAL, EMP, EMP, WAL},
			{EMP, WAL, WAL, EMP, EMP, EMP},
			{BWL, EMP, EMP, ENM, EMP, EMP},
			{EMP, EMP, EMP, EMP, SWL, AWL},
			{WAL, WAL, EMP, EMP, AWL, WAL}
		};*/
	private static final int[][] bendSection = {
			{WAL, BWE, EMP, BWE, WAL, ANY},
			{BWL, ENM, BWE, BWE, ANY, ANY},
			{EMP, BWE, EMP, EMP, BWE, EMP},
			{BWE, BWE, EMP, BOE, EMP, BWE},
			{SWL, AWL, EMP, EMP, EMP, WAL},
			{AWL, AWL, BWL, BWL, WAL, WAL}
		};
	private static final int[][] straightSection = {
			{EMP, EMP, BWE, EMP, SWL, EMP},
			{EMP, BOE, BWE, BWE, SWL, WAL},
			{EMP, BWL, EMP, EMP, BWL, EMP},
			{BWE, WAL, EMP, BWE, BWE, BWE},
			{ENM, BWE, BWE, EMP, WAL, ANY},
			{ANY, AWL, BWE, EMP, ANY, EMP}
		};
	private static final int[][] crossSection = {
			{BWE, EMP, BWL, EMP, WAL, ANY},
			{EMP, BOE, EMP, BWE, WAL, ANY},
			{BWE, EMP, BWE, EMP, BWE, EMP},
			{EMP, BWL, EMP, BWE, EMP, BWL},
			{EMP, WAL, BWE, EMP, ENM, AWL},
			{ANY, WAL, EMP, BWE, BWE, SWL}
		};
	private static final int[][] twaySection = {
			{SWL, BWL, BWE, BWE, SWL, SWL},
			{ANY, BWL, ANY, EMP, EMP, AWL},
			{BWE, BWE, EMP, BOE, BWE, BWE},
			{EMP, BWE, BWE, EMP, EMP, BWE},
			{WAL, ENM, EMP, BWE, ENM, ANY},
			{AWL, ANY, EMP, BWE, BWL, SWL}
		};
	private static final int[][] endSection = {
			{SWL, WAL, EMP, EMP, WAL, SWL},
			{SWL, ENM, EMP, EMP, ENM, SWL},
			{BWE, EMP, BWE, BWE, BWL, BWE},
			{BWE, ANY, BWE, BOE, EMP, BWE},
			{WAL, ANY, BWL, EMP, EMP, AWL},
			{AWL, SWL, WAL, WAL, SWL, AWL}
		};
	
	private Segment up;
	private boolean linkedUp = false;
	private Segment down;
	private boolean linkedDown = false;
	private Segment left;
	private boolean linkedLeft = false;
	private Segment right;
	private boolean linkedRight = false;
	private int specialType = GameManager.EMPTY;
	
	public Segment(){
	}
	public void setConnections(Segment up, Segment down, Segment left, Segment right){
		this.up = up;
		this.down = down;
		this.left = left;
		this.right = right;
	}
	public void link(int direction){
		switch(direction){
		case UP:
			linkedUp = true;
			break;
		case DOWN:
			linkedDown = true;
			break;
		case LEFT:
			linkedLeft = true;
			break;
		case RIGHT:
			linkedRight = true;
			break;
		}
	}
	public void randomLink(){
		Random rand = new Random();
		int dir = 1 + rand.nextInt(2);
		if(rand.nextInt(2) == 0)dir *= -1;
		switch(dir){
			case UP:
				if(up == null)return;
				up.link(-dir);
				linkedUp = true;
				break;
			case DOWN:
				if(down == null)return;
				down.link(-dir);
				linkedDown = true;
				break;
			case LEFT:
				if(left == null)return;
				left.link(-dir);
				linkedLeft = true;
				break;
			case RIGHT:
				if(right == null)return;
				right.link(-dir);
				linkedRight = true;
				break;
		}
	}
	public void setSpecialType(int type){
		specialType = type;
	}
	public ArrayList<Segment> getLinkedSegments(){
		ArrayList<Segment> links = new ArrayList<Segment>();
		if(linkedUp)links.add(up);
		if(linkedDown)links.add(down);
		if(linkedLeft)links.add(left);
		if(linkedRight)links.add(right);
		return links;
	}
	public int[][] getMapArray(){
		int[][] output = null;
		Random rand = new Random();
		if(linkedUp && linkedDown && linkedLeft && linkedRight){
			output = convertToObjectCodes(crossSection);
			boolean flipX = rand.nextBoolean();
			boolean flipY = rand.nextBoolean();
			if(flipX||flipY)output = mirror(output,flipX,flipY);
			if(rand.nextBoolean())rotate(output);
			return output;
		}else if(linkedUp && linkedDown){
			output = convertToObjectCodes(straightSection);
			boolean flipX = rand.nextBoolean();
			if(flipX)output = mirror(output,true,false);
			return output;
		}else if(linkedLeft && linkedRight){
			output = convertToObjectCodes(straightSection);
			boolean flipX = rand.nextBoolean();
			if(flipX)output = mirror(output,true,false);
			output = rotate(output);
			return output;
		}else if(linkedLeft && linkedDown && linkedRight){
			output = convertToObjectCodes(twaySection);
			boolean flipX = rand.nextBoolean();
			if(flipX)output = mirror(output,true,false);
			return output;
		}else if(linkedLeft && linkedUp && linkedRight){
			output = convertToObjectCodes(twaySection);
			boolean flipX = rand.nextBoolean();
			output = mirror(output,flipX,true);
			return output;
		}else if(linkedUp && linkedDown && linkedRight){
			output = convertToObjectCodes(twaySection);
			boolean flipX = rand.nextBoolean();
			output = mirror(output,flipX,true);
			output = rotate(output);
			return output;
		}else if(linkedLeft && linkedDown && linkedUp){
			output = convertToObjectCodes(twaySection);
			boolean flipX = rand.nextBoolean();
			if(flipX)output = mirror(output,true,false);
			output = rotate(output);
			return output;
		}else if(linkedLeft && linkedUp){
			output = convertToObjectCodes(bendSection);
			return output;
		}else if(linkedRight && linkedUp){
			output = convertToObjectCodes(bendSection);
			output = mirror(output,true,false);
			return output;
		}else if(linkedLeft && linkedDown){
			output = convertToObjectCodes(bendSection);
			output = mirror(output,false,true);
			return output;
		}else if(linkedRight && linkedDown){
			output = convertToObjectCodes(bendSection);
			output = mirror(output,true,true);
			return output;
		}else if(linkedUp){
			output = convertToObjectCodes(endSection);
			boolean flipX = rand.nextBoolean();
			if(flipX)output = mirror(output,true,false);
			return output;
		}else if(linkedDown){
			output = convertToObjectCodes(endSection);
			boolean flipX = rand.nextBoolean();
			output = mirror(output,flipX,true);
			return output;
		}else if(linkedRight){
			output = convertToObjectCodes(endSection);
			boolean flipX = rand.nextBoolean();
			if(flipX)output = mirror(output,true,false);
			output = rotate(output);
			return output;
		}else if(linkedLeft){
			output = convertToObjectCodes(endSection);
			boolean flipX = rand.nextBoolean();
			output = mirror(output,flipX,true);
			output = rotate(output);
			return output;
		}
		return null;
	}
	/*
	public int[][] getStartArray(){
		return convertToObjectCodes(startSection);
	}
	*/
	private int[][] convertToObjectCodes(int[][] map){
		int[][] output = new int[6][6];
		Random rand = new Random();
		int difficulty = GameManager.getGameManager().getDifficulty();
		for(int x = 0; x < 6; x++){
			for(int y = 0; y < 6; y++){
				switch(map[y][x]){
				case EMP:
					output[y][x] = GameManager.EMPTY;
					break;
				case WAL:
					output[y][x] = GameManager.WALL;
					break;
				case PLR:
					output[y][x] = GameManager.PLAYER;
					break;
				case END:
					output[y][x] = GameManager.ENDZONE;
					break;
				case BWL:
					output[y][x] = GameManager.BREAKABLE_WALL;
					break;
				case ENM:
					output[y][x] = GameManager.ENEMY;
					break;
				case AWL:
					int value = GameManager.EMPTY;
					float empty = 0.5f;
					float breakable = 0.8f;
					float unbreakable = 1f;
					if(difficulty == 1){
						empty = 0.8f;
						breakable = 1f;
						unbreakable = 0f;
					}else if(difficulty == 3){
						empty = 0.2f;
						breakable = 0.5f;
						unbreakable = 1f;
					}
					float r = rand.nextFloat();
					if(r<=empty)value = GameManager.EMPTY;
					else if(r<=breakable)value = GameManager.BREAKABLE_WALL;
					else if(r<=unbreakable)value = GameManager.WALL;
					output[y][x] = value;
					break;
				case SWL:
					value = GameManager.EMPTY;
					
					breakable = 0.5f;
					unbreakable = 1f;
					if(difficulty == 1){
						breakable = 0.8f;
						unbreakable = 1f;
					}else if(difficulty == 3){
						breakable = 0.2f;
						unbreakable = 1f;
					}
					r = rand.nextFloat();
					if(r<=breakable)value = GameManager.BREAKABLE_WALL;
					else if(r<=unbreakable)value = GameManager.WALL;
					output[y][x] = value;
					break;
				case BWE:
					value = GameManager.EMPTY;
					breakable = 0.5f;
					empty = 1f;
					if(difficulty == 1){
						breakable = 0.4f;
						empty = 1f;
					}else if(difficulty == 3){
						breakable = 0.7f;
						empty = 1f;
					}
					r = rand.nextFloat();
					if(r<=breakable)value = GameManager.BREAKABLE_WALL;
					else if(r<=empty)value = GameManager.EMPTY;
					output[y][x] = value;
					break;
				case ANY:
					value = GameManager.EMPTY;
					
					breakable = 0.4f;
					unbreakable = 0.7f;
					empty = 0.9f;
					float enemy = 1f;
					if(difficulty == 1){
						breakable = 0.3f;
						unbreakable = 0.5f;
						empty = 1f;
						enemy = 0f;
					}else if(difficulty == 3){
						breakable = 0.2f;
						unbreakable = 0.5f;
						empty = 0.6f;
						enemy = 1f;
					}
					r = rand.nextFloat();
					if(r<=breakable)value = GameManager.BREAKABLE_WALL;
					else if(r<=unbreakable)value = GameManager.WALL;
					else if(r<=empty)value = GameManager.EMPTY;
					else if(r<=enemy)value = GameManager.ENEMY;
					output[y][x] = value;
					break;
				case BOE:
					output[y][x] = specialType;
					break;
				
				}
			}
		}
		return output;
	}
	private int[][] mirror(int[][] map,boolean xFlip,boolean yFlip){
		if(!xFlip && !yFlip)return map;
		int l = map.length;
		int[][] output = new int[l][l];
		
		for(int y = 0; y < l; y++){
			for(int x = 0; x< l; x++){
				output[y][x] = map[yFlip?l-1-y:y][xFlip?l-1-x:x];
			}
		}
		
		return output;
	}
	private int[][] rotate(int[][] map){
		int l = map.length;
		int[][] output = new int[l][l];
		for(int x = 0; x < l; x++){
			for(int y = 0; y < l; y++){
				output[x][y] = map[y][x];
			}
		}
		return output;
	}
	
}
