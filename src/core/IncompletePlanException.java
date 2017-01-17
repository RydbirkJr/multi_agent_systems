package core;

/**
 * Created by Anders on 26/04/16.
 */
public class IncompletePlanException extends Exception{

    public final State state;

    /**
     *
     * @param state The state when the error occurred.
     */
    public IncompletePlanException(State state){
        this.state = state;
    }
}
