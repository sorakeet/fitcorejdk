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
package com.sun.org.apache.xml.internal.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class IndentPrinter
        extends Printer{
    private StringBuffer _line;
    private StringBuffer _text;
    private int _spaces;
    private int _thisIndent;
    private int _nextIndent;

    public IndentPrinter(Writer writer,OutputFormat format){
        super(writer,format);
        // Initialize everything for a first/second run.
        _line=new StringBuffer(80);
        _text=new StringBuffer(20);
        _spaces=0;
        _thisIndent=_nextIndent=0;
    }

    public void enterDTD(){
        // Can only enter DTD state once. Once we're out of DTD
        // state, can no longer re-enter it.
        if(_dtdWriter==null){
            _line.append(_text);
            _text=new StringBuffer(20);
            flushLine(false);
            _dtdWriter=new StringWriter();
            _docWriter=_writer;
            _writer=_dtdWriter;
        }
    }

    public String leaveDTD(){
        // Only works if we're going out of DTD mode.
        if(_writer==_dtdWriter){
            _line.append(_text);
            _text=new StringBuffer(20);
            flushLine(false);
            _writer=_docWriter;
            return _dtdWriter.toString();
        }else
            return null;
    }

    public void printText(String text){
        _text.append(text);
    }

    public void printText(StringBuffer text){
        _text.append(text.toString());
    }

    public void printText(char[] chars,int start,int length){
        _text.append(chars,start,length);
    }

    public void printText(char ch){
        _text.append(ch);
    }

    public void printSpace(){
        // The line consists of the text accumulated in _line,
        // followed by one or more spaces as counted by _spaces,
        // followed by more space accumulated in _text:
        // -  Text is printed and accumulated into _text.
        // -  A space is printed, so _text is added to _line and
        //    a space is counted.
        // -  More text is printed and accumulated into _text.
        // -  A space is printed, the previous spaces are added
        //    to _line, the _text is added to _line, and a new
        //    space is counted.
        // If text was accumulated with printText(), then the space
        // means we have to move that text into the line and
        // start accumulating new text with printText().
        if(_text.length()>0){
            // If the text breaks a line bounary, wrap to the next line.
            // The printed line size consists of the indentation we're going
            // to use next, the accumulated line so far, some spaces and the
            // accumulated text so far.
            if(_format.getLineWidth()>0&&
                    _thisIndent+_line.length()+_spaces+_text.length()>_format.getLineWidth()){
                flushLine(false);
                try{
                    // Print line and new line, then zero the line contents.
                    _writer.write(_format.getLineSeparator());
                }catch(IOException except){
                    // We don't throw an exception, but hold it
                    // until the end of the document.
                    if(_exception==null)
                        _exception=except;
                }
            }
            // Add as many spaces as we accumulaed before.
            // At the end of this loop, _spaces is zero.
            while(_spaces>0){
                _line.append(' ');
                --_spaces;
            }
            _line.append(_text);
            _text=new StringBuffer(20);
        }
        // Starting a new word: accumulate the text between the line
        // and this new word; not a new word: just add another space.
        ++_spaces;
    }

    public void breakLine(){
        breakLine(false);
    }

    public void breakLine(boolean preserveSpace){
        // Equivalent to calling printSpace and forcing a flushLine.
        if(_text.length()>0){
            while(_spaces>0){
                _line.append(' ');
                --_spaces;
            }
            _line.append(_text);
            _text=new StringBuffer(20);
        }
        flushLine(preserveSpace);
        try{
            // Print line and new line, then zero the line contents.
            _writer.write(_format.getLineSeparator());
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
        }
    }

    public void flushLine(boolean preserveSpace){
        int indent;
        if(_line.length()>0){
            try{
                if(_format.getIndenting()&&!preserveSpace){
                    // Make sure the indentation does not blow us away.
                    indent=_thisIndent;
                    if((2*indent)>_format.getLineWidth()&&_format.getLineWidth()>0)
                        indent=_format.getLineWidth()/2;
                    // Print the indentation as spaces and set the current
                    // indentation to the next expected indentation.
                    while(indent>0){
                        _writer.write(' ');
                        --indent;
                    }
                }
                _thisIndent=_nextIndent;
                // There is no need to print the spaces at the end of the line,
                // they are simply stripped and replaced with a single line
                // separator.
                _spaces=0;
                _writer.write(_line.toString());
                _line=new StringBuffer(40);
            }catch(IOException except){
                // We don't throw an exception, but hold it
                // until the end of the document.
                if(_exception==null)
                    _exception=except;
            }
        }
    }

    public void flush(){
        if(_line.length()>0||_text.length()>0)
            breakLine();
        try{
            _writer.flush();
        }catch(IOException except){
            // We don't throw an exception, but hold it
            // until the end of the document.
            if(_exception==null)
                _exception=except;
        }
    }

    public void indent(){
        _nextIndent+=_format.getIndent();
    }

    public void unindent(){
        _nextIndent-=_format.getIndent();
        if(_nextIndent<0)
            _nextIndent=0;
        // If there is no current line and we're de-identing then
        // this indentation level is actually the next level.
        if((_line.length()+_spaces+_text.length())==0)
            _thisIndent=_nextIndent;
    }

    public int getNextIndent(){
        return _nextIndent;
    }

    public void setNextIndent(int indent){
        _nextIndent=indent;
    }

    public void setThisIndent(int indent){
        _thisIndent=indent;
    }
}
