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
package com.sun.org.apache.regexp.internal;

public final class CharacterArrayCharacterIterator implements CharacterIterator{
    private final char[] src;
    private final int off;
    private final int len;

    public CharacterArrayCharacterIterator(char[] src,int off,int len){
        this.src=src;
        this.off=off;
        this.len=len;
    }

    public String substring(int beginIndex,int endIndex){
        if(endIndex>len){
            throw new IndexOutOfBoundsException("endIndex="+endIndex
                    +"; sequence size="+len);
        }
        if(beginIndex<0||beginIndex>endIndex){
            throw new IndexOutOfBoundsException("beginIndex="+beginIndex
                    +"; endIndex="+endIndex);
        }
        return new String(src,off+beginIndex,endIndex-beginIndex);
    }

    public String substring(int beginIndex){
        return substring(beginIndex,len);
    }

    public char charAt(int pos){
        return src[off+pos];
    }

    public boolean isEnd(int pos){
        return (pos>=len);
    }
}
