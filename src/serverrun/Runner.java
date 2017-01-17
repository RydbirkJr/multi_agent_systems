package serverrun;

/**
 * Created by Anders on 27/03/16.
 */
public abstract class Runner {

    /**
     * Placeholder to be replaced by >>"<< on Windows and used for splitting commands
     * on Mac.
     */
    protected final String
            PLACEHOLDER = "DEL",
            PATH_SEPARATOR = "PATH_SEP",
            runServer = "java -jar server.jar ",
            runLevel = "-l ",
            runTime = "-g 0 -p ",
            runTimeOut = "",
            runCommand = "-c "+ PLACEHOLDER + "java -Xmx1024m -classpath bin" + PATH_SEPARATOR + "lib/* client.Client" + PLACEHOLDER;

    public abstract ProcessBuilder getBuilder();

    /**
     * The runCommand must be the last argument of the command string
     * or else it fails on Mac.
     * @param level The relative path for the level.
     * @return Returns a concat command to execute the given level with the server.
     */
    protected String getCommand(String level){
        String result = runServer
                + runLevel
                + level + " "
                + runTime
                + runTimeOut
                + runCommand;

        return result;

    }
}
