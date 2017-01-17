package planning;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import core.Agent;
import core.Box;
import core.State;
import enums.Color;
import map.Edge;
import map.GoalField;
import map.Square;

public class EdgeGoal extends Goal {

	public final Edge edge;
	private final List<Box> boxes;
	public EdgeGoal(Set<Square> resources, Edge edge, List<Box> boxes) {
		super(resources);
		this.edge = edge;
		this.boxes = boxes;
	}

	@Override
	public boolean isCompleted(State state) {
		
		Set<Square> resources = edge.getResources();
		
		return getBoxes(state).isEmpty();
		//return !boxes.stream()
		//.map(b-> state.getBox(b.getID() ))
		//.map(b -> state.getLevel().getSquare(b.row, b.col))
		//.anyMatch(sq -> resources.contains(sq));
	}

	@Override
	public int estimate(State state, int weight) {

		Agent agent = state.getAgent();

		//If there is no MinDistance, we cannot reach and false is returned
		for(Square square : edge.edges.values()){
			return square.getDistance(agent.getRow(), agent.getCol()).d * boxes.size();
		}

		return 0;
	}

	@Override
	public Color getColor() {
		// TODO Auto-generated method stub
		return Color.blue;
	}

	@Override
	public boolean preconditions(State state) {
		boolean dependencies = true;
		
		for (Goal g : this.getDependencyList()) {
            dependencies = dependencies && g.isCompleted(state);
        }
		return dependencies;
	}

	@Override
	public State getExpectedState(State state) {
		// TODO Auto-generated method stub
		return state;
	}

	@Override
	public Square getContinueTowardsSquare(State state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "EdgeGoal(Edge: " + edge.getNumber() + "Boxes: " + String.join(", ",boxes.stream().map(b -> b.toString()).collect(Collectors.toList())) + ")";
	}

	@Override
	public int timeToCompletion(State state) {

		return estimate(state, 1);
	}
	
	public int getEdgeNumber()
	{
		return edge.getNumber();
	}
	
	
	public void addBox(Box box)
	{
		if(!boxes.contains(box))
		{
			boxes.add(box);
		}
	}

	@Override
	public boolean canReach(State state) {

		Agent agent = state.getAgent();

		//If there is no MinDistance, we cannot reach and false is returned
		for(Square square : edge.edges.values()){
			return square.getDistance(agent.getRow(), agent.getCol()) != null;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;

		result = result * prime + edge.getNumber();

		for(Box box : boxes){
			result = prime * result + box.hashCode();
		}
		return result;
	}
	
	public List<Box> getBoxes(State state)
	{
		Set<Square> resources = edge.getResources();
		List<Box> newBoxes = new ArrayList<>();
		
		for(Box box : boxes)
		{
			Box tempBox = state.getBox(box.getID());
			Square tempSquare = state.getLevel().getSquare(tempBox.row, tempBox.col);
			if(resources.contains(tempSquare))			
			{
				if(tempSquare instanceof GoalField){
					if(((GoalField) tempSquare).getLetter() == tempBox.getLetter()){
						continue;
					}
				}
				newBoxes.add(tempBox);

			}
		}
		return newBoxes;
	}
	
	public int getNumberOfboxes()
	{
		return boxes.size();
	}

}
