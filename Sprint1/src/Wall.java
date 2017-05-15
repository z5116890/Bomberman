import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Wall extends GameObject{

    public Wall (int x, int y){
    	super("Wall.png",x,y);
    	/*
        this.gridX = x;
        this.gridY = y;
        this.realX = x*GameManager.CELL_SIZE;
        this.realY = y*GameManager.CELL_SIZE;

        //add image
        BufferedImage img = null;
        try{
            //this image is already sized to 32x32px, i.e. CELL_SIZE*CELL_SIZE
            img = ImageIO.read(new File("wall_dummy.jpg"));
        }catch(IOException e){
            e.printStackTrace();
        }

        this.image = img;

        //System.out.println("making a wall");
       */
    }

}
