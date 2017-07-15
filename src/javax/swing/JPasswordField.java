/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleTextSequence;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class JPasswordField extends JTextField{
    // --- variables -----------------------------------------------
    private static final String uiClassID="PasswordFieldUI";
    private char echoChar;
    private boolean echoCharSet=false;

    public JPasswordField(){
        this(null,null,0);
    }

    public JPasswordField(Document doc,String txt,int columns){
        super(doc,txt,columns);
        // We could either leave this on, which wouldn't be secure,
        // or obscure the composted text, which essentially makes displaying
        // it useless. Therefore, we turn off input methods.
        enableInputMethods(false);
    }

    public JPasswordField(String text){
        this(null,text,0);
    }

    public JPasswordField(int columns){
        this(null,null,columns);
    }

    public JPasswordField(String text,int columns){
        this(null,text,columns);
    }

    public void updateUI(){
        if(!echoCharSet){
            echoChar='*';
        }
        super.updateUI();
    }

    @Deprecated
    public String getText(int offs,int len) throws BadLocationException{
        return super.getText(offs,len);
    }
    // --- JTextComponent methods ----------------------------------

    public void cut(){
        if(getClientProperty("JPasswordField.cutCopyAllowed")!=Boolean.TRUE){
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        }else{
            super.cut();
        }
    }

    public void copy(){
        if(getClientProperty("JPasswordField.cutCopyAllowed")!=Boolean.TRUE){
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        }else{
            super.copy();
        }
    }

    @Deprecated
    public String getText(){
        return super.getText();
    }

    public char getEchoChar(){
        return echoChar;
    }

    public void setEchoChar(char c){
        echoChar=c;
        echoCharSet=true;
        repaint();
        revalidate();
    }

    public boolean echoCharIsSet(){
        return echoChar!=0;
    }

    public char[] getPassword(){
        Document doc=getDocument();
        Segment txt=new Segment();
        try{
            doc.getText(0,doc.getLength(),txt); // use the non-String API
        }catch(BadLocationException e){
            return null;
        }
        char[] retValue=new char[txt.count];
        System.arraycopy(txt.array,txt.offset,retValue,0,txt.count);
        return retValue;
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        if(getUIClassID().equals(uiClassID)){
            byte count=JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this,--count);
            if(count==0&&ui!=null){
                ui.installUI(this);
            }
        }
    }

    public String getUIClassID(){
        return uiClassID;
    }

    protected String paramString(){
        return super.paramString()+
                ",echoChar="+echoChar;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJPasswordField();
        }
        return accessibleContext;
    }
/////////////////
// Accessibility support
////////////////

    boolean customSetUIProperty(String propertyName,Object value){
        if(propertyName=="echoChar"){
            if(!echoCharSet){
                setEchoChar((Character)value);
                echoCharSet=false;
            }
            return true;
        }
        return false;
    }

    protected class AccessibleJPasswordField extends AccessibleJTextField{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.PASSWORD_TEXT;
        }

        public AccessibleText getAccessibleText(){
            return this;
        }

        public String getAtIndex(int part,int index){
            String str=null;
            if(part==AccessibleText.CHARACTER){
                str=super.getAtIndex(part,index);
            }else{
                // Treat the text displayed in the JPasswordField
                // as one word and sentence.
                char password[]=getPassword();
                if(password==null||
                        index<0||index>=password.length){
                    return null;
                }
                str=new String(password);
            }
            return getEchoString(str);
        }

        private String getEchoString(String str){
            if(str==null){
                return null;
            }
            char[] buffer=new char[str.length()];
            Arrays.fill(buffer,getEchoChar());
            return new String(buffer);
        }

        public String getAfterIndex(int part,int index){
            if(part==AccessibleText.CHARACTER){
                String str=super.getAfterIndex(part,index);
                return getEchoString(str);
            }else{
                // There is no word or sentence after the text
                // displayed in the JPasswordField.
                return null;
            }
        }

        public String getBeforeIndex(int part,int index){
            if(part==AccessibleText.CHARACTER){
                String str=super.getBeforeIndex(part,index);
                return getEchoString(str);
            }else{
                // There is no word or sentence before the text
                // displayed in the JPasswordField.
                return null;
            }
        }

        public String getTextRange(int startIndex,int endIndex){
            String str=super.getTextRange(startIndex,endIndex);
            return getEchoString(str);
        }

        public AccessibleTextSequence getTextSequenceAt(int part,int index){
            if(part==AccessibleText.CHARACTER){
                AccessibleTextSequence seq=super.getTextSequenceAt(part,index);
                if(seq==null){
                    return null;
                }
                return new AccessibleTextSequence(seq.startIndex,seq.endIndex,
                        getEchoString(seq.text));
            }else{
                // Treat the text displayed in the JPasswordField
                // as one word, sentence, line and attribute run
                char password[]=getPassword();
                if(password==null||
                        index<0||index>=password.length){
                    return null;
                }
                String text=new String(password);
                return new AccessibleTextSequence(0,password.length-1,
                        getEchoString(text));
            }
        }

        public AccessibleTextSequence getTextSequenceAfter(int part,int index){
            if(part==AccessibleText.CHARACTER){
                AccessibleTextSequence seq=super.getTextSequenceAfter(part,index);
                if(seq==null){
                    return null;
                }
                return new AccessibleTextSequence(seq.startIndex,seq.endIndex,
                        getEchoString(seq.text));
            }else{
                // There is no word, sentence, line or attribute run
                // after the text displayed in the JPasswordField.
                return null;
            }
        }

        public AccessibleTextSequence getTextSequenceBefore(int part,int index){
            if(part==AccessibleText.CHARACTER){
                AccessibleTextSequence seq=super.getTextSequenceBefore(part,index);
                if(seq==null){
                    return null;
                }
                return new AccessibleTextSequence(seq.startIndex,seq.endIndex,
                        getEchoString(seq.text));
            }else{
                // There is no word, sentence, line or attribute run
                // before the text displayed in the JPasswordField.
                return null;
            }
        }
    }
}
