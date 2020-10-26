package syntax.xle.FstructureElements;


public class TerminalAVP extends AttributeValuePair {

    public TerminalAVP(String attr, String val)
    {
        this.attribute = attr;
        this.value = val;
    }



    // Translates terminal avps in strings suitable for latex
    public static String terminalAVP2tex(AttributeValuePair avp)
    {
        StringBuilder builder = new StringBuilder();

        String attribute = avp.attribute.replaceAll("'","");
        String texOut = builder.append(attribute + " " + avp.value).toString();

        String out = texOut.replaceAll("_","\\\\_");

        return out;
    }
}
