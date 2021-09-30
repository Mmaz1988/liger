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

package de.ukon.liger.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class DBASettings {
    //LFG or dependency
    public String mode;
    //Input path
    public String inputFile;
    //Output Path
    public String outputFile;
    //Input sentence
    public String input;
    //rule file
    public String ruleFile;
    //resources directory
    public String resources;

    public Boolean semanticParsing;
    public Boolean interactiveMode;

    public BufferedWriter outputWriter;

    public Boolean web;

    public DBASettings()
    {
        this.interactiveMode = true;
        this.semanticParsing = false;
        this.web = false;
    }

    public void setOutputWriter(File file)
    {
        try {
            this.outputWriter = new BufferedWriter(new FileWriter(file, true));
        }catch(Exception e)
        {
            System.out.println("Failed to create outputWriter for path: " + file.toString());
        }
        }
}



