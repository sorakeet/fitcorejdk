/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

public interface Position{
    public int getOffset();

    public static final class Bias{
        public static final Bias Forward=new Bias("Forward");
        public static final Bias Backward=new Bias("Backward");
        private String name;

        private Bias(String name){
            this.name=name;
        }

        public String toString(){
            return name;
        }
    }
}
