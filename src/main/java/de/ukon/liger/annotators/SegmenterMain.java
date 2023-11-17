package de.ukon.liger.annotators;


import de.ukon.liger.analysis.RuleParser.RuleParser;
import de.ukon.liger.annotation.*;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.SyntaxOperator;
import de.ukon.liger.syntax.ud.UDoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.webservice.rest.LigerService;
import de.ukon.liger.webservice.rest.dtos.GkrDTO;
import de.ukon.liger.webservice.rest.dtos.LigerArgument;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Takes a text as input and returns a segmented annotation structure
 */
public class SegmenterMain {

    private SyntaxOperator udOperator = new UDoperator();


    public static void main(String[] args){

       // String text = args[0];
        boolean ssplit = true;

        String text = "This is an example. This is a second sentence. This is a sentence with Barack Obama";


        coreAnnotation(text);


    }

    public static void coreAnnotation(String text)
    {
        CoreDocument doc = new CoreDocument(text);

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,sentiment,udfeats");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        pipeline.annotate(doc);
        List<CoreSentence> sentences = doc.sentences();

        String laID = "la1";

        LigerAnnotation annotation = new LigerAnnotation("la1");
        annotation.text = doc.text();

        int sents = 0;
        int words = 0;

        for (CoreSentence sent : sentences) {

            String sentenceID = "s" + sents;
            String startsWith = "w" + words;

            List<Word> wordList = new ArrayList<>();
            for (CoreLabel token : sent.tokens())
            {
                String wordID = "w" + words;
                Word w = new Word(wordID,token.originalText(), annotation);
                annotation.addElementAnnotation(w);
                wordList.add(w);

                w.annotations.put("pos_tag",token.tag());


                words++;
            }
            String endsWith = "w" + (words-1);
            Span s = new Span(sentenceID, sent.text(), AnnotationTypes.sentences,startsWith,endsWith,annotation);
            s.text = sent.text();
            for (Word w : wordList)
            {
                s.addElementAnnotation(w);
            }
            annotation.addElementAnnotation(s);

            s.annotations.put("sentiment",sent.sentiment());

            //complexity features
            s.annotations.put("number_of_words",sent.tokens().size()-1);
            s.annotations.put("number of characters",s.text.length());

            //constituent complexity features
            s.annotations.put("number_of_constituents",sent.constituencyParse().constituents().size());
            s.annotations.put("constituent_parse_depth",sent.constituencyParse().depth());
            s.annotations.put("constituent_depth_length_ratio", (double) sent.tokens().size()-1/sent.constituencyParse().depth());
            s.annotations.put("nps_per_constituent_ratio", (double) sent.nounPhrases().size()/sent.constituencyParse().constituents().size());
            s.annotations.put("vps_per_constituent_ratio",(double) sent.verbPhrases().size()/sent.constituencyParse().constituents().size());

            s.annotations.put("number_of_named_entities",sent.entityMentions().size());

            s.annotations.put("named_entities", sent.entityMentions().stream().
                    map(object -> Objects.toString(object,null)).
                    collect(Collectors.joining(",")));

            //  words++;
            sents++;
        }


        System.out.println(annotation.returnLigerAnnotation());
        System.out.println(annotation.returnAsJson());
        //System.out.println(HelperMethods.getIntegerFromID("w5"));

    }

    public static Map<String,Object> coreAnnotationArgument(LigerArgument ligerArgument,  StanfordCoreNLP pipeline, UDoperator udOps) {

        //    CoreDocument doc = new CoreDocument(text);

        CoreDocument premiseDoc = new CoreDocument(ligerArgument.premise);
        CoreDocument conclusionDoc = new CoreDocument(ligerArgument.conclusion);

        String relation = ligerArgument.relation;

        Object[][] docs = new Object[2][2];
        docs[0][0] = premiseDoc;
        docs[1][0] = conclusionDoc;


        int annotations = 0;



        for (int i = 0; i < 2; i++) {

            CoreDocument doc = (CoreDocument) docs[i][0];

            pipeline.annotate(doc);
            List<CoreSentence> sentences = doc.sentences();

            String laID = "la" + annotations;

            LigerAnnotation annotation = new LigerAnnotation(laID);
            annotation.text = doc.text();

            int sents = 0;
            int words = 0;

            for (CoreSentence sent : sentences) {

                String sentenceID = "s" + sents;
                String startsWith = "w" + words;

                List<Word> wordList = new ArrayList<>();
                for (CoreLabel token : sent.tokens()) {
                    String wordID = "w" + words;
                    Word w = new Word(wordID, token.originalText(), annotation);
                    annotation.addElementAnnotation(w);
                    wordList.add(w);

                    w.annotations.put("pos_tag", token.tag());


                    words++;
                }
                String endsWith = "w" + (words - 1);
                Span s = new Span(sentenceID, sent.text(), AnnotationTypes.sentences, startsWith, endsWith, annotation);
                s.text = sent.text();
                for (Word w : wordList) {
                    s.addElementAnnotation(w);
                }

                //Adding complexity annotations
                annotation.addElementAnnotation(s);

                s.annotations.put("sentiment", sent.sentiment());

                //complexity features
                s.annotations.put("number_of_words", sent.tokens().size() - 1);
                s.annotations.put("number of characters", s.text.length());

                //constituent complexity features
                s.annotations.put("number_of_constituents", sent.constituencyParse().constituents().size());
                s.annotations.put("constituent_parse_depth", sent.constituencyParse().depth());
                s.annotations.put("constituent_depth_length_ratio", (float) sent.tokens().size() - 1 / sent.constituencyParse().depth());
                s.annotations.put("nps_per_constituent_ratio", (float) sent.nounPhrases().size() / sent.constituencyParse().constituents().size());
                s.annotations.put("vps_per_constituent_ratio", (float) sent.verbPhrases().size() / sent.constituencyParse().constituents().size());

                s.annotations.put("number_of_named_entities", sent.entityMentions().size());

                s.annotations.put("named_entities", sent.entityMentions().stream().
                        map(object -> Objects.toString(object, null)).
                        collect(Collectors.joining(",")));

                //  words++;

                try {
                    //Adding semantic annotations
                    LinkedHashMap<String, Object> gkrData = loadGKR(sent.text(), "");

                //    LinkedHashMap<String, Object> rolesCtxAndPropsGraph = (LinkedHashMap<String, Object>) gkrData.get("rolesCtxAndPropertiesGraph");
                    LinkedHashMap<String, Object> ctxGraph = (LinkedHashMap<String, Object>) gkrData.get("contextGraph");
                  //  LinkedHashMap<String, Object> ctxConceptGraph = (LinkedHashMap<String, Object>) gkrData.get("rolesAndCtxGraph");

                   // List<LinkedHashMap> ctxNodes = (List<LinkedHashMap>) ctxGraph.get("nodes");

                    List<LinkedHashMap> edges = (List<LinkedHashMap>) ctxGraph.get("edges");


                   // int contextHeads = edges.stream().filter(x -> x.get("label").equals("ctx_hd")).collect(Collectors.toList()).size();
                    int veridicalHeads = edges.stream().filter(x -> x.get("label").equals("veridical")).collect(Collectors.toList()).size() + 1;
                    int averidicalHeads = edges.stream().filter(x -> x.get("label").equals("averidical")).collect(Collectors.toList()).size();
                    int antiveridicalHeads = edges.stream().filter(x -> x.get("label").equals("antiveridical")).collect(Collectors.toList()).size();


                    s.annotations.put("veridical_ratio", (float) veridicalHeads/edges.size());
                    s.annotations.put("averidical_ratio", (float) averidicalHeads / edges.size());
                    s.annotations.put("antiveridical_ratio", (float) antiveridicalHeads / edges.size());



                    /*
                    contextEdges.forEach(edge -> {
                        nodes.stream()
                                .filter(node -> edge.get("sourceVertexId").equals(node.get("id")))
                                .findFirst()
                                .ifPresent(contextNodes::add);
                    });


                     */


                    /*

                    Optional<LinkedHashMap> optional = contextNodes.stream().filter(x -> "top".equals(x.get("label"))).findFirst();

                    if (optional.isPresent()) {
                        LinkedHashMap top = optional.get();

                        List<LinkedHashMap> daughters = edges.stream().filter(x ->
                                x.get("sourceVertexId").equals(top.get("id"))).collect(Collectors.toList());




                        //Recursive search required
                        for (LinkedHashMap daughter : daughters) {
                            if (daughter.get("label").equals("veridical")) {

                            } else if ((daughter.get("label").equals("averidical"))) {

                            } else if (daughter.get("label").equals("antiveridical")) {

                            }

                        }


                     */

                }catch(Exception e)
                {
                    System.out.println("Semantic annotation failed.");
                }

                LinguisticStructure parse = null;

                try {

                    UDoperator udParser = new UDoperator();

                    parse = udParser.parseSingle(sent.text());

                    List<LinguisticStructure> fsList = new ArrayList<>();
                    fsList.add(parse);

                    RuleParser rp = new RuleParser(Paths.get(PathVariables.testPath + "mpgFeatureRules.txt"));
                    rp.addAnnotation2(parse);

                    RuleParser rp2 = new RuleParser(Paths.get(PathVariables.testPath + "testRulesUD4b.txt"));
                    rp2.addAnnotation2(parse);

                    List<GraphConstraint> modals = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("MODAL")).collect(Collectors.toList());
                    s.annotations.put("modals", String.join(",", modals.stream().map(x -> x.getFsValue().toString()).collect(Collectors.toSet())));

                    List<GraphConstraint> propAtts = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("prop-attitude")).collect(Collectors.toList());
                    s.annotations.put("prop-atts", String.join(",", propAtts.stream().map(x -> x.getFsValue().toString()).collect(Collectors.toSet())));

                    s.annotations.put("no-of-atts", propAtts.size());

                    List<GraphConstraint> attitudeHolders = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("attitude-holder")).collect(Collectors.toList());
                    s.annotations.put("attitude-holders", String.join(",", propAtts.stream().map(x -> x.getFsValue().toString()).collect(Collectors.toSet())));
                    
                    List<GraphConstraint> embeddingVerbs = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("embedding-verb")).collect(Collectors.toList());
                    s.annotations.put("embedding-verbs", String.join(",", embeddingVerbs.stream().map(x -> x.getFsValue().toString()).collect(Collectors.toSet())));

                    List<GraphConstraint> nounMods = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("noun-advmod")).collect(Collectors.toList());
                    s.annotations.put("noun-mods", String.join(",", nounMods.stream().map(x -> x.getFsValue().toString()).collect(Collectors.toSet())));

                    List<GraphConstraint> verbMods = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("verb-advmod")).collect(Collectors.toList());
                    s.annotations.put("verb-mods", String.join(",", verbMods.stream().map(x -> x.getFsValue().toString()).collect(Collectors.toSet())));

                    List<GraphConstraint> tamConstraint = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("TAM")).collect(Collectors.toList());

                    List<GraphConstraint> nounNeg = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("noun-negation")).collect(Collectors.toList());
                    s.annotations.put("number-of-noun-negation", nounNeg.size());

                    List<GraphConstraint> verbNeg = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("verb-negation")).collect(Collectors.toList());
                    s.annotations.put("number-of-verb-negation", verbNeg.size());

                    Set<Object> tamNodes = tamConstraint.stream().map(GraphConstraint::getFsValue).collect(Collectors.toSet());

                    List<GraphConstraint> tenseAnnos = new ArrayList<>();
                            for (Object node : tamNodes) {
                               tenseAnnos.addAll(parse.annotation.stream().filter(x -> x.getFsNode().equals(node.toString()) && x.getRelationLabel().equals("TENSE")).collect(Collectors.toList()));
                            }
                    s.annotations.put("tense-markers", String.join(",", tenseAnnos.stream().map(x -> x.getFsValue().toString()).collect(Collectors.toSet())));

                    List<GraphConstraint> aspectAnnos = new ArrayList<>();
                    for (Object node : tamNodes) {
                        aspectAnnos.addAll(parse.annotation.stream().filter(x -> x.getFsNode().equals(node.toString()) && x.getRelationLabel().equals("ASPECT")).collect(Collectors.toList()));
                    }
                    s.annotations.put("aspect-markers", String.join(",", aspectAnnos.stream().map(x -> x.getFsValue().toString()).collect(Collectors.toSet())));

                            /*
                    s.annotations.put("named_entities", sent.entityMentions().stream().
                            map(object -> Objects.toString(object, null)).
                            collect(Collectors.joining(",")));
                             */

                    /*
                    List<Object> annotatedWords = parse.annotation.stream().filter(x -> x.getRelationLabel().equals("word_annotation")).map(GraphConstraint::getFsValue).collect(Collectors.toList());

                    List<GraphConstraint> allAnnotations = new ArrayList<>();

                    for (Object node : annotatedWords)
                    {
                     List<GraphConstraint> las = parse.annotation.stream().filter(x -> x.getFsNode().equals(node.toString())).collect(Collectors.toList());
                     allAnnotations.addAll(las);
                    }

                     */



                    s.annotations.put("ud_parse",  parse.toJson());



                }
                catch (Exception e)
                {
                    System.out.println("Rewrite annotation failed.");
                }





                sents++;
            }

            docs[i][1] = annotation;


        //    System.out.println(annotation.returnLigerAnnotation());
          //  System.out.println(annotation.returnAsJson());
            //System.out.println(HelperMethods.getIntegerFromID("w5"));
            annotations++;
        }

        ArgumentAnnotation argument = new ArgumentAnnotation("arg1",(LigerAnnotation) docs[0][1],(LigerAnnotation) docs[1][1]);
        argument.argumentRelation = ligerArgument.relation;
        argument.text = ligerArgument.premise + " " + ligerArgument.conclusion;

        return argument.returnLigerAnnotation();
    }


    public static LinkedHashMap loadGKR(String sentence, String context){
        GkrDTO gkrDTO = new GkrDTO(sentence,context);
        return LigerService.accessGKR(gkrDTO);
    }

}
