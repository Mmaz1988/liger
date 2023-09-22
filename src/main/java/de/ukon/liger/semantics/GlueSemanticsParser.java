package de.ukon.liger.semantics;

import de.ukon.liger.utilities.VariableHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlueSemanticsParser {


    public LinearLogicParser llp;
    public VariableHandler vh;

    public GlueSemanticsParser(VariableHandler vh) {
        this.vh = vh;
        this.llp = new LinearLogicParser(vh);

    }

    public static void main(String[] args) throws IOException {


         String testFile = "/Users/princess_zelda/Projects/multistage_proving/multistage_xle_adj.lfg.glue";

        GlueSemanticsParser gs = new GlueSemanticsParser(new VariableHandler());

        gs.createLFGfile(testFile);
    }

    public void createLFGfile(String lfgGlueFile) throws IOException {
        //open File lfgGlueFile and read to String


        String content = Files.readString(Path.of(lfgGlueFile));


        Pattern p = Pattern.compile("(?s)(?m):\\$(.+?)(\\.\\s|,\\s)");

        Matcher m = p.matcher(content);

        while (m.find()) { // using find instead of matches
            String mcLine = m.group(0);
            String mc = m.group(1);
            String parsed = parseMeaningConstructor(mc);
            if (!m.group(2).equals(",\n")) {
                parsed = parsed + ".\n";
            }
            content = content.replace(mcLine, parsed);
        }

        //write to file
        String newFileName = lfgGlueFile.replace(".lfg.glue", ".lfg");
        Files.writeString(Path.of(newFileName), content);

    }


    public String parseMeaningConstructor(String mc) {
        //split at :
        String[] parts = mc.split(" : ");

        try {
            String[] ll = llp.linearLogic2AVM(parts[1].trim());
            String prologLL = ll[1];
            String meaning = Quoter.backquoteSpaceRegion(parts[0]);

            String meaningVar = this.vh.returnNewVar(VariableHandler.variableType.LOCAL_NAME,null);

            String concat = "@(CONCAT" + meaning + " " + meaningVar + ")";

            StringBuilder sb = new StringBuilder();
            sb.append(prologLL);
            sb.append(System.lineSeparator());
            sb.append(concat);
            sb.append(System.lineSeparator());
            sb.append("@(GLUE-MEANING " + ll[0] + " " + meaningVar +  ")");

            return sb.toString();

        } catch (Exception e) {
            System.out.println("Error parsing meaning constructor: " + mc);
            e.printStackTrace();
        }
        return null;
    }


    static class Quoter {

        private static final List<Character> xleQuotedChars = Arrays.asList('<', '>', '(', ')', '{', '}', '[', ']', '*', '+',
                '-', '&', '|', '\\', '/', '~', '^', '$', '`', '#', '\'', ',', ':', '?', '=', '!', '@', '.', ';', '_');

        public static String backquoteRegion(String input) {
            StringBuilder output = new StringBuilder();
            for (char c : input.toCharArray()) {
                if (xleQuotedChars.contains(c)) {
                    output.append('`');
                }
                output.append(c);
            }
            return output.toString();
        }

        public static String backquoteSpaceRegion(String input) {
            StringBuilder output = new StringBuilder();
            for (char c : input.toCharArray()) {
                if (xleQuotedChars.contains(c)) {
                    output.append(' ');
                    output.append('`');
                    output.append(c);
                    output.append(' ');
                } else {
                    output.append(c);
                }
            }
            return output.toString();
        }
    }
}