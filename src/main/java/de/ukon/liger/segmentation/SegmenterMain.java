package de.ukon.liger.segmentation;


import de.ukon.liger.annotation.*;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.SyntaxOperator;
import de.ukon.liger.syntax.ud.UDoperator;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.webservice.rest.LigerService;
import de.ukon.liger.webservice.rest.dtos.GkrDTO;
import de.ukon.liger.webservice.rest.dtos.LigerArgument;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.boot.actuate.endpoint.web.Link;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


                s.annotations.put("ud_parse",  udOps.parseSingle(sent.text()).toJson());

                //  words++;
                sents++;

                try {
                    //Adding semantic annotations
                    LinkedHashMap<String, Object> gkrData = loadGKR(sent.text(), "");

                //    LinkedHashMap<String, Object> rolesCtxAndPropsGraph = (LinkedHashMap<String, Object>) gkrData.get("rolesCtxAndPropertiesGraph");
                  //  LinkedHashMap<String, Object> ctxGraph = (LinkedHashMap<String, Object>) gkrData.get("contextGraph");
                    LinkedHashMap<String, Object> ctxConceptGraph = (LinkedHashMap<String, Object>) gkrData.get("rolesAndCtxGraph");

                   // List<LinkedHashMap> ctxNodes = (List<LinkedHashMap>) ctxGraph.get("nodes");

                    List<LinkedHashMap> nodes = (List<LinkedHashMap>) ctxConceptGraph.get("nodes");
                    List<LinkedHashMap> edges = (List<LinkedHashMap>) ctxConceptGraph.get("edges");


                    List<LinkedHashMap> contextEdges = edges.stream().filter(x -> x.get("label").equals("ctx_hd")).collect(Collectors.toList());

                    List<LinkedHashMap> contextNodes = new ArrayList<>();

                    /*
                    for (LinkedHashMap edge : contextEdges)
                    {
                        for (LinkedHashMap node : nodes)
                        {
                            if (edge.get("sourceVertixId").equals(node.get("id")))
                            {
                                contextNodes.add(node);
                                break;
                            }
                        }
                    }

                     */

                    contextEdges.forEach(edge -> {
                        nodes.stream()
                                .filter(node -> edge.get("sourceVertexId").equals(node.get("id")))
                                .findFirst()
                                .ifPresent(contextNodes::add);
                    });


                    Optional<LinkedHashMap> optional = contextNodes.stream().filter(x -> "top".equals(x.get("label"))).findFirst();

                    if (optional.isPresent()) {
                        LinkedHashMap top = optional.get();

                        List<LinkedHashMap> daughters = edges.stream().filter(x ->
                                x.get("sourceVertexId").equals(top.get("id"))).collect(Collectors.toList());

                        int veridicalNodes = 0;
                        int averidicalNodes = 0;
                        int antiveridicalNodes = 0;


                        //Recursive search required
                        for (LinkedHashMap daughter : daughters) {
                            if (daughter.get("label").equals("veridical")) {

                            } else if ((daughter.get("label").equals("averidical"))) {

                            } else if (daughter.get("label").equals("antiveridical")) {

                            }

                        }


                    }
                }catch(Exception e)
                {
                    System.out.println("Semantic annotation failed.");
                }






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
