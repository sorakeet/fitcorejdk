/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: XMLString.java,v 1.2.4.1 2005/09/15 08:16:02 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: XMLString.java,v 1.2.4.1 2005/09/15 08:16:02 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import java.util.Locale;

public interface XMLString{
    public abstract void dispatchCharactersEvents(org.xml.sax.ContentHandler ch)
            throws org.xml.sax.SAXException;

    public abstract void dispatchAsComment(org.xml.sax.ext.LexicalHandler lh)
            throws org.xml.sax.SAXException;

    public XMLString fixWhiteSpace(boolean trimHead,
                                   boolean trimTail,
                                   boolean doublePunctuationSpaces);

    public abstract int length();

    public abstract char charAt(int index);

    public abstract void getChars(int srcBegin,int srcEnd,char dst[],
                                  int dstBegin);

    public abstract boolean equals(XMLString anObject);

    public abstract boolean equals(String anotherString);

    public abstract boolean equalsIgnoreCase(String anotherString);

    public abstract int compareTo(XMLString anotherString);

    public abstract int compareToIgnoreCase(XMLString str);

    public abstract boolean startsWith(String prefix,int toffset);

    public abstract boolean startsWith(XMLString prefix,int toffset);

    public abstract boolean startsWith(String prefix);

    public abstract boolean startsWith(XMLString prefix);

    public abstract boolean endsWith(String suffix);

    public abstract int hashCode();

    public abstract boolean equals(Object anObject);

    public abstract String toString();

    public abstract int indexOf(int ch);

    public abstract int indexOf(int ch,int fromIndex);

    public abstract int lastIndexOf(int ch);

    public abstract int lastIndexOf(int ch,int fromIndex);

    public abstract int indexOf(String str);

    public abstract int indexOf(XMLString str);

    public abstract int indexOf(String str,int fromIndex);

    public abstract int lastIndexOf(String str);

    public abstract int lastIndexOf(String str,int fromIndex);

    public abstract XMLString substring(int beginIndex);

    public abstract XMLString substring(int beginIndex,int endIndex);

    public abstract XMLString concat(String str);

    public abstract XMLString toLowerCase(Locale locale);

    public abstract XMLString toLowerCase();

    public abstract XMLString toUpperCase(Locale locale);

    public abstract XMLString toUpperCase();

    public abstract XMLString trim();

    public abstract boolean hasString();

    public double toDouble();
}
