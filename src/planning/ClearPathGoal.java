package planning;

import core.Agent;
import core.Box;
import core.Logger;
import core.State;
import enums.Color;
import map.MinDistance;
import map.Square;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Anders on 12/05/16.
 */
public class ClearPathGoal extends Goal {

    private final List<Box> boxes;
    public final Color color;
    public final Color owner;
    public boolean isCompleted = false;

    public ClearPathGoal(List<Box> boxes, Color owner) {
        super(new HashSet<>());
        this.boxes = boxes;
        this.color = boxes.get(0).getColor();
        this.owner = owner;
    }

    public List<Box> getBoxes(){
        return boxes;
    }

    @Override
    public boolean canReach(State state) {
        if(this.boxes.isEmpty()) return true;
        Box box = this.boxes.get(0);
        Square boxSquare = state.getLevel().getSquare(box.row, box.col);

        return boxSquare.getDistance(state.getAgent().getRow(), state.getAgent().getCol()) != null;
    }

    public void setCompleted(){
        isCompleted = true;
    }

    @Override
    public boolean isCompleted(State state) {
        if(isCompleted) return true;

        boolean isCompleted = true;

        for(Box box : boxes){
            Box temp = state.getBox(box.getID());

//            Logger.global("temp box is: " + temp + " (expected: " + box +")");

            if(temp == null || !temp.equals(box)){
                isCompleted = isCompleted && true;
            } else {
                isCompleted = false;
                break;
            }
        }

        return isCompleted;
    }

    @Override
    public int estimate(State state, int weight) {
        Agent agent = state.getAgent();
        Box first = boxes.get(0);
        Square agentSquare = state.getLevel().getSquare(agent.getRow(),agent.getCol());

        int extraTime = 0;
        if(!preconditions(state)){
            extraTime = notCompletedPrecondition(state)
                    .stream()
                    .map(g -> g.timeToCompletion(state))
                    .max((g1, g2) -> Integer.compare(g1, g2))
                    .get();

            if(extraTime == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }

        MinDistance dist = agentSquare.getDistance(first.row, first.col);

        if(dist == null) return Integer.MAX_VALUE;

        return extraTime + boxes.size() * dist.d;
    }

    private List<Goal> notCompletedPrecondition(State state){
        return this.getDependencyList()
                .stream()
                .filter(g -> !g.isCompleted(state))
                .collect(Collectors.toList());
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public boolean preconditions(State state) {
        boolean dependencies = true;

        for(Goal g : this.getDependencyList()){
            dependencies = dependencies && g.isCompleted(state);
        }

        return dependencies;
    }

    @Override
    public State getExpectedState(State state) {
        Box last = boxes.get(boxes.size() - 1);
        Agent newAgent = new Agent(last.row, last.col, state.getAgent().getNumber(), state.getAgent().getColor());

        return new State(state, newAgent, estimate(state,0));


    }

    @Override
    public Square getContinueTowardsSquare(State state) {
        Box first = boxes.get(0);
        return state.getLevel().getSquare(first.row, first.col);
    }

    @Override
    public String toString() {
        return "ClearPathGoal(color: " + color + ", # of boxes: " + this.boxes.size() + ", boxes{ "+ String.join(",", boxes.stream().map(b -> b.toString()).collect(Collectors.toList())) +"} )";
    }

    @Override
    public int timeToCompletion(State state) {
        if(isCompleted(state)) return 0;

        int maxTime = 0;

        for(Box box : boxes){
            Box temp = state.getBox(box.row, box.col);

            if( !(temp == null || !temp.equals(box) ) ){

                int res = state.getTimeToBoxMove(box.row, box.col);

                if(res == -1) {
                    return Integer.MAX_VALUE;
                }

                maxTime = Integer.max(maxTime, res);
            }
        }

        return state.time - maxTime;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result + color.ordinal();
        result = prime * result + owner.ordinal();

        for(Box box : boxes){
            result = prime * result + box.hashCode();
        }

        return result;
    }


}
