
public class Item extends GameObject {
	/*
	 * An item can be collected by a player to increase their bomb limmit, explosion size or time. They are randomly created when the player destroy things.
	 */
	public static final int BOMB = 1;
	public static final int EXPLOSION = 2;
	public static final int TIME = 3;
			
	
	private int type;
	/**
	 * Creates an item of the specified type at the specified location.
	 * @param x the horizontal coordinate of the item
	 * @param y the vertical coordinate of the item
	 * @param type the type of item
	 */
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
