package merging;

import core.Box;
import core.Logger;
import map.Level;
import map.Square;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Anders on 07/05/16.
 */
public class BoxStructure {

    private HashMap<Square, TreeMap<Integer, Box>> boxes;
    private HashMap<Integer, BoxWrapper> boxMap;
    private final Level level;

    public BoxStructure(List<Box> boxes, Level level, int time){
        this.level = level;
        this.boxes = new HashMap<>(boxes.size());
        this.boxMap = new HashMap<>(boxes.size());



        //Load both data structures
        boxes.forEach(b -> {
            Square square = level.getSquare(b.row, b.col);
            put(square, b, time);
        });

//        //Load boxMap
//        for (Box box : boxes) {
//            this.boxMap.put(box.getID(), new BoxWrapper(box, time));
//        }
    }

    public BoxStructure(BoxStructure parent){
        this.level = parent.level;
        this.boxes = new HashMap<>();
        //Copy the set
        parent.boxes.entrySet().forEach(e -> this.boxes.put(e.getKey(), new TreeMap<>(e.getValue())));
        this.boxMap = new HashMap<>();
        parent.boxMap.entrySet().forEach(e -> this.boxMap.put(e.getKey(), new BoxWrapper(e.getValue().getBox(), e.getValue().getLastMove())));
    }

    /**
     * When moving a box.
     * Creates a copy of the old structure, and updates the two sets
     * containing the new box positions.
     * @param prev
     * @param box
     * @param to
     */
    public BoxStructure(BoxStructure prev, Box box, Square to, int time){
        this.level = prev.level;
        this.boxMap = new HashMap<>(prev.boxMap);

        //Create new box
        Box newBox = new Box(box, to.row, to.col);

        //This overwrites the old box wrapper
        boxMap.put(box.getID(), new BoxWrapper(newBox, time));

        Square curBoxPosition = level.getSquare(box.row, box.col);

        //Tight copy: Only a new hashmap but not new treemaps
        this.boxes = new HashMap<>(prev.boxes);

        //Replace the two TreeMaps that are affected.
        replaceAffectedSets(curBoxPosition, to);

        //Insert and remove the box.
        put(to, newBox, time);
        remove(curBoxPosition, time);
    }

    protected void put(Square square, Box box, int time){
        if(!boxes.containsKey(square)){
            TreeMap<Integer, Box> tree = new TreeMap<>();
            tree.put(time, box);
            boxes.put(square, tree);
        } else {
            boxes.get(square).put(time, box);
        }

        this.boxMap.put(box.getID(), new BoxWrapper(box, time));
    }

    public boolean containsBox(Square square, int time){
        return getBox(square, time) != null;
    }

    public boolean containsBoxIgnore(Square square, int time, Box ignore){
        Box box = getBox(square, time);

        if(box != null){
            Logger.global("Found box " + box + " and ignore is: " + ignore);
            return !box.equalsID(ignore);
        }

        return false;
    }

    /**
     * Finds the box for the given square for the given field.
     * @param square
     * @param time
     * @return Null of not found.
     */
    public Box getBox(Square square, int time){
        TreeMap<Integer, Box> tree = boxes.get(square);

        if(tree != null){
            Map.Entry<Integer, Box> entry = tree.floorEntry(time);

            if(entry != null){
                return entry.getValue();
            }
        }

        return null;
    }

    public BoxWrapper getBoxByID(int id){
        return boxMap.get(id);
    }

    public List<Box> getBoxList(){

        List<Box> b = new ArrayList<>(boxMap.size());

        boxMap.entrySet()
                .stream()
                .forEach(entry -> b.add(entry.getValue().getBox()));

        return b;
    }

    /**
     * BEWARE: This removes the box from the current square WITHOUT putting it elsewhere.
     * @param square
     * @param time
     */
    public void remove(Square square, int time){
        TreeMap<Integer, Box> tree =  boxes.get(square);
        if(tree != null){
            tree.put(time, null);
        }
    }

    /**
     * Returns -1 if the box is not moved in the future.
     * @param square
     * @param time
     * @return
     */
    public int getBoxMove(Square square, int time){
        Integer res = boxes.get(square).ceilingKey(time);

        if(res == null) {
            return -1;
        }

        return res;
    }

    private void replaceAffectedSets(Square from, Square to){
        TreeMap<Integer, Box> tree = boxes.get(from);

//        Logger.global("Box from: " + from + ", box to: " + to);

        //Tree should always be present
        //Replace with new TreeMap
        boxes.put(from, new TreeMap<>(tree));

        tree = boxes.get(to);

        //If not null, create new with old content, or else just create new
        if(tree != null){
            tree = new TreeMap<>(tree);
        } else {
            tree = new TreeMap<>();
        }

        //Insert
        boxes.put(to, tree);

    }
}
