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
 * $Id: Text.java,v 1.2.4.1 2005/09/12 11:33:09 pvedula Exp $
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
 * $Id: Text.java,v 1.2.4.1 2005/09/12 11:33:09 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

final class Text extends Instruction{
    private String _text;
    private boolean _escaping=true;
    private boolean _ignore=false;
    private boolean _textElement=false;

    public Text(){
        _textElement=true;
    }

    public Text(String text){
        _text=text;
    }

    protected String getText(){
        return _text;
    }

    protected void setText(String text){
        if(_text==null)
            _text=text;
        else
            _text=_text+text;
    }

    public void parseContents(Parser parser){
        final String str=getAttribute("disable-output-escaping");
        if((str!=null)&&(str.equals("yes"))) _escaping=false;
        parseChildren(parser);
        if(_text==null){
            if(_textElement){
                _text=EMPTYSTRING;
            }else{
                _ignore=true;
            }
        }else if(_textElement){
            if(_text.length()==0) _ignore=true;
        }else if(getParent() instanceof LiteralElement){
            LiteralElement element=(LiteralElement)getParent();
            String space=element.getAttribute("xml:space");
            if((space==null)||(!space.equals("preserve"))){
                int i;
                final int textLength=_text.length();
                for(i=0;i<textLength;i++){
                    char c=_text.charAt(i);
                    if(!isWhitespace(c))
                        break;
                }
                if(i==textLength)
                    _ignore=true;
            }
        }else{
            int i;
            final int textLength=_text.length();
            for(i=0;i<textLength;i++){
                char c=_text.charAt(i);
                if(!isWhitespace(c))
                    break;
            }
            if(i==textLength)
                _ignore=true;
        }
    }

    protected boolean contextDependent(){
        return false;
    }

    public void display(int indent){
        indent(indent);
        Util.println("Text");
        indent(indent+IndentIncrement);
        Util.println(_text);
    }

    private static boolean isWhitespace(char c){
        return (c==0x20||c==0x09||c==0x0A||c==0x0D);
    }

    public void ignore(){
        _ignore=true;
    }

    public boolean isIgnore(){
        return _ignore;
    }

    public boolean isTextElement(){
        return _textElement;
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        if(!_ignore){
            // Turn off character escaping if so is wanted.
            final int esc=cpg.addInterfaceMethodref(OUTPUT_HANDLER,
                    "setEscaping","(Z)Z");
            if(!_escaping){
                il.append(methodGen.loadHandler());
                il.append(new PUSH(cpg,false));
                il.append(new INVOKEINTERFACE(esc,2));
            }
            il.append(methodGen.loadHandler());
            // Call characters(String) or characters(char[],int,int), as
            // appropriate.
            if(!canLoadAsArrayOffsetLength()){
                final int characters=cpg.addInterfaceMethodref(OUTPUT_HANDLER,
                        "characters",
                        "("+STRING_SIG+")V");
                il.append(new PUSH(cpg,_text));
                il.append(new INVOKEINTERFACE(characters,2));
            }else{
                final int characters=cpg.addInterfaceMethodref(OUTPUT_HANDLER,
                        "characters",
                        "([CII)V");
                loadAsArrayOffsetLength(classGen,methodGen);
                il.append(new INVOKEINTERFACE(characters,4));
            }
            // Restore character escaping setting to whatever it was.
            // Note: setEscaping(bool) returns the original (old) value
            if(!_escaping){
                il.append(methodGen.loadHandler());
                il.append(SWAP);
                il.append(new INVOKEINTERFACE(esc,2));
                il.append(POP);
            }
        }
        translateContents(classGen,methodGen);
    }

    public boolean canLoadAsArrayOffsetLength(){
        // Magic number!  21845*3 == 65535.  BCEL uses a DataOutputStream to
        // serialize class files.  The Java run-time places a limit on the size
        // of String data written using a DataOutputStream - it cannot require
        // more than 64KB when represented as UTF-8.  The number of bytes
        // required to represent a Java string as UTF-8 cannot be greater
        // than three times the number of char's in the string, hence the
        // check for 21845.
        return (_text.length()<=21845);
    }

    public void loadAsArrayOffsetLength(ClassGenerator classGen,
                                        MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        final XSLTC xsltc=classGen.getParser().getXSLTC();
        // The XSLTC object keeps track of character data
        // that is to be stored in char arrays.
        final int offset=xsltc.addCharacterData(_text);
        final int length=_text.length();
        String charDataFieldName=
                STATIC_CHAR_DATA_FIELD+(xsltc.getCharacterDataCount()-1);
        il.append(new GETSTATIC(cpg.addFieldref(xsltc.getClassName(),
                charDataFieldName,
                STATIC_CHAR_DATA_FIELD_SIG)));
        il.append(new PUSH(cpg,offset));
        il.append(new PUSH(cpg,_text.length()));
    }
}
