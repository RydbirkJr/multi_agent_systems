package actions;

import core.State;
import planning.Goal;

/**
 * Created by Anders on 12/05/16.
 */
public abstract class TopLevelAction extends Action {

    public TopLevelAction(State state) {
        super(state);
    }

    public abstract Goal getGoal();
}
