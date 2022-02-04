package de.ukon.liger.cuepaq.mmpanalyzer;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinimalPairAnalyzer {

    public static Properties properties = new Properties();
    public  StanfordCoreNLP sfc;

    private final static Logger LOGGER = LoggerFactory.getLogger(MinimalPairAnalyzer.class);



    public MinimalPairAnalyzer(StanfordCoreNLP sfc){
        properties.setProperty("annotators","tokenize,ssplit");
        this.sfc = sfc;
    }



    public static void main(String[] args) {
        StanfordCoreNLP sfc = new StanfordCoreNLP(properties);

        MinimalPairAnalyzer mmp = new MinimalPairAnalyzer(sfc);

        LOGGER.info("Starting interactive mode...\n");
        Scanner s = new Scanner(System.in);

        String arg1 = "";
        String arg2 = "";

        while (true) {
            LOGGER.info("Enter first sentence to be compared:");
         arg1 = s.nextLine();

            LOGGER.info("Enter second sentence to be compared:");
           arg2 = s.nextLine();
            break;
            //if (input.equals("quit"))
            //    bre
        }
        mmp.compareArguments(arg1,arg2);
    }

    public boolean compareArguments(String arg1, String arg2)
    {
        if (arg1.equals(arg2))
        {
            return false;
        }

        CoreDocument cd1 = new CoreDocument(arg1);
        sfc.annotate(cd1);

        CoreDocument cd2 = new CoreDocument(arg2);
        sfc.annotate(cd2);

        List<CoreLabel> arg1tokens = cd1.tokens();
        List<CoreLabel> arg2tokens = cd2.tokens();

        int j = 0;

        if (arg1tokens.size() == arg2tokens.size()) {
            for (int i = 0; i < arg1tokens.size(); i++) {
                if (!arg1tokens.get(i).word().equals(arg2tokens.get(i).word())) {
                    j++;
                }
            }
            return j == 1;
        } else
        {
            j = Math.abs(arg1tokens.size() - arg2tokens.size());
            if (!(j >= 2))
            {

                ListIterator<CoreLabel> iter1 = arg1tokens.listIterator();


                while (iter1.hasNext())
                {
                    CoreLabel t1 = iter1.next();
                    ListIterator<CoreLabel> iter2 = arg2tokens.listIterator();
                    while(iter2.hasNext())
                    {
                        CoreLabel t2 = iter2.next();
                        if (t1.word().equals(t2.word()))
                        {
                            iter1.remove();
                            iter2.remove();
                            break;
                        }
                    }
                }

                if ((!arg1tokens.isEmpty() && !arg2tokens.isEmpty()) ||
                        (arg1tokens.isEmpty() && arg2tokens.isEmpty()))
                {
                    return false;
                }

                if (arg1tokens.isEmpty() && arg2tokens.size() == 1)
                {
                    return true;
                }

                if (arg2tokens.isEmpty() && arg1tokens.size() == 1)
                {
                    return true;
                }



            } else
            {
                return false;
            }
        }

        return false;

    }



}
