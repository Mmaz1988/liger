package de.ukon.liger.segmentation;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * Takes a text as input and returns a segmented annotation structure
 */
public class SegmenterMain {

    public static void main(String[] args){

       // String text = args[0];

        String text = "This is an example. This is a second sentence.";

        CoreDocument doc = new CoreDocument(text);

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        pipeline.annotate(doc);
        List<CoreSentence> sentences = doc.sentences();

        for (CoreSentence sent : sentences) {
            System.out.println(sent.tokens());
            for (CoreLabel token : sent.tokens())
            {
                System.out.println(token.tag());
            }
          System.out.println(sent.constituencyParse());
            System.out.println(sent.constituencyParse().constituents().size());
        }



    }

}
