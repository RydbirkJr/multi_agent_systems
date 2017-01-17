package messages;

import core.State;
import planning.Goal;

/**
 * Created by Anders on 12/05/16.
 */
public class MergerResponse {

    public final State state;
    public final Goal goal;
    public final boolean accepted;

    public MergerResponse(State state, Goal goal, boolean accepted){
        this.state = state;
        this.goal = goal;
        this.accepted = accepted;
    }
}
