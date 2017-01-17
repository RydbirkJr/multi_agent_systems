package planning;

import core.Agent;
import core.Box;

import java.util.List;

/**
 * Created by Anders on 13/05/16.
 */
public class BlockingElements {

    public final List<Box> boxes;
    public final List<Agent> agents;

    public BlockingElements(List<Box> boxes, List<Agent> agents){
        this.boxes = boxes;
        this.agents = agents;
    }
}
