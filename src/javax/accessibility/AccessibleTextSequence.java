/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

public class AccessibleTextSequence{
    public int startIndex;
    public int endIndex;
    public String text;

    public AccessibleTextSequence(int start,int end,String txt){
        startIndex=start;
        endIndex=end;
        text=txt;
    }
};
