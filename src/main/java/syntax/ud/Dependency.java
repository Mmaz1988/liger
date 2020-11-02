package syntax.ud;


import syntax.GraphConstraint;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

public class Dependency implements Serializable {

    private List<GraphConstraint> syntax;
    private String name;
    private LinkedHashMap<Integer,String> wordList;


    private String sentence;

    public Dependency(List<GraphConstraint> syntax, String name)
    {
        this.syntax = syntax;
        this.name = name;
    }

    public Dependency(List<GraphConstraint> syntax, String name, String sentence)
    {
        this.syntax = syntax;
        this.name = name;
        this.sentence = sentence;
    }

    public List<GraphConstraint> getSyntax() {
        return syntax;
    }

    public void setSyntax(List<GraphConstraint> syntax) {
        this.syntax = syntax;
    }


    @Override
    public String toString() {
        return name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public LinkedHashMap<Integer,String> getWordList() {
        return wordList;
    }

    public void setWordList(LinkedHashMap<Integer,String> wordList) {
        this.wordList = wordList;
    }
}
