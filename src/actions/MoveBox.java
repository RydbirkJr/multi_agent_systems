package actions;

import core.Agent;
import core.Box;
import map.Level;
import map.Square;

import java.sql.SQLClientInfoException;

public class MoveBox extends Action{

	final Box box;
	public final Square square, moveBackTo;

	
	public MoveBox(Action parent, Box box, Square square){
		super(parent);
		this.box = box;
		this.square = square;
		moveBackTo = null;
	}

	public MoveBox(Action parent, Box box, Square square, Square moveBackTo){
		super(parent);
		this.box = box;
		this.square = square;
		this.moveBackTo = moveBackTo;
	}

	public Box getBox() {
		return box;
	}

	public Square getSquare() {
		return square;
	}

	public String toString(){
		return "MoveBox(agent:" + this.getPreState().getAgent().toString() + ", box:" + box.toString() + ", square:" + square.toString() + ")";
	}
	
}
