package syntax.ud;

import syntax.SyntacticStructure;
import syntax.GraphConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class UDtoTex {

    public static void main(String[] args) {
        List<String> testSentences = new ArrayList<String>(Arrays.asList(args));

        UDoperator udops = new UDoperator();

        if (testSentences.isEmpty()) {
            Scanner s = new Scanner(System.in);
            String input;

            while (true) {
                System.out.println("Enter sentence to be analyzed or enter 'quit'.");
                input = s.nextLine();
                if (input.equals("quit"))
                    break;
                SyntacticStructure out = udops.parseSingle(input);
                //System.out.println(out.constraints);
                System.out.println(newString(input, out));


            }

        }

    }

    public static String newString(String input, SyntacticStructure out) {
        StringBuilder myString = new StringBuilder();

        myString.append("\\begin{dependency}\r\n" +
                "\\begin{deptext}\n");

        myString.append(getSentence(input));
        myString.append(getPOS(out));
        myString.append("\\end{deptext}\n");
        myString.append(getEdges(out));
        myString.append("\\end{dependency}\n");

        return myString.toString();

    }

    private static String getSentence(String input) {

        StringBuilder aString = new StringBuilder();
        StringBuilder finalAString = new StringBuilder();

        String[] words = input.split(" ");

        for (String word : words){
            if (word.contains(".")){
                word = word.replace(".", "");
            }
            aString.append(word+" \\& ");
        }



        int cutOff = aString.length()-3;
        finalAString.append(aString.substring(0, cutOff));

        finalAString.append("\\\\\n");


        return finalAString.toString();
    }

    private static String getPOS(SyntacticStructure out) {
        StringBuilder bString = new StringBuilder();
        StringBuilder finalBString = new StringBuilder();
        List<GraphConstraint> tmp = out.constraints;

        for (GraphConstraint element : tmp){
            String test = element.toString();
            if (test.contains("POS")){
                //System.out.println(test);
                String[] something = test.split(" ");
                bString.append(something[3]);
                bString.append("/");
                String another = something[1];
                //System.out.println(another);
                String lastOne = another.substring(1);
                bString.append(lastOne);
                bString.append(" \\& ");
            }
        }

        int cutOff = bString.length()-3;
        finalBString.append(bString.substring(0, cutOff));

        finalBString.append("\\\\\n");

        return finalBString.toString();

    }

    private static String getEdges(SyntacticStructure out) {
        StringBuilder cString = new StringBuilder();
        StringBuilder finalCString = new StringBuilder();

        List<GraphConstraint> tmp = out.constraints;

        for (GraphConstraint element : tmp) {
            String test = element.toString();

            if (test.matches(".*#.*#.*")){
                //System.out.println(test);
                String[] something = test.split(" ");
                if (test.contains("root")){
                    cString.append("\\deproot{");
                    cString.append(something[3].substring(1));
                    cString.append("}{");
                    cString.append(something[2]);
                    cString.append("}\n");
                }
                else {
                    cString.append("\\depedge{");
                    cString.append(something[1].substring(1));
                    cString.append("}{");
                    cString.append(something[3].substring(1));
                    cString.append("}{");
                    cString.append(something[2]);
                    cString.append("}\n");
                }

            }
        }



        return cString.toString();
    }
}
