/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

package de.ukon.liger.syntax.ud;

import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.GraphConstraint;

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
                LinguisticStructure out = udops.parseSingle(input);
                //System.out.println(out.constraints);
                System.out.println(newString(input, out));


            }

        }

    }

    public static String newString(String input, LinguisticStructure out) {
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

    private static String getPOS(LinguisticStructure out) {
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

    private static String getEdges(LinguisticStructure out) {
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
