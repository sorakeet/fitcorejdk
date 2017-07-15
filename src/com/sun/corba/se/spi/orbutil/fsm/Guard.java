/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orbutil.fsm;

public interface Guard{
    public Result evaluate(FSM fsm,Input in);

    public static final class Complement extends GuardBase{
        private Guard guard;

        public Complement(GuardBase guard){
            super("not("+guard.getName()+")");
            this.guard=guard;
        }

        public Result evaluate(FSM fsm,Input in){
            return guard.evaluate(fsm,in).complement();
        }
    }

    public static final class Result{
        public static final Result ENABLED=new Result("ENABLED");
        public static final Result DISABLED=new Result("DISABLED");
        public static final Result DEFERED=new Result("DEFERED");
        private String name;

        private Result(String name){
            this.name=name;
        }

        public static Result convert(boolean res){
            return res?ENABLED:DISABLED;
        }

        public Result complement(){
            if(this==ENABLED)
                return DISABLED;
            else if(this==DISABLED)
                return ENABLED;
            else
                return DEFERED;
        }

        public String toString(){
            return "Guard.Result["+name+"]";
        }
    }
}
// end of Action.java
