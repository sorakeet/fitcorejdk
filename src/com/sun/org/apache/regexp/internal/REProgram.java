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

import java.io.Serializable;

public class REProgram implements Serializable{
    static final int OPT_HASBACKREFS=1;
    char[] instruction;         // The compiled regular expression 'program'
    int lenInstruction;         // The amount of the instruction buffer in use
    char[] prefix;              // Prefix string optimization
    int flags;                  // Optimization flags (REProgram.OPT_*)
    int maxParens=-1;

    public REProgram(char[] instruction){
        this(instruction,instruction.length);
    }

    public REProgram(char[] instruction,int lenInstruction){
        setInstructions(instruction,lenInstruction);
    }

    public void setInstructions(char[] instruction,int lenInstruction){
        // Save reference to instruction array
        this.instruction=instruction;
        this.lenInstruction=lenInstruction;
        // Initialize other program-related variables
        flags=0;
        prefix=null;
        // Try various compile-time optimizations if there's a program
        if(instruction!=null&&lenInstruction!=0){
            // If the first node is a branch
            if(lenInstruction>=RE.nodeSize&&instruction[0+RE.offsetOpcode]==RE.OP_BRANCH){
                // to the end node
                int next=instruction[0+RE.offsetNext];
                if(instruction[next+RE.offsetOpcode]==RE.OP_END){
                    // and the branch starts with an atom
                    if(lenInstruction>=(RE.nodeSize*2)&&instruction[RE.nodeSize+RE.offsetOpcode]==RE.OP_ATOM){
                        // then get that atom as an prefix because there's no other choice
                        int lenAtom=instruction[RE.nodeSize+RE.offsetOpdata];
                        prefix=new char[lenAtom];
                        System.arraycopy(instruction,RE.nodeSize*2,prefix,0,lenAtom);
                    }
                }
            }
            BackrefScanLoop:
            // Check for backreferences
            for(int i=0;i<lenInstruction;i+=RE.nodeSize){
                switch(instruction[i+RE.offsetOpcode]){
                    case RE.OP_ANYOF:
                        i+=(instruction[i+RE.offsetOpdata]*2);
                        break;
                    case RE.OP_ATOM:
                        i+=instruction[i+RE.offsetOpdata];
                        break;
                    case RE.OP_BACKREF:
                        flags|=OPT_HASBACKREFS;
                        break BackrefScanLoop;
                }
            }
        }
    }

    public REProgram(int parens,char[] instruction){
        this(instruction,instruction.length);
        this.maxParens=parens;
    }

    public char[] getInstructions(){
        // Ensure program has been compiled!
        if(lenInstruction!=0){
            // Return copy of program
            char[] ret=new char[lenInstruction];
            System.arraycopy(instruction,0,ret,0,lenInstruction);
            return ret;
        }
        return null;
    }
}
