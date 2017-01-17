package planning;

import java.util.Set;

import core.State;
import enums.Color;
import map.Node;
import map.Square;

public class NodeGoal extends Goal {

	public Node node;
	public NodeGoal(Set<Square> resources, Node node) {
		super(resources);
		this.node = node;
	}

	@Override
	public boolean isCompleted(State state) {
		boolean isCompleted = true;
		
		for(Goal goal : dependencyList){
			isCompleted = isCompleted && goal.isCompleted(state);
		}
		
		return isCompleted;
	}

	@Override
	public int estimate(State state, int weight) {

		return 0;
	}

	@Override
	public Color getColor() {
		return null;
	}

	@Override
	public boolean preconditions(State state) {
		return false;
		/*boolean dependencies = true;
		
		for (Goal g : this.getDependencyList()) {
            dependencies = dependencies && g.isCompleted(state);
        }
		return dependencies;*/
	}

	@Override
	public State getExpectedState(State state) {
		return null;
	}

	@Override
	public Square getContinueTowardsSquare(State state) {
		return null;
	}

	@Override
	public String toString() {
		return "NodeGoal(Node: " + node.getNumber() + ")";
	}

	@Override
	public int timeToCompletion(State state) {
		return 0;
	}
	
	public int getNodeNumber()
	{
		return node.getNumber();
	}

	@Override
	public boolean canReach(State state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}
}
