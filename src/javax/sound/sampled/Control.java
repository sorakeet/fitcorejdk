/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public abstract class Control{
    // INSTANCE VARIABLES
    private final Type type;
    // CONSTRUCTORS

    protected Control(Type type){
        this.type=type;
    }
    // METHODS

    public static class Type{
        // CONTROL TYPE DEFINES
        // INSTANCE VARIABLES
        private String name;
        // CONSTRUCTOR

        protected Type(String name){
            this.name=name;
        }
        // METHODS

        public final int hashCode(){
            return super.hashCode();
        }

        public final boolean equals(Object obj){
            return super.equals(obj);
        }

        public final String toString(){
            return name;
        }
    } // class Type    public Type getType(){
        return type;
    }
    // ABSTRACT METHODS

    public String toString(){
        return new String(getType()+" Control");
    }


} // class Control
