/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Sep 14, 2000:
//  Fixed serializer to report IO exception directly, instead at
//  the end of document processing.
//  Reported by Patrick Higgins <phiggins@transzap.com>
package com.sun.org.apache.xml.internal.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class Printer{
    private static final int BufferSize=4096;
    protected final OutputFormat _format;
    private final char[] _buffer=new char[BufferSize];
    protected Writer _writer;
    protected StringWriter _dtdWriter;
    protected Writer _docWriter;
    protected IOException _exception;
    private int _pos=0;

    public Printer(Writer writer,OutputFormat format){
        _writer=writer;
        _format=format;
        _exception=null;
        _dtdWriter=null;
        _docWriter=null;
        _pos=0;
    }

    public IOException getException(){
        return _exception;
    }

    public void enterDTD()
            throws IOException{
        // Can only enter DTD state once. Once we're out of DTD
        // state, can no longer re-enter it.
        if(_dtdWriter==null){
            flushLine(false);
            _dtdWriter=new StringWriter();
            _docWriter=_writer;
            _writer=_dtdWriter;
        }
    }

    public void flushLine(boolean preserveSpace)
            throws IOException{
        // Write anything left in the buffer into the writer.
        try{
            _writer.write(_buffer,0,_pos);
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
        }
        _pos=0;
    }

    public String leaveDTD()
            throws IOException{
        // Only works if we're going out of DTD mode.
        if(_writer==_dtdWriter){
            flushLine(false);
            _writer=_docWriter;
            return _dtdWriter.toString();
        }else
            return null;
    }

    public void printText(String text)
            throws IOException{
        try{
            int length=text.length();
            for(int i=0;i<length;++i){
                if(_pos==BufferSize){
                    _writer.write(_buffer);
                    _pos=0;
                }
                _buffer[_pos]=text.charAt(i);
                ++_pos;
            }
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
            throw except;
        }
    }

    public void printText(StringBuffer text)
            throws IOException{
        try{
            int length=text.length();
            for(int i=0;i<length;++i){
                if(_pos==BufferSize){
                    _writer.write(_buffer);
                    _pos=0;
                }
                _buffer[_pos]=text.charAt(i);
                ++_pos;
            }
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
            throw except;
        }
    }

    public void printText(char[] chars,int start,int length)
            throws IOException{
        try{
            while(length-->0){
                if(_pos==BufferSize){
                    _writer.write(_buffer);
                    _pos=0;
                }
                _buffer[_pos]=chars[start];
                ++start;
                ++_pos;
            }
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
            throw except;
        }
    }

    public void printText(char ch)
            throws IOException{
        try{
            if(_pos==BufferSize){
                _writer.write(_buffer);
                _pos=0;
            }
            _buffer[_pos]=ch;
            ++_pos;
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
            throw except;
        }
    }

    public void printSpace()
            throws IOException{
        try{
            if(_pos==BufferSize){
                _writer.write(_buffer);
                _pos=0;
            }
            _buffer[_pos]=' ';
            ++_pos;
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
            throw except;
        }
    }

    public void breakLine(boolean preserveSpace)
            throws IOException{
        breakLine();
    }

    public void breakLine()
            throws IOException{
        try{
            if(_pos==BufferSize){
                _writer.write(_buffer);
                _pos=0;
            }
            _buffer[_pos]='\n';
            ++_pos;
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
            throw except;
        }
    }

    public void flush()
            throws IOException{
        try{
            _writer.write(_buffer,0,_pos);
            _writer.flush();
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
            throw except;
        }
        _pos=0;
    }

    public void indent(){
        // NOOP
    }

    public void unindent(){
        // NOOP
    }

    public int getNextIndent(){
        return 0;
    }

    public void setNextIndent(int indent){
    }

    public void setThisIndent(int indent){
    }
}
