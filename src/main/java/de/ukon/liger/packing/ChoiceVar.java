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

package de.ukon.liger.packing;


import java.util.LinkedHashMap;
import java.util.Objects;

public class ChoiceVar {

    public String choiceID;
    //e.g. A,B,C,...
 //   public String level;
    //e.g. 1,2,3,...
  //  public String choice;
    public Boolean propValue;

    public ChoiceVar(String choiceID)
    {
        this.choiceID = choiceID;
        if (choiceID.equals("1"))
        {
            this.propValue = true;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChoiceVar choiceVar = (ChoiceVar) o;
        return Objects.equals(choiceID, choiceVar.choiceID) &&
                Objects.equals(propValue, choiceVar.propValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(choiceID, propValue);
    }

    @Override
    public String toString()
    {
        return choiceID;
    }


    public LinkedHashMap<String,String> toJson() {
        LinkedHashMap<String,String> out = new LinkedHashMap<>();
        out.put("choiceID",this.choiceID);
        if (this.propValue != null) {
            out.put("propValue", this.propValue.toString());
        } else
        {
            out.put("propValue","null");
        }

        return out;
    }

    public static ChoiceVar parseJson(LinkedHashMap<String,String> input)
    {
        ChoiceVar c = new ChoiceVar(input.get("choiceID"));
        if (input.get("propValue") != "null") {
            c.propValue = Boolean.parseBoolean(input.get("propValue"));
        } else
        {
            c.propValue = null;
        }

       return c;
    }


}
