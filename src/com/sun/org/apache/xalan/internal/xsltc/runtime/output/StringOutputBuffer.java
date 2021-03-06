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
 * $Id: StringOutputBuffer.java,v 1.2.4.1 2005/09/06 11:36:16 pvedula Exp $
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
 * $Id: StringOutputBuffer.java,v 1.2.4.1 2005/09/06 11:36:16 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.runtime.output;

class StringOutputBuffer implements OutputBuffer{
    private StringBuffer _buffer;

    public StringOutputBuffer(){
        _buffer=new StringBuffer();
    }

    public String close(){
        return _buffer.toString();
    }

    public OutputBuffer append(char ch){
        _buffer.append(ch);
        return this;
    }

    public OutputBuffer append(String s){
        _buffer.append(s);
        return this;
    }

    public OutputBuffer append(char[] s,int from,int to){
        _buffer.append(s,from,to);
        return this;
    }
}
