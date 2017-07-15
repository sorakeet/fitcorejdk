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
 * $Id: WriterToASCI.java,v 1.2.4.1 2005/09/15 08:15:31 suresh_emailid Exp $
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
 * $Id: WriterToASCI.java,v 1.2.4.1 2005/09/15 08:15:31 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

class WriterToASCI extends Writer implements WriterChain{
    private final OutputStream m_os;

    public WriterToASCI(OutputStream os){
        m_os=os;
    }

    public void write(int c) throws IOException{
        m_os.write(c);
    }

    public void write(char chars[],int start,int length)
            throws IOException{
        int n=length+start;
        for(int i=start;i<n;i++){
            m_os.write(chars[i]);
        }
    }

    public void write(String s) throws IOException{
        int n=s.length();
        for(int i=0;i<n;i++){
            m_os.write(s.charAt(i));
        }
    }

    public void flush() throws IOException{
        m_os.flush();
    }

    public void close() throws IOException{
        m_os.close();
    }

    public Writer getWriter(){
        return null;
    }

    public OutputStream getOutputStream(){
        return m_os;
    }
}
