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

package de.ukon.liger.cuepaq.claimanalysis;

import org.junit.jupiter.api.Test;
import de.ukon.liger.utilities.PathVariables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestClaimAnalysis {


    public TestClaimAnalysis()
    {
    //    PathVariables.workingDirectory = "C:\\Users\\Celeste\\IdeaProjects\\LiGER\\liger_resources\\";
        PathVariables.initializePathVariables();
    }

    @Test
    void testClaimComparison() throws IOException {
        String sent1 = "John visits his grandmother.";
        String sent2 = "John visited his grandmother.";

        ClaimAnalysis ca = new ClaimAnalysis();

        ClaimComparisonReport rp = ca.compareMinimalPairElements(sent1,sent2, Classifier.valueOf("CPST"));

        assertTrue(rp.success);
        assertEquals("",rp.explanation);
    }

    @Test
    void testClaimComparison2() throws IOException {

        String sent1 = "John visits his grandmother.";
        String sent2 = "John visits his grandmother.";

        ClaimAnalysis ca = new ClaimAnalysis();

        ClaimComparisonReport rp = ca.compareMinimalPairElements(sent1,sent2, Classifier.valueOf("CPST"));

        assertTrue(!rp.success);
        assertEquals("Searching for explanation!",rp.explanation);
    }


    @Test
    void testClaimComparison3() throws IOException {

        String sent1 = "John visited his grandmother.";
        String sent2 = "John visits his grandmother.";

        ClaimAnalysis ca = new ClaimAnalysis();

        ClaimComparisonReport rp = ca.compareMinimalPairElements(sent1,sent2, Classifier.CPRS);

        assertTrue(rp.success);
        assertEquals("",rp.explanation);
    }

    @Test
    void testClaimComparison4() throws IOException {

        String sent1 = "John visits his grandmother.";
        String sent2 = "John visits his grandmother.";

        ClaimAnalysis ca = new ClaimAnalysis();

        ClaimComparisonReport rp = ca.compareMinimalPairElements(sent1,sent2, Classifier.valueOf("CPRS"));

        assertTrue(!rp.success);
 //       assertEquals("Searching for explanation!",rp.explanation);
        System.out.println(rp.explanation);
    }

    @Test
    void testClaimComparison5() throws IOException {

        String sent1 = "John visits his grandmother.";
        String sent2 = "John has visited his grandmother.";

        ClaimAnalysis ca = new ClaimAnalysis();

        ClaimComparisonReport rp = ca.compareMinimalPairElements(sent1,sent2, Classifier.valueOf("CPSP"));

        assertTrue(rp.success);
        //       assertEquals("Searching for explanation!",rp.explanation);
        System.out.println(rp.explanation);
    }


    @Test
    void testCheckClassifier() throws IOException {
        String input = "John visits his grandmother.";

        ClaimAnalysis ca = new ClaimAnalysis();

        assertTrue(ca.checkForClassifier(input,Classifier.valueOf("CPRS")));
    }

    @Test
    void testCheckClassifier1() throws IOException {
        String input = "John has visited his grandmother.";

        ClaimAnalysis ca = new ClaimAnalysis();

        assertTrue(ca.checkForClassifier(input,Classifier.valueOf("CPRS")));
        assertTrue(ca.checkForClassifier(input,Classifier.valueOf("CPSP")));
    }

    @Test
    void testSearchClassifiers() throws IOException{
        String input = "John has visited his grandmother.";

        ClaimAnalysis ca = new ClaimAnalysis();

        assertTrue(ca.searchClassifiers(input).size() == 2);

    }

    @Test
    void testSearchClassifiers1() throws IOException{
        String input = "John visited his grandmother.";

        ClaimAnalysis ca = new ClaimAnalysis();

        assertTrue(ca.searchClassifiers(input).size() == 1);

    }

    @Test
    void testSearchClassifiers2() throws IOException {
    String input = "Jamie visited Erin. They played board games together.";

    ClaimAnalysis ca = new ClaimAnalysis();

        Map<String,Boolean> eval = ca.checkForClassifier2(input,Classifier.CPST);

        for (String key : eval.keySet())
        {
         assertTrue(eval.get(key));
        }
    }


    @Test
    public void testMinimalPairAnalyzer() throws IOException {
        String arg1 = "John loves Mary.";
        String arg2 = "John really loves Mary.";
        String arg3 = "Maybe John loves Mary.";
        String arg4 ="asdfasdf";
        String arg5 = "";
        String arg6 = "Peter really loves Tanja.";


        ClaimAnalysis ca = new ClaimAnalysis();

        List<ClaimComparisonReport>  reports = new ArrayList<>();

        //false
        reports.add(ca.compareMinimalPair(arg1,arg1));

        //true
        reports.add(ca.compareMinimalPair(arg1,arg2));
        reports.add(ca.compareMinimalPair(arg2,arg1));

        //true
        reports.add(ca.compareMinimalPair(arg1,arg3));
        reports.add(ca.compareMinimalPair(arg3,arg1));


        //false
        reports.add(ca.compareMinimalPair(arg2,arg3));
        reports.add(ca.compareMinimalPair(arg3,arg2));
        reports.add(ca.compareMinimalPair(arg1,arg4));
        reports.add(ca.compareMinimalPair(arg4,arg1));
        reports.add(ca.compareMinimalPair(arg1,arg5));
        reports.add(ca.compareMinimalPair(arg5,arg1));

        reports.add(ca.compareMinimalPair(arg6,arg1));
        reports.add(ca.compareMinimalPair(arg1,arg6));

        for (ClaimComparisonReport r : reports)
        {
            System.out.println(r.success + ">>\n" + r.explanation);
        }


    }


}
