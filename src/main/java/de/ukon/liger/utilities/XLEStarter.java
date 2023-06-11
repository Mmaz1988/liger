package de.ukon.liger.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class XLEStarter {

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS, UNKNOWN
    }

    ;

    public String xlePath;
    public String grammarPath;
    public OS operatingSystem;

    private final static Logger LOGGER = LoggerFactory.getLogger(XLEStarter.class);

    public XLEStarter(String xlePath, String grammarPath, OS operatingSystem) {
        this.xlePath = xlePath;
        this.grammarPath = grammarPath;
        this.operatingSystem = operatingSystem;
    }

    public XLEStarter()
    {
    initiateFromFile();
    }

    public void initiateFromFile()
    {
        //Open file and read in the paths
        File f = new File(Paths.get(  PathVariables.workingDirectory, "xle_paths.txt").toString());

        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (i == 0) {
                String[] lineOne = line.split("=");
                xlePath = lineOne[1].replace("\"","");
                }
                if (i == 1){
                    String[] lineTwo = line.split("=");
                    grammarPath = lineTwo[1].replace("\"","");
                }
                if (i == 2){
                    String[] lineThree = line.split("=");
                    String osString = lineThree[1].replace("\"","");
                    if (osString.equalsIgnoreCase("windows")){
                        operatingSystem = OS.WINDOWS;
                    } else if (osString.equalsIgnoreCase("mac"))
                    {
                        operatingSystem = OS.MAC;
                    } else
                    {
                        operatingSystem = OS.LINUX;
                    }

                }
                i++;
            }
            br.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Initialized paths...");

    }



    public void generateXLEStarterFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n");

        /*
        export XLEPATH=/bin/xle
        export PATH=${XLEPATH}/bin:$PATH
        export DYLD_LIBRARY_PATH=$XLEPATH/lib:$XLEPATH/bin/sp-3.12.7
        export LD_LIBRARY_PATH=${XLEPATH}/lib
        export LD_LIBRARY_PATH=${XLEPATH}/lib:$LD_LIBRARY_PATH
        export DYLD_LIBRARY_PATH=${XLEPATH}/lib:$DYLD_LIBRARY_PATH
         */

        sb.append("export XLEPATH=");
        sb.append(xlePath);
        sb.append("\n");
        sb.append("export PATH=${XLEPATH}/bin:$PATH");
        sb.append("\n");
        sb.append("export DYLD_LIBRARY_PATH=$XLEPATH/lib:$XLEPATH/bin/sp-3.12.7");
        sb.append("\n");
        sb.append("export LD_LIBRARY_PATH=${XLEPATH}/lib");
        sb.append("\n");
        sb.append("export LD_LIBRARY_PATH=${XLEPATH}/lib:$LD_LIBRARY_PATH");
        sb.append("\n");
        sb.append("export DYLD_LIBRARY_PATH=${XLEPATH}/lib:$DYLD_LIBRARY_PATH");
        sb.append("\n");
        sb.append("\n");

        if (this.operatingSystem.equals(OS.WINDOWS)) {
            /*
            export TCL_LIBRARY=${XLEPATH}/tcl/scripts/tcl
            export TCLLIBPATH=${XLEPATH}/tcl/scripts/tcl
            export TKLIBPATH=${XLEPATH}/tcl/scripts/tk
            export TK_LIBRARY=${XLEPATH}/tcl/scripts/tk
             */
            sb.append("export TCL_LIBRARY=${XLEPATH}/tcl/scripts/tcl");
            sb.append("\n");
            sb.append("export TCLLIBPATH=${XLEPATH}/tcl/scripts/tcl");
            sb.append("\n");
            sb.append("export TKLIBPATH=${XLEPATH}/tcl/scripts/tk");
            sb.append("\n");
            sb.append("export TK_LIBRARY=${XLEPATH}/tcl/scripts/tk");
            sb.append("\n");
            sb.append("\n");
        }

        // xle -noTk -e "create-parser /mnt/d/Resources/english_pargram/index/main.lfg; parse-testfile testfile.lfg -outputPrefix parser_output/sentence; exit"

        sb.append("xle -noTk -e \"create-parser ");

        String grammarString = grammarPath;

        if (operatingSystem.equals(OS.WINDOWS)){
            grammarString = HelperMethods.formatWslString(grammarString);
        }

        sb.append(grammarString);
        sb.append("; parse-testfile ");


        String testFileString = Paths.get(PathVariables.workingDirectory, "tmp","testfile.lfg").toString();

        if (operatingSystem.equals(OS.WINDOWS)){
            testFileString = HelperMethods.formatWslString(testFileString);
        }

        sb.append(testFileString);

        String outputPrefix = Paths.get(PathVariables.workingDirectory, "tmp","parser_output/sentence").toString();

        if (operatingSystem.equals(OS.WINDOWS)){
            outputPrefix = HelperMethods.formatWslString(outputPrefix);
        }


        sb.append(" -outputPrefix ");
        sb.append(outputPrefix);
        sb.append("; exit\"");
        sb.append("\n");


        File tempDir = new File(Paths.get(PathVariables.workingDirectory, "tmp").toString());

        if (!tempDir.exists()){
            tempDir.mkdir();
        }

        //open file and write
        File file = new File(Paths.get(PathVariables.workingDirectory, "tmp", "xlebash.sh").toString());

        try {
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write(sb.toString());
            fw.close();
        } catch (
                Exception e) {
            System.out.println("Failed to write xlebash.sh");
        }

        try {

            String chmodCommand = "";

            if (operatingSystem.equals(OS.WINDOWS)){
                chmodCommand = "wsl chmod +x " + HelperMethods.formatWslString(file.getAbsolutePath());
            } else {
                chmodCommand = "chmod +x " + file.getAbsolutePath();
            }

            Runtime.getRuntime().exec(chmodCommand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Generated xlebash.sh at " + file.getAbsolutePath());
    }
    }



