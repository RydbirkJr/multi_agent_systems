package messages;

import core.State;

/**
 * Created by Anders on 10/05/16.
 */
public class IncompleteSolutionMessage {

    public final State state;

    public IncompleteSolutionMessage(State state){
        this.state = state;
    }
}
