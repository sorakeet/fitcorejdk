/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.ArrayList;

public class MaskFormatter extends DefaultFormatter{
    // Potential values in mask.
    private static final char DIGIT_KEY='#';
    private static final char LITERAL_KEY='\'';
    private static final char UPPERCASE_KEY='U';
    private static final char LOWERCASE_KEY='L';
    private static final char ALPHA_NUMERIC_KEY='A';
    private static final char CHARACTER_KEY='?';
    private static final char ANYTHING_KEY='*';
    private static final char HEX_KEY='H';
    private static final MaskCharacter[] EmptyMaskChars=new MaskCharacter[0];
    private String mask;
    private transient MaskCharacter[] maskChars;
    private String validCharacters;
    private String invalidCharacters;
    private String placeholderString;
    private char placeholder;
    private boolean containsLiteralChars;

    public MaskFormatter(String mask) throws ParseException{
        this();
        setMask(mask);
    }

    public MaskFormatter(){
        setAllowsInvalid(false);
        containsLiteralChars=true;
        maskChars=EmptyMaskChars;
        placeholder=' ';
    }

    public String getValidCharacters(){
        return validCharacters;
    }    public void setMask(String mask) throws ParseException{
        this.mask=mask;
        updateInternalMask();
    }

    public void setValidCharacters(String validCharacters){
        this.validCharacters=validCharacters;
    }    public String getMask(){
        return mask;
    }

    public String getInvalidCharacters(){
        return invalidCharacters;
    }

    public void setInvalidCharacters(String invalidCharacters){
        this.invalidCharacters=invalidCharacters;
    }

    public void install(JFormattedTextField ftf){
        super.install(ftf);
        // valueToString doesn't throw, but stringToValue does, need to
        // update the editValid state appropriately
        if(ftf!=null){
            Object value=ftf.getValue();
            try{
                stringToValue(valueToString(value));
            }catch(ParseException pe){
                setEditValid(false);
            }
        }
    }

    public Object stringToValue(String value) throws ParseException{
        return stringToValue(value,true);
    }

    public String valueToString(Object value) throws ParseException{
        String sValue=(value==null)?"":value.toString();
        StringBuilder result=new StringBuilder();
        String placeholder=getPlaceholder();
        int[] valueCounter={0};
        append(result,sValue,valueCounter,placeholder,maskChars);
        return result.toString();
    }

    public String getPlaceholder(){
        return placeholderString;
    }

    public void setPlaceholder(String placeholder){
        this.placeholderString=placeholder;
    }

    private void append(StringBuilder result,String value,int[] index,
                        String placeholder,MaskCharacter[] mask)
            throws ParseException{
        for(int counter=0, maxCounter=mask.length;
            counter<maxCounter;counter++){
            mask[counter].append(result,value,index,placeholder);
        }
    }

    boolean isNavigatable(int offset){
        if(!getAllowsInvalid()){
            return (offset<getMaxLength()&&!isLiteral(offset));
        }
        return true;
    }

    boolean isValidEdit(ReplaceHolder rh){
        if(!getAllowsInvalid()){
            String newString=getReplaceString(rh.offset,rh.length,rh.text);
            try{
                rh.value=stringToValue(newString,false);
                return true;
            }catch(ParseException pe){
                return false;
            }
        }
        return true;
    }

    boolean canReplace(ReplaceHolder rh){
        // This method is rather long, but much of the burden is in
        // maintaining a String and swapping to a StringBuilder only if
        // absolutely necessary.
        if(!getAllowsInvalid()){
            StringBuilder replace=null;
            String text=rh.text;
            int tl=(text!=null)?text.length():0;
            if(tl==0&&rh.length==1&&getFormattedTextField().
                    getSelectionStart()!=rh.offset){
                // Backspace, adjust to actually delete next non-literal.
                while(rh.offset>0&&isLiteral(rh.offset)){
                    rh.offset--;
                }
            }
            int max=Math.min(getMaxLength()-rh.offset,
                    Math.max(tl,rh.length));
            for(int counter=0, textIndex=0;counter<max;counter++){
                if(textIndex<tl&&isValidCharacter(rh.offset+counter,
                        text.charAt(textIndex))){
                    char aChar=text.charAt(textIndex);
                    if(aChar!=getCharacter(rh.offset+counter,aChar)){
                        if(replace==null){
                            replace=new StringBuilder();
                            if(textIndex>0){
                                replace.append(text.substring(0,textIndex));
                            }
                        }
                    }
                    if(replace!=null){
                        replace.append(getCharacter(rh.offset+counter,
                                aChar));
                    }
                    textIndex++;
                }else if(isLiteral(rh.offset+counter)){
                    if(replace!=null){
                        replace.append(getLiteral(rh.offset+counter));
                        if(textIndex<tl){
                            max=Math.min(max+1,getMaxLength()-
                                    rh.offset);
                        }
                    }else if(textIndex>0){
                        replace=new StringBuilder(max);
                        replace.append(text.substring(0,textIndex));
                        replace.append(getLiteral(rh.offset+counter));
                        if(textIndex<tl){
                            // Evaluate the character in text again.
                            max=Math.min(max+1,getMaxLength()-
                                    rh.offset);
                        }else if(rh.cursorPosition==-1){
                            rh.cursorPosition=rh.offset+counter;
                        }
                    }else{
                        rh.offset++;
                        rh.length--;
                        counter--;
                        max--;
                    }
                }else if(textIndex>=tl){
                    // placeholder
                    if(replace==null){
                        replace=new StringBuilder();
                        if(text!=null){
                            replace.append(text);
                        }
                    }
                    replace.append(getPlaceholderCharacter());
                    if(tl>0&&rh.cursorPosition==-1){
                        rh.cursorPosition=rh.offset+counter;
                    }
                }else{
                    // Bogus character.
                    return false;
                }
            }
            if(replace!=null){
                rh.text=replace.toString();
            }else if(text!=null&&rh.offset+tl>getMaxLength()){
                rh.text=text.substring(0,getMaxLength()-rh.offset);
            }
            if(getOverwriteMode()&&rh.text!=null){
                rh.length=rh.text.length();
            }
        }
        return super.canReplace(rh);
    }

    private char getLiteral(int index){
        return getMaskCharacter(index).getChar((char)0);
    }

    private char getCharacter(int index,char aChar){
        return getMaskCharacter(index).getChar(aChar);
    }

    private Object stringToValue(String value,boolean completeMatch) throws
            ParseException{
        int errorOffset;
        if((errorOffset=getInvalidOffset(value,completeMatch))==-1){
            if(!getValueContainsLiteralCharacters()){
                value=stripLiteralChars(value);
            }
            return super.stringToValue(value);
        }
        throw new ParseException("stringToValue passed invalid value",
                errorOffset);
    }

    public boolean getValueContainsLiteralCharacters(){
        return containsLiteralChars;
    }

    public void setValueContainsLiteralCharacters(
            boolean containsLiteralChars){
        this.containsLiteralChars=containsLiteralChars;
    }

    private int getInvalidOffset(String string,boolean completeMatch){
        int iLength=string.length();
        if(iLength!=getMaxLength()){
            // trivially false
            return iLength;
        }
        for(int counter=0, max=string.length();counter<max;counter++){
            char aChar=string.charAt(counter);
            if(!isValidCharacter(counter,aChar)&&
                    (completeMatch||!isPlaceholder(counter,aChar))){
                return counter;
            }
        }
        return -1;
    }    private void updateInternalMask() throws ParseException{
        String mask=getMask();
        ArrayList<MaskCharacter> fixed=new ArrayList<MaskCharacter>();
        ArrayList<MaskCharacter> temp=fixed;
        if(mask!=null){
            for(int counter=0, maxCounter=mask.length();
                counter<maxCounter;counter++){
                char maskChar=mask.charAt(counter);
                switch(maskChar){
                    case DIGIT_KEY:
                        temp.add(new DigitMaskCharacter());
                        break;
                    case LITERAL_KEY:
                        if(++counter<maxCounter){
                            maskChar=mask.charAt(counter);
                            temp.add(new LiteralCharacter(maskChar));
                        }
                        // else: Could actually throw if else
                        break;
                    case UPPERCASE_KEY:
                        temp.add(new UpperCaseCharacter());
                        break;
                    case LOWERCASE_KEY:
                        temp.add(new LowerCaseCharacter());
                        break;
                    case ALPHA_NUMERIC_KEY:
                        temp.add(new AlphaNumericCharacter());
                        break;
                    case CHARACTER_KEY:
                        temp.add(new CharCharacter());
                        break;
                    case ANYTHING_KEY:
                        temp.add(new MaskCharacter());
                        break;
                    case HEX_KEY:
                        temp.add(new HexCharacter());
                        break;
                    default:
                        temp.add(new LiteralCharacter(maskChar));
                        break;
                }
            }
        }
        if(fixed.size()==0){
            maskChars=EmptyMaskChars;
        }else{
            maskChars=new MaskCharacter[fixed.size()];
            fixed.toArray(maskChars);
        }
    }

    private boolean isPlaceholder(int index,char aChar){
        return (getPlaceholderCharacter()==aChar);
    }

    public char getPlaceholderCharacter(){
        return placeholder;
    }

    public void setPlaceholderCharacter(char placeholder){
        this.placeholder=placeholder;
    }

    private boolean isValidCharacter(int index,char aChar){
        return getMaskCharacter(index).isValidCharacter(aChar);
    }

    private MaskCharacter getMaskCharacter(int index){
        if(index>=maskChars.length){
            return null;
        }
        return maskChars[index];
    }

    private int getMaxLength(){
        return maskChars.length;
    }

    private String stripLiteralChars(String string){
        StringBuilder sb=null;
        int last=0;
        for(int counter=0, max=string.length();counter<max;counter++){
            if(isLiteral(counter)){
                if(sb==null){
                    sb=new StringBuilder();
                    if(counter>0){
                        sb.append(string.substring(0,counter));
                    }
                    last=counter+1;
                }else if(last!=counter){
                    sb.append(string.substring(last,counter));
                }
                last=counter+1;
            }
        }
        if(sb==null){
            // Assume the mask isn't all literals.
            return string;
        }else if(last!=string.length()){
            if(sb==null){
                return string.substring(last);
            }
            sb.append(string.substring(last));
        }
        return sb.toString();
    }

    private boolean isLiteral(int index){
        return getMaskCharacter(index).isLiteral();
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        try{
            updateInternalMask();
        }catch(ParseException pe){
            // assert();
        }
    }

    //
    // Interal classes used to represent the mask.
    //
    private class MaskCharacter{
        public void append(StringBuilder buff,String formatting,int[] index,
                           String placeholder)
                throws ParseException{
            boolean inString=index[0]<formatting.length();
            char aChar=inString?formatting.charAt(index[0]):0;
            if(isLiteral()){
                buff.append(getChar(aChar));
                if(getValueContainsLiteralCharacters()){
                    if(inString&&aChar!=getChar(aChar)){
                        throw new ParseException("Invalid character: "+
                                aChar,index[0]);
                    }
                    index[0]=index[0]+1;
                }
            }else if(index[0]>=formatting.length()){
                if(placeholder!=null&&index[0]<placeholder.length()){
                    buff.append(placeholder.charAt(index[0]));
                }else{
                    buff.append(getPlaceholderCharacter());
                }
                index[0]=index[0]+1;
            }else if(isValidCharacter(aChar)){
                buff.append(getChar(aChar));
                index[0]=index[0]+1;
            }else{
                throw new ParseException("Invalid character: "+aChar,
                        index[0]);
            }
        }

        public boolean isLiteral(){
            return false;
        }

        public boolean isValidCharacter(char aChar){
            if(isLiteral()){
                return (getChar(aChar)==aChar);
            }
            aChar=getChar(aChar);
            String filter=getValidCharacters();
            if(filter!=null&&filter.indexOf(aChar)==-1){
                return false;
            }
            filter=getInvalidCharacters();
            if(filter!=null&&filter.indexOf(aChar)!=-1){
                return false;
            }
            return true;
        }

        public char getChar(char aChar){
            return aChar;
        }
    }

    private class LiteralCharacter extends MaskCharacter{
        private char fixedChar;

        public LiteralCharacter(char fixedChar){
            this.fixedChar=fixedChar;
        }

        public boolean isLiteral(){
            return true;
        }

        public char getChar(char aChar){
            return fixedChar;
        }
    }

    private class DigitMaskCharacter extends MaskCharacter{
        public boolean isValidCharacter(char aChar){
            return (Character.isDigit(aChar)&&
                    super.isValidCharacter(aChar));
        }
    }

    private class UpperCaseCharacter extends MaskCharacter{
        public boolean isValidCharacter(char aChar){
            return (Character.isLetter(aChar)&&
                    super.isValidCharacter(aChar));
        }

        public char getChar(char aChar){
            return Character.toUpperCase(aChar);
        }
    }

    private class LowerCaseCharacter extends MaskCharacter{
        public boolean isValidCharacter(char aChar){
            return (Character.isLetter(aChar)&&
                    super.isValidCharacter(aChar));
        }

        public char getChar(char aChar){
            return Character.toLowerCase(aChar);
        }
    }

    private class AlphaNumericCharacter extends MaskCharacter{
        public boolean isValidCharacter(char aChar){
            return (Character.isLetterOrDigit(aChar)&&
                    super.isValidCharacter(aChar));
        }
    }

    private class CharCharacter extends MaskCharacter{
        public boolean isValidCharacter(char aChar){
            return (Character.isLetter(aChar)&&
                    super.isValidCharacter(aChar));
        }
    }

    private class HexCharacter extends MaskCharacter{
        public boolean isValidCharacter(char aChar){
            return ((aChar=='0'||aChar=='1'||
                    aChar=='2'||aChar=='3'||
                    aChar=='4'||aChar=='5'||
                    aChar=='6'||aChar=='7'||
                    aChar=='8'||aChar=='9'||
                    aChar=='a'||aChar=='A'||
                    aChar=='b'||aChar=='B'||
                    aChar=='c'||aChar=='C'||
                    aChar=='d'||aChar=='D'||
                    aChar=='e'||aChar=='E'||
                    aChar=='f'||aChar=='F')&&
                    super.isValidCharacter(aChar));
        }

        public char getChar(char aChar){
            if(Character.isDigit(aChar)){
                return aChar;
            }
            return Character.toUpperCase(aChar);
        }
    }






}
