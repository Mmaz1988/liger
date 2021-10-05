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

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.utilities.DBASettings;
import de.ukon.liger.utilities.PathVariables;
import main.Settings;
import org.springframework.stereotype.Service;
import de.ukon.liger.syntax.SyntacticStructure;
import de.ukon.liger.syntax.ud.UDoperator;
import de.ukon.liger.webservice.ClaimRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

@Service
public class ClaimAnalysis {

    /**
     * This class processes the calls from the minimalPairController.
     */
   // public List<ClassifierProperties> classifierProperties;
    public Map<Classifier,ClassifierProperties> classifierMap;
    public final UDoperator ud = new UDoperator();

    /**
     * The constructor reads in the json object describing the properties of the different classifiers.
     * It also creates a map that maps all classifiers to their properties for easier access.
     * @throws IOException
     */

    public ClaimAnalysis() throws IOException {



        try {
            File f = new File(PathVariables.workingDirectory + "\\claim_analysis\\classifier_rule_mapping.json");


        JsonObject jo = JsonParser.parseString(Files.readString(f.toPath(), Charset.defaultCharset())).getAsJsonObject();

        List<ClassifierProperties> cpList = new ArrayList<>();
        HashMap<Classifier,ClassifierProperties> cpMap = new HashMap<>();

        JsonObject ja = jo.get("classifiers").getAsJsonObject();

        for (String key : ja.keySet())
        {

            JsonObject content = ja.get(key).getAsJsonObject();
            String rules = content.get("rules").getAsString();
            String query = content.get("query").getAsString();
            ClassifierProperties cp = new ClassifierProperties(key,rules,query);
            cpList.add(cp);
            cpMap.put(cp.cl,cp);
        }

   //     this.classifierProperties = cpList;
        this.classifierMap = cpMap;
}
           catch (Exception e)
        {
            System.out.println("Failed to load file");
        }

    }

    /***
     * This method returns the set of classifiers that apply to a given claim
     * @return Set of string identifiers of classifiers
     */

    public Set<String> searchClassifiers(String input)
    {
        Set<String> classifiers = new HashSet<>();
        SyntacticStructure synstr = ud.parseSingle(input);

       for (Classifier c : classifierMap.keySet())
       {
           if (checkForClassifier(synstr,c))
           {
               classifiers.add(c.toString());
           }
       }
        return classifiers;
    }


    public ClaimComparisonReport compareClaimRequest(ClaimRequest cr)
    {
        return compareMinimalPairElements(cr.input,cr.output,Classifier.valueOf(cr.classifier));
    }

    /**
     * Checks whether a sentence satisfies the conditions of a specific classifier
     * @param claim input sentence
     * @param classifier classifier to be checked for
     * @return
     */

    public boolean checkForClassifier(String claim, Classifier classifier) {
        SyntacticStructure synstr = ud.parseSingle(claim);
        return checkForClassifier(synstr,classifier);
    }

    //Second variant, so sentence is only parsed once
    public boolean checkForClassifier(SyntacticStructure synstr, Classifier classifier)
    {
        List<SyntacticStructure> syn = new ArrayList<>();
        syn.add(synstr);

        ClassifierProperties cp = classifierMap.get(classifier);
        if (classifier.equals(cp.cl)) {
            File f = new File(cp.path);
            RuleParser rp = new RuleParser(syn, f);
            rp.addAnnotation();

            QueryParser qp = new QueryParser(cp.query, synstr);
            qp.generateQuery();
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            if (qpr.isSuccess)
            {return true;}
            else
            {return false;}

        }

        return false;
    }


    /**
     * Checks if a specific claim satisfies a modification following a specific classifier.
     * @param input unmodified sentence
     * @param output modified sentence
     * @param classifier
     */
    public ClaimComparisonReport compareMinimalPairElements(String input, String output, Classifier classifier)
    {

        //First produce a UD parse for both structures

        SyntacticStructure inputStructure = ud.parseSingle(input);
        SyntacticStructure outputStructure = ud.parseSingle(output);

        List<SyntacticStructure> inout = new ArrayList<>();

        inout.add(inputStructure);
        inout.add(outputStructure);

        File inFile;
        String queryString = "";
        RuleParser rp;

        //Find ClassifierProperties for specified classifier
        inFile = new File(classifierMap.get(classifier).path);
        rp = new RuleParser(inout,inFile);
        rp.addAnnotation();
        queryString = classifierMap.get(classifier).query;

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
