/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

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
