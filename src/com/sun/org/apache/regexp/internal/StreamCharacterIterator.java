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

import java.io.IOException;
import java.io.InputStream;

public final class StreamCharacterIterator implements CharacterIterator{
    private final InputStream is;
    private final StringBuffer buff;
    private boolean closed;

    public StreamCharacterIterator(InputStream is){
        this.is=is;
        this.buff=new StringBuffer(512);
        this.closed=false;
    }

    public String substring(int beginIndex,int endIndex){
        try{
            ensure(endIndex);
            return buff.toString().substring(beginIndex,endIndex);
        }catch(IOException e){
            throw new StringIndexOutOfBoundsException(e.getMessage());
        }
    }

    public String substring(int beginIndex){
        try{
            readAll();
            return buff.toString().substring(beginIndex);
        }catch(IOException e){
            throw new StringIndexOutOfBoundsException(e.getMessage());
        }
    }

    public char charAt(int pos){
        try{
            ensure(pos);
            return buff.charAt(pos);
        }catch(IOException e){
            throw new StringIndexOutOfBoundsException(e.getMessage());
        }
    }

    public boolean isEnd(int pos){
        if(buff.length()>pos){
            return false;
        }else{
            try{
                ensure(pos);
                return (buff.length()<=pos);
            }catch(IOException e){
                throw new StringIndexOutOfBoundsException(e.getMessage());
            }
        }
    }

    private void readAll() throws IOException{
        while(!closed){
            read(1000);
        }
    }

    private void ensure(int idx) throws IOException{
        if(closed){
            return;
        }
        if(idx<buff.length()){
            return;
        }
        read(idx+1-buff.length());
    }

    private int read(int n) throws IOException{
        if(closed){
            return 0;
        }
        int c;
        int i=n;
        while(--i>=0){
            c=is.read();
            if(c<0) // EOF
            {
                closed=true;
                break;
            }
            buff.append((char)c);
        }
        return n-i;
    }
}
