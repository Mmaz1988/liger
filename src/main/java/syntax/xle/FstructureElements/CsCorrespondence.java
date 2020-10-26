package syntax.xle.FstructureElements;

import java.util.List;

public class CsCorrespondence extends AttributeValuePair {

    public CsCorrespondence(String attribute, List<String> value)
    {
        this.attribute = attribute;
        this.value = value.toString();
    }
}
