/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

import java.util.Enumeration;

public interface Name
        extends Cloneable, java.io.Serializable, Comparable<Object>{
    static final long serialVersionUID=-3617482732056931635L;

    public Object clone();

    public int compareTo(Object obj);

    public int size();

    public boolean isEmpty();

    public Enumeration<String> getAll();

    public String get(int posn);

    public Name getPrefix(int posn);

    public Name getSuffix(int posn);

    public boolean startsWith(Name n);

    public boolean endsWith(Name n);

    public Name addAll(Name suffix) throws InvalidNameException;

    public Name addAll(int posn,Name n) throws InvalidNameException;

    public Name add(String comp) throws InvalidNameException;

    public Name add(int posn,String comp) throws InvalidNameException;

    public Object remove(int posn) throws InvalidNameException;
}
