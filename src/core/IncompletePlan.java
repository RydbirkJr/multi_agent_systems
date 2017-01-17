package core;

import actions.Action;
import actions.TopLevelAction;

/**
 * Created by Anders on 26/04/16.
 */
public class IncompletePlan extends Result{

    public State state;

    public IncompletePlan(TopLevelAction plan, State state){
        super(plan);
        this.state = state;
    }
}
