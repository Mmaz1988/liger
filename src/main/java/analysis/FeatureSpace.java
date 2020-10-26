package analysis;

import syntax.SyntacticStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureSpace {

    public List dataSet;
    public boolean extractMatrix;

    public FeatureSpace(List dataSet)
    {
        this.dataSet = dataSet;
    }



    public void generateCSV(List<String> arguments)
    {
        if (!arguments.isEmpty()) {

            String[][] table = new String[arguments.size()+1][dataSet.size()-1];

            for (int i = 0; i < dataSet.size(); i++)
            {
                SyntacticStructure str = (SyntacticStructure) dataSet.get(i);
                String sentence = str.sentence;
                table[i][0] = str.local_id;
                table[i][1] = str.sentence;
                for (int j = 2; j < arguments.size()+2; j++)
                {

                }
            }

        }


    }

    public static void main(String[] args) {

        if (args[0].equals("-em"))
        {

        }

        List<SyntacticStructure> structure = new ArrayList<>();

        List<String> arguments = Arrays.asList(args);
        FeatureSpace fs = new FeatureSpace(structure);
        fs.generateCSV(arguments);
    }


}

