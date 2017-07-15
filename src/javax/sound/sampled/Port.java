/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public interface Port extends Line{
    // INNER CLASSES

    public static class Info extends Line.Info{
        // AUDIO PORT TYPE DEFINES
        // SOURCE PORTS
        public static final Info MICROPHONE=new Info(Port.class,"MICROPHONE",true);
        public static final Info LINE_IN=new Info(Port.class,"LINE_IN",true);
        public static final Info COMPACT_DISC=new Info(Port.class,"COMPACT_DISC",true);
        // TARGET PORTS
        public static final Info SPEAKER=new Info(Port.class,"SPEAKER",false);
        public static final Info HEADPHONE=new Info(Port.class,"HEADPHONE",false);
        public static final Info LINE_OUT=new Info(Port.class,"LINE_OUT",false);
        // FUTURE DIRECTIONS...
        // telephone
        // DAT
        // DVD
        // INSTANCE VARIABLES
        private String name;
        private boolean isSource;
        // CONSTRUCTOR

        public Info(Class<?> lineClass,String name,boolean isSource){
            super(lineClass);
            this.name=name;
            this.isSource=isSource;
        }
        // METHODS

        public boolean matches(Line.Info info){
            if(!(super.matches(info))){
                return false;
            }
            if(!(name.equals(((Info)info).getName()))){
                return false;
            }
            if(!(isSource==((Info)info).isSource())){
                return false;
            }
            return true;
        }

        public String getName(){
            return name;
        }

        public boolean isSource(){
            return isSource;
        }

        public final String toString(){
            return (name+((isSource==true)?" source":" target")+" port");
        }

        public final int hashCode(){
            return super.hashCode();
        }

        public final boolean equals(Object obj){
            return super.equals(obj);
        }
    } // class Info
}
