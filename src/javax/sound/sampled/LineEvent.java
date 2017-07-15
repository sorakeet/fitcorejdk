/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public class LineEvent extends java.util.EventObject{
    // INSTANCE VARIABLES
    private final Type type;
    private final long position;

    public LineEvent(Line line,Type type,long position){
        super(line);
        this.type=type;
        this.position=position;
    }

    public final Type getType(){
        return type;
    }

    public final long getFramePosition(){
        return position;
    }

    public String toString(){
        String sType="";
        if(type!=null) sType=type.toString()+" ";
        String sLine;
        if(getLine()==null){
            sLine="null";
        }else{
            sLine=getLine().toString();
        }
        return new String(sType+"event from line "+sLine);
    }

    public final Line getLine(){
        return (Line)getSource();
    }

    public static class Type{
        // LINE EVENT TYPE DEFINES
        public static final Type OPEN=new Type("Open");
        public static final Type CLOSE=new Type("Close");
        public static final Type START=new Type("Start");
        public static final Type STOP=new Type("Stop");
        // $$kk: 03.25.99: why can't this be final??
        private /**final*/
                String name;
        protected Type(String name){
            this.name=name;
        }

        public final int hashCode(){
            return super.hashCode();
        }

        //$$fb 2002-11-26: fix for 4695001: SPEC: description of equals() method contains typo
        public final boolean equals(Object obj){
            return super.equals(obj);
        }

        public String toString(){
            return name;
        }
        /**
         * A type of event that is sent when a line ceases to engage in active
         * input or output of audio data because the end of media has been reached.
         */
        /**
         * ISSUE: we may want to get rid of this.  Is JavaSound
         * responsible for reporting this??
         *
         * [If it's decided to keep this API, the docs will need to be updated to include mention
         * of EOM events elsewhere.]
         */
        //public static final Type EOM  = new Type("EOM");
        /**
         * A type of event that is sent when a line begins to engage in active
         * input or output of audio data.  Examples of when this happens are
         * when a source line begins or resumes writing data to its mixer, and
         * when a target line begins or resumes reading data from its mixer.
         * @see #STOP
         * @see SourceDataLine#write
         * @see TargetDataLine#read
         * @see DataLine#start
         */
        //public static final Type ACTIVE       = new Type("ACTIVE");
        /**
         * A type of event that is sent when a line ceases active input or output
         * of audio data.
         * @see #START
         * @see DataLine#stop
         */
        //public static final Type INACTIVE     = new Type("INACTIVE");
    } // class Type
} // class LineEvent
