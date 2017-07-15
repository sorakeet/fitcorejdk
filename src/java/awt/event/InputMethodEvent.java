/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

import java.awt.*;
import java.awt.font.TextHitInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.annotation.Native;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

public class InputMethodEvent extends AWTEvent{
    @Native
    public static final int INPUT_METHOD_FIRST=1100;
    @Native
    public static final int INPUT_METHOD_TEXT_CHANGED=INPUT_METHOD_FIRST;
    @Native
    public static final int CARET_POSITION_CHANGED=INPUT_METHOD_FIRST+1;
    @Native
    public static final int INPUT_METHOD_LAST=INPUT_METHOD_FIRST+1;
    private static final long serialVersionUID=4727190874778922661L;
    long when;
    // Text object
    private transient AttributedCharacterIterator text;
    private transient int committedCharacterCount;
    private transient TextHitInfo caret;
    private transient TextHitInfo visiblePosition;

    public InputMethodEvent(Component source,int id,
                            AttributedCharacterIterator text,int committedCharacterCount,
                            TextHitInfo caret,TextHitInfo visiblePosition){
        this(source,id,
                getMostRecentEventTimeForSource(source),
                text,committedCharacterCount,
                caret,visiblePosition);
    }

    public InputMethodEvent(Component source,int id,long when,
                            AttributedCharacterIterator text,int committedCharacterCount,
                            TextHitInfo caret,TextHitInfo visiblePosition){
        super(source,id);
        if(id<INPUT_METHOD_FIRST||id>INPUT_METHOD_LAST){
            throw new IllegalArgumentException("id outside of valid range");
        }
        if(id==CARET_POSITION_CHANGED&&text!=null){
            throw new IllegalArgumentException("text must be null for CARET_POSITION_CHANGED");
        }
        this.when=when;
        this.text=text;
        int textLength=0;
        if(text!=null){
            textLength=text.getEndIndex()-text.getBeginIndex();
        }
        if(committedCharacterCount<0||committedCharacterCount>textLength){
            throw new IllegalArgumentException("committedCharacterCount outside of valid range");
        }
        this.committedCharacterCount=committedCharacterCount;
        this.caret=caret;
        this.visiblePosition=visiblePosition;
    }

    private static long getMostRecentEventTimeForSource(Object source){
        if(source==null){
            // throw the IllegalArgumentException to conform to EventObject spec
            throw new IllegalArgumentException("null source");
        }
        AppContext appContext=SunToolkit.targetToAppContext(source);
        EventQueue eventQueue=SunToolkit.getSystemEventQueueImplPP(appContext);
        return AWTAccessor.getEventQueueAccessor().getMostRecentEventTime(eventQueue);
    }

    public InputMethodEvent(Component source,int id,TextHitInfo caret,
                            TextHitInfo visiblePosition){
        this(source,id,
                getMostRecentEventTimeForSource(source),
                null,0,caret,visiblePosition);
    }

    public AttributedCharacterIterator getText(){
        return text;
    }

    public int getCommittedCharacterCount(){
        return committedCharacterCount;
    }

    public TextHitInfo getCaret(){
        return caret;
    }

    public TextHitInfo getVisiblePosition(){
        return visiblePosition;
    }

    public long getWhen(){
        return when;
    }

    public String paramString(){
        String typeStr;
        switch(id){
            case INPUT_METHOD_TEXT_CHANGED:
                typeStr="INPUT_METHOD_TEXT_CHANGED";
                break;
            case CARET_POSITION_CHANGED:
                typeStr="CARET_POSITION_CHANGED";
                break;
            default:
                typeStr="unknown type";
        }
        String textString;
        if(text==null){
            textString="no text";
        }else{
            StringBuilder textBuffer=new StringBuilder("\"");
            int committedCharacterCount=this.committedCharacterCount;
            char c=text.first();
            while(committedCharacterCount-->0){
                textBuffer.append(c);
                c=text.next();
            }
            textBuffer.append("\" + \"");
            while(c!=CharacterIterator.DONE){
                textBuffer.append(c);
                c=text.next();
            }
            textBuffer.append("\"");
            textString=textBuffer.toString();
        }
        String countString=committedCharacterCount+" characters committed";
        String caretString;
        if(caret==null){
            caretString="no caret";
        }else{
            caretString="caret: "+caret.toString();
        }
        String visiblePositionString;
        if(visiblePosition==null){
            visiblePositionString="no visible position";
        }else{
            visiblePositionString="visible position: "+visiblePosition.toString();
        }
        return typeStr+", "+textString+", "+countString+", "+caretString+", "+visiblePositionString;
    }

    public void consume(){
        consumed=true;
    }

    public boolean isConsumed(){
        return consumed;
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException{
        s.defaultReadObject();
        if(when==0){
            // Can't use getMostRecentEventTimeForSource because source is always null during deserialization
            when=EventQueue.getMostRecentEventTime();
        }
    }
}
