package core;

import enums.Color;

import java.util.Arrays;

/**
 * Created by Anders on 22/03/16.
 */
public class Box {
    public final int row, col;
    private final char letter;
    private final Color color;
    private final int id;

    public Box(int row, int col, char letter, Color color){
        this.row = row;
        this.col = col;
        this.letter = Character.toLowerCase(letter);
        this.color = color == null ? Color.blue : color;
        this.id = hashCode();
    }

    public Box(Box oldBox, int row, int col){
        this.id = oldBox.id;
        this.letter = oldBox.letter;
        this.color = oldBox.color;
        this.row = row;
        this.col = col;
    }

    public int getID(){
        return id;
    }

    public char getLetter(){
        return this.letter;
    }

	public Color getColor() {
		return color;
	}

    public String toString(){
        return "" + letter + ":(" + row + "," + col + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + row;
        result = prime * result + col;
        result = prime * result + letter;

        return result;
    }

    public boolean equalsID(Box box){

        if(box == null) return false;

        return this.id == box.id;
    }

    /**
     * This compares id, row and col - thus they need to be placed on the same field to be equal.
     * @param obj
     * @return
     */
    @Override
    public boolean equals( Object obj ) {
        if(obj instanceof Box){
            Box other = (Box) obj;
            return other.id == this.id && other.row == this.row && other.col == this.col;
        }
        return false;
    }
}
