/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

package de.ukon.liger.syntax.xle.avp_elements;


public class TerminalAVP extends AttributeValuePair {

    public TerminalAVP(String attr, String val, String projection)
    {
        this.attribute = attr;
        this.value = val;
        this.projection = projection;
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
