package serverrun;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public abstract class RunServer {

    public static String levelPath = "level" + File.separator;

    public static String getLevel(){

        //Eksempel 1
//        String levelName = "easy_SAD1";
        //Eksempel 2
//        String levelName = "validatepath";

        //Eksempel 3
//        String levelName = "testLevel";

        //Eksempel 4 + 5
//        String levelName = "medium_SAwallE";
//        String levelName = "SAAIMuffins2";

        //Eksempel 6
//        String levelName = "easy_MAFireFly";

        //Eksempel 7,8,9
//        String levelName = "diff_MAschwenke";
//        String levelName = "competition/MAButterBot"; //√
//          String levelName = "competition/MATheAgency"; //√

//        String levelName = "easy_SAD1";
//        String levelName = "easy_SAD2";
//        String levelName = "easy_Crunch";
//        String levelName = "easy_Firefly";
//        String levelName = "long_SAbispebjerg";
//        String levelName = "easy_SAbruteforce";

//        String levelName = "medium_SAnull";
//        String levelName = "medium_SATeamNoOp";
//        String levelName = "medium_SAFOAM";
//        String levelName = "long_SAEpicPhail";
        String levelName = "long_SAWatsOn";
//        String levelName = "long_SAMASters";


        //This map fails because of too many boxes to savespot
//        String levelName = "diff_SADinMor";
//        String levelName = "diff_SAschwenke";

        //Need to identify blocking boxGoals
//                String levelName = "diff_SASkynet";


//        String levelName = "MATheAgency"; //√

        //same for this one - for some reason it gives up but it's actually going pretty strong
        //Issue: Circular references

        //Multiagents!
        //Can solve
//        String levelName = "easy_MAFireFly";
//        String levelName = "easy_MAYSoSirius";
//        String levelName = "medium_MAsampdoria";
//        String levelName = "easy_MACrunch";
//        String levelName = "diff_MAschwenke";
//        String levelName = "diff_MAwallE";
//        String levelName = "medium_MATeamNoOp";
//        String levelName = "diff_MAholdkaeft";
//        String levelName = "diff_MAStuddyBuddy";
//        String levelName = "diff_MABullFight";
//        String levelName = "diff_MAbruteforce";
//        String levelName = "diff_MAGroupXX";
//        String levelName = "diff_MAnull";
//        String levelName = "diff_MAHumans";

        //Circular goal reference - endless loop
//        String levelName = "easy_MADeliRobot";

        //***********************************************
        // COMPETITION LEVELS - MULTIAGENT
        //***********************************************

        //NEMT
//        String levelName = "MADangerBot"; //√
//        String levelName = "MAextra1"; //√
//        String levelName = "MALazarus"; //√
//        String levelName = "MASojourner"; //√
//        String levelName = "MATheAgency"; //√

//        String levelName = "MAWallE"; //√
//        String levelName = "MAButterBot"; //√
//        String levelName = "MAOptimal"; //√
//        String levelName = "MAboXboXboX"; //x

        //Swap error - burde kunne klares nemt
//        String levelName = "MASolo"; //x

//        String levelName = "MAteamhal"; //x
//        String levelName = "MAAIMuffins"; //x
//        String levelName = "MAbotbot"; //x
//       String levelName = "MATheRedDot"; //X

        //***********************************************
        // COMPETITION LEVELS - SINGLE
        //***********************************************

        //NEMT
//        String levelName = "SADangerBot"; //√
//        String levelName = "SALazarus"; //√
//        String levelName = "SAOptimal"; //√
//        String levelName = "SASojourner"; //√
//        String levelName = "SATheAgency"; //√
//        String levelName = "SATheRedDot"; //√
//        String levelName = "SAteamhal"; //√
//        String levelName = "SATAIM"; //√
//        String levelName = "SAextra2"; //√
//        String levelName = "SASolo"; //√
//        String levelName = "SAbotbot"; //√
//        String levelName = "SAButterBot"; //√
//        String levelName = "SAAIMuffins"; //√
//        String levelName = "SAboXboXboX"; //√

        //*******************************
        // EDGE ISSUES
        //*******************************


        //*******************************
        // SAVESPOT ISSUES
        //*******************************

        //For få savespots
//        String levelName = "SAFortyTwo";
    	
        //*******************************

        //nice try - not gonna happen
//        String levelName = "SANoOp";



//        String competition = "competition/";
//        levelName = competition + levelName;

        return levelPath + levelName + ".lvl";
    }

    public static void main(String[] args) throws Exception
    {
        String os = System.getProperty("os.name");
        System.out.println(os);
        String[] osAr = os.split(" ");
        boolean windows = (osAr[0].equals("Windows"));


        Runner runner = windows
                ? new WindowsRunner(getLevel())
                : new MacRunner(getLevel());


        ProcessBuilder builder = runner.getBuilder();

        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while(true)
        {
            line = br.readLine();
            if(line == null)
            {
                break;
            }
            System.out.println(line);
        }
    }
}
