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

package claimanalysis;

import analysis.QueryParser.QueryParser;
import analysis.QueryParser.QueryParserResult;
import analysis.RuleParser.RuleParser;
import org.springframework.stereotype.Service;
import syntax.SyntacticStructure;
import syntax.ud.UDoperator;
import webservice.ClaimRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ClaimAnalysis {

    /***
    The database should contain the necessary rules
     */
    public Object database;


    public ClaimAnalysis(){};


    /***
     * This method returns the set of classifiers that apply to a given claim
     * @return Set of string identifiers of classifiers
     */

    public Set<String> analyzeClaim()
    {

        return null;
    }


    public ClaimComparisonReport compareClaimRequest(ClaimRequest cr)
    {
        return compareMinimalPairElements(cr.input,cr.output,cr.classifier);
    }


    /**
     * Checks if a specific claim satisfies a modification following a specific classifier.
     * @param input unmodified sentence
     * @param output modified sentence
     * @param classifier
     */
    public ClaimComparisonReport compareMinimalPairElements(String input, String output, String classifier)
    {

        //First produce a UD parse for both structures
        UDoperator ud = new UDoperator();
        SyntacticStructure inputStructure = ud.parseSingle(input);
        SyntacticStructure outputStructure = ud.parseSingle(output);

        List<SyntacticStructure> inout = new ArrayList<>();

        inout.add(inputStructure);
        inout.add(outputStructure);

        File inFile;
        String queryString = "";
        RuleParser rp;

        //Specify rule set to use according to classifier
        switch(classifier){
            case"CPST":

                inFile = new File("C:\\Users\\Celeste\\IdeaProjects\\SpringDemo\\resources\\testFiles\\testRulesUD4c.txt");
                rp = new RuleParser(inout,inFile);
                rp.addAnnotation();

                queryString = "#a root #b MORPH #c TAM #d TENSE 'past'";
                break;
            case"CPRS":

                inFile = new File("C:\\Users\\Celeste\\IdeaProjects\\SpringDemo\\resources\\testFiles\\testRulesUD4c.txt");
                rp = new RuleParser(inout,inFile);
                rp.addAnnotation();

                queryString = "#a root #b MORPH #c TAM #d TENSE 'pres'";
                break;
            case"CFUT":
                inFile = new File("C:\\Users\\Celeste\\IdeaProjects\\SpringDemo\\resources\\testFiles\\testRulesUD4c.txt");
                rp = new RuleParser(inout,inFile);

                queryString = "#a root #b MORPH #c TAM #d TENSE 'future'";
                break;
        }


        //Search for query to check whether classifier has been applied
        QueryParserResult[] qprs = new QueryParserResult[2];

        for (int i = 0; i < inout.size(); i++) {
            SyntacticStructure fs = inout.get(i);
            QueryParser qp = new QueryParser(queryString, fs);
            qp.generateQuery();
            qprs[i] = qp.parseQuery(qp.getQueryList());

        }

        boolean success = false;
        String explanation = "";

        if (!qprs[0].isSuccess && qprs[1].isSuccess)
        {
             success = true;
        }

        if (!success)
        {
            explanation = "Searching for explanation!";
        }

        if (qprs[0].isSuccess && qprs[1].isSuccess)
        {
            explanation = "The two input claims receive the same value for classifier " + classifier;
        }


        return new ClaimComparisonReport(success,explanation);
            }

}
