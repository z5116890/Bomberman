
public class Item extends GameObject {
	public static final int BOMB = 1;
	public static final int EXPLOSION = 2;
	public static final int TIME = 3;
			
	
	private int type;
	
	public Item(int x, int y, int type) {
		super("Item_"+type+".png",x, y);
		
		this.type = type;
	}
	@Override
	public boolean interact(int direction){
		return true;
	}
	public int getType(){
		return type;
	}
}
