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
 * $Id: Axis.java,v 1.2.4.1 2005/09/15 08:14:51 suresh_emailid Exp $
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
 * $Id: Axis.java,v 1.2.4.1 2005/09/15 08:14:51 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm;

public final class Axis{
    public static final int ANCESTOR=0;
    public static final int ANCESTORORSELF=1;
    public static final int ATTRIBUTE=2;
    public static final int CHILD=3;
    public static final int DESCENDANT=4;
    public static final int DESCENDANTORSELF=5;
    public static final int FOLLOWING=6;
    public static final int FOLLOWINGSIBLING=7;
    public static final int NAMESPACEDECLS=8;
    public static final int NAMESPACE=9;
    public static final int PARENT=10;
    public static final int PRECEDING=11;
    public static final int PRECEDINGSIBLING=12;
    public static final int SELF=13;
    public static final int ALLFROMNODE=14;
    public static final int PRECEDINGANDANCESTOR=15;
    // ===========================================
    // All axis past this are absolute.
    public static final int ALL=16;
    public static final int DESCENDANTSFROMROOT=17;
    public static final int DESCENDANTSORSELFFROMROOT=18;
    public static final int ROOT=19;
    public static final int FILTEREDLIST=20;
    private static final boolean[] isReverse={
            true,  // ancestor
            true,  // ancestor-or-self
            false, // attribute
            false, // child
            false, // descendant
            false, // descendant-or-self
            false, // following
            false, // following-sibling
            false, // namespace
            false, // namespace-declarations
            false, // parent (one node, has no order)
            true,  // preceding
            true,  // preceding-sibling
            false  // self (one node, has no order)
    };
    private static final String[] names=
            {
                    "ancestor",  // 0
                    "ancestor-or-self",  // 1
                    "attribute",  // 2
                    "child",  // 3
                    "descendant",  // 4
                    "descendant-or-self",  // 5
                    "following",  // 6
                    "following-sibling",  // 7
                    "namespace-decls",  // 8
                    "namespace",  // 9
                    "parent",  // 10
                    "preceding",  // 11
                    "preceding-sibling",  // 12
                    "self",  // 13
                    "all-from-node",  // 14
                    "preceding-and-ancestor",  // 15
                    "all",  // 16
                    "descendants-from-root",  // 17
                    "descendants-or-self-from-root",  // 18
                    "root",  // 19
                    "filtered-list"  // 20
            };

    public static boolean isReverse(int axis){
        return isReverse[axis];
    }

    public static String getNames(int index){
        return names[index];
    }

    public static int getNamesLength(){
        return names.length;
    }
}
