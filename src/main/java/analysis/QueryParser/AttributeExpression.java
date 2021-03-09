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

package analysis.QueryParser;

import syntax.GraphConstraint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class AttributeExpression extends QueryExpression {


    private QueryExpression left;
    private Attribute right;

    public AttributeExpression(QueryExpression left, Attribute right) {
        this.left = left;
        this.right = right;

        setParser(left.getParser());
        setQuery(right.getQuery());
        setNodeVar(left.getNodeVar());

        calculateSolutions();
    }

    @Override
    public void calculateSolutions()
    {
        HashMap<Set<SolutionKey>,HashMap<Integer,GraphConstraint>> out = left.getSolution();

        HashMap<Integer,GraphConstraint> fsIndices = new HashMap<>();

            Iterator<Set<SolutionKey>> it = out.keySet().iterator();


            while (it.hasNext())
            {
                Set<SolutionKey> key = it.next();
                Boolean containsAtribute = false;


                for (SolutionKey skey : key)
                {
                    if (skey.variable.equals(left.getNodeVar()))
                    {
                        String nodeRef = skey.reference;
                    }
                }



                String nodeRef = left.getSolution().get(key).get(left.getNodeVar()).keySet().stream().findAny().get();
                HashMap<Integer,GraphConstraint> boundIndices = left.getSolution().get(key).get(left.getNodeVar()).get(nodeRef);

                    if (left instanceof Node || left instanceof NodeExpression)
                    {
                     for (Integer key2 : right.getFsIndices().keySet())
                     {
                         if (right.getFsIndices().get(key2).getFsNode().equals(nodeRef))
                         {
                             boundIndices.put(key2,right.getFsIndices().get(key2));
                             fsIndices.put(key2,right.getFsIndices().get(key2));
                             containsAtribute = true;
                         }
                     }
                    }

                    /*
                Iterator<Integer> boundIter = boundIndices.keySet().iterator();

                    while (boundIter.hasNext())
                    {
                        Integer key3 = boundIter.next();

                        if(boundIndices.get(key3).getRelationLabel().equals(right.getQuery()))
                        {
                            fsIndices.put(key3,boundIndices.get(key3));
                            containsAtribute = true;
                        } else
                        {
                            boundIter.remove();
                        }

                    }
*/
/*
                    for (Integer key3 : boundIndices.keySet())
                    {
                        if(boundIndices.get(key3).getRelationLabel().equals(right.getQuery()))
                        {

                            fsIndices.put(key3,boundIndices.get(key3));
                            containsAtribute = true;
                        }

                    }
*/
            if (!containsAtribute)
            {
                it.remove();
            }
        }
            setNodeVar(left.getNodeVar());
            setSolution(out);
            setFsIndices(fsIndices);
    }




}
