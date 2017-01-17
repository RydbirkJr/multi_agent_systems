package core;

import enums.Diagonal;
import enums.Direction;
import map.Level;
import map.Square;
import merging.AgentStructure;
import merging.AgentWrapper;
import merging.BoxStructure;
import merging.ResourceStructure;

import java.util.List;

/**
 * Created by Anders on 19/04/16.
 */
public class State {

    private final Agent agent;
    private final Level level;
    private BoxStructure boxStructure;
    private final ResourceStructure resourceStructure;
    public final int time;
    private final AgentStructure agentStructure;


    /**
     * Called when initiating State the first time
     * @param agent
     * @param level
     */

    public State(Agent agent, Level level, BoxStructure boxStructure, ResourceStructure resourceStructure, int time, AgentWrapper[] agents, boolean useCanAllocateOnly){
        this.agent = agent;
        this.level = level;
        this.boxStructure = new BoxStructure(boxStructure);
        this.resourceStructure = new ResourceStructure(resourceStructure);
        this.time = time;
        this.agentStructure = new AgentStructure(agent, agents, level, useCanAllocateOnly);
    }

    /**
     * This constructor is ONLY for when searching and updating the box position.
     * @param oldState
     * @param agent
     * @param box
     * @param to
     * @param increment The amount of time to increment the timer by.
     */
    public State(State oldState, Agent agent, Box box, Square to, int increment){
        this(oldState, agent, increment);
        moveBox(box, to);
    }

    /**
     * This constructor is ONLY for when searching and updating the agent position.
     * @param oldState
     * @param agent
     */
    public State(State oldState, Agent agent, int increment){
        this.level = oldState.level;
        this.time = oldState.time + increment;
        this.agent = agent;
        this.resourceStructure = oldState.resourceStructure;
        this.boxStructure = oldState.boxStructure;
        this.agentStructure = oldState.agentStructure;
    }

    public State(State oldState, Box removeBox){
        this.level = oldState.level;
        this.time = oldState.time;
        this.agent = oldState.agent;
        this.resourceStructure = oldState.resourceStructure;
        this.boxStructure = new BoxStructure(oldState.boxStructure);
        this.boxStructure.remove(level.getSquare(removeBox.row, removeBox.col), time);
        this.agentStructure = oldState.agentStructure;
    }

    public Agent getAgent() {
        return agent;
    }

    public Level getLevel() {
        return level;
    }

    private void moveBox(Box box, Square to){
        //New data structure for box handling
        this.boxStructure = new BoxStructure(this.boxStructure, box, to, time);
    }

    public Box getBox(int row, int col){
        return getBox(level.getSquare(row, col));
    }

    public Box getBox(Square square, int offset){
        return boxStructure.getBox(square, time + offset);
    }

    public Box getBox(Square square){
        return boxStructure.getBox(square, time);
    }


    /**
     * Used to find the box belonging to the goal and to estimate.
     * @param boxID
     * @return
     */
    public Box getBox(int boxID){
        return boxStructure.getBoxByID(boxID).getBox();
    }

    public int getTimeToBoxMove(int row, int col){
        return boxStructure.getBoxMove(level.getSquare(row, col), time);
    }

    public List<Box> getBoxes() {
        return boxStructure.getBoxList();
    }
    /**
     * Gets the square and checks if it is passable and that no boxes is placed on it.
     * @param row
     * @param col
     * @return
     */
    public boolean cellIsFree( int row, int col ) {
        Square square = level.getSquare(row, col);

        return square.isPassable() && !boxStructure.containsBox(square, time) && !resourceStructure.contains(time, square) && !agentStructure.contains(time, square);
    }

    public boolean cellIsAllocated(int row, int col){
        Square square = level.getSquare(row, col);
//        Logger.global("Does resources contain " + square + " for time: " + time + ": " + resourceStructure.contains(time, square));


        return resourceStructure.contains(time, square) || agentStructure.contains(time, square);
    }

    public boolean canAllocateSaveSpot(int row, int col){
        Square square = level.getSquare(row, col);
  //      Logger.global("can allocate square: " + square + " : " + agentStructure.canAllocate(square) + " for time: " + time);

        //if allowed to allocate in resources and allowed to allocate by agentStructure
        Logger.global("canAlloc("+row+","+col+") at time "+ time +": "+resourceStructure.canAllocate(row, col, time));
        Logger.global("canAlloc square: "+ square);
        return resourceStructure.canAllocate(row, col, time) && agentStructure.canAllocate(square);
    }

    /**
     * Returns the direction of the possible blocking box, or else null.
     * @param square
     * @return
     */
    public Direction blockingSaveSpot(Square square){
        Direction wall = null;
        Direction box = null;
        int countWalls = 0;
        int countBoxes = 0;
        int boxSum = 0;

        for(Direction d : Direction.values()){
            Square neighbour = getLevel().getSquare(square, d);

            if(neighbour == null) continue;

            if(!neighbour.isPassable()){
                countWalls++;
                wall = d;
            }

            if(getBox(neighbour) != null){
                countBoxes++;
                box = d;
                boxSum += d.ordinal();
            }
        }

        if(countWalls == 1 && countBoxes == 1){
            if( ( wall.ordinal() + box.ordinal() ) == 3){
                return box;
            }
        }

        if(countWalls == 0 && countBoxes == 2 && boxSum == 3){
            return box;
        }

        return null;
    }

    /**
     * Returns true if diagonals blocks the field.
     * @param square
     * @return
     */
    public boolean blockingDiagonals(Square square){
        int countDiagonal = 0;
        int countBoxes = 0;
        for(Diagonal d : Diagonal.values()){
            Square diagonal = getLevel().getSquare(square, d);

            if(getBox(diagonal) != null) {
                countBoxes++;
            }

            if(!diagonal.isPassable()){
                countDiagonal++;
            }
        }

        if(countDiagonal + countBoxes >= 2 && countBoxes > 0){
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + agent.getRow();
        result = prime * result + agent.getCol();

        for(Box box : boxStructure.getBoxList()){
            result = prime * result + box.hashCode();
        }

        return result;
    }

    /**
     * Gets the combined distance from the agent to the box and from the box to the square.
     * @param agent
     * @param box
     * @param square
     * @return
     */
    public int getDistance(Agent agent, Box box, Square square){
        int agentDist = getLevel().getSquare(agent.getRow(), agent.getCol())
                .getDistance(box.row, box.col).d;

        int boxDist = square.getDistance(box.row, box.col).d;

        return agentDist + boxDist;

    }

    public String toString(){
        String result = agent.toString();

        for(Box box : boxStructure.getBoxList()){
            result += "\n\t" + box.toString();
        }

//        result += "\nResources:";

//        for(Square sq : resourceStructure.getResources(time)){
//            result += "\n\t" + sq.toString();
//        }
//
//        for(Square sq : resourceStructure.getResources(time + 1)){
//            result += "\n+1\t" + sq.toString();
//        }

        return result;
    }
}
