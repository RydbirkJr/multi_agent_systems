package merging;

import core.Command;
import core.Logger;
import map.Field;
import map.Square;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Anders on 06/05/16.
 */
public class ResourceStructure {

    private int[][] lastUsed;
    private List<HashSet<Square>> resources;

    public ResourceStructure(int row, int col){
        this.resources = new ArrayList<>();
        //Initial empty resources
        this.resources.add(new HashSet<>());
        this.lastUsed = new int[row][col];
    }

    public ResourceStructure(ResourceStructure parent){
        this.resources = new ArrayList<>();
        parent.resources.forEach(set -> this.resources.add(new HashSet<>(set)));
        this.lastUsed = new int[parent.lastUsed.length][] ;
        System.arraycopy(parent.lastUsed, 0, this.lastUsed, 0, parent.lastUsed.length);
    }

    public boolean contains(int time, Square square){

        if(time >= resources.size() ) return false;

        HashSet<Square> res = get(time);

        if(res != null){
            return res.contains(square);
        }

        return false;
    }

    public boolean canAllocate(int row, int col, int time){
        return lastUsed[row][col] < time || lastUsed[row][col] == 0;
    }

    protected void put(int time, Square square){
        HashSet<Square> res = get(time);

        if(lastUsed[square.row][square.col] < time){
            lastUsed[square.row][square.col] = time;
        }

        res.add(square);
    }

    /**
     * Add all resources from a specific starting point
     * @param time
     * @param resources
     */
    protected void addResources(int time, List<HashSet<Square>> resources){
        for(HashSet<Square> set : resources){
            //Use put here to also update the lastUsed
            for (Square sq : set) {
                put(time, sq);
            }
            time++;
        }
    }

    protected void freezeField(int from, int to, Square square){

        while (from < to){
            this.put(from, square);
            from++;
        }
    }

    private HashSet<Square> get(int time){
        if(resources.size() == time){
            resources.add(new HashSet<>());
        }

        return resources.get(time);
    }

    public HashSet<Square> getResources(int time){
        return get(time);
    }
}
