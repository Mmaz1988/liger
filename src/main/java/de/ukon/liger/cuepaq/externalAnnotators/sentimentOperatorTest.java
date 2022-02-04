package de.ukon.liger.cuepaq.externalAnnotators;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

public class sentimentOperatorTest {


    @Test
    public void testSentimentOperator()
    {
        String input = "Unfortunately, Erin visits his mother.";

        Properties props = new Properties();
        props.setProperty("annotators","tokenize,ssplit,pos,parse,sentiment");

        StanfordCoreNLP sfc = new StanfordCoreNLP(props);

        CoreDocument cd = new CoreDocument(input);

        sfc.annotate(cd);

       Object sentimentTree = cd.sentences().stream().findAny().get().sentimentTree();
        String sentiment = cd.sentences().stream().findAny().get().sentiment();

        assertEquals("Negative",sentiment);
    }
}
