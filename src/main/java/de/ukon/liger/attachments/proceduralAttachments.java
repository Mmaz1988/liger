package de.ukon.liger.attachments;

import java.util.List;

import static de.ukon.liger.attachments.Attachments.*;


public class proceduralAttachments {

    private Attachments attachment;

    /*
    This class allows for the specification of
     */
    public String executeAttachment(String identifier, List<String> arguments)
    {

        attachment = Attachments.valueOf(identifier);

/*
        switch(attachment){
            case(test):
                System.out.println("test");
                break;
            case(test2):
                System.out.println("test2");
                break;
            default:
                return null;
        }

 */
        return null;

    }






public String greaterThan()
{return null;}
}

