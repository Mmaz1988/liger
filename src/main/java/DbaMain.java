import analysis.LinguisticDictionary;
import analysis.RuleParser.RuleParser;
import semantics.GlueSemantics;
import syntax.SyntacticStructure;
import syntax.ud.SyntaxOperator;
import syntax.ud.UDoperator;
import syntax.GraphConstraint;
import syntax.xle.XLEoperator;
import utilities.DBASettings;
import utilities.PathVariables;
import utilities.VariableHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.*;


public class DbaMain {

    public enum mode {
        LFG,
        DEP
    }

    public BufferedWriter outputWriter;
    public static DBASettings settings;


    public static void main(String[] args) {

        initiateArguments(args);
    }

    public static void initiateArguments(String[] args) {
        settings = new DBASettings();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {

                case "-i":
                    settings.inputFile = args[i + 1];
                    settings.interactiveMode = false;
                    i++;
                    break;
                case "-o":
                    settings.outputFile = args[i + 1];
                    i++;
                    break;
                case "-rf":
                    settings.ruleFile = args[i + 1];
                    i++;
                    break;
                case "-dep":
                    settings.mode = "dep";
                    break;
                case "-lfg":
                    settings.mode = "lfg";
                    break;
                case "-sem":
                    settings.semanticParsing = true;

            }
        }


        runDBA();
    }

    public static void runDBA() {

        //    LinguisticDictionary ld = new LinguisticDictionary();

        if (settings.inputFile != null) {
            try {

                File inFile = new File(settings.inputFile);

                if (inFile.exists()) {
                    List<String> lines = null;

                    lines = Files.readAllLines(inFile.toPath());

                }
            } catch (Exception e1) {
                System.out.println("Could not load rule file:" + settings.inputFile);
            }
        }

        if (settings.outputFile != null) {
            String outPath = settings.outputFile;
            try {
                File outFile = new File(outPath);

                if (outFile.exists()) {
                    outFile.delete();
                }
                outFile.createNewFile();
                //  outFile.createNewFile();
                settings.setOutputWriter(outFile);
            } catch (Exception e) {
                System.out.println("Failed to write output to:" + outPath);
            }
        }


        if (settings.interactiveMode) {
            System.out.println("Starting interactive mode...\n");
            Scanner s = new Scanner(System.in);

            String input;
            while (true) {
                System.out.println("Enter sentence to be analyzed or enter 'quit' to exit the program.");
                input = s.nextLine();
                break;
                //if (input.equals("quit"))
                //    break;
            }

            String ruleFile = "";

            if (settings.ruleFile == null)
            {
                ruleFile = PathVariables.testPath + "testRulesUD4c.txt";
            } else
            {
                ruleFile = settings.ruleFile;
            }

            SyntacticStructure fs = parserInteractiveWrapper(settings.mode,input, ruleFile);


            if (settings.semanticParsing) {
                semanticsInteractiveWrapper(fs);
            }
        }


    }

    public static void semanticsInteractiveWrapper(SyntacticStructure fs)
    {
        System.out.println(System.lineSeparator());

        System.out.println("Glue prover output:");
        GlueSemantics sem = new GlueSemantics();
        String result = sem.calculateSemantics(fs);

        System.out.println(System.lineSeparator());
        System.out.println("Result of the Glue derivation:");
        System.out.println(result);

        System.out.println("Done");
    }

    public static SyntacticStructure parserInteractiveWrapper(String parserType, String input, String path)
    {

        SyntacticStructure fs = null;
        SyntaxOperator syn = null;

        switch(parserType)
        {
            case "dep": {
                syn = new UDoperator();
                break;
            }
            case "lfg": {
                VariableHandler vh = new VariableHandler();
                syn = new XLEoperator(vh);
                break;
            }
        }

        assert syn != null;
        fs = syn.parseSingle(input);
        System.out.println(fs.constraints);

        List<SyntacticStructure> fsList = new ArrayList<>();
        fsList.add(fs);

        RuleParser rp = new RuleParser(fsList, new File(path));

        rp.addAnnotation2(fs);

        try {
            fs.annotation.sort(Comparator.comparing(GraphConstraint::getFsNode));
        } catch (Exception e) {
            System.out.println("Sorting annotation failed.");
        }

        System.out.println("Annotation output:");

        for (GraphConstraint g : fs.annotation) {
            System.out.println(g);
        }

        return fs;

    }

}





    /*
       System.out.println("Starting interactive lfg mode...\n");
        Scanner s = new Scanner(System.in);
        String input;
        while (true) {
            System.out.println("Enter sentence to be analyzed or enter 'quit' to exit the program.");
            input = s.nextLine();
            break;
            //if (input.equals("quit"))
            //    break;
        }

        List<String> sentences = new ArrayList<>();
        sentences.add(input);

        XLEoperator xle = new XLEoperator();
        GlueSemantics sem = new GlueSemantics();

        xle.parseSentences(sentences);

        File fsFile = new File("C:\\Users\\Mark\\IdeaProjects\\xle_operator\\parser_output");

        if (fsFile.isDirectory()) {
            File[] files = fsFile.listFiles((d, name) -> name.endsWith(".pl"));


            for(int i = 0; i < files.length; i++) {

                LinkedHashMap<String, SyntacticStructure> fsRef = xle.fs2Java(files[i].getPath());

               SyntacticStructure fs = fsRef.get(fsRef.keySet().stream().findAny().get());

                List<SyntacticStructure> fsList = new ArrayList<>();
                fsList.add(fs);

                RuleParser rp = new RuleParser(fsList,"C:\\Users\\Mark\\IdeaProjects\\xle_operator\\src\\test\\testRules5.txt");

                rp.addAnnotation2(fs);

                sem.calculateSemantics(fs);

                for (Premise p : sem.llprover.getSolutions())
                {
                    System.out.println(p.toString());
                }
            }
        }

     */


