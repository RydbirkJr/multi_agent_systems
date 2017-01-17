package messages;

import core.Agent;
import core.State;

import java.util.List;

/**
 * Created by Anders on 12/05/16.
 */
public class RequestNewStatesMessage {

    public final List<Integer> agentIDs;
    public final int time;

    public RequestNewStatesMessage(List<Integer> agentIDs, int time){
        this.agentIDs = agentIDs;
        this.time = time;
    }
}
