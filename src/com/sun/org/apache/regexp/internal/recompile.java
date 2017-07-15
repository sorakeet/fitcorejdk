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

public class recompile{
    static public void main(String[] arg){
        // Create a compiler object
        RECompiler r=new RECompiler();
        // Print usage if arguments are incorrect
        if(arg.length<=0||arg.length%2!=0){
            System.out.println("Usage: recompile <patternname> <pattern>");
            System.exit(0);
        }
        // Loop through arguments, compiling each
        for(int i=0;i<arg.length;i+=2){
            try{
                // Compile regular expression
                String name=arg[i];
                String pattern=arg[i+1];
                String instructions=name+"PatternInstructions";
                // Output program as a nice, formatted character array
                System.out.print("\n    // Pre-compiled regular expression '"+pattern+"'\n"
                        +"    private static char[] "+instructions+" = \n    {");
                // Compile program for pattern
                REProgram program=r.compile(pattern);
                // Number of columns in output
                int numColumns=7;
                // Loop through program
                char[] p=program.getInstructions();
                for(int j=0;j<p.length;j++){
                    // End of column?
                    if((j%numColumns)==0){
                        System.out.print("\n        ");
                    }
                    // Print character as padded hex number
                    String hex=Integer.toHexString(p[j]);
                    while(hex.length()<4){
                        hex="0"+hex;
                    }
                    System.out.print("0x"+hex+", ");
                }
                // End of program block
                System.out.println("\n    };");
                System.out.println("\n    private static RE "+name+"Pattern = new RE(new REProgram("+instructions+"));");
            }catch(RESyntaxException e){
                System.out.println("Syntax error in expression \""+arg[i]+"\": "+e.toString());
            }catch(Exception e){
                System.out.println("Unexpected exception: "+e.toString());
            }catch(Error e){
                System.out.println("Internal error: "+e.toString());
            }
        }
    }
}
