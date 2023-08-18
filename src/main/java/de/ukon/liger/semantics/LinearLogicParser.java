package de.ukon.liger.semantics;


import de.ukon.liger.semantics.linearLogicElements.LLImplication;
import de.ukon.liger.semantics.linearLogicElements.LLResource;
import de.ukon.liger.semantics.linearLogicElements.LinearLogicElement;
import de.ukon.liger.utilities.VariableHandler;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.LinkedList;
public class LinearLogicParser {

    public HashMap<Integer,Boolean> isFormula;
    public Integer pos;
    public Integer layer;

    public VariableHandler vh;

    public LinearLogicParser(VariableHandler vh) {
        this.pos = 0;
        this.layer = 0;
        this.vh = vh;

    }
    public static void main(String[] args) {
        String input = "(((a_e -o a_t) -o b_t) -o ((c_e -o d_e) -o e_t))";
        String input2 = "((s::((SPEC ^) VAR)_e -o s::((SPEC ^) RESTR)_t) -o ((s::(SPEC ^)_e -o s::((SPEC ^) GF)_t) -o s::((SPEC ^) GF)_t))";
        String input3 = "((s::(SPEC ^)_e -o s::((SPEC ^) GF)_t) -o s::((SPEC ^) GF)_t))";
        String input4 = "(s::(^SUBJ)_e -o (s::(^OBJ)_e -o (s::(^SUBJ)_e -o s::^_t)))";
        String input5 = "s::^";
       LinearLogicParser llp = new LinearLogicParser(new VariableHandler());
       //LinearLogicElement sb = llp.parseExpression(input);
       System.out.println(llp.linearLogic2AVM(input5)[1]);


    }

       /*
    LLTEST(N A AT C CT) = @(LLCLOSURE %mc1 N)
       	 @(LLATOM %ANT A AT)
	 @(LLATOM %CONS C CT)
	 @(LLIMP %mc %ANT %CONS)
	 @(LLATOM %CONS1 C CT)
	 @(LLIMP %mc1 %mc %CONS1).
     */

    public String[] linearLogic2AVM(String expr){
        this.pos = 0;
        this.layer = 0;
        LinearLogicElement ll = parseExpression(expr);

        String[] output = new String[2];


        StringBuilder sb = new StringBuilder();
        sb.append("@(LLCLOSURE " + ll.localName + ")");
        sb.append(System.lineSeparator());
        sb.append(ll.toPrologString());

        output[1] = sb.toString();
        output[0] = ll.localName;

        return output;

    }

    public LinearLogicElement parseExpression(String expr) {

        expr = expr.trim();

        HashMap<Integer, String> subElements = new HashMap<>();

        StringBuilder sb = new StringBuilder();

        Integer startLayer = layer;
        Integer startPos = pos;


        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '(') {
                pos++;
                    sb.append(c);
                layer++;
            } else if (c == ')') {
                pos++;
                    sb.append(c);
                layer--;

            } else if (c == '-' && expr.charAt(pos + 1) == 'o' && layer - 1 == startLayer)
                    {
                        LinearLogicParser subExpressionParser = new LinearLogicParser(this.vh);
                        LinearLogicElement left = subExpressionParser.parseExpression(sb.toString().substring(1,sb.length() - 1));
                        /*
                        left.start = startPos + left.start;
                        left.end = startPos + left.end;
                         */
                       LinearLogicParser subExpressionParser1 = new LinearLogicParser(this.vh);
                        LinearLogicElement right = subExpressionParser1.parseExpression(expr.substring(pos + 2,expr.length() - 1));
                        /*
                        right.start = pos + right.start + 2;
                        right.end = pos + right.end + 2;
                        pos = pos + right.end + 2;

                         */
                        return new LLImplication(left, right, this.vh.returnNewVar(VariableHandler.variableType.LOCAL_NAME, null));
            } else
            {
                sb.append(c);
                pos++;
            }
        }
        if (!sb.toString().equals(""))
        {
            return new LLResource(sb.toString(),this.vh.returnNewVar(VariableHandler.variableType.LOCAL_NAME, null));
        }
return null;
    }


}
