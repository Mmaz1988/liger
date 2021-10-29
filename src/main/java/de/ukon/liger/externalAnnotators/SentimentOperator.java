package de.ukon.liger.externalAnnotators;

import de.ukon.liger.syntax.LinguisticStructure;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class SentimentOperator {

    StanfordCoreNLP annotator;

    public SentimentOperator(StanfordCoreNLP annotator)
    {
        this.annotator = annotator;
    }

    public void annotateSentiment(LinguisticStructure str)
    {
        CoreDocument cd = new CoreDocument(str.text);

        annotator.annotate(cd);



    }
}
