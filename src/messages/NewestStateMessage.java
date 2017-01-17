package messages;

import core.State;

/**
 * Created by Anders on 18/05/16.
 */
public class NewestStateMessage {

    public final State state;

    public NewestStateMessage(State state){
        this.state = state;
    }
}
