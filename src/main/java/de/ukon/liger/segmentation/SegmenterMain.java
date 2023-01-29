package de.ukon.liger.segmentation;


import de.ukon.liger.annotation.*;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.webservice.rest.LigerService;
import de.ukon.liger.webservice.rest.dtos.LigerArgument;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Takes a text as input and returns a segmented annotation structure
 */
public class SegmenterMain {


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

    public static Map<String,Object> coreAnnotationArgument(LigerArgument ligerArgument, List<LinkedHashMap> semanticParses,  StanfordCoreNLP pipeline) {

        //    CoreDocument doc = new CoreDocument(text);

        CoreDocument premiseDoc = new CoreDocument(ligerArgument.premise);
        CoreDocument conclusionDoc = new CoreDocument(ligerArgument.conclusion);

        String relation = ligerArgument.relation;

        Object[][] docs = new Object[2][3];
        docs[0][0] = premiseDoc;
        docs[1][0] = conclusionDoc;
        docs[0][1] = semanticParses.get(0);
        docs[1][1] = semanticParses.get(1);

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
                sents++;
            }

            docs[i][2] = annotation;


        //    System.out.println(annotation.returnLigerAnnotation());
          //  System.out.println(annotation.returnAsJson());
            //System.out.println(HelperMethods.getIntegerFromID("w5"));
            annotations++;
        }

        ArgumentAnnotation argument = new ArgumentAnnotation("arg1",(LigerAnnotation) docs[0][2],(LigerAnnotation) docs[1][2]);
        argument.argumentRelation = ligerArgument.relation;
        argument.text = ligerArgument.premise + " " + ligerArgument.conclusion;

        return argument.returnLigerAnnotation();
    }

}
