/**
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

class TextAreaDocument extends PlainDocument{
    String initialText;

    void reset(){
        try{
            remove(0,getLength());
            if(initialText!=null){
                insertString(0,initialText,null);
            }
        }catch(BadLocationException e){
        }
    }

    void storeInitialText(){
        try{
            initialText=getText(0,getLength());
        }catch(BadLocationException e){
        }
    }
}
