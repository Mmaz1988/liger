package syntax;

import utilities.HelperMethods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyntacticStructure {

    public String local_id;
    public String sentence;
    public List<GraphConstraint> constraints;

    public List<GraphConstraint> annotation = new ArrayList<>();



    public SyntacticStructure(String local_id, String sentence, List<GraphConstraint> fsFacts)
    {
        this.local_id = local_id;
        this.sentence = sentence;
        this.constraints = fsFacts;
    }

    public List<List<GraphConstraint>> getSubstructures(String name)
    {
        List<String> topNodes = new ArrayList<>();
        for (GraphConstraint g : annotation)
        {
            if (g.getRelationLabel().equals(name))
            {
                topNodes.add((String) g.getFsValue());
            }
        }

        List<List<GraphConstraint>> out = new ArrayList<>();

        for (String node : topNodes)
        {
            List<GraphConstraint> matrix = new ArrayList<>();
            Set<String> daugtherNodes = new HashSet<>();

            for (GraphConstraint g: annotation)
            {
                if (g.getFsNode().equals(node))
                {

                    if (HelperMethods.isInteger(g.getFsValue())) {
                    daugtherNodes.add((String) g.getFsValue());
                }
                    matrix.add(g);
                }

                while (!daugtherNodes.isEmpty())
                {
                    Set<String> helperList = new HashSet<>();
                    for (GraphConstraint g1 : annotation)
                    {
                        if (daugtherNodes.contains(g1.getFsNode()))
                        {
                            matrix.add(g1);
                            if (HelperMethods.isInteger(g1.getFsValue()))
                            {
                                helperList.add((String) g1.getFsValue());
                            }
                        }
                    }
                    daugtherNodes = helperList;

                }

            }

        out.add(matrix);
        }

        return out;

    }

}
