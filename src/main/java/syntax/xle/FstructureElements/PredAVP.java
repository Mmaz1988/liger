package syntax.xle.FstructureElements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PredAVP extends AttributeValuePair
{
    public List<String> themeArguments;
    public List<String> nonThemeArguments;

    public PredAVP(String attr, String val)
    {
        this.attribute = attr;
        this.value = val;
    }

    public static String pred2Tex(AttributeValuePair avp, LinkedHashMap<Integer,List<AttributeValuePair>> fsHash)
    {
        StringBuilder builder = new StringBuilder();

        List<String> argumentList = new ArrayList<String>();

        Pattern pred = Pattern.compile("semform\\('(.*)',.+,\\[(.*)\\],\\[(.*)\\]\\)");
        Pattern vars = Pattern.compile("var\\((\\d+)\\)");

        Matcher predMatcher = pred.matcher(avp.value);

        String themeArguments = "";
        String nonThemeArguments ="";

        if (predMatcher.find())
        {
             themeArguments = predMatcher.group(2);
             nonThemeArguments = predMatcher.group(3);
        }

        Matcher varsMatcher = vars.matcher(themeArguments);

        while (varsMatcher.find())
        {
            String predString = getPREDstring(Integer.parseInt(varsMatcher.group(1)),fsHash);
            argumentList.add( "[" + varsMatcher.group(1) + ":" + predString + "]" );
        }

        builder.append("\\avmspan{");
        builder.append(avp.attribute2tex() + " ");
        builder.append("'" + predMatcher.group(1));

        // Adds list of arguments
        if (!themeArguments.matches("")) {
            builder.append(" \\<");
            themeArguments = argumentList.stream().collect(Collectors.joining(", "));
            builder.append(themeArguments);
            builder.append("\\>'}");
        }
        else
        {
            builder.append("'}");
        }


        return builder.toString();
    }

    public static String getPREDstring(int key, LinkedHashMap<Integer,List<AttributeValuePair>> fsHash){

        List<AttributeValuePair> currentMatrix = fsHash.get(key);
        String predString = "";

        try
        {
            for (AttributeValuePair avp : currentMatrix)
            {
                if (avp instanceof PredAVP)
                {
                    Pattern pred = Pattern.compile("semform\\('(.+)',.*\\)");
                    Matcher predMatcher = pred.matcher(avp.value);
                    if (predMatcher.find())
                    {
                        predString = predMatcher.group(1);
                        return predString;
                    }
                    else
                        {
                            break;
                        }
                }
            }
        }
        catch (NoSuchElementException e)
        {
         System.out.println("Could not find pred value. \n" +e.getMessage());
        }

        return predString;
    }
}
