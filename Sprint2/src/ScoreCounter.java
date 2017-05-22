import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreCounter {
    
	private boolean inProgress = false;
	private long endScore = 0;
    private long startTime = 0;
    private long maxTime = 0;
    private long endTime = 0;
	
	public ScoreCounter (long maxTime) {
        this.maxTime = maxTime;
    }
    
    public void startScoreCounter(){
    	this.startTime = System.nanoTime();
    	this.inProgress = true;
    }
    
    public void endScoreCounter() {
    	this.endTime = this.startTime - System.nanoTime();
    	this.endScore = this.maxTime - this.endTime;
    	this.inProgress = false;
    }
    //get the score from here - may need to mess around with the scale 
    public long getScore(){
    	if(this.inProgress) {
    		return this.maxTime - (this.startTime - System.nanoTime());
    	} else {
    		return this.endScore;
    	}
    }
    
    public String getScoreCounter() {
    	long time;
    	if(this.inProgress) {
    	    time = this.startTime - System.nanoTime();
    	} else {
    		time = this.endTime;
    	}
    	SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
    	return formatter.format(new Date(time/1000000L));
    } 
    
}
