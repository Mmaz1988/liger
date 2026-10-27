package de.ukon.liger.webservice.rest.dtos;

/**
 * Which parser a parse request uses (xleplusglue docs/plans/UD_PARSING_PLAN.md). Absent or
 * {@code kind: "xle"}: XLE with the loaded grammar, as before. {@code kind: "ud"}: the Stanza
 * service with {@code model} (a language id from {@code /list_ud_models}).
 */
public class LigerParserSelection {
    public String kind;
    public String model;

    public LigerParserSelection() {
    }

    public LigerParserSelection(String kind, String model) {
        this.kind = kind;
        this.model = model;
    }

    public static boolean isUd(LigerParserSelection parser) {
        return parser != null && "ud".equals(parser.kind);
    }
}
