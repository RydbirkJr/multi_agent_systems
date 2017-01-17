package serverrun;

/**
 * Created by Anders on 27/03/16.
 */
public class WindowsRunner extends Runner {

    private String level;

    public WindowsRunner(String level){
        this.level = level;
    }

    private String getServerCommand(String levelPath){
        return super.getCommand(levelPath)
                .replace(super.PLACEHOLDER, "\"")
                .replace(super.PATH_SEPARATOR, ";");
    }

    public ProcessBuilder getBuilder(){
        return new ProcessBuilder("cmd.exe", "/c", getServerCommand(level));
    }
}
