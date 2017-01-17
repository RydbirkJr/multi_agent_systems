package core;

import enums.Color;

/**
 * Created by Anders on 22/03/16.
 */
public class Agent {
    private final int row, col, number;
    private Color color;

    /**
     * If color is null, defaults to blue.
     * @param row
     * @param col
     * @param number
     * @param color
     */
    public Agent(int row, int col, int number, Color color){
        this.row = row;
        this.col = col;
        this.number = number;
        this.color = color == null ? Color.blue : color;
    }

    /**
     * Color IS NULL if Single Agent map. Please notice!
     * @return
     */
    public Color getColor() {
		return color;
	}

    public int getRow()
    {
    	return row;    	
    }
    public int getCol() {
        return col;
    }
    public int getNumber(){
        return number;
    }


    @Override
    public boolean equals( Object obj ) {
        if(obj instanceof Agent){
            Agent other = (Agent) obj;
            return this.row == other.row && this.col == other.col && this.number == other.number && this.color == other.color;
        }
        return false;
    }

    public String toString(){
        return "" + number + ":(" + row + "," + col + ")";
    }
}
