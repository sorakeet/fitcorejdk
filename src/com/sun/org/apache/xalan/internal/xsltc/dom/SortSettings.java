/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: SortSettings.java,v 1.2.4.1 2005/09/06 10:19:22 pvedula Exp $
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: SortSettings.java,v 1.2.4.1 2005/09/06 10:19:22 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;

import java.text.Collator;
import java.util.Locale;

final class SortSettings{
    private AbstractTranslet _translet;
    private int[] _sortOrders;
    private int[] _types;
    private Locale[] _locales;
    private Collator[] _collators;
    private String[] _caseOrders;

    SortSettings(AbstractTranslet translet,int[] sortOrders,int[] types,
                 Locale[] locales,Collator[] collators,String[] caseOrders){
        _translet=translet;
        _sortOrders=sortOrders;
        _types=types;
        _locales=locales;
        _collators=collators;
        _caseOrders=caseOrders;
    }

    AbstractTranslet getTranslet(){
        return _translet;
    }

    int[] getSortOrders(){
        return _sortOrders;
    }

    int[] getTypes(){
        return _types;
    }

    Locale[] getLocales(){
        return _locales;
    }

    Collator[] getCollators(){
        return _collators;
    }

    String[] getCaseOrders(){
        return _caseOrders;
    }
}
