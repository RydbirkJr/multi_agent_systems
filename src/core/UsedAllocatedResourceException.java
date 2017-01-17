package core;

import map.Square;

/**
 * Created by Anders on 07/05/16.
 */
public class UsedAllocatedResourceException extends Exception {

    public final Square square;


    public UsedAllocatedResourceException(Square square){
        this.square = square;
    }
}
