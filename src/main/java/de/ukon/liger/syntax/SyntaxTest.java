package de.ukon.liger.syntax;

import de.ukon.liger.syntax.xle.XLEoperator;
import de.ukon.liger.utilities.PathVariables;
import de.ukon.liger.utilities.VariableHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SyntaxTest {

    @Test
    public void testToAndFromJson()
    {

        PathVariables.initializePathVariables();
        String testfile = PathVariables.testPath + "testdirS12.pl";

        XLEoperator xle = new XLEoperator(new VariableHandler());

        List<LinkedHashMap<String, LinguisticStructure>> fsList = new ArrayList<>();

    //    fsList.add(xle.fs2Java(testfile));

        LinkedHashMap<String,LinguisticStructure> fs = xle.fs2Java(testfile);

        for (String key : fs.keySet()) {

            LinguisticStructure ls = fs.get(key);

            LinkedHashMap jsonMap = ls.toJson();
            LinguisticStructure lsCopy = LinguisticStructure.parseFromJson(jsonMap);

                    assertEquals(ls.text,lsCopy.text);
                    assertEquals(ls.local_id,lsCopy.local_id);
                    assertEquals(ls.constraints.toString(),lsCopy.constraints.toString());
                    assertEquals(ls.annotation.toString(),lsCopy.annotation.toString());
                    assertEquals(ls.cp.toString(),lsCopy.cp.toString());

        }

      //  ReadFsProlog fs = ReadFsProlog.readPrologFile(new File(testfile),new VariableHandler());

        //LinkedHashMap jsonMap = fs.cp.toJson();

    //    ChoiceSpace cp2 = ChoiceSpace.parseJson(jsonMap);

    }

}
