package DodgeBall;

import java.awt.Color;
import java.awt.Point;

public class MovingEntity extends Entity{

    public int direction;
    public Point aheadPosition;
    public Point nextPosition;

    public MovingEntity(Point point, Color color, int direction){
        super(point, color);
        this.direction = direction;
        this.nextPosition = new Point(point.x, point.y);
    }

    /* Position ahead */
	protected Point aheadPosition(int step) {
		Point newpoint = new Point(currentPosition.x,currentPosition.y);
		switch(direction) {
			case 0: newpoint.y += step; break;
			case 90: newpoint.x += step; break;
			case 180: newpoint.y -= step; break;
			case 270: newpoint.x -= step; break;
			default: return null;
		}
		return newpoint;
	}

    public void moveAhead() {
		this.nextPosition = new Point(aheadPosition.x, aheadPosition.y);
	}

    public void updatePosition(){
		if(this.currentPosition.equals(this.nextPosition)) 
			return;
        this.currentPosition = Utils.copyPoint(nextPosition);
    }

	
}