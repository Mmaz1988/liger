package de.ukon.liger.discourse;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;

import java.util.List;

public class DiscourseStructure extends LinguisticStructure {
    public DiscourseStructure(String local_id, String sentence, List<GraphConstraint> fsFacts) {
        super(local_id, sentence, fsFacts);
    }
}
