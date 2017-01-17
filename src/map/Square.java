package map;

import core.Logger;
import enums.Direction;

import java.util.ArrayDeque;

public abstract class Square {
	public final int row, col;
	private MinDistance[][] minimumDistance;
	private boolean isSaveSpot = false;

	public Square(int row, int col){
		this.row = row;
		this.col = col;
	}

	public abstract Boolean isPassable();

//	public void setMinimumDistance(MinDistance[][] minimumDistance){
//		this.minimumDistance = minimumDistance;
//	}

	public synchronized MinDistance getDistance(int row, int col){

        if(!Level.distances[this.row][this.col]){
            minimumDistance = fillMinimumDistances();
            //Flag that it is calculated now
            Level.distances[this.row][this.col] = true;
        }

        if(this.minimumDistance == null || !isPassable()){
            return null;
        }

        return this.minimumDistance[row][col];
	}

	public void setIsSaveSpot(boolean save)
	{
		this.isSaveSpot = save;
	}
	
	public boolean getIsSaveSpot()
	{
		if(this instanceof GoalField)
		{
			return false;
		}
		return this.isSaveSpot;
	}
	
	public boolean getIsSaveSpot(boolean includeGoal)
	{
		if(includeGoal)
		{
			return getIsSaveSpot();
		}
		return this.isSaveSpot;
	}
	
	@Override
	public int hashCode(){
		return this.col * 100 + this.row;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Square){
			Square sq = (Square) obj;
			return sq.row == this.row && sq.col == this.col;
		}

		return false;
	}

	private synchronized MinDistance[][] fillMinimumDistances() {
        if(!isPassable()){
            return null;
        }
		//Init minimum distances, queue and insert first element
		MinDistance[][] distances = new MinDistance[Level.MAX_ROW][Level.MAX_COLUMN];
		ArrayDeque<MinDistance> queue = new ArrayDeque<MinDistance>(Level.MAX_ROW * Level.MAX_COLUMN);
		distances[this.row][this.col] = new MinDistance(this.row, this.col, 0,null);
		queue.add(distances[this.row][this.col]);

		while (!queue.isEmpty()) {
			//Pop first element in queue
			MinDistance node = queue.pollFirst();

			//For each direction, search
			for (Direction d : Direction.values()) {
				int row = Level.getRow(d, node.row);
				int col = Level.getCol(d, node.col);

				boolean rowOutOfBounds = row < 0 || row > (Level.MAX_ROW - 1);
				boolean colOutOfBounds = col < 0 || col > (Level.MAX_COLUMN - 1);
				if(rowOutOfBounds | colOutOfBounds) continue;

				//If no distance is set yet and the given square is passable, continue
				if (distances[row][col] == null && Level.squares[row][col].isPassable()) {
					distances[row][col] = new MinDistance(row, col, node.d + 1,node);
					queue.add(distances[row][col]);
				}
			}

		}

        return distances;
	}

	public String toString(){
		return "(" + row + "," + col + ")";
	}
}
