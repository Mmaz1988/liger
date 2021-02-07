package utilities;

import java.net.URISyntaxException;

public class PathVariables {

    public static final String workingDirectory = "resources/";
    //Path to the test package
    public static String testPath;
    //Path to the dict package
    public static String dictPath;

    /*

    public PathVariables()
    {
        try {
            testPath = returnResourcesFolder() + "testFiles/";
            dictPath = returnResourcesFolder() + "dicts/";
        }catch(Exception e)
        {
            System.out.println("Failed to locate working directory.");
                    }
    }

    public static void main(String[] args) {
        try {
            PathVariables pv = new PathVariables();
            System.out.println(PathVariables.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString());
            System.out.println(ClassLoader.getSystemClassLoader().getResource(".").getPath().toString());

            System.out.println(testPath);
            System.out.println(dictPath);


        }
        catch(Exception e)
        {System.out.println("failed");}
    }
*/

    public static void initializePathVariables()
    {
        try {
            testPath = returnResourcesFolder() + "testFiles/";
            dictPath = returnResourcesFolder() + "dicts/";
        }catch(Exception e)
        {
            System.out.println("Failed to locate working directory.");
        }
    }

    public static String returnResourcesFolder() throws URISyntaxException {
        return PathVariables.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString()  + "resources/";
    }


 //   return new File(MyClass.class.getProtectionDomain().getCodeSource().getLocation()
 //   .toURI()).getPath();*

}
