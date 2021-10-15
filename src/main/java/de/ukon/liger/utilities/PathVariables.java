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

import de.ukon.liger.main.DbaMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class PathVariables {

    public static String workingDirectory;
    //Path to the test package
    public static String testPath;
    //Path to the dict package
    public static String dictPath;

    private final static Logger LOGGER = LoggerFactory.getLogger(PathVariables.class);

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

    public static void initializePathVariables() {
        if (workingDirectory != null) {
            if (!workingDirectory.endsWith(File.separator)) {
                workingDirectory = workingDirectory + File.separator;
            }
            testPath = workingDirectory + "testFiles" + File.separator;
            dictPath = workingDirectory + "dicts" + File.separator;
        }
        else
        {
        try {
            workingDirectory = returnResourcesFolder();
            if (!workingDirectory.endsWith(File.separator)) {
                workingDirectory = workingDirectory + File.separator;
            }
            testPath = workingDirectory + "testFiles" + File.separator;
            dictPath = workingDirectory + "dicts" + File.separator;
        }catch(Exception e)
        {
            System.out.println("Failed to locate working directory.");
            e.printStackTrace();
        }

        LOGGER.info("dir " + workingDirectory);
        LOGGER.info("test " + testPath);
        LOGGER.info("dict " + dictPath);
    }
    }




    public static String returnResourcesFolder() throws FileNotFoundException {

/*
        ClassLoader classLoader = PathVariables.class.getClassLoader();
        final URI uri = new URI(classLoader.getResource("").getPath());
*/      File f = null;

        try {
            String fileString = System.getProperty("user.dir");
            f = new File(fileString);
            return f.toString() + File.separator + "liger_resources";

            //  File f = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI());
        }
        catch(Exception e)
        {
        LOGGER.warn("Failed to find working directory.");
        LOGGER.info("Set resources directory to home directory: " + System.getProperty("user.home"));
         f = new File(System.getProperty("user.home") + File.separator + "liger_resources" );

         if (f.exists()) {
             LOGGER.info("Set resources directory to home directory: " + System.getProperty("user.home"));
             return f.toString();
         }
         else
         {
             throw new FileNotFoundException(f.toString());
         }
        }


 //      return uri.getPath();
    }
}
