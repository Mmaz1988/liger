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

package de.ukon.liger.claimanalysis;

import org.junit.jupiter.api.Test;
import de.ukon.liger.utilities.PathVariables;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestClaimAnalysis {


    public TestClaimAnalysis()
    {
        PathVariables.workingDirectory = "C:\\Users\\Celeste\\IdeaProjects\\LiGER\\resources\\";
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

}
