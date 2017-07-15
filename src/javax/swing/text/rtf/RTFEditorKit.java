/**
 * Copyright (c) 1997, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.rtf;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import java.io.*;

public class RTFEditorKit extends StyledEditorKit{
    public RTFEditorKit(){
        super();
    }

    public String getContentType(){
        return "text/rtf";
    }

    public void read(InputStream in,Document doc,int pos) throws IOException, BadLocationException{
        if(doc instanceof StyledDocument){
            // PENDING(prinz) this needs to be fixed to
            // insert to the given position.
            RTFReader rdr=new RTFReader((StyledDocument)doc);
            rdr.readFromStream(in);
            rdr.close();
        }else{
            // treat as text/plain
            super.read(in,doc,pos);
        }
    }

    public void write(OutputStream out,Document doc,int pos,int len)
            throws IOException, BadLocationException{
        // PENDING(prinz) this needs to be fixed to
        // use the given document range.
        RTFGenerator.writeDocument(doc,out);
    }

    public void read(Reader in,Document doc,int pos)
            throws IOException, BadLocationException{
        if(doc instanceof StyledDocument){
            RTFReader rdr=new RTFReader((StyledDocument)doc);
            rdr.readFromReader(in);
            rdr.close();
        }else{
            // treat as text/plain
            super.read(in,doc,pos);
        }
    }

    public void write(Writer out,Document doc,int pos,int len)
            throws IOException, BadLocationException{
        throw new IOException("RTF is an 8-bit format");
    }
}
