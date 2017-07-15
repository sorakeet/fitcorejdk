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
 * $Id: DTMIterator.java,v 1.2.4.1 2005/09/15 08:14:54 suresh_emailid Exp $
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
 * $Id: DTMIterator.java,v 1.2.4.1 2005/09/15 08:14:54 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm;

public interface DTMIterator{
    // Constants returned by acceptNode, borrowed from the DOM Traversal chapter
    // %REVIEW% Should we explicitly initialize them from, eg,
    // org.w3c.dom.traversal.NodeFilter.FILTER_ACCEPT?
    public static final short FILTER_ACCEPT=1;
    public static final short FILTER_REJECT=2;
    public static final short FILTER_SKIP=3;

    public DTM getDTM(int nodeHandle);

    public DTMManager getDTMManager();

    public int getRoot();

    public void setRoot(int nodeHandle,Object environment);

    public void reset();

    public int getWhatToShow();

    public boolean getExpandEntityReferences();

    public int nextNode();

    public int previousNode();

    public void detach();

    public void allowDetachToRelease(boolean allowRelease);

    public int getCurrentNode();

    public boolean isFresh();
    //========= Random Access ==========

    public void setShouldCacheNodes(boolean b);

    public boolean isMutable();

    public int getCurrentPos();

    public void setCurrentPos(int i);

    public void runTo(int index);

    public int item(int index);

    public void setItem(int node,int index);

    public int getLength();
    //=========== Cloning operations. ============

    public DTMIterator cloneWithReset() throws CloneNotSupportedException;

    public Object clone() throws CloneNotSupportedException;

    public boolean isDocOrdered();

    public int getAxis();
}
