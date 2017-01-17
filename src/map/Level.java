package map;

import core.Agent;
import core.Box;
import core.Logger;
import enums.Color;
import enums.Diagonal;
import enums.Direction;

import java.util.*;

public class Level {

    protected static Square[][] squares;
    public static int MAX_ROW;
    public static int MAX_COLUMN;
    public static  ArrayList<GoalField> goals;
    protected static boolean[][] distances;

    public Graph graph;
    
    /**
     * Use this constructor only ONCE to set the map. Use the other constructor when adding a new set of boxes.
     * @param rows
     * @param cols
     * @param walls
     * @param goals
     * @param colors
     */
    public Level(int rows, int cols, boolean[][] walls, char[][] goals, Map<Character, Color> colors) {
        MAX_ROW = rows;
        MAX_COLUMN = cols;
        distances = new boolean[MAX_ROW][MAX_COLUMN];

        loadSquares(walls, goals, colors);
        graph = new Graph(this, squares);
        graph.printgraph();
        graph.getNodeListSmallestFirst();
    }

    public Square getSquare(int row, int col) {
        return squares[row][col];
    }

    public Square getSquare(Square square, Diagonal d){
        int row = square.row;
        int col = square.col;

        switch (d){
            case NE:
                row -= 1;
                col += 1;
                break;
            case NW:
                row -= 1;
                col -= 1;
                break;
            case SE:
                row += 1;
                col += 1;
                break;
            case SW:
                col -= 1;
                row += 1;
                break;
        }

        if(row >= 0 && col >= 0 && row < MAX_ROW && col < MAX_COLUMN){
            return getSquare(row, col);
        }

        return null;
    }

    public Square getSquare(Square square, Direction d){
        int row = square.row;
        int col = square.col;

        switch (d){
            case N: row += -1;
                break;
            case S: row += 1;
                break;
            case E: col += 1;
                break;
            case W: col += -1;
                break;
        }

        if(row > 0 && col > 0 && row < MAX_ROW && col < MAX_COLUMN){
            return getSquare(row, col);
        }

        return null;
    }

    public Set<Square> getResources(Square from, Square to){
        return getResources(from, to.row, to.col);
    }

    /**
     * Returns the used fields ("resources") in the distance between the given square
     * (this) and the given input.
     * @param row Row of the field to find resources for.
     * @param col Col of the field to find resources for.
     * @return A hashset of squares with the given fields
     */
    public Set<Square> getResources(Square square, int row, int col){

        MinDistance dist = square.getDistance(row, col);

        //Test if it is the same square or unreachable
        boolean sameSquare = square.row == row && square.col == col;
        if(dist == null) return null;

        if(sameSquare) return new HashSet<>();

        LinkedHashSet<Square> set = new LinkedHashSet<>(dist.d);

        //Loops through all distances and adds the corresponding square to the set
        while (dist.prev != null){
            set.add(squares[dist.row][dist.col]);
            dist = dist.prev;
        }
        return set;
    }

    private void loadSquares(boolean[][] walls, char[][] goals, Map<Character, Color> colors) {
        this.squares = new Square[MAX_ROW][MAX_COLUMN];
        this.goals = new ArrayList<>();

        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COLUMN; col++) {
                if (walls[row][col]) {
                    this.squares[row][col] = new Wall(row, col);
                } else if (goals[row][col] > 0) {
                    GoalField goal = new GoalField(row, col, goals[row][col], colors.get(Character.toUpperCase(goals[row][col])));
                    this.squares[row][col] = goal;
                    this.goals.add(goal);
                } else {
                    this.squares[row][col] = new Field(row, col);
                }
            }
        }

        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COLUMN; col++) {
                if(walls[row][col]) continue;

//                fillMinimumDistances(squares[row][col]);                
                //if(boxGoals[row][col] == 0) {
                squares[row][col].setIsSaveSpot(setIsSquareSaveSpot(squares[row][col]));
                //}

            }
        }
        
        printSaveSpot();
    }

    /**
     * Generates minimum distance arrays for all fields added to the level.
     */
//    private void fillMinimumDistances(Square square) {
//        //Init minimum distances, queue and insert first element
//        MinDistance[][] distances = new MinDistance[MAX_ROW][MAX_COLUMN];
//        ArrayDeque<MinDistance> queue = new ArrayDeque<MinDistance>(MAX_ROW * MAX_COLUMN);
//        distances[square.row][square.col] = new MinDistance(square.row, square.col, 0,null);
//        queue.add(distances[square.row][square.col]);
//
//
//        while (!queue.isEmpty()) {
//            //Pop first element in queue
//            MinDistance node = queue.pollFirst();
//
//            //For each direction, search
//            for (Direction d : Direction.values()) {
//                int row = getRow(d, node.row);
//                int col = getCol(d, node.col);
//
//                boolean rowOutOfBounds = row < 0 || row > (MAX_ROW - 1);
//                boolean colOutOfBounds = col < 0 || col > (MAX_COLUMN - 1);
//                if(rowOutOfBounds | colOutOfBounds) continue;
//
//                //If no distance is set yet and the given square is passable, continue
//                if (distances[row][col] == null && squares[row][col].isPassable()) {
//                    distances[row][col] = new MinDistance(row, col, node.d + 1,node);
//                    queue.add(distances[row][col]);
//                }
//            }
//            square.setMinimumDistance(distances);
//        }
//    }

    protected static int getRow(Direction d, int row) {

        switch (d) {
            case E:
                return row;
            case W:
                return row;
            case N:
                return row - 1;
            case S:
                return row + 1;
            default:
                return row;
        }
    }

    protected static int getCol(Direction d, int col) {
        switch (d) {
            case N:
                return col;
            case S:
                return col;
            case E:
                return col + 1;
            case W:
                return col - 1;
            default:
                return col;
        }
    }
    
	private boolean setIsSquareSaveSpot(Square square)
	{
		//The way the fields is been number around the field
		//7 0 1		
		//6 x 2
		//5 4 3
		int walls = 0;
		int fillCorner = 0;
		if(square.row == 0 || square.col == 0 || square.row >= MAX_ROW -1 || square.col >= MAX_COLUMN - 1)
		{
			return false;
		}
		Square[] corner = new Square[8];
		corner[0] = this.squares[getRow(Direction.N,square.row)][getCol(Direction.N, square.col)]; //N
		corner[1] = this.squares[getRow(Direction.N,square.row)][getCol(Direction.E, square.col)]; //NE
		corner[2] = this.squares[getRow(Direction.E,square.row)][getCol(Direction.E, square.col)];//E
		corner[3] = this.squares[getRow(Direction.S,square.row)][getCol(Direction.E, square.col)]; //SE
		corner[4] = this.squares[getRow(Direction.S,square.row)][getCol(Direction.S, square.col)]; //S
		corner[5] = this.squares[getRow(Direction.S,square.row)][getCol(Direction.W, square.col)]; //SW
		corner[6] = this.squares[getRow(Direction.W,square.row)][getCol(Direction.W, square.col)]; //W
		corner[7] = this.squares[getRow(Direction.N,square.row)][getCol(Direction.W, square.col)]; //NW 

		
		for(int i = 0; i < corner.length; i++)
		{
			if(corner[i] instanceof Wall)
			{
				if(i % 2 == 0)
				{
					walls++;
				}
				else
				{
					fillCorner++;
				}
				
			}
			
		}

		if(walls == 3)
		{
			return true;
		}
		else if(walls == 0)
		{
			if(fillCorner < 2)
			{
				return true;
			}
		}
		else if(walls == 1)
		{
			if(!corner[0].isPassable() && corner[3].isPassable() && corner[5].isPassable())
			{
				return true;
			}
			else if(!corner[2].isPassable() && corner[5].isPassable() && corner[7].isPassable())
			{
				return true;
			}
			else if(!corner[4].isPassable() && corner[7].isPassable() && corner[1].isPassable())
			{
				return true;
			}
			else if(!corner[6].isPassable() && corner[1].isPassable() && corner[3].isPassable())
			{
				return true;
			}
		}
		else if(walls == 2)
		{			
			if(!((corner[0].isPassable() && corner[4].isPassable()) || (corner[2].isPassable() && corner[6].isPassable())))
			{
				if(!corner[0].isPassable())
				{
					if(!corner[2].isPassable() && corner[5].isPassable())
					{
						return true;
					}
					else if(!corner[6].isPassable() && corner[3].isPassable())
					{
						return true;
					}
				}
				else if(!corner[4].isPassable())
				{
					if(!corner[2].isPassable() && corner[7].isPassable())
					{
						return true;
					}
					else if(!corner[6].isPassable() && corner[1].isPassable())
					{
						return true;
					}
				}
				
			}
		}
		return false;
	}
	
	public void printSaveSpot()
	{
        Logger logger = new Logger("Map");

		for(int i = 0; i < MAX_ROW; i++)
		{
            String line = "";
			for(int j = 0; j < MAX_COLUMN; j++)
			{
				if(!this.squares[i][j].isPassable())
				{
                    line += "x";
				}
				else if(this.squares[i][j].getIsSaveSpot(true))
				{
                    line += "s";
				}
				else
				{
                    line += " ";
				}
			}
            logger.info(line);
		}
	}
}
