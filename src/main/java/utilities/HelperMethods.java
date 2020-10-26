package utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelperMethods {


    public static Pattern fsNodePattern = Pattern.compile("#([a-z])");
    public static Pattern valueVarPattern = Pattern.compile("(%[a-z])");
    //semform('say',3,[var(11),var(2)],[]))
    public static Pattern predPattern = Pattern.compile("semform\\('(.+)',.+\\)");
    public static Pattern valueStringPattern = Pattern.compile("'(.+)'");
    public static Pattern stripPattern = Pattern.compile("(strip\\((.+)\\))");
    public static Pattern uncertaintyPattern = Pattern.compile("([\\^|!])\\((.*)\\)");


        public static boolean isInteger (Object s)
        {
            boolean isValidInteger = false;
            try {
                Integer.parseInt((String) s);

                // s is a valid integer

                isValidInteger = true;
            } finally {
                return isValidInteger;
                // s is not an integer
            }
        }

    public static String stripValue(String value)
    {
        Matcher m = predPattern.matcher(value);

        if (m.matches())
        {
            return m.group(1);
        }

        return value;
        //semform('say',3,[var(11),var(2)],[]))
    }


    public static String stripValeue2(String value) {

        StringBuilder newString = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c == 's') {
               try {
                   int j = i;
                   if (value.charAt(j + 1) == 't' && value.charAt(j + 2) == 'r' && value.charAt(j + 3) == 'i' && value.charAt(j + 4) == 'p'
                           && value.charAt(j + 5) == '(') {
                       StringBuilder sb = new StringBuilder();
                       int bracketCounter = 1;
                       i = i + 6;
                       while (bracketCounter > 0) {
                           char ch = value.charAt(i);
                           if (ch == '(') {
                               sb.append(ch);
                               i++;
                               bracketCounter++;
                           } else if (ch == ')' && bracketCounter > 1) {
                               sb.append(ch);
                               i++;
                               bracketCounter = bracketCounter - 1;
                           } else if (ch == ')' && bracketCounter == 1) {
                               bracketCounter = bracketCounter - 1;
                               i++;
                           } else {
                               sb.append(ch);
                               i++;
                           }
                       }
                       newString.append(HelperMethods.stripValue(sb.toString()));
                   }
               } catch(Exception e)
               {
                   System.out.println("Hit end of string");
               }
            }
            if (i < value.length()) {
                newString.append(value.charAt(i));
            }else
            {
                break;
            }
        }
        return newString.toString();
    }






}


