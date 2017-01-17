package actions;

import map.Square;

import java.util.Set;

public class ValidatePath extends Action {
	
	final Square fromSquare, toSquare;
	private Set<Square> resources;
    private Set<Square> prevResources;
	
	public ValidatePath(Action parent, Square fromSquare, Square toSquare) {
		super(parent);
		this.fromSquare = fromSquare;
		this.toSquare = toSquare;
	}

    public ValidatePath(Action parent, Square fromSquare, Square toSquare, Set<Square> prevResources){
        this(parent, fromSquare, toSquare);
        this.prevResources = prevResources;
    }

	public Square getFromSquare() {
		return fromSquare;
	}

	public Square getToSquare() {
		return toSquare;
	}

	public void setResources(Set<Square> resources){
		this.resources = resources;
	}

	public Set<Square> getResources(){
		return this.resources;
	}

    public Set<Square> getPrevResources(){
        return this.prevResources;
    }

	public String toString(){
		return "ValidatePath(HasParent:"+ isFirstChild +", agent:" + this.getPreState().getAgent().toString() + ", from:" + fromSquare.toString() + ", to:" + toSquare.toString() + ")";
	}

}
