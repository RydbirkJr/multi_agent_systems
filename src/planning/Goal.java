package planning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.State;
import enums.Color;
import map.Square;

public abstract class Goal {
	
	protected Set<Square> resources;
	protected final List<Goal> dependencyList;
	
	Goal(Set<Square> resources){
		setResources(resources);
		this.dependencyList = new ArrayList<Goal>();
	}

	protected void setResources(Set<Square> resources){
		this.resources = resources == null ? new HashSet<>() : resources;
	}
	
	public List<Goal> getDependencyList(){
		return dependencyList; 
	}
	
	public boolean checkResources(Square square){

        return this.resources.contains((square));
    }
	
    public void addDependencyTo(Goal goalPriority){
        this.dependencyList.add((goalPriority));
    }

	public void relaxDependency(Goal goal){
		this.dependencyList.remove(goal);
	}

	public abstract boolean canReach(State state);
	public abstract boolean isCompleted(State state);
	public abstract int estimate(State state, int weight);
	public abstract Color getColor();
	public abstract boolean preconditions(State state);
	public abstract State getExpectedState(State state);
	public abstract Square getContinueTowardsSquare(State state);
	public abstract String toString();
	public abstract int timeToCompletion(State state);

	@Override
	public abstract int hashCode();
}
