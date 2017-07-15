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
 * $Id: MultiHashtable.java,v 1.2.4.1 2005/09/05 11:18:51 pvedula Exp $
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
 * $Id: MultiHashtable.java,v 1.2.4.1 2005/09/05 11:18:51 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MultiHashtable<K,V>{
    static final long serialVersionUID=-6151608290510033572L;
    private final Map<K,Set<V>> map=new HashMap<>();
    private boolean modifiable=true;

    public Set<V> put(K key,V value){
        if(modifiable){
            Set<V> set=map.get(key);
            if(set==null){
                set=new HashSet<>();
                map.put(key,set);
            }
            set.add(value);
            return set;
        }
        throw new UnsupportedOperationException("The MultiHashtable instance is not modifiable.");
    }

    public V maps(K key,V value){
        if(key==null) return null;
        final Set<V> set=map.get(key);
        if(set!=null){
            for(V v : set){
                if(v.equals(value)){
                    return v;
                }
            }
        }
        return null;
    }

    public void makeUnmodifiable(){
        modifiable=false;
    }
}
