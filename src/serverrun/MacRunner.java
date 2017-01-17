package serverrun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Anders on 27/03/16.
 */
public class MacRunner extends Runner {

    private String pathCommand;
    private String level;

    public MacRunner(String level){
        this.level = level;
        setPath();

    }

    /**
     * Sets the absolute path the execution environment.
     * It assumes that the class loader is in the /bin directory.
     * If spaces exist in the path (on the form "%20"), they are replaced with "\ ".
     */
    private void setPath(){
        pathCommand = "cd " + this.getClass().getClassLoader().getResource("").getPath();
        pathCommand = pathCommand.replace("/bin", "");
        pathCommand = pathCommand.replace("%20", "\\ ");

    }

    public ProcessBuilder getBuilder(){
        ProcessBuilder builder = new ProcessBuilder("xterm");
        builder.command(this.pathCommand);
        builder.command(getServerCommand());
        return builder;
    }

    /**
     * Gets the server command and splits on the placeholder.
     * Since it is assumed that the client command is the last part
     * of the server command, the client command will be the second element of the
     * serverCommands array.
     * @return
     */
    private String[] getServerCommand(){
        String[] serverCommands = super.getCommand(level)
                .replace(super.PATH_SEPARATOR, ":")
                .split(super.PLACEHOLDER);

        String javaCommand = serverCommands[1];
        List<String> aList = new ArrayList<String>(Arrays.asList(serverCommands[0].split(" ")));
        aList.add(javaCommand);
        return aList.toArray(new String[aList.size()]);
    }
}
