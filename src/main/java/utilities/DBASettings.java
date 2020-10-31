package utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class DBASettings {
    //LFG or dependency
    public String mode;
    //Input path
    public String inputFile;
    //Output Path
    public String outputFile;
    //Input sentence
    public String input;
    //rule file
    public String ruleFile;

    public Boolean semanticParsing;
    public Boolean interactiveMode;

    public BufferedWriter outputWriter;

    public DBASettings()
    {
        this.interactiveMode = true;
        this.semanticParsing = false;
    }

    public void setOutputWriter(File file)
    {
        try {
            this.outputWriter = new BufferedWriter(new FileWriter(file, true));
        }catch(Exception e)
        {
            System.out.println("Failed to create outputWriter for path: " + file.toString());
        }
        }
}



