/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.CharacterIterator;
import java.text.Format;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

public class InternationalFormatter extends DefaultFormatter{
    private static final Format.Field[] EMPTY_FIELD_ARRAY=new Format.Field[0];
    private Format format;
    private Comparable max;
    private Comparable min;
    private transient BitSet literalMask;
    private transient AttributedCharacterIterator iterator;
    private transient boolean validMask;
    private transient String string;
    private transient boolean ignoreDocumentMutate;

    public InternationalFormatter(Format format){
        this();
        setFormat(format);
    }

    public InternationalFormatter(){
        setOverwriteMode(false);
    }

    public void install(JFormattedTextField ftf){
        super.install(ftf);
        updateMaskIfNecessary();
        // invoked again as the mask should now be valid.
        positionCursorAtInitialLocation();
    }

    public Object stringToValue(String text) throws ParseException{
        Object value=stringToValue(text,getFormat());
        // Convert to the value class if the Value returned from the
        // Format does not match.
        if(value!=null&&getValueClass()!=null&&
                !getValueClass().isInstance(value)){
            value=super.stringToValue(value.toString());
        }
        try{
            if(!isValidValue(value,true)){
                throw new ParseException("Value not within min/max range",0);
            }
        }catch(ClassCastException cce){
            throw new ParseException("Class cast exception comparing values: "
                    +cce,0);
        }
        return value;
    }

    public String valueToString(Object value) throws ParseException{
        if(value==null){
            return "";
        }
        Format f=getFormat();
        if(f==null){
            return value.toString();
        }
        return f.format(value);
    }

    public Object clone() throws CloneNotSupportedException{
        InternationalFormatter formatter=(InternationalFormatter)super.
                clone();
        formatter.literalMask=null;
        formatter.iterator=null;
        formatter.validMask=false;
        formatter.string=null;
        return formatter;
    }

    boolean isNavigatable(int offset){
        return !isLiteral(offset);
    }

    boolean isLiteral(int index){
        if(isValidMask()&&index<string.length()){
            return literalMask.get(index);
        }
        return false;
    }

    void updateValue(Object value){
        super.updateValue(value);
        updateMaskIfNecessary();
    }

    boolean canReplace(ReplaceHolder rh){
        if(!getAllowsInvalid()){
            String text=rh.text;
            int tl=(text!=null)?text.length():0;
            JTextComponent c=getFormattedTextField();
            if(tl==0&&rh.length==1&&c.getSelectionStart()!=rh.offset){
                // Backspace, adjust to actually delete next non-literal.
                rh.offset=getNextNonliteralIndex(rh.offset,-1);
            }else if(getOverwriteMode()){
                int pos=rh.offset;
                int textPos=pos;
                boolean overflown=false;
                for(int i=0;i<rh.length;i++){
                    while(isLiteral(pos)) pos++;
                    if(pos>=string.length()){
                        pos=textPos;
                        overflown=true;
                        break;
                    }
                    textPos=++pos;
                }
                if(overflown||c.getSelectedText()==null){
                    rh.length=pos-rh.offset;
                }
            }else if(tl>0){
                // insert (or insert and remove)
                rh.offset=getNextNonliteralIndex(rh.offset,1);
            }else{
                // remove only
                rh.offset=getNextNonliteralIndex(rh.offset,-1);
            }
            ((ExtendedReplaceHolder)rh).endOffset=rh.offset;
            ((ExtendedReplaceHolder)rh).endTextLength=(rh.text!=null)?
                    rh.text.length():0;
        }else{
            ((ExtendedReplaceHolder)rh).endOffset=rh.offset;
            ((ExtendedReplaceHolder)rh).endTextLength=(rh.text!=null)?
                    rh.text.length():0;
        }
        boolean can=super.canReplace(rh);
        if(can&&!getAllowsInvalid()){
            ((ExtendedReplaceHolder)rh).resetFromValue(this);
        }
        return can;
    }

    void replace(DocumentFilter.FilterBypass fb,int offset,
                 int length,String text,
                 AttributeSet attrs) throws BadLocationException{
        if(ignoreDocumentMutate){
            fb.replace(offset,length,text,attrs);
            return;
        }
        super.replace(fb,offset,length,text,attrs);
    }

    boolean replace(ReplaceHolder rh) throws BadLocationException{
        int start=-1;
        int direction=1;
        int literalCount=-1;
        if(rh.length>0&&(rh.text==null||rh.text.length()==0)&&
                (getFormattedTextField().getSelectionStart()!=rh.offset||
                        rh.length>1)){
            direction=-1;
        }
        if(!getAllowsInvalid()){
            if((rh.text==null||rh.text.length()==0)&&rh.length>0){
                // remove
                start=getFormattedTextField().getSelectionStart();
            }else{
                start=rh.offset;
            }
            literalCount=getLiteralCountTo(start);
        }
        if(super.replace(rh)){
            if(start!=-1){
                int end=((ExtendedReplaceHolder)rh).endOffset;
                end+=((ExtendedReplaceHolder)rh).endTextLength;
                repositionCursor(literalCount,end,direction);
            }else{
                start=((ExtendedReplaceHolder)rh).endOffset;
                if(direction==1){
                    start+=((ExtendedReplaceHolder)rh).endTextLength;
                }
                repositionCursor(start,direction);
            }
            return true;
        }
        return false;
    }

    ReplaceHolder getReplaceHolder(DocumentFilter.FilterBypass fb,int offset,
                                   int length,String text,
                                   AttributeSet attrs){
        if(replaceHolder==null){
            replaceHolder=new ExtendedReplaceHolder();
        }
        return super.getReplaceHolder(fb,offset,length,text,attrs);
    }

    Object stringToValue(String text,Format f) throws ParseException{
        if(f==null){
            return text;
        }
        return f.parseObject(text);
    }

    boolean isValidValue(Object value,boolean wantsCCE){
        Comparable min=getMinimum();
        try{
            if(min!=null&&min.compareTo(value)>0){
                return false;
            }
        }catch(ClassCastException cce){
            if(wantsCCE){
                throw cce;
            }
            return false;
        }
        Comparable max=getMaximum();
        try{
            if(max!=null&&max.compareTo(value)<0){
                return false;
            }
        }catch(ClassCastException cce){
            if(wantsCCE){
                throw cce;
            }
            return false;
        }
        return true;
    }

    public Comparable getMinimum(){
        return min;
    }

    public void setMinimum(Comparable minimum){
        if(getValueClass()==null&&minimum!=null){
            setValueClass(minimum.getClass());
        }
        min=minimum;
    }

    public Comparable getMaximum(){
        return max;
    }

    public void setMaximum(Comparable max){
        if(getValueClass()==null&&max!=null){
            setValueClass(max.getClass());
        }
        this.max=max;
    }

    void updateMaskIfNecessary(){
        if(!getAllowsInvalid()&&(getFormat()!=null)){
            if(!isValidMask()){
                updateMask();
            }else{
                String newString=getFormattedTextField().getText();
                if(!newString.equals(string)){
                    updateMask();
                }
            }
        }
    }

    public Format getFormat(){
        return format;
    }

    public void setFormat(Format format){
        this.format=format;
    }

    void updateMask(){
        if(getFormat()!=null){
            Document doc=getFormattedTextField().getDocument();
            validMask=false;
            if(doc!=null){
                try{
                    string=doc.getText(0,doc.getLength());
                }catch(BadLocationException ble){
                    string=null;
                }
                if(string!=null){
                    try{
                        Object value=stringToValue(string);
                        AttributedCharacterIterator iterator=getFormat().
                                formatToCharacterIterator(value);
                        updateMask(iterator);
                    }catch(ParseException pe){
                    }catch(IllegalArgumentException iae){
                    }catch(NullPointerException npe){
                    }
                }
            }
        }
    }

    private void updateMask(AttributedCharacterIterator iterator){
        if(iterator!=null){
            validMask=true;
            this.iterator=iterator;
            // Update the literal mask
            if(literalMask==null){
                literalMask=new BitSet();
            }else{
                for(int counter=literalMask.length()-1;counter>=0;
                    counter--){
                    literalMask.clear(counter);
                }
            }
            iterator.first();
            while(iterator.current()!=CharacterIterator.DONE){
                Map attributes=iterator.getAttributes();
                boolean set=isLiteral(attributes);
                int start=iterator.getIndex();
                int end=iterator.getRunLimit();
                while(start<end){
                    if(set){
                        literalMask.set(start);
                    }else{
                        literalMask.clear(start);
                    }
                    start++;
                }
                iterator.setIndex(start);
            }
        }
    }

    boolean isLiteral(Map attributes){
        return ((attributes==null)||attributes.size()==0);
    }

    boolean isValidMask(){
        return validMask;
    }

    public Format.Field[] getFields(int offset){
        if(getAllowsInvalid()){
            // This will work if the currently edited value is valid.
            updateMask();
        }
        Map<Attribute,Object> attrs=getAttributes(offset);
        if(attrs!=null&&attrs.size()>0){
            ArrayList<Attribute> al=new ArrayList<Attribute>();
            al.addAll(attrs.keySet());
            return al.toArray(EMPTY_FIELD_ARRAY);
        }
        return EMPTY_FIELD_ARRAY;
    }

    Map<Attribute,Object> getAttributes(int index){
        if(isValidMask()){
            AttributedCharacterIterator iterator=getIterator();
            if(index>=0&&index<=iterator.getEndIndex()){
                iterator.setIndex(index);
                return iterator.getAttributes();
            }
        }
        return null;
    }

    AttributedCharacterIterator getIterator(){
        return iterator;
    }

    protected Action[] getActions(){
        if(getSupportsIncrement()){
            return new Action[]{new IncrementAction("increment",1),
                    new IncrementAction("decrement",-1)};
        }
        return null;
    }

    boolean getSupportsIncrement(){
        return false;
    }

    int getAttributeStart(Attribute id){
        if(isValidMask()){
            AttributedCharacterIterator iterator=getIterator();
            iterator.first();
            while(iterator.current()!=CharacterIterator.DONE){
                if(iterator.getAttribute(id)!=null){
                    return iterator.getIndex();
                }
                iterator.next();
            }
        }
        return -1;
    }

    int getLiteralCountTo(int index){
        int lCount=0;
        for(int counter=0;counter<index;counter++){
            if(isLiteral(counter)){
                lCount++;
            }
        }
        return lCount;
    }

    char getLiteral(int index){
        if(isValidMask()&&string!=null&&index<string.length()){
            return string.charAt(index);
        }
        return (char)0;
    }

    private int getNextNonliteralIndex(int index,int direction){
        int max=getFormattedTextField().getDocument().getLength();
        while(index>=0&&index<max){
            if(isLiteral(index)){
                index+=direction;
            }else{
                return index;
            }
        }
        return (direction==-1)?0:max;
    }

    private void repositionCursor(int startLiteralCount,int end,
                                  int direction){
        int endLiteralCount=getLiteralCountTo(end);
        if(endLiteralCount!=end){
            end-=startLiteralCount;
            for(int counter=0;counter<end;counter++){
                if(isLiteral(counter)){
                    end++;
                }
            }
        }
        repositionCursor(end,1 /**direction*/);
    }

    char getBufferedChar(int index){
        if(isValidMask()){
            if(string!=null&&index<string.length()){
                return string.charAt(index);
            }
        }
        return (char)0;
    }

    boolean canIncrement(Object field,int cursorPosition){
        return (field!=null);
    }

    void selectField(Object f,int count){
        AttributedCharacterIterator iterator=getIterator();
        if(iterator!=null&&
                (f instanceof Attribute)){
            Attribute field=
                    (Attribute)f;
            iterator.first();
            while(iterator.current()!=CharacterIterator.DONE){
                while(iterator.getAttribute(field)==null&&
                        iterator.next()!=CharacterIterator.DONE) ;
                if(iterator.current()!=CharacterIterator.DONE){
                    int limit=iterator.getRunLimit(field);
                    if(--count<=0){
                        getFormattedTextField().select(iterator.getIndex(),
                                limit);
                        break;
                    }
                    iterator.setIndex(limit);
                    iterator.next();
                }
            }
        }
    }

    Object getAdjustField(int start,Map attributes){
        return null;
    }

    private int getFieldTypeCountTo(Object f,int start){
        AttributedCharacterIterator iterator=getIterator();
        int count=0;
        if(iterator!=null&&
                (f instanceof Attribute)){
            Attribute field=
                    (Attribute)f;
            iterator.first();
            while(iterator.getIndex()<start){
                while(iterator.getAttribute(field)==null&&
                        iterator.next()!=CharacterIterator.DONE) ;
                if(iterator.current()!=CharacterIterator.DONE){
                    iterator.setIndex(iterator.getRunLimit(field));
                    iterator.next();
                    count++;
                }else{
                    break;
                }
            }
        }
        return count;
    }

    Object adjustValue(Object value,Map attributes,Object field,
                       int direction) throws
            BadLocationException, ParseException{
        return null;
    }

    void resetValue(Object value) throws BadLocationException, ParseException{
        Document doc=getFormattedTextField().getDocument();
        String string=valueToString(value);
        try{
            ignoreDocumentMutate=true;
            doc.remove(0,doc.getLength());
            doc.insertString(0,string,null);
        }finally{
            ignoreDocumentMutate=false;
        }
        updateValue(value);
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        updateMaskIfNecessary();
    }

    static class ExtendedReplaceHolder extends ReplaceHolder{
        int endOffset;
        int endTextLength;

        void resetFromValue(InternationalFormatter formatter){
            // Need to reset the complete string as Format's result can
            // be completely different.
            offset=0;
            try{
                text=formatter.valueToString(value);
            }catch(ParseException pe){
                // Should never happen, otherwise canReplace would have
                // returned value.
                text="";
            }
            length=fb.getDocument().getLength();
        }
    }

    private class IncrementAction extends AbstractAction{
        private int direction;

        IncrementAction(String name,int direction){
            super(name);
            this.direction=direction;
        }

        public void actionPerformed(ActionEvent ae){
            if(getFormattedTextField().isEditable()){
                if(getAllowsInvalid()){
                    // This will work if the currently edited value is valid.
                    updateMask();
                }
                boolean validEdit=false;
                if(isValidMask()){
                    int start=getFormattedTextField().getSelectionStart();
                    if(start!=-1){
                        AttributedCharacterIterator iterator=getIterator();
                        iterator.setIndex(start);
                        Map attributes=iterator.getAttributes();
                        Object field=getAdjustField(start,attributes);
                        if(canIncrement(field,start)){
                            try{
                                Object value=stringToValue(
                                        getFormattedTextField().getText());
                                int fieldTypeCount=getFieldTypeCountTo(
                                        field,start);
                                value=adjustValue(value,attributes,
                                        field,direction);
                                if(value!=null&&isValidValue(value,false)){
                                    resetValue(value);
                                    updateMask();
                                    if(isValidMask()){
                                        selectField(field,fieldTypeCount);
                                    }
                                    validEdit=true;
                                }
                            }catch(ParseException pe){
                            }catch(BadLocationException ble){
                            }
                        }
                    }
                }
                if(!validEdit){
                    invalidEdit();
                }
            }
        }
    }
}
