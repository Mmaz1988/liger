package utilities;

import java.net.URISyntaxException;

public class PathVariables {
    //Path to the test package
    public static final String testPath = "C:\\Users\\Celeste\\IdeaProjects\\SpringDemo\\resources\\testFiles\\";
    //Path to the dict package
    public static final String dictPath = "C:\\Users\\Celeste\\IdeaProjects\\SpringDemo\\resources\\dicts\\";

/*
    public static void main(String[] args) throws URISyntaxException {
        System.out.println(PathVariables.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString());
        System.out.println(ClassLoader.getSystemClassLoader().getResource(".").getPath().toString());

    }
*/
 //   return new File(MyClass.class.getProtectionDomain().getCodeSource().getLocation()
 //   .toURI()).getPath();

}
