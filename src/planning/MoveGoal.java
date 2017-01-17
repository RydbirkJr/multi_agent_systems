package planning;

import core.Agent;
import core.Box;
import core.Logger;
import core.State;
import enums.Color;
import map.GoalField;
import map.MinDistance;
import map.Square;

import java.util.Set;

/**
 * Created by Anders on 20/03/16.
 */
public class MoveGoal extends Goal {
    private final GoalField goal;
    private Box box;

    public MoveGoal(GoalField goal, Set<Square> resources, Box box) {
        super(resources);
        this.goal = goal;
        this.box = box;
    }

    public boolean checkResources(Square square) {

        return this.resources.contains((square));
    }

    @Override
    public boolean canReach(State state) {
        return this.goal.getDistance(state.getAgent().getRow(), state.getAgent().getCol()) != null;
    }

    public int estimate(State state, int weight) {
        Box curBox = state.getBox(box.getID());
        Agent agent = state.getAgent();
        Square boxSquare = state.getLevel().getSquare(curBox.row, curBox.col);
        MinDistance boxToAgent = boxSquare.getDistance(agent.getRow(), agent.getCol());
        MinDistance boxToGoal = goal.getDistance(curBox.row, curBox.col);

        int distance = Integer.MAX_VALUE;

        if (boxToAgent != null && boxToGoal != null) {
            distance = weight * boxToAgent.d + boxToGoal.d + dependencyList.size() * weight;
        }

//        Logger.global(curBox + "\t: estimate: " + distance + "\t" + agent.toString());

        return distance;
    }

    @Override
    public Color getColor() {
        return goal.getColor();
    }

    @Override
    public boolean preconditions(State state) {
        Agent agent = state.getAgent();
        boolean letter = goal.getLetter() == getBox().getLetter();
        boolean colour = getBox().getColor() == agent.getColor();
        boolean dependencies = true;
        boolean canReachGoal = goal.getDistance(agent.getRow(), agent.getCol()) != null;

        for (Goal g : this.getDependencyList()) {
            dependencies = dependencies && g.isCompleted(state);
        }

        return letter && colour && dependencies && canReachGoal;
    }

    @Override
    public State getExpectedState(State state) {

        GoalField prevGoal = getGoalField();
        Agent prevAgent = state.getAgent();
        Agent newAgent = new Agent(prevGoal.row, prevGoal.col, prevAgent.getNumber(), prevAgent.getColor());
        return new State(state, newAgent, box, prevGoal, estimate(state, 1));
    }

    @Override
    public Square getContinueTowardsSquare(State state) {
        return state.getLevel().getSquare(this.box.row, this.box.col);
    }

    @Override
    public String toString() {
        return "MoveGoal(GoalField: " + goal + ", box: " + box + ")";
    }

    @Override
    public int timeToCompletion(State state) {
        if(isCompleted(state)) return 0;

        int maxTime = 0;

        Box temp = state.getBox(box.row, box.col);

        if (!(temp == null || !temp.equals(box))) {

            int res = state.getTimeToBoxMove(box.row, box.col);

            if (res == -1) {
                return Integer.MAX_VALUE;
            }

            maxTime = res;
        }

        return state.time - maxTime;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = result * prime + goal.hashCode();
        return result;
    }

    public GoalField getGoalField() {
        return this.goal;
    }

    public Box getBox() {
        return box;
    }
    
    public void setBox(Box box)
    {
    	this.box = box;
    }
    
    public char getLetter()
    {
    	return box.getLetter();
    }

    public boolean isCompleted(State state) {

        Box box = state.getBox(goal.row, goal.col);
        if (box != null) {
            Logger.global("Checking if " + goal.toString() + " is completed for box: " + box + " : " + (box.getLetter() == goal.getLetter()));
            return box.getLetter() == goal.getLetter();
        }

        return false;
    }

    public void updateBox(State state){
        //update box
        this.box = state.getBox(box.getID());

        //update resources
        this.setResources(state.getLevel().getResources(goal, box.row, box.col));

        //clear dependencies
        this.dependencyList.clear();
    }
}
