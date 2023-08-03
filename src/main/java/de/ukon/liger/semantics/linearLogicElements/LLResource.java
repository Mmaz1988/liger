package de.ukon.liger.semantics.linearLogicElements;

public class LLResource extends LinearLogicElement {
    String resource;

    public LLResource(String resource, String localName) {
        this.resource =  resource.trim();
        this.localName = localName;
    }

    @Override
    public String toString() {
        return this.resource;
    }

    @Override
    public String toPrologString() {

        //Split resource at underscore
        String[] parts = this.resource.split("_");
        String resource = parts[0];
        String type = parts[1];

        /*
         @(LLATOM %ANT A AT)
         */

        return "@(LLATOM " + this.localName + " " + resource + " " + type + ")";
    }

    /*
       GLUE-REL0-MC(R TY M) =
	  @(GLUE-RESOURCE R %mc TY)
	  @(GLUE-MEANING %mc M)
	  %mc $ (R GLUE).

"Predicates with one or more arguments.  These are the simple cases,
which do not rely on attributes in the semantic structure.  Each
argument has a semantic structure and a type.  Note that when the
meaning expression is broken into parts as input to the CONCAT
template, special characters (including square brackets, the forward
slash, the comma, and the period) must be preceded by a backquote.
The underscore (and possibly other characters as well) must be
preceded by a backquote AND preceded and followed by a space."

"1-place predicates:
   Representation without quotes for special characters: [/x_A1TY.M(x)]"
   GLUE-REL1-MC(A1 A1TY R TY M) =
	  @(GLUE-RESOURCE R %mc TY)
	  @(GLUE-RESOURCE A1 (%mc ARG1) A1TY)
	  @(CONCAT `[ `/x `_ A1TY `. M `( x `) `] %MEANING)
	  @(GLUE-MEANING %mc %MEANING)
	  %mc $ (R GLUE).
     */


}
