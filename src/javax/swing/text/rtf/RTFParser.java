/**
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.rtf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

abstract class RTFParser extends AbstractFilter{
    // table of non-text characters in rtf
    static final boolean rtfSpecialsTable[];

    static{
        rtfSpecialsTable=noSpecialsTable.clone();
        rtfSpecialsTable['\n']=true;
        rtfSpecialsTable['\r']=true;
        rtfSpecialsTable['{']=true;
        rtfSpecialsTable['}']=true;
        rtfSpecialsTable['\\']=true;
    }

    // value for the 'state' variable
    private final int S_text=0;          // reading random text
    private final int S_backslashed=1;   // read a backslash, waiting for next
    private final int S_token=2;         // reading a multicharacter token
    private final int S_parameter=3;     // reading a token's parameter
    private final int S_aftertick=4;     // after reading \'
    private final int S_aftertickc=5;    // after reading \'x
    private final int S_inblob=6;        // in a \bin blob
    public int level;
    protected PrintStream warnings;
    ByteArrayOutputStream binaryBuf;
    private int state;
    private StringBuffer currentCharacters;
    private String pendingKeyword;                // where keywords go while we
    // read their parameters
    private int pendingCharacter;                 // for the \'xx construct
    private long binaryBytesLeft;                  // in a \bin blob?
    private boolean[] savedSpecials;

    public RTFParser(){
        currentCharacters=new StringBuffer();
        state=S_text;
        pendingKeyword=null;
        level=0;
        //warnings = System.out;
        specialsTable=rtfSpecialsTable;
    }    public abstract void handleText(String text);

    public abstract boolean handleKeyword(String keyword);

    public abstract boolean handleKeyword(String keyword,int parameter);

    public void handleText(char ch){
        handleText(String.valueOf(ch));
    }

    public abstract void handleBinaryBlob(byte[] data);    public abstract void endgroup();

    public abstract void begingroup();

    public void write(String s)
            throws IOException{
        if(state!=S_text){
            int index=0;
            int length=s.length();
            while(index<length&&state!=S_text){
                write(s.charAt(index));
                index++;
            }
            if(index>=length)
                return;
            s=s.substring(index);
        }
        if(currentCharacters.length()>0)
            currentCharacters.append(s);
        else
            handleText(s);
    }

    public void write(char ch)
            throws IOException{
        boolean ok;
        switch(state){
            case S_text:
                if(ch=='\n'||ch=='\r'){
                    break;  // unadorned newlines are ignored
                }else if(ch=='{'){
                    if(currentCharacters.length()>0){
                        handleText(currentCharacters.toString());
                        currentCharacters=new StringBuffer();
                    }
                    level++;
                    begingroup();
                }else if(ch=='}'){
                    if(currentCharacters.length()>0){
                        handleText(currentCharacters.toString());
                        currentCharacters=new StringBuffer();
                    }
                    if(level==0)
                        throw new IOException("Too many close-groups in RTF text");
                    endgroup();
                    level--;
                }else if(ch=='\\'){
                    if(currentCharacters.length()>0){
                        handleText(currentCharacters.toString());
                        currentCharacters=new StringBuffer();
                    }
                    state=S_backslashed;
                }else{
                    currentCharacters.append(ch);
                }
                break;
            case S_backslashed:
                if(ch=='\''){
                    state=S_aftertick;
                    break;
                }
                if(!Character.isLetter(ch)){
                    char newstring[]=new char[1];
                    newstring[0]=ch;
                    if(!handleKeyword(new String(newstring))){
                        warning("Unknown keyword: "+newstring+" ("+(byte)ch+")");
                    }
                    state=S_text;
                    pendingKeyword=null;
                    /** currentCharacters is already an empty stringBuffer */
                    break;
                }
                state=S_token;
                /** FALL THROUGH */
            case S_token:
                if(Character.isLetter(ch)){
                    currentCharacters.append(ch);
                }else{
                    pendingKeyword=currentCharacters.toString();
                    currentCharacters=new StringBuffer();
                    // Parameter following?
                    if(Character.isDigit(ch)||(ch=='-')){
                        state=S_parameter;
                        currentCharacters.append(ch);
                    }else{
                        ok=handleKeyword(pendingKeyword);
                        if(!ok)
                            warning("Unknown keyword: "+pendingKeyword);
                        pendingKeyword=null;
                        state=S_text;
                        // Non-space delimiters get included in the text
                        if(!Character.isWhitespace(ch))
                            write(ch);
                    }
                }
                break;
            case S_parameter:
                if(Character.isDigit(ch)){
                    currentCharacters.append(ch);
                }else{
                    /** TODO: Test correct behavior of \bin keyword */
                    if(pendingKeyword.equals("bin")){  /** magic layer-breaking kwd */
                        long parameter=Long.parseLong(currentCharacters.toString());
                        pendingKeyword=null;
                        state=S_inblob;
                        binaryBytesLeft=parameter;
                        if(binaryBytesLeft>Integer.MAX_VALUE)
                            binaryBuf=new ByteArrayOutputStream(Integer.MAX_VALUE);
                        else
                            binaryBuf=new ByteArrayOutputStream((int)binaryBytesLeft);
                        savedSpecials=specialsTable;
                        specialsTable=allSpecialsTable;
                        break;
                    }
                    int parameter=Integer.parseInt(currentCharacters.toString());
                    ok=handleKeyword(pendingKeyword,parameter);
                    if(!ok)
                        warning("Unknown keyword: "+pendingKeyword+
                                " (param "+currentCharacters+")");
                    pendingKeyword=null;
                    currentCharacters=new StringBuffer();
                    state=S_text;
                    // Delimiters here are interpreted as text too
                    if(!Character.isWhitespace(ch))
                        write(ch);
                }
                break;
            case S_aftertick:
                if(Character.digit(ch,16)==-1)
                    state=S_text;
                else{
                    pendingCharacter=Character.digit(ch,16);
                    state=S_aftertickc;
                }
                break;
            case S_aftertickc:
                state=S_text;
                if(Character.digit(ch,16)!=-1){
                    pendingCharacter=pendingCharacter*16+Character.digit(ch,16);
                    ch=translationTable[pendingCharacter];
                    if(ch!=0)
                        handleText(ch);
                }
                break;
            case S_inblob:
                binaryBuf.write(ch);
                binaryBytesLeft--;
                if(binaryBytesLeft==0){
                    state=S_text;
                    specialsTable=savedSpecials;
                    savedSpecials=null;
                    handleBinaryBlob(binaryBuf.toByteArray());
                    binaryBuf=null;
                }
        }
    }
    // TODO: Handle wrapup at end of file correctly.

    public void writeSpecial(int b)
            throws IOException{
        write((char)b);
    }

    protected void warning(String s){
        if(warnings!=null){
            warnings.println(s);
        }
    }





    public void flush()
            throws IOException{
        super.flush();
        if(state==S_text&&currentCharacters.length()>0){
            handleText(currentCharacters.toString());
            currentCharacters=new StringBuffer();
        }
    }

    public void close()
            throws IOException{
        flush();
        if(state!=S_text||level>0){
            warning("Truncated RTF file.");
            /** TODO: any sane way to handle termination in a non-S_text state? */
            /** probably not */
            /** this will cause subclasses to behave more reasonably
             some of the time */
            while(level>0){
                endgroup();
                level--;
            }
        }
        super.close();
    }
}
