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

package de.ukon.liger.syntax.xle.prolog2java;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.utilities.HelperMethods;
import de.ukon.liger.utilities.VariableHandler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//This class represents paths in the f-structure between two f-structure nodes;
public class FsPath {

    private Deque<String> path = new LinkedList<>();
    private VariableHandler vh = new VariableHandler();
    private boolean ignoreRoot;



    public FsPath(GraphConstraint fsC, List<GraphConstraint> fs, Boolean ignoreRoot) {
        this.ignoreRoot = ignoreRoot;
        generatePath(fsC, fs);

    }

    public FsPath(GraphConstraint fsC, List<GraphConstraint> fs, VariableHandler vh, Boolean ignoreRoot) {
        this.vh = vh;
        this.ignoreRoot = ignoreRoot;
        generatePath(fsC, fs);
    }

    public FsPath(GraphConstraint fsIn, GraphConstraint fsTarget, VariableHandler vh, List<GraphConstraint> fs) {

        this.vh = vh;

      Deque<String> out = generateRelationWrapper(fsIn, fsTarget,fs, new LinkedList<>(),true);


        if (out != null)
        {
            this.path = out;
        }
        else{
            this.path = generateRelationWrapper(fsTarget,fsIn,fs,new LinkedList<>(),false);
        }

    }



    //TODO might be faster if we use hashmap isntead of linkedHashMap

    public boolean generatePath(GraphConstraint fsC, List<GraphConstraint> fs) {


        if (ignoreRoot == true)
        {
            List<GraphConstraint> fsCopy = new ArrayList<>(fs);

            for (GraphConstraint fsc : fs)
            {
                if (fsc.getRelationLabel().equals("root"))
                {
                    fsCopy.remove(fsc);
                }
            }
            fs = fsCopy;
        }

        for (GraphConstraint fsvar : fs) {
            if (fsC.getFsNode().equals(fsvar.getFsValue())) {
                if (HelperMethods.isInteger(fsC.getFsValue())) {


                    String var = vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                            Integer.parseInt(fsC.getFsValue().toString()));

                    boolean contains = false;

                    for (Integer key : vh.getAssignment().keySet())
                    {
                        if (!vh.getAssignment().get(key).contains(var))
                        {
                            contains = true;
                        }
                    }


                    path.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                                                                Integer.parseInt(fsC.getFsValue().toString())));
                } else {
                    path.addFirst(fsC.getFsValue().toString());
                }
                path.addFirst(fsC.getRelationLabel());


                System.out.println(fsvar.toString());
                System.out.println(path);
                return generatePath(fsvar, fs);
            }
        }

        if (HelperMethods.isInteger(fsC.getFsValue())) {
            path.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                                                        Integer.parseInt(fsC.getFsValue().toString())));
        } else {
            path.addFirst(fsC.getFsValue().toString());
        }

        path.addFirst(fsC.getRelationLabel());
        path.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                                                Integer.parseInt(fsC.getFsNode())));

        return true;
    }


    public Deque<String> generateRelationWrapper(GraphConstraint start, GraphConstraint end,
                                                 List<GraphConstraint> fs, Deque<String> re, Boolean in)
    {
        return generateRelation(start, end,end,fs,re, true);
    }


    public Deque<String> generateRelation(GraphConstraint start, GraphConstraint ladder, GraphConstraint end,
                                          List<GraphConstraint> fs, Deque<String> re,
                                          Boolean down
                                         ) {

        List<GraphConstraint> fsCopy = new ArrayList<>(fs);



        if (!(HelperMethods.isInteger(start.getFsValue()) && HelperMethods.isInteger(ladder.getFsValue())))
        {
            return null;
        }




            if (down) {


                if (ladder.equals(start)) {
                    re.addFirst("#" +
                            vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                                    Integer.parseInt(start.getFsValue().toString())));
        //            re.addFirst(ladder.getRelationLabel());
          //          re.addFirst("#" +
            //                vh.returnNewVar(VariableHandler.variableType.FS_NODE,
              //                      Integer.parseInt(ladder.getFsNode().toString())));

                    return re;

                }


                for (GraphConstraint fsvar : fs) {
                    if (ladder.getFsNode().equals(fsvar.getFsValue())) {


                        //   if (generateRelation(start, fsvar, end, fsCopy, re, false) == null)
                        //  {
                        Deque<String> reCopy = new LinkedList<>(re);

                        re.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                                Integer.parseInt((String) (ladder.getFsValue()).toString())));
                        re.addFirst(ladder.getRelationLabel());

                        if (generateRelation(start, fsvar, fsvar, fsCopy, reCopy, false) == null) {

                            fsCopy.remove(ladder);
                            System.out.println("Removed ladder " + ladder + "from f-structure."  );
                            return generateRelation(start, fsvar, end, fsCopy, re, true);
                        } else {
                            re.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                                    Integer.parseInt((String) (fsvar.getFsValue()).toString())));
                            end = fsvar;
                            ladder = fsvar;
                        }


                        // System.out.println("Current ReList " + generateRelation(start, fsvar, end, fsCopy, re, false));
                        //}

                    }
                }


                //      if (!re.isEmpty()) {
                //          re.removeFirst();
                if (!re.isEmpty()) {
                    re.add("&");
                    //        }
                }
                return generateRelation(start, ladder, end, fsCopy, re, false);


            } else {
        /*
        if (GraphConstraint.isInteger(start.getFsValue()) &&
                ladder.equals(start)
                ) {
            re.add(start.getRelationLabel());
            re.add("#" +
                    vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                            Integer.parseInt(end.getFsNode().toString())));

            return re;
        }
        */

        /*
        if (ladder.getFsValue().equals(end.getFsValue())) {
            re.add("#" +
                    vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                            Integer.parseInt(ladder.getFsNode().toString())));

            return re;
        }
        */

                for (GraphConstraint fsvar : fs) {
                    if (ladder.getFsValue().equals(fsvar.getFsNode()) &&
                            HelperMethods.isInteger(fsvar.getFsValue())) {


                        Deque<String> reCopy = new LinkedList<>(re);

                        //reCopy.add(fsvar.getRelationLabel());
                        reCopy.add("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                                Integer.parseInt(fsvar.getFsNode().toString())));
                        reCopy.add(fsvar.getRelationLabel());
                        reCopy.add("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                                Integer.parseInt(fsvar.getFsValue().toString())));

                        System.out.println(fsvar + " <--> " + start + " <--> " + ladder);
                        if (fsvar.equals(start)) {
                            return reCopy;
                        }

                        if (generateRelation(start, fsvar, end, fsCopy, reCopy, false) == null) {
                            continue;
                        }

                    }

                    // } else {
                    //     path.addFirst(end.getFsValue().toString());
                    // }
                    // path.addFirst(end.getRelationLabel());


                    //   System.out.println(fsvar.toString());
                    //   System.out.println(relation);



        /*
            if (GraphConstraint.isInteger(end.getFsValue())) {
                re.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                        Integer.parseInt(end.getFsValue().toString())));
            } else {
                re.addFirst(end.getFsValue().toString());
            }

            re.addFirst(end.getRelationLabel());
            re.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                    Integer.parseInt(end.getFsNode())));

*/
                }

                //          Deque<String> rel = generateRelation(start, fsvar, end, fs, re, false);


            }



        return null;

    }
/*
    public boolean generateRootPath(GraphConstraint fsC, GraphConstraint root, List<GraphConstraint> fs) {


        if (fsC == root)
        {
            return true;
        }

        for (GraphConstraint fsvar : fs) {
            if (fsC.getFsNode().equals(fsvar.getFsValue())) {
                if (GraphConstraint.isInteger(fsC.getFsValue())) {
                    path.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                            Integer.parseInt(fsC.getFsValue().toString())));
                } else {
                    path.addFirst(fsC.getFsValue().toString());
                }
                path.addFirst(fsC.getRelationLabel());


                System.out.println(fsvar.toString());
                System.out.println(path);
                return generatePath(fsvar, fs);
            }
        }

        if (GraphConstraint.isInteger(fsC.getFsValue())) {
            path.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                    Integer.parseInt(fsC.getFsValue().toString())));
        } else {
            path.addFirst(fsC.getFsValue().toString());
        }

        path.addFirst(fsC.getRelationLabel());
        path.addFirst("#" + vh.returnNewVar(VariableHandler.variableType.FS_NODE,
                Integer.parseInt(fsC.getFsNode())));

        return true;
    }

*/

    /*This method is important for querying treebanks but more important for finding antecedents to annotated rules, so
    rules can be automatically adapted by other sentences in the tree
    */
    /*
    public static List<Integer> searchPath(Deque<String> query,
                                           HashMap<Integer, GraphConstraint> fsIndices,
                                           HashMap<Integer, GraphConstraint> newIndices,
                                           HashMap<Integer, Set<String>> varAssignment) {


        //Delete elements of fsIndices if they are not matching the current element of query.

        String currentQueryElement = query.getFirst();



            for (Integer key : fsIndices.keySet()) {
                // Integer which is represented as String
                String fsNode = fsIndices.get(key).getFsNode();
                // Always a string of characters
                String attribute = fsIndices.get(key).getRelationLabel();
                //Either Integer as String or sequence of characters
                String value = fsIndices.get(key).getFsValue();


                if (fsIndices.get(key).equals(attribute)) {
                    for (Integer key1 : fsIndices.keySet()) {
                     //   if (fsIndices.get(key1).)

                    }

                }


            }


            //TODO do we need to separate values and fs-nodes? Probably not

        return new ArrayList<Integer>(fsIndices.keySet());

        }
*/

        /*

        Pattern fsNode = Pattern.compile("#(.+)");

        Iterator<Integer> i = fsIndices.keySet().iterator();

        while (i.hasNext()) {

            System.out.println("New interation!");
            System.out.println("Current element: " + fsIndices.get(i.next()));

            //Attributes match

            if (query.getFirst().equals(fsIndices.get(i.next()).getRelationLabel())) {
                continue;
            }


            //Values match (terminal match)

            else if (query.getFirst().equals(fsIndices.get(i.next()).getFsValue()))
            {
                continue;
            }
            //Fs nodes match;
            else if (unifyFsNodes(query.getFirst(), fsIndices.get(i.next()).getFsNode(), varAssignment)) {
                Matcher qMatcher = fsNode.matcher(query.getFirst());
                qMatcher.find();

                if (varAssignment.keySet().contains(Integer.parseInt(fsIndices.get(i.next()).getFsNode()))) {



                    System.out.println("Current key: " + fsIndices.get(i.next()).getFsNode());
                    System.out.println(varAssignment);


                    if (varAssignment.get(Integer.parseInt(fsIndices.get(i.next()).getFsNode())).contains(qMatcher.group(1))) {
                        continue;
                    } else {

                        varAssignment.get(Integer.parseInt(fsIndices.get(i.next()).getFsNode())).add(qMatcher.group(1));
                        continue;
                    }
                } else {
                    Set<String> newAssignment = new HashSet<String>();
                    newAssignment.add(qMatcher.group(1));
                    varAssignment.put(Integer.parseInt(fsIndices.get(i.next()).getFsNode()), newAssignment);
                }

            }


            //Values match (non-terminal match)
            else if (unifyFsNodes(query.getFirst(), fsIndices.get(i.next()).getFsValue(), varAssignment)) {
                Matcher qMatcher = fsNode.matcher(query.getFirst());
                qMatcher.find();
                if (varAssignment.keySet().contains(Integer.parseInt(fsIndices.get(i.next()).getFsValue()))) {
                    if (varAssignment.get(Integer.parseInt(fsIndices.get(i.next()).getFsValue())).contains(qMatcher.group(1))) {
                        continue;
                    } else {

                        varAssignment.get(Integer.parseInt(fsIndices.get(i.next()).getFsValue())).add(qMatcher.group(1));
                        continue;
                    }
                } else {
                    Set<String> newAssignment = new HashSet<String>();
                    newAssignment.add(qMatcher.group(1));
                    varAssignment.put(Integer.parseInt(fsIndices.get(i.next()).getFsValue()), newAssignment);
                }

            }
        }

            if (!fsIndices.isEmpty()) {
                query.removeFirst();
                return searchPath(query, fsIndices, varAssignment);

            } else {
                return new ArrayList<>(fsIndices.keySet());
            }

    }

*/

    public static boolean unifyFsNodes(String query, String fsNode,HashMap<Integer,Set<String>> varAssignment)
    {
        Pattern queryPattern = Pattern.compile("#(.+)");
       // Pattern fnPattern = Pattern.compile("#(.+)");

        Matcher qMatcher = queryPattern.matcher(query);
       // Matcher fnMatcher = fnPattern.matcher(fsNode);

        qMatcher.find();


        //Test

        if (!qMatcher.find())
        {
            return false;
        }


        if(varAssignment.keySet().contains(Integer.parseInt(fsNode))) {
            if (varAssignment.get(Integer.parseInt(fsNode)).contains(qMatcher.group(1)))
            {
                System.out.println("Unified " + query + " and " + fsNode);
                return true;
            }
            else
                {
                return false;
            }
        } else
            {
                System.out.println("Unified " + query + " and " + fsNode);
                return true;
            }
    }

   /*
   Output:
   0 equal
   1 equally complex
   2 a more complex
   3 b more complex
    */
    public static Integer equalPath(Deque<String> a, Deque<String> b)
    {

      Pattern fsNode = Pattern.compile("#(.+)");


        if (a.size() > b.size())
        {
            return 2;
        }
        else if (a.size() < b.size())
        {
            return 3;
        }

        boolean equal = true;

        for (String inA : a){
            for (String inB : b)
            {
                if (a.equals(b))
                {
                    break;
                }
                Matcher am = fsNode.matcher(inA);
                Matcher bm = fsNode.matcher(inB);

                if (am.find() && bm.find())
                {
                    break;
                }
                equal = false;
                break;
            }
            if (equal)
            {
                continue;
            }
            else
                {
                return 1;
            }
        }

        return 0;
    }


    @Override
    public String toString() {
        return String.join( " ",path);
    }

    //Getter
    public Deque<String> getPath() {
        return path;
    }

    }


