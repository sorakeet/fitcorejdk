/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.nimbus;

import javax.swing.*;
import javax.swing.plaf.synth.SynthConstants;
import java.util.HashMap;
import java.util.Map;

public abstract class State<T extends JComponent>{
    static final Map<String,StandardState> standardStates=new HashMap<String,StandardState>(7);
    static final State Enabled=new StandardState(SynthConstants.ENABLED);
    static final State MouseOver=new StandardState(SynthConstants.MOUSE_OVER);
    static final State Pressed=new StandardState(SynthConstants.PRESSED);
    static final State Disabled=new StandardState(SynthConstants.DISABLED);
    static final State Focused=new StandardState(SynthConstants.FOCUSED);
    static final State Selected=new StandardState(SynthConstants.SELECTED);
    static final State Default=new StandardState(SynthConstants.DEFAULT);
    private String name;

    protected State(String name){
        this.name=name;
    }

    static boolean isStandardStateName(String name){
        return standardStates.containsKey(name);
    }

    static StandardState getStandardState(String name){
        return standardStates.get(name);
    }

    @Override
    public String toString(){
        return name;
    }

    boolean isInState(T c,int s){
        return isInState(c);
    }

    protected abstract boolean isInState(T c);

    String getName(){
        return name;
    }

    static final class StandardState extends State<JComponent>{
        private int state;

        private StandardState(int state){
            super(toString(state));
            this.state=state;
            standardStates.put(getName(),this);
        }

        private static String toString(int state){
            StringBuffer buffer=new StringBuffer();
            if((state&SynthConstants.DEFAULT)==SynthConstants.DEFAULT){
                buffer.append("Default");
            }
            if((state&SynthConstants.DISABLED)==SynthConstants.DISABLED){
                if(buffer.length()>0) buffer.append("+");
                buffer.append("Disabled");
            }
            if((state&SynthConstants.ENABLED)==SynthConstants.ENABLED){
                if(buffer.length()>0) buffer.append("+");
                buffer.append("Enabled");
            }
            if((state&SynthConstants.FOCUSED)==SynthConstants.FOCUSED){
                if(buffer.length()>0) buffer.append("+");
                buffer.append("Focused");
            }
            if((state&SynthConstants.MOUSE_OVER)==SynthConstants.MOUSE_OVER){
                if(buffer.length()>0) buffer.append("+");
                buffer.append("MouseOver");
            }
            if((state&SynthConstants.PRESSED)==SynthConstants.PRESSED){
                if(buffer.length()>0) buffer.append("+");
                buffer.append("Pressed");
            }
            if((state&SynthConstants.SELECTED)==SynthConstants.SELECTED){
                if(buffer.length()>0) buffer.append("+");
                buffer.append("Selected");
            }
            return buffer.toString();
        }

        public int getState(){
            return state;
        }

        @Override
        boolean isInState(JComponent c,int s){
            return (s&state)==state;
        }

        @Override
        protected boolean isInState(JComponent c){
            throw new AssertionError("This method should never be called");
        }
    }
}
