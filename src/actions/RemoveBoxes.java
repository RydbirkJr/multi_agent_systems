package actions;

import core.Agent;
import core.Box;
import map.Level;
import map.Square;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Anders on 17/04/16.
 */
public class RemoveBoxes extends Action {
    public final List<Box> boxes;
    public final Square fromSquare;
    public final Set<Square> resources;

    public RemoveBoxes(Action parent, List<Box> boxes, Square fromSquare, Set<Square> resources){
        super(parent);
        this.boxes = boxes;
        this.fromSquare = fromSquare;
        this.resources = resources;        
    }

    public List<MoveBox> getMoveBackBoxes(){
        return this.children
                .stream()
                .filter(b -> b instanceof MoveBox)
                .map(b -> (MoveBox) b)
                .filter(b -> b.moveBackTo != null )
                .collect(Collectors.toList());
    }

    public String toString(){
        return "RemoveBoxes(agent:" + this.getPreState().getAgent().toString() + ", count:" + boxes.size() + ", SearchFrom:"+ fromSquare +")";
    }
}
