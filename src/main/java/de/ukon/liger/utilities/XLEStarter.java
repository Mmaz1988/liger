package de.ukon.liger.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class XLEStarter {

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    }

    ;

    public String xlePath;
    public String grammarPath;
    public final OS operatingSystem;

    private final static Logger LOGGER = LoggerFactory.getLogger(XLEStarter.class);

    public XLEStarter(String xlePath, String grammarPath, OS operatingSystem) {
        this.xlePath = xlePath;
        this.grammarPath = grammarPath;
        this.operatingSystem = operatingSystem;
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
        sb.append(grammarPath);
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
            Runtime.getRuntime().exec("chmod +x "+ file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Generated xlebash.sh at " + file.getAbsolutePath());
    }
    }



