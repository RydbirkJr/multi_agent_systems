package planning;

import core.State;

/**
 * Created by Anders on 14/05/16.
 */
public class GoalOccurrence {

    public final Goal goal;
    public final State state;

    public GoalOccurrence(Goal goal, State state){
        this.goal = goal;
        this.state = state;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int stateCode = state == null ? 1 : state.time * prime;
        return goal.hashCode() * prime + stateCode;
    }
}
