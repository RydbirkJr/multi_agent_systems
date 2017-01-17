package core;

import actions.Action;
import actions.CreatePlan;
import actions.TopLevelAction;

/**
 * Created by Anders on 26/04/16.
 */
public class CompletedPlan extends Result {

    public CompletedPlan(TopLevelAction plan){
        super(plan);
    }
}
