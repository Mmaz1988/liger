package syntax.ud;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class DependencyParser {

    public enum language {
        GERMAN,
        ENGLISH
    }

    public language LNG;

    private final static String PCG_MODEL_ENGLISH = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
    private final static String PCG_MODEL_GERMAN = "C:\\Users\\User\\IdeaProjects\\xle_operator\\resources\\edu\\stanford\\nlp\\models\\lexparser\\germanPCFG.ser.gz";


    private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");

    private final TreebankLanguagePack tlp = new PennTreebankLanguagePack();

    //for dependencies
    private final GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

    private LexicalizedParser parser = null;

    public List<GrammaticalStructure> parseSet;


    public DependencyParser()
    {
        this.parser = LexicalizedParser.loadModel(PCG_MODEL_ENGLISH);
    }

    public DependencyParser(language LNG)
    {
        this.LNG = LNG;

        switch(LNG)
        {
            case GERMAN:
                this.parser = LexicalizedParser.loadModel(PCG_MODEL_GERMAN);
                break;
            case ENGLISH:
                this.parser = LexicalizedParser.loadModel(PCG_MODEL_ENGLISH);
                break;
        }
    }

    /*
    public DependencyParser(Boolean is_text_english) {
        if (is_text_english) {
            PCG_MODEL = PCG_MODEL_GERMAN;
        }
        else{
            PCG_MODEL = PCG_MODEL_ENGLISH;
        }
    }
     */



    //Main method for testing parser functionalities

    /*

    public static void main(String[] args) {

        DependencyParser dp = new DependencyParser();

        List<String> testSentences = new ArrayList<String>();

        testSentences.add("John snores.");

        // TODO: For some reason ungrammatical
        // testSentences.add("Cats chase dogs.");
        testSentences.add("John loves Maria.");
        testSentences.add("Every man loves a woman.");
        testSentences.add("John said that Mary was sick.");



        DependencyParser parser = new DependencyParser();

        for (String sentence : testSentences)
        {
            Tree tree = parser.parse(sentence);
            GrammaticalStructure gs = dp.gsf.newGrammaticalStructure(tree);


            tree.pennPrint();
            System.out.print(gs.typedDependenciesEnhanced());
            System.out.println();
            System.out.print(gs.typedDependencies());
        }
    }
*/

    public List<GrammaticalStructure> generateParses(List<String> sentences) {

        List<GrammaticalStructure> parsedSentences = new ArrayList<>();

        for (String sentence : sentences)
        {
            Tree tree = parser.parse(sentence);
            GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);


            parsedSentences.add(gs);
        }
        return parsedSentences;
    }


    public Tree parse(String str) {
        List<CoreLabel> tokens = tokenize(str);
        Tree tree = parser.apply(tokens);
        return tree;
    }

    public List<CoreLabel> tokenize(String str) {
        Tokenizer<CoreLabel> tokenizer =
                tokenizerFactory.getTokenizer(
                        new StringReader(str));


        return tokenizer.tokenize();



    }

    }


