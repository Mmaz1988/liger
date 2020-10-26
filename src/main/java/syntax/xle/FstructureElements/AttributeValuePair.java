package syntax.xle.FstructureElements;


import java.io.Serializable;

public class AttributeValuePair implements Serializable {

    public String attribute;
    public String value;


    public AttributeValuePair()
    {
        super();
        this.attribute = "'null'";
        this.value = null;
    }


    @Override
    public String toString()
    {
        String out = "(" + this.attribute + " " + this.value + ")";
        return out;
    }


    // removes quotes
    public String attribute2tex()
    {
        this.attribute = this.attribute.replaceAll("'","");
        this.attribute = this.attribute.replaceAll("_","\\_");
        return this.attribute;
    }

    // removes quotes
    public String value2tex()
    {
        this.value = this.value.replaceAll("'","");
        this.value = this.value.replaceAll("_","\\_");
        return this.value;
    }


}
