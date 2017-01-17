package planning;

import core.Agent;
import core.Logger;
import core.State;
import enums.Color;
import map.Square;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Anders on 13/05/16.
 */
public class AgentGoal extends Goal {

    public final Agent agent;
    public final Set<Square> blockedResources;

    public AgentGoal(Set<Square> resources, Agent agent, Set<Square> blockedResources) {
        super(resources);
        this.agent = agent;
        this.blockedResources = blockedResources;
    }

    @Override
    public boolean canReach(State state) {
        return false;
    }

    @Override
    public boolean isCompleted(State state) {
//        Agent temp = state.getAgent(this.agent.getNumber());
//
//        Square agentSquare = state.getLevel().getSquare(temp.getRow(), temp.getCol());
//
//        Logger.global("Testing isCompleted for " + this.toString());
//
//        if(this.blockedResources.contains(agentSquare)){
//            return false;
//        }

        return true;
    }

    @Override
    public int estimate(State state, int weight) {
        return 5;
    }

    @Override
    public Color getColor() {
        return agent.getColor();
    }

    @Override
    public boolean preconditions(State state) {
        return false;
    }

    @Override
    public State getExpectedState(State state) {
        return null;
    }

    @Override
    public Square getContinueTowardsSquare(State state) {
        return state.getLevel().getSquare(this.agent.getRow(), this.agent.getCol());
    }

    @Override
    public String toString() {
        return "MoveAgentGoal(agent: " + agent + ")";
    }

    @Override
    public int timeToCompletion(State state) {
        return estimate(state, 0);
    }

    @Override
    public int hashCode() {

        return 0;
    }
}
