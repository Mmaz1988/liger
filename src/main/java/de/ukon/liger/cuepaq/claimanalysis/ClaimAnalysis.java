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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ukon.liger.analysis.QueryParser.QueryParser;
import de.ukon.liger.analysis.QueryParser.QueryParserResult;
import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.claimanalysis.ClassifierRuleMapping;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.ud.UDoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.webservice.rest.dtos.ClaimRequest;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ClaimAnalysis {

    private final Logger LOGGER = LoggerFactory.getLogger(ClaimAnalysis.class);

    /**
     * This class processes the calls from the minimalPairController.
     */
    // public List<ClassifierProperties> classifierProperties;
    public Map<Classifier, ClassifierProperties> classifierMap;
    public final UDoperator ud = new UDoperator();
    public StanfordCoreNLP pipeline;

    /**
     * The constructor reads in the json object describing the properties of the different classifiers.
     * It also creates a map that maps all classifiers to their properties for easier access.
     *
     * @throws IOException
     */

    public ClaimAnalysis() throws IOException {
        LOGGER.warn("creating new claim analysis object");

        try {
            LOGGER.warn("Trying to load json from {}", PathVariables.workingDirectory + "claim_analysis/classifier_rule_mapping.json");
            this.classifierMap = readClassifierMap();
        } catch (Exception e) {
            System.out.println("Failed to load file");
            e.printStackTrace();
        }

        Properties props = new Properties();
        props.setProperty("annotators","tokenize,ssplit,pos,parse,sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public static ClassifierRuleMapping readClassifierRuleMapping() throws IOException {
        File f = new File(PathVariables.workingDirectory + "claim_analysis/classifier_rule_mapping.json");

        ObjectMapper om = new ObjectMapper();
        return om.readValue(f, ClassifierRuleMapping.class);
    }

    public static Map<Classifier, ClassifierProperties> readClassifierMap() throws IOException {
        return readClassifierRuleMapping().getClassifiers();
    }

    public static void writeClassifierMap(ClassifierRuleMapping rm) throws IOException {
        File f = new File(PathVariables.workingDirectory + "claim_analysis/classifier_rule_mapping.json");
        ObjectMapper om = new ObjectMapper();
        om.writeValue(f, rm);
    }

    /***
     * This method returns the set of classifiers that apply to a given claim
     * @return Set of string identifiers of classifiers
     */

    public Set<Classifier> searchClassifiers(String input) {
        Set<Classifier> classifiers = new HashSet<>();
        LinguisticStructure synstr = ud.parseSingle(input);

        for (Classifier c : classifierMap.keySet()) {
            if (checkForClassifier(synstr, c)) {
                classifiers.add(c);
            }
        }
        return classifiers;
    }

    public Set<Classifier> getNonPresentClassifiers(String input) {
        final Set<Classifier> presentClassifiers = searchClassifiers(input);
        final Set<Classifier> allClassifiers = Set.of(Classifier.values());
        return Sets.diff(allClassifiers, presentClassifiers);
    }


    public ClaimComparisonReport compareClaimRequest(ClaimRequest cr) {
        return compareMinimalPairElements(cr.input, cr.output, Classifier.valueOf(cr.classifier));
    }

    /**
     * Checks whether a sentence satisfies the conditions of a specific classifier
     *
     * @param claim      input sentence
     * @param classifier classifier to be checked for
     * @return
     */


    public boolean checkForClassifier(String claim, Classifier classifier) {

        CoreDocument doc = new CoreDocument(claim);
        this.pipeline.annotate(doc);


        LinguisticStructure synstr = ud.parseSingle(claim);

        return checkForClassifier(synstr, classifier);
    }



    public Map<String,Boolean> checkForClassifier2(String claim, Classifier classifier) {

        CoreDocument doc = new CoreDocument(claim);
        this.pipeline.annotate(doc);

        Map<String,Boolean> sentenceMap = new HashMap<>();

        for (CoreSentence sent : doc.sentences())
        {
            LinguisticStructure synstr = ud.parseSingle(sent.text());
            sentenceMap.put(synstr.local_id,checkForClassifier(synstr,classifier));
        }

        return sentenceMap;
    }


    //Second variant, so sentence is only parsed once
    public boolean checkForClassifier(LinguisticStructure synstr, Classifier classifier) {
        List<LinguisticStructure> syn = new ArrayList<>();
        syn.add(synstr);

        ClassifierProperties cp = classifierMap.get(classifier);
        if (classifier.equals(cp.cl)) {
            if (!cp.path.equals(""));
            {
                RuleParser rp = new RuleParser(syn, Paths.get(PathVariables.workingDirectory + cp.path));
                rp.addAnnotation();
            }

            QueryParser qp = new QueryParser(cp.query, synstr);
            qp.generateQuery();
            QueryParserResult qpr = qp.parseQuery(qp.getQueryList());

            if (qpr.isSuccess) {
                return true;
            } else {
                return false;
            }

        }

        return false;
    }


    /**
     * Checks if a specific claim satisfies a modification following a specific classifier.
     *
     * @param input      unmodified sentence
     * @param output     modified sentence
     * @param classifier
     */
    public ClaimComparisonReport compareMinimalPairElements(String input, String output, Classifier classifier) {

        //First produce a UD parse for both structures

        LinguisticStructure inputStructure = ud.parseSingle(input);
        LinguisticStructure outputStructure = ud.parseSingle(output);

        List<LinguisticStructure> inout = new ArrayList<>();

        inout.add(inputStructure);
        inout.add(outputStructure);

        String inFile;
        String queryString = "";
        RuleParser rp;

        //Find ClassifierProperties for specified classifier
        inFile = classifierMap.get(classifier).path;
        rp = new RuleParser(inout, Paths.get(PathVariables.workingDirectory + inFile));
        rp.addAnnotation();
        queryString = classifierMap.get(classifier).query;

        //Search for query to check whether classifier has been applied
        QueryParserResult[] qprs = new QueryParserResult[2];

        for (int i = 0; i < inout.size(); i++) {
            LinguisticStructure fs = inout.get(i);
            QueryParser qp = new QueryParser(queryString, fs);
            qp.generateQuery();
            qprs[i] = qp.parseQuery(qp.getQueryList());

        }

        boolean success = false;
        String explanation = "";

        if (!qprs[0].isSuccess && qprs[1].isSuccess) {
            success = true;
        }

        if (!success) {
            explanation = "Searching for explanation!";
        }

        if (qprs[0].isSuccess && qprs[1].isSuccess) {
            explanation = "The two input claims receive the same value for classifier " + classifier;
        }


        return new ClaimComparisonReport(success, explanation);
    }



    /**
     * Checks if a specific claim satisfies a modification following a specific classifier.
     *
     * @param input      unmodified sentence
     * @param output     modified sentence
     * @param classifier
     */
    public ClaimComparisonReport compareMinimalPairElements2(String input, String output, Classifier classifier) {

        //First produce a UD parse for both structures

        CoreDocument inputDoc = new CoreDocument(input);
        CoreDocument outputDoc = new CoreDocument(output);

        this.pipeline.annotate(inputDoc);
        this.pipeline.annotate(outputDoc);

        List<LinguisticStructure> inputStructures = new ArrayList<>();
        List<LinguisticStructure> outputStructures = new ArrayList<>();
        List<LinguisticStructure> syntacticStructures = new ArrayList<>();

        for (CoreSentence sent : inputDoc.sentences())
        {
            LinguisticStructure inputStructure = ud.parseSingle(sent.text());
            inputStructures.add(inputStructure);
            syntacticStructures.add(inputStructure);
        }

        for (CoreSentence sent : outputDoc.sentences())
        {
            LinguisticStructure outputStructure = ud.parseSingle(sent.text());
            outputStructures.add(outputStructure);
            syntacticStructures.add(outputStructure);
        }



        List<LinguisticStructure>[] inout = new List[2];

        inout[0] = inputStructures;
        inout[1] = outputStructures;

        String inFile;
        String queryString = "";
        RuleParser rp;

        //Find ClassifierProperties for specified classifier
        inFile = classifierMap.get(classifier).path;
        rp = new RuleParser(syntacticStructures, Paths.get(PathVariables.workingDirectory + inFile));
        rp.addAnnotation();
        queryString = classifierMap.get(classifier).query;

        //Search for query to check whether classifier has been applied
        QueryParserResult[] qprs = new QueryParserResult[2];

        /*
        for (int i = 0; i < inout.length; i++) {
            LinguisticStructure fs = inout.get(i);
            QueryParser qp = new QueryParser(queryString, fs);
            qp.generateQuery();
            qprs[i] = qp.parseQuery(qp.getQueryList());

        }
         */

        boolean success = false;
        String explanation = "";

        if (!qprs[0].isSuccess && qprs[1].isSuccess) {
            success = true;
        }

        if (!success) {
            explanation = "Searching for explanation!";
        }

        if (qprs[0].isSuccess && qprs[1].isSuccess) {
            explanation = "The two input claims receive the same value for classifier " + classifier;
        }


        return new ClaimComparisonReport(success, explanation);
    }
}
