package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** One surface token of the parsed input, with 0-based {@code [start, end)} character offsets
 *  into the structure's text. {@code id} is the c-structure node carrying the token. */
public class LigerSourceToken {
    public String id;
    public String form;
    public int start;
    public int end;
    /** f-structures the token projects to ({@code phi}), used to bound referent descriptions. */
    public List<String> fsNodes = new ArrayList<>();

    public LigerSourceToken() {}

    public LigerSourceToken(String id, String form, int start, int end) {
        this.id = id;
        this.form = form;
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LigerSourceToken other && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
