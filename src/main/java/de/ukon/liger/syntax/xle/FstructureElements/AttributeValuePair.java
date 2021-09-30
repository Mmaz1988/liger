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
