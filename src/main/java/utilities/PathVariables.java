package utilities;

import java.net.URISyntaxException;

public class PathVariables {
    //Path to the test package
    public static final String testPath = "/Users/red_queen/IdeaProjects/syntax-annotator-glue/resources/testFiles/";
    //Path to the dict package
    public static final String dictPath = "/Users/red_queen/IdeaProjects/syntax-annotator-glue/resources/dicts/";


    public static void main(String[] args) {
        try {
            System.out.println(PathVariables.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString());
            System.out.println(ClassLoader.getSystemClassLoader().getResource(".").getPath().toString());
        }
        catch(Exception e)
        {System.out.println("failed");}
    }

 //   return new File(MyClass.class.getProtectionDomain().getCodeSource().getLocation()
 //   .toURI()).getPath();

}
