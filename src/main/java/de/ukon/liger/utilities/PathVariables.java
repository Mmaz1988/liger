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

import java.net.URISyntaxException;

public class PathVariables {

    public static String workingDirectory;
    //Path to the test package
    public static String testPath;
    //Path to the dict package
    public static String dictPath;

    /*

    public PathVariables()
    {
        try {
            testPath = returnResourcesFolder() + "testFiles/";
            dictPath = returnResourcesFolder() + "dicts/";
        }catch(Exception e)
        {
            System.out.println("Failed to locate working directory.");
                    }
    }

    public static void main(String[] args) {
        try {
            PathVariables pv = new PathVariables();
            System.out.println(PathVariables.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString());
            System.out.println(ClassLoader.getSystemClassLoader().getResource(".").getPath().toString());

            System.out.println(testPath);
            System.out.println(dictPath);


        }
        catch(Exception e)
        {System.out.println("failed");}
    }
*/

    public static void initializePathVariables()
    {
        if (workingDirectory != null)
        {
            testPath = workingDirectory + "testFiles\\";
            dictPath = workingDirectory + "dicts\\";
        }
        else
        {

        try {
            testPath = returnResourcesFolder() + "testFiles\\";
            dictPath = returnResourcesFolder() + "dicts\\";
        }catch(Exception e)
        {
            System.out.println("Failed to locate working directory.");
        }
    }
    }


    public static String returnResourcesFolder() throws URISyntaxException {
        return PathVariables.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString();
    }

 //   return new File(MyClass.class.getProtectionDomain().getCodeSource().getLocation()
 //   .toURI()).getPath();*

}
