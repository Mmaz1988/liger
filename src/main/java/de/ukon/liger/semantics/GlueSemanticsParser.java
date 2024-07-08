package de.ukon.liger.semantics;

import de.ukon.liger.utilities.VariableHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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


        /*
         String testFile = "/Users/princess_zelda/Projects/multistage_proving/multistage_xle_adj.lfg.glue";

        GlueSemanticsParser gs = new GlueSemanticsParser(new VariableHandler());

        gs.createLFGfile(testFile);

         */

        GlueSemanticsParser glueSemanticsParser = new GlueSemanticsParser(new VariableHandler());

        String mc =  glueSemanticsParser.parseMeaningConstructor(":$ lam(U,lam(V,lam(E,merge(drs([],[]),merge(app(U,E),app(V,E)))))) :\n" +
                "\t    ((%scope_e -o s::^_t) -o ((%scope_v -o %scope_t) -o (%scope_v -o %scope_t))),");

        System.out.println(mc);

    }

    public void createLFGfile(String lfgGlueFile) throws IOException {
        //open File lfgGlueFile and read to String


        String content = Files.readString(Path.of(lfgGlueFile));


        Pattern p = Pattern.compile("(?s)(?m):\\$(.+?)(\\.\\r?\\n|,\\r?\\n)");

        Pattern lb = Pattern.compile(",\\r?\\n");

        Matcher m = p.matcher(content);

        while (m.find()) { // using find instead of matches
            String mcLine = m.group(0);
            String mc = m.group(1);
            String parsed = parseMeaningConstructor(mc);
            String end = m.group(2);
            Matcher m2 = lb.matcher(end);

            if (!m2.matches()) {
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
        String[] parts = mc.split(" :\\s+");

        try {

            String[] llandparams = parts[1].split("\\|\\|");

            String params = null;

            if (llandparams.length == 2)
            {
                params = llandparams[1];
            }

            String[] ll = llp.linearLogic2AVM(llandparams[0].trim());
            String prologLL = ll[1];

            //encode meaning
            String meaning = Quoter.backquoteSpaceRegion(parts[0]);
            String meaningVar = this.vh.returnNewVar(VariableHandler.variableType.LOCAL_NAME,null);
            String concat = "@(CONCAT" + meaning + " " + meaningVar + ")";


            List<String> paramStrings = new ArrayList<>();
            //encode parameters
            if (params != null)
            {
                String[] paramList = params.trim().split(",");
                for (String param : paramList) {
                    param = param.trim();
                    switch (param) {
                        case "noscope":

                            String noscope = "@(NOSCOPE " + ll[0] + ")";
                          paramStrings.add(noscope);
                            break;
                        case ("widescope"):
                            //TODO
                            break;
                    }
                }

            }

            StringBuilder sb = new StringBuilder();
            sb.append(prologLL);
            sb.append(System.lineSeparator());
            sb.append(concat);
            sb.append(System.lineSeparator());
            sb.append("@(GLUE-MEANING " + ll[0] + " " + meaningVar +  ")");

            for (String paramString : paramStrings) {
                sb.append(System.lineSeparator());
                sb.append(paramString);
            }

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