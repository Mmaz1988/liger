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

package de.ukon.liger.analysis;

import de.ukon.liger.syntax.LinguisticStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureSpace {

    public List dataSet;
    public boolean extractMatrix;

    public FeatureSpace(List dataSet)
    {
        this.dataSet = dataSet;
    }



    public void generateCSV(List<String> arguments)
    {
        if (!arguments.isEmpty()) {

            String[][] table = new String[arguments.size()+1][dataSet.size()-1];

            for (int i = 0; i < dataSet.size(); i++)
            {
                LinguisticStructure str = (LinguisticStructure) dataSet.get(i);
                String sentence = str.text;
                table[i][0] = str.local_id;
                table[i][1] = str.text;
                for (int j = 2; j < arguments.size()+2; j++)
                {

                }
            }

        }


    }

    public static void main(String[] args) {

        if (args[0].equals("-em"))
        {

        }

        List<LinguisticStructure> structure = new ArrayList<>();

        List<String> arguments = Arrays.asList(args);
        FeatureSpace fs = new FeatureSpace(structure);
        fs.generateCSV(arguments);
    }


}

