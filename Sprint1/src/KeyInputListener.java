import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyInputListener implements KeyListener {

	@Override
	public void keyPressed(KeyEvent arg0) {
		//System.out.println("Waffles!!! "+arg0.getKeyChar());
		GameManager.getGameManager().keyPressed(arg0.getKeyCode());
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		//System.out.println("Pancakes!!! "+arg0.getKeyChar());
		GameManager.getGameManager().keyPressed(arg0.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		//System.out.println("Potatoes!!! "+arg0.getKeyChar());
		GameManager.getGameManager().keyPressed(arg0.getKeyCode());
	}

}
