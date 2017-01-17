package planning;

import core.Logger;
import core.State;

import java.util.HashMap;

/**
 * Created by Anders on 14/05/16.
 */
public class OccurrenceDetector {

    private final HashMap<Integer, Integer> occurrences;
    private final HashMap<Integer, Integer> totalOccurrences;

    public OccurrenceDetector(){

        this.occurrences = new HashMap<>();
        this.totalOccurrences = new HashMap<>();
    }

    public int occurrences(Goal goal, State state){
        Integer res = occurrences.get(new GoalOccurrence(goal, state).hashCode());

        if(res == null) return 0;

        Logger.global("Occurrences for goal: " + goal + " : " + res);
        return res;
    }

    public int totalOccurrences(Goal goal){
        Integer res = totalOccurrences.get(new GoalOccurrence(goal, null).hashCode());

        Logger.global(goal + " totalocc: " + res);
        if(res == null) return 0;

        return res;
    }

    public void putOccurrence(Goal goal, State state){
        GoalOccurrence go = new GoalOccurrence(goal, state);
//        Logger.global("New occurrence for " + goal + " in time: " + state.time + ", hashcode: " + go.hashCode());

        if(!occurrences.containsKey(go.hashCode())){

            occurrences.put(go.hashCode(), new Integer(1));

        } else {

            Logger.global("Occurrence for " + goal + " #: " + occurrences.get(go.hashCode()));
            occurrences.put(go.hashCode(), occurrences.get(go.hashCode()) + 1);
        }

        if(!totalOccurrences.containsKey(go.hashCode())){
            Logger.global("No occurrences found for " + goal + " for hashcode " + go.hashCode());
            Integer res2 = totalOccurrences.get(go.hashCode());
            Logger.global("Inserted occurrence. res2: " + res2);
            totalOccurrences.put(go.hashCode(), new Integer(1));
        } else {
            totalOccurrences.put(go.hashCode(), totalOccurrences.get(go.hashCode()) + 1);
        }
    }
}
