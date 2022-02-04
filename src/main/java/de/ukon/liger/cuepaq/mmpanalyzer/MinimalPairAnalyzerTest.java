package de.ukon.liger.cuepaq.mmpanalyzer;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinimalPairAnalyzerTest {

    @Test
    public void testMinimalPairAnalyzer()
    {
        String arg1 = "John loves Mary.";
        String arg2 = "John really loves Mary.";
        String arg3 = "Maybe John loves Mary.";
        String arg4 ="asdfasdf";
        String arg5 = "";


        Properties props = new Properties();
        props.setProperty("annotators","tokenize,ssplit");

        StanfordCoreNLP sfc = new StanfordCoreNLP(props);
        MinimalPairAnalyzer mmp = new MinimalPairAnalyzer(sfc);

        assertEquals(false, mmp.compareArguments(arg1,arg1));
        assertEquals(true,mmp.compareArguments(arg1,arg2));
        assertEquals(true,mmp.compareArguments(arg2,arg1));
        assertEquals(true,mmp.compareArguments(arg1,arg3));
        assertEquals(true,mmp.compareArguments(arg3,arg1));
        assertEquals(false,mmp.compareArguments(arg2,arg3));
        assertEquals(false,mmp.compareArguments(arg3,arg2));
        assertEquals(false,mmp.compareArguments(arg1,arg4));
        assertEquals(false,mmp.compareArguments(arg4,arg1));
        assertEquals(false,mmp.compareArguments(arg1,arg5));
        assertEquals(false,mmp.compareArguments(arg5,arg1));
        assertEquals(false,mmp.compareArguments(arg5,arg1));
    }
}
