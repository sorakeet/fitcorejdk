/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orbutil.fsm;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.orbutil.fsm.StateEngineImpl;

public class FSMImpl implements FSM{
    private boolean debug;
    private State state;
    private StateEngineImpl stateEngine;

    public FSMImpl(StateEngine se,State startState){
        this(se,startState,false);
    }

    public FSMImpl(StateEngine se,State startState,boolean debug){
        state=startState;
        stateEngine=(StateEngineImpl)se;
        this.debug=debug;
    }

    public State getState(){
        return state;
    }

    public void doIt(Input in){
        stateEngine.doIt(this,in,debug);
    }
    // Methods for use only by StateEngineImpl

    public void internalSetState(State nextState){
        if(debug){
            ORBUtility.dprint(this,"Calling internalSetState with nextState = "+
                    nextState);
        }
        state=nextState;
        if(debug){
            ORBUtility.dprint(this,"Exiting internalSetState with state = "+
                    state);
        }
    }
}
// end of FSMImpl.java
