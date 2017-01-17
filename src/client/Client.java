package client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import core.Agent;
import core.Box;
import core.Logger;
import core.Memory;
import enums.Color;
import map.Level;
import merging.MergeActor;
import merging.PlanMerger;
import planning.GoalPrioritizer;
import planning.PlannerActor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anders on 24/04/16.
 */
public class Client {

    public Level level;
    public List<Agent> agents;
    public List<Box> boxes;

    public Client(BufferedReader serverMessages) throws Exception {
        Map< Character, Color> colors = new HashMap<>();

        String line;
        int agentCol = -1, agentRow = -1;
        int colorLines = 0, row = 0;

        // Read lines specifying colors
        while ( ( line = serverMessages.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
            line = line.replaceAll( "\\s", "" );
            String[] colonSplit = line.split( ":" );
            Color color = mapColor(colonSplit[0].trim());

            for ( String id : colonSplit[1].split( "," ) ) {
                colors.put( id.trim().charAt( 0 ), color );
            }
            colorLines++;
        }

        ArrayList<String> aList = new ArrayList<>();

        int maxCol = 0;
        int maxRow;
        do{
            maxCol = Math.max(maxCol, line.length());
            aList.add(line);

        } while(!(line = serverMessages.readLine()).equals(""));

        maxRow = aList.size();

        boolean[][] walls = new boolean[maxRow][maxCol];
        char[][] goals = new char[maxRow][maxCol];
        boxes = new ArrayList<>();
        agents = new ArrayList<>();

        for(String mapLine : aList){
            for ( int col = 0; col < mapLine.length(); col++ ) {

                //Character and if-variables
                char chr = mapLine.charAt( col );
                boolean isWall = '+' == chr;
                boolean isAgent = '0' <= chr && chr <= '9';
                boolean isBox =  'A' <= chr && chr <= 'Z';
                boolean isGoal = 'a' <= chr && chr <= 'z';

                if (isWall) { // Walls
                    walls[row][col] = true;

                } else if (isAgent) { // Agents
                    if ( agentCol == -1 && agentRow == -1 ) {
                        agents.add(new Agent(row, col, Character.getNumericValue(chr), colors.get(chr)));
                        //As long as we don't work on other states
                    }

                } else if (isBox) { // Boxes
                    boxes.add(new Box(row, col, chr, colors.get(chr)));

                } else if (isGoal) { // Goal cells
                    goals[row][col] = chr;
                }
            }
            row++;
        }


        //This part is needed always. Sets the level
        Logger.global("Creating level...");
        level = new Level(maxRow, maxCol, walls, goals, colors);
        Logger.global("Creating level completed");
    }

    private Color mapColor(String input){
        switch(input){
            case "blue": return Color.blue;
            case "red": return Color.red;
            case "green": return Color.green;
            case "cyan": return Color.cyan;
            case "magenta": return Color.magenta;
            case "orange": return Color.orange;
            case "pink": return Color.pink;
            case "yellow": return Color.yellow;
            default: return Color.blue;
        }
    }

    public static void main( String[] args ) throws Exception {
        BufferedReader serverMessages = new BufferedReader( new InputStreamReader( System.in ) );

        // Use stderr to print to console
        Logger.global("SearchClient initializing. I am sending this using the error output stream.");

        Client client = new Client(serverMessages);

        Logger.global(Memory.stringRep());
        ActorSystem system = ActorSystem.create("aimuffins");
        ActorRef ref = system.actorOf(PlannerActor.props(client.agents, client.level, client.boxes, new ServerClient(serverMessages)));
    }
}

