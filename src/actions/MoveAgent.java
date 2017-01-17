package actions;

import core.Agent;
import map.Level;
import map.Square;

public class MoveAgent extends Action{

	private final Square from, to;
	
	public MoveAgent(Action parent, Square from, Square to){
		super(parent);
		this.from = from;
		this.to = to;
	}

	public Square getFromSquare(){
		return from;
	}

	public Square getToSquare(){
		return to;
	}


	public String toString(){
		return "MoveAgent(from: " + from + ", to: " + to + ")";
	}
}
