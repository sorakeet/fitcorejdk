/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: CustomStringPool.java,v 1.2.4.1 2005/09/15 08:14:59 suresh_emailid Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: CustomStringPool.java,v 1.2.4.1 2005/09/15 08:14:59 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import java.util.HashMap;
import java.util.Map;

public class CustomStringPool extends DTMStringPool{
    public static final int NULL=-1;
    final Map<String,Integer> m_stringToInt=new HashMap<>();

    public CustomStringPool(){
        super();
    }

    public void removeAllElements(){
        m_intToString.removeAllElements();
        if(m_stringToInt!=null){
            m_stringToInt.clear();
        }
    }

    @Override
    public String indexToString(int i)
            throws ArrayIndexOutOfBoundsException{
        return (String)m_intToString.elementAt(i);
    }

    @Override
    public int stringToIndex(String s){
        if(s==null){
            return NULL;
        }
        Integer iobj=m_stringToInt.get(s);
        if(iobj==null){
            m_intToString.addElement(s);
            iobj=m_intToString.size();
            m_stringToInt.put(s,iobj);
        }
        return iobj;
    }
}
