/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2005 The Apache Software Foundation.
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
 * $Id: FunctionTable.java,v 1.3 2005/09/28 13:49:34 pvedula Exp $
 */
/**
 * Copyright 1999-2005 The Apache Software Foundation.
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
 * $Id: FunctionTable.java,v 1.3 2005/09/28 13:49:34 pvedula Exp $
 */
package com.sun.org.apache.xpath.internal.compiler;

import com.sun.org.apache.xpath.internal.functions.Function;

import javax.xml.transform.TransformerException;
import java.util.HashMap;

public class FunctionTable{
    public static final int FUNC_CURRENT=0;
    public static final int FUNC_LAST=1;
    public static final int FUNC_POSITION=2;
    public static final int FUNC_COUNT=3;
    public static final int FUNC_ID=4;
    public static final int FUNC_KEY=5;
    public static final int FUNC_LOCAL_PART=7;
    public static final int FUNC_NAMESPACE=8;
    public static final int FUNC_QNAME=9;
    public static final int FUNC_GENERATE_ID=10;
    public static final int FUNC_NOT=11;
    public static final int FUNC_TRUE=12;
    public static final int FUNC_FALSE=13;
    public static final int FUNC_BOOLEAN=14;
    public static final int FUNC_NUMBER=15;
    public static final int FUNC_FLOOR=16;
    public static final int FUNC_CEILING=17;
    public static final int FUNC_ROUND=18;
    public static final int FUNC_SUM=19;
    public static final int FUNC_STRING=20;
    public static final int FUNC_STARTS_WITH=21;
    public static final int FUNC_CONTAINS=22;
    public static final int FUNC_SUBSTRING_BEFORE=23;
    public static final int FUNC_SUBSTRING_AFTER=24;
    public static final int FUNC_NORMALIZE_SPACE=25;
    public static final int FUNC_TRANSLATE=26;
    public static final int FUNC_CONCAT=27;
    public static final int FUNC_SUBSTRING=29;
    public static final int FUNC_STRING_LENGTH=30;
    public static final int FUNC_SYSTEM_PROPERTY=31;
    public static final int FUNC_LANG=32;
    public static final int FUNC_EXT_FUNCTION_AVAILABLE=33;
    public static final int FUNC_EXT_ELEM_AVAILABLE=34;
    public static final int FUNC_UNPARSED_ENTITY_URI=36;
    // Proprietary
    public static final int FUNC_DOCLOCATION=35;
    private static final int NUM_BUILT_IN_FUNCS=37;
    private static final int NUM_ALLOWABLE_ADDINS=30;
    private static Class m_functions[];
    private static HashMap m_functionID=new HashMap();

    static{
        m_functions=new Class[NUM_BUILT_IN_FUNCS];
        m_functions[FUNC_CURRENT]=com.sun.org.apache.xpath.internal.functions.FuncCurrent.class;
        m_functions[FUNC_LAST]=com.sun.org.apache.xpath.internal.functions.FuncLast.class;
        m_functions[FUNC_POSITION]=com.sun.org.apache.xpath.internal.functions.FuncPosition.class;
        m_functions[FUNC_COUNT]=com.sun.org.apache.xpath.internal.functions.FuncCount.class;
        m_functions[FUNC_ID]=com.sun.org.apache.xpath.internal.functions.FuncId.class;
        // J2SE does not support Xalan interpretive
        // m_functions[FUNC_KEY] =
        //   com.sun.org.apache.xalan.internal.templates.FuncKey.class;
        m_functions[FUNC_LOCAL_PART]=
                com.sun.org.apache.xpath.internal.functions.FuncLocalPart.class;
        m_functions[FUNC_NAMESPACE]=
                com.sun.org.apache.xpath.internal.functions.FuncNamespace.class;
        m_functions[FUNC_QNAME]=com.sun.org.apache.xpath.internal.functions.FuncQname.class;
        m_functions[FUNC_GENERATE_ID]=
                com.sun.org.apache.xpath.internal.functions.FuncGenerateId.class;
        m_functions[FUNC_NOT]=com.sun.org.apache.xpath.internal.functions.FuncNot.class;
        m_functions[FUNC_TRUE]=com.sun.org.apache.xpath.internal.functions.FuncTrue.class;
        m_functions[FUNC_FALSE]=com.sun.org.apache.xpath.internal.functions.FuncFalse.class;
        m_functions[FUNC_BOOLEAN]=com.sun.org.apache.xpath.internal.functions.FuncBoolean.class;
        m_functions[FUNC_LANG]=com.sun.org.apache.xpath.internal.functions.FuncLang.class;
        m_functions[FUNC_NUMBER]=com.sun.org.apache.xpath.internal.functions.FuncNumber.class;
        m_functions[FUNC_FLOOR]=com.sun.org.apache.xpath.internal.functions.FuncFloor.class;
        m_functions[FUNC_CEILING]=com.sun.org.apache.xpath.internal.functions.FuncCeiling.class;
        m_functions[FUNC_ROUND]=com.sun.org.apache.xpath.internal.functions.FuncRound.class;
        m_functions[FUNC_SUM]=com.sun.org.apache.xpath.internal.functions.FuncSum.class;
        m_functions[FUNC_STRING]=com.sun.org.apache.xpath.internal.functions.FuncString.class;
        m_functions[FUNC_STARTS_WITH]=
                com.sun.org.apache.xpath.internal.functions.FuncStartsWith.class;
        m_functions[FUNC_CONTAINS]=com.sun.org.apache.xpath.internal.functions.FuncContains.class;
        m_functions[FUNC_SUBSTRING_BEFORE]=
                com.sun.org.apache.xpath.internal.functions.FuncSubstringBefore.class;
        m_functions[FUNC_SUBSTRING_AFTER]=
                com.sun.org.apache.xpath.internal.functions.FuncSubstringAfter.class;
        m_functions[FUNC_NORMALIZE_SPACE]=
                com.sun.org.apache.xpath.internal.functions.FuncNormalizeSpace.class;
        m_functions[FUNC_TRANSLATE]=
                com.sun.org.apache.xpath.internal.functions.FuncTranslate.class;
        m_functions[FUNC_CONCAT]=com.sun.org.apache.xpath.internal.functions.FuncConcat.class;
        m_functions[FUNC_SYSTEM_PROPERTY]=
                com.sun.org.apache.xpath.internal.functions.FuncSystemProperty.class;
        m_functions[FUNC_EXT_FUNCTION_AVAILABLE]=
                com.sun.org.apache.xpath.internal.functions.FuncExtFunctionAvailable.class;
        m_functions[FUNC_EXT_ELEM_AVAILABLE]=
                com.sun.org.apache.xpath.internal.functions.FuncExtElementAvailable.class;
        m_functions[FUNC_SUBSTRING]=
                com.sun.org.apache.xpath.internal.functions.FuncSubstring.class;
        m_functions[FUNC_STRING_LENGTH]=
                com.sun.org.apache.xpath.internal.functions.FuncStringLength.class;
        m_functions[FUNC_DOCLOCATION]=
                com.sun.org.apache.xpath.internal.functions.FuncDoclocation.class;
        m_functions[FUNC_UNPARSED_ENTITY_URI]=
                com.sun.org.apache.xpath.internal.functions.FuncUnparsedEntityURI.class;
    }

    static{
        m_functionID.put(Keywords.FUNC_CURRENT_STRING,
                new Integer(FunctionTable.FUNC_CURRENT));
        m_functionID.put(Keywords.FUNC_LAST_STRING,
                new Integer(FunctionTable.FUNC_LAST));
        m_functionID.put(Keywords.FUNC_POSITION_STRING,
                new Integer(FunctionTable.FUNC_POSITION));
        m_functionID.put(Keywords.FUNC_COUNT_STRING,
                new Integer(FunctionTable.FUNC_COUNT));
        m_functionID.put(Keywords.FUNC_ID_STRING,
                new Integer(FunctionTable.FUNC_ID));
        m_functionID.put(Keywords.FUNC_KEY_STRING,
                new Integer(FunctionTable.FUNC_KEY));
        m_functionID.put(Keywords.FUNC_LOCAL_PART_STRING,
                new Integer(FunctionTable.FUNC_LOCAL_PART));
        m_functionID.put(Keywords.FUNC_NAMESPACE_STRING,
                new Integer(FunctionTable.FUNC_NAMESPACE));
        m_functionID.put(Keywords.FUNC_NAME_STRING,
                new Integer(FunctionTable.FUNC_QNAME));
        m_functionID.put(Keywords.FUNC_GENERATE_ID_STRING,
                new Integer(FunctionTable.FUNC_GENERATE_ID));
        m_functionID.put(Keywords.FUNC_NOT_STRING,
                new Integer(FunctionTable.FUNC_NOT));
        m_functionID.put(Keywords.FUNC_TRUE_STRING,
                new Integer(FunctionTable.FUNC_TRUE));
        m_functionID.put(Keywords.FUNC_FALSE_STRING,
                new Integer(FunctionTable.FUNC_FALSE));
        m_functionID.put(Keywords.FUNC_BOOLEAN_STRING,
                new Integer(FunctionTable.FUNC_BOOLEAN));
        m_functionID.put(Keywords.FUNC_LANG_STRING,
                new Integer(FunctionTable.FUNC_LANG));
        m_functionID.put(Keywords.FUNC_NUMBER_STRING,
                new Integer(FunctionTable.FUNC_NUMBER));
        m_functionID.put(Keywords.FUNC_FLOOR_STRING,
                new Integer(FunctionTable.FUNC_FLOOR));
        m_functionID.put(Keywords.FUNC_CEILING_STRING,
                new Integer(FunctionTable.FUNC_CEILING));
        m_functionID.put(Keywords.FUNC_ROUND_STRING,
                new Integer(FunctionTable.FUNC_ROUND));
        m_functionID.put(Keywords.FUNC_SUM_STRING,
                new Integer(FunctionTable.FUNC_SUM));
        m_functionID.put(Keywords.FUNC_STRING_STRING,
                new Integer(FunctionTable.FUNC_STRING));
        m_functionID.put(Keywords.FUNC_STARTS_WITH_STRING,
                new Integer(FunctionTable.FUNC_STARTS_WITH));
        m_functionID.put(Keywords.FUNC_CONTAINS_STRING,
                new Integer(FunctionTable.FUNC_CONTAINS));
        m_functionID.put(Keywords.FUNC_SUBSTRING_BEFORE_STRING,
                new Integer(FunctionTable.FUNC_SUBSTRING_BEFORE));
        m_functionID.put(Keywords.FUNC_SUBSTRING_AFTER_STRING,
                new Integer(FunctionTable.FUNC_SUBSTRING_AFTER));
        m_functionID.put(Keywords.FUNC_NORMALIZE_SPACE_STRING,
                new Integer(FunctionTable.FUNC_NORMALIZE_SPACE));
        m_functionID.put(Keywords.FUNC_TRANSLATE_STRING,
                new Integer(FunctionTable.FUNC_TRANSLATE));
        m_functionID.put(Keywords.FUNC_CONCAT_STRING,
                new Integer(FunctionTable.FUNC_CONCAT));
        m_functionID.put(Keywords.FUNC_SYSTEM_PROPERTY_STRING,
                new Integer(FunctionTable.FUNC_SYSTEM_PROPERTY));
        m_functionID.put(Keywords.FUNC_EXT_FUNCTION_AVAILABLE_STRING,
                new Integer(FunctionTable.FUNC_EXT_FUNCTION_AVAILABLE));
        m_functionID.put(Keywords.FUNC_EXT_ELEM_AVAILABLE_STRING,
                new Integer(FunctionTable.FUNC_EXT_ELEM_AVAILABLE));
        m_functionID.put(Keywords.FUNC_SUBSTRING_STRING,
                new Integer(FunctionTable.FUNC_SUBSTRING));
        m_functionID.put(Keywords.FUNC_STRING_LENGTH_STRING,
                new Integer(FunctionTable.FUNC_STRING_LENGTH));
        m_functionID.put(Keywords.FUNC_UNPARSED_ENTITY_URI_STRING,
                new Integer(FunctionTable.FUNC_UNPARSED_ENTITY_URI));
        m_functionID.put(Keywords.FUNC_DOCLOCATION_STRING,
                new Integer(FunctionTable.FUNC_DOCLOCATION));
    }

    private Class m_functions_customer[]=new Class[NUM_ALLOWABLE_ADDINS];
    private HashMap m_functionID_customer=new HashMap();
    private int m_funcNextFreeIndex=NUM_BUILT_IN_FUNCS;

    public FunctionTable(){
    }

    String getFunctionName(int funcID){
        if(funcID<NUM_BUILT_IN_FUNCS) return m_functions[funcID].getName();
        else return m_functions_customer[funcID-NUM_BUILT_IN_FUNCS].getName();
    }

    Function getFunction(int which)
            throws TransformerException{
        try{
            if(which<NUM_BUILT_IN_FUNCS)
                return (Function)m_functions[which].newInstance();
            else
                return (Function)m_functions_customer[
                        which-NUM_BUILT_IN_FUNCS].newInstance();
        }catch(IllegalAccessException ex){
            throw new TransformerException(ex.getMessage());
        }catch(InstantiationException ex){
            throw new TransformerException(ex.getMessage());
        }
    }

    public int installFunction(String name,Class func){
        int funcIndex;
        Object funcIndexObj=getFunctionID(name);
        if(null!=funcIndexObj){
            funcIndex=((Integer)funcIndexObj).intValue();
            if(funcIndex<NUM_BUILT_IN_FUNCS){
                funcIndex=m_funcNextFreeIndex++;
                m_functionID_customer.put(name,new Integer(funcIndex));
            }
            m_functions_customer[funcIndex-NUM_BUILT_IN_FUNCS]=func;
        }else{
            funcIndex=m_funcNextFreeIndex++;
            m_functions_customer[funcIndex-NUM_BUILT_IN_FUNCS]=func;
            m_functionID_customer.put(name,
                    new Integer(funcIndex));
        }
        return funcIndex;
    }

    Object getFunctionID(String key){
        Object id=m_functionID_customer.get(key);
        if(null==id) id=m_functionID.get(key);
        return id;
    }

    public boolean functionAvailable(String methName){
        Object tblEntry=m_functionID.get(methName);
        if(null!=tblEntry) return true;
        else{
            tblEntry=m_functionID_customer.get(methName);
            return (null!=tblEntry)?true:false;
        }
    }
}
