package client;

import core.Command;
import core.InvalidMoveException;
import core.Logger;
import enums.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * Created by Anders on 02/04/16.
 */
public class ServerClient {
    private final BufferedReader serverMessages;
    private static int count = 0;

    public ServerClient(BufferedReader serverMessages) throws InvalidMoveException {
        this.serverMessages = serverMessages;
    }

    public boolean move(Command[] commands) throws InvalidMoveException {
        //Transform commands to string
        String output = convertCommands(commands);
//        String output = "[" + command.toString() + "]";
        System.out.println( output );
        try{
            String response = serverMessages.readLine();
            if(response.equals("")){
                Logger.global("Reading new line from server. Received empty line.");
                response = serverMessages.readLine();
            }
            if(response.contains("false")){
                System.err.println(output + " : " + response + " : time: " + count);
                throw new InvalidMoveException();
            }
            count++;
//
            return response.contains("success");
        }catch(IOException e){
            e.printStackTrace();
            return false;
        } catch(InvalidMoveException e){

            throw e;
        }
    }

    private String convertCommands(Command[] commands){

        String[] cmds = Stream.of(commands)
                .map(c -> c != null ? c.toString() : Type.NoOp.toString())
                .toArray(size -> new String[size]);

        return "[" + String.join(",", cmds) + "]";
    }

    public int getCount(){
        return count;
    }
}
