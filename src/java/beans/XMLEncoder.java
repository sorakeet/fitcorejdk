/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class XMLEncoder extends Encoder implements AutoCloseable{
    private final CharsetEncoder encoder;
    private final String charset;
    private final boolean declaration;
    private OutputStreamWriter out;
    private Object owner;
    private int indentation=0;
    private boolean internal=false;
    private Map<Object,ValueData> valueToExpression;
    private Map<Object,List<Statement>> targetToStatementList;
    private boolean preambleWritten=false;
    private NameGenerator nameGenerator;

    public XMLEncoder(OutputStream out){
        this(out,"UTF-8",true,0);
    }

    public XMLEncoder(OutputStream out,String charset,boolean declaration,int indentation){
        if(out==null){
            throw new IllegalArgumentException("the output stream cannot be null");
        }
        if(indentation<0){
            throw new IllegalArgumentException("the indentation must be >= 0");
        }
        Charset cs=Charset.forName(charset);
        this.encoder=cs.newEncoder();
        this.charset=charset;
        this.declaration=declaration;
        this.indentation=indentation;
        this.out=new OutputStreamWriter(out,cs.newEncoder());
        valueToExpression=new IdentityHashMap<>();
        targetToStatementList=new IdentityHashMap<>();
        nameGenerator=new NameGenerator();
    }

    private static boolean isValidCharCode(int code){
        return (0x0020<=code&&code<=0xD7FF)
                ||(0x000A==code)
                ||(0x0009==code)
                ||(0x000D==code)
                ||(0xE000<=code&&code<=0xFFFD)
                ||(0x10000<=code&&code<=0x10ffff);
    }

    private static String quoteCharCode(int code){
        switch(code){
            case '&':
                return "&amp;";
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '"':
                return "&quot;";
            case '\'':
                return "&apos;";
            case '\r':
                return "&#13;";
            default:
                return null;
        }
    }

    private static String createString(int code){
        return "<char code=\"#"+Integer.toString(code,16)+"\"/>";
    }

    @SuppressWarnings("rawtypes")
    static Class primitiveTypeFor(Class wrapper){
        if(wrapper==Boolean.class) return Boolean.TYPE;
        if(wrapper==Byte.class) return Byte.TYPE;
        if(wrapper==Character.class) return Character.TYPE;
        if(wrapper==Short.class) return Short.TYPE;
        if(wrapper==Integer.class) return Integer.TYPE;
        if(wrapper==Long.class) return Long.TYPE;
        if(wrapper==Float.class) return Float.TYPE;
        if(wrapper==Double.class) return Double.TYPE;
        if(wrapper==Void.class) return Void.TYPE;
        return null;
    }

    public Object getOwner(){
        return owner;
    }

    public void setOwner(Object owner){
        this.owner=owner;
        writeExpression(new Expression(this,"getOwner",new Object[0]));
    }

    public void writeObject(Object o){
        if(internal){
            super.writeObject(o);
        }else{
            writeStatement(new Statement(this,"writeObject",new Object[]{o}));
        }
    }

    public void writeStatement(Statement oldStm){
        // System.out.println("XMLEncoder::writeStatement: " + oldStm);
        boolean internal=this.internal;
        this.internal=true;
        try{
            super.writeStatement(oldStm);
            /**
             Note we must do the mark first as we may
             require the results of previous values in
             this context for this statement.
             Test case is:
             os.setOwner(this);
             os.writeObject(this);
             */
            mark(oldStm);
            Object target=oldStm.getTarget();
            if(target instanceof Field){
                String method=oldStm.getMethodName();
                Object[] args=oldStm.getArguments();
                if((method==null)||(args==null)){
                }else if(method.equals("get")&&(args.length==1)){
                    target=args[0];
                }else if(method.equals("set")&&(args.length==2)){
                    target=args[0];
                }
            }
            statementList(target).add(oldStm);
        }catch(Exception e){
            getExceptionListener().exceptionThrown(new Exception("XMLEncoder: discarding statement "+oldStm,e));
        }
        this.internal=internal;
    }

    public void writeExpression(Expression oldExp){
        boolean internal=this.internal;
        this.internal=true;
        Object oldValue=getValue(oldExp);
        if(get(oldValue)==null||(oldValue instanceof String&&!internal)){
            getValueData(oldValue).exp=oldExp;
            super.writeExpression(oldExp);
        }
        this.internal=internal;
    }

    void clear(){
        super.clear();
        nameGenerator.clear();
        valueToExpression.clear();
        targetToStatementList.clear();
    }

    private ValueData getValueData(Object o){
        ValueData d=valueToExpression.get(o);
        if(d==null){
            d=new ValueData();
            valueToExpression.put(o,d);
        }
        return d;
    }

    private List<Statement> statementList(Object target){
        List<Statement> list=targetToStatementList.get(target);
        if(list==null){
            list=new ArrayList<>();
            targetToStatementList.put(target,list);
        }
        return list;
    }

    private void mark(Object o,boolean isArgument){
        if(o==null||o==this){
            return;
        }
        ValueData d=getValueData(o);
        Expression exp=d.exp;
        // Do not mark liternal strings. Other strings, which might,
        // for example, come from resource bundles should still be marked.
        if(o.getClass()==String.class&&exp==null){
            return;
        }
        // Bump the reference counts of all arguments
        if(isArgument){
            d.refs++;
        }
        if(d.marked){
            return;
        }
        d.marked=true;
        Object target=exp.getTarget();
        mark(exp);
        if(!(target instanceof Class)){
            statementList(target).add(exp);
            // Pending: Why does the reference count need to
            // be incremented here?
            d.refs++;
        }
    }

    private void mark(Statement stm){
        Object[] args=stm.getArguments();
        for(int i=0;i<args.length;i++){
            Object arg=args[i];
            mark(arg,true);
        }
        mark(stm.getTarget(),stm instanceof Expression);
    }

    public void flush(){
        if(!preambleWritten){ // Don't do this in constructor - it throws ... pending.
            if(this.declaration){
                writeln("<?xml version="+quote("1.0")+
                        " encoding="+quote(this.charset)+"?>");
            }
            writeln("<java version="+quote(System.getProperty("java.version"))+
                    " class="+quote(XMLDecoder.class.getName())+">");
            preambleWritten=true;
        }
        indentation++;
        List<Statement> statements=statementList(this);
        while(!statements.isEmpty()){
            Statement s=statements.remove(0);
            if("writeObject".equals(s.getMethodName())){
                outputValue(s.getArguments()[0],this,true);
            }else{
                outputStatement(s,this,false);
            }
        }
        indentation--;
        Statement statement=getMissedStatement();
        while(statement!=null){
            outputStatement(statement,this,false);
            statement=getMissedStatement();
        }
        try{
            out.flush();
        }catch(IOException e){
            getExceptionListener().exceptionThrown(e);
        }
        clear();
    }

    Statement getMissedStatement(){
        for(List<Statement> statements : this.targetToStatementList.values()){
            for(int i=0;i<statements.size();i++){
                if(Statement.class==statements.get(i).getClass()){
                    return statements.remove(i);
                }
            }
        }
        return null;
    }

    public void close(){
        flush();
        writeln("</java>");
        try{
            out.close();
        }catch(IOException e){
            getExceptionListener().exceptionThrown(e);
        }
    }

    private String quote(String s){
        return "\""+s+"\"";
    }

    private void writeln(String exp){
        try{
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<indentation;i++){
                sb.append(' ');
            }
            sb.append(exp);
            sb.append('\n');
            this.out.write(sb.toString());
        }catch(IOException e){
            getExceptionListener().exceptionThrown(e);
        }
    }

    private void outputValue(Object value,Object outer,boolean isArgument){
        if(value==null){
            writeln("<null/>");
            return;
        }
        if(value instanceof Class){
            writeln("<class>"+((Class)value).getName()+"</class>");
            return;
        }
        ValueData d=getValueData(value);
        if(d.exp!=null){
            Object target=d.exp.getTarget();
            String methodName=d.exp.getMethodName();
            if(target==null||methodName==null){
                throw new NullPointerException((target==null?"target":
                        "methodName")+" should not be null");
            }
            if(isArgument&&target instanceof Field&&methodName.equals("get")){
                Field f=(Field)target;
                writeln("<object class="+quote(f.getDeclaringClass().getName())+
                        " field="+quote(f.getName())+"/>");
                return;
            }
            Class<?> primitiveType=primitiveTypeFor(value.getClass());
            if(primitiveType!=null&&target==value.getClass()&&
                    methodName.equals("new")){
                String primitiveTypeName=primitiveType.getName();
                // Make sure that character types are quoted correctly.
                if(primitiveType==Character.TYPE){
                    char code=((Character)value).charValue();
                    if(!isValidCharCode(code)){
                        writeln(createString(code));
                        return;
                    }
                    value=quoteCharCode(code);
                    if(value==null){
                        value=Character.valueOf(code);
                    }
                }
                writeln("<"+primitiveTypeName+">"+value+"</"+
                        primitiveTypeName+">");
                return;
            }
        }else if(value instanceof String){
            writeln(createString((String)value));
            return;
        }
        if(d.name!=null){
            if(isArgument){
                writeln("<object idref="+quote(d.name)+"/>");
            }else{
                outputXML("void"," idref="+quote(d.name),value);
            }
        }else if(d.exp!=null){
            outputStatement(d.exp,outer,isArgument);
        }
    }

    private String createString(String string){
        StringBuilder sb=new StringBuilder();
        sb.append("<string>");
        int index=0;
        while(index<string.length()){
            int point=string.codePointAt(index);
            int count=Character.charCount(point);
            if(isValidCharCode(point)&&this.encoder.canEncode(string.substring(index,index+count))){
                String value=quoteCharCode(point);
                if(value!=null){
                    sb.append(value);
                }else{
                    sb.appendCodePoint(point);
                }
                index+=count;
            }else{
                sb.append(createString(string.charAt(index)));
                index++;
            }
        }
        sb.append("</string>");
        return sb.toString();
    }

    private void outputStatement(Statement exp,Object outer,boolean isArgument){
        Object target=exp.getTarget();
        String methodName=exp.getMethodName();
        if(target==null||methodName==null){
            throw new NullPointerException((target==null?"target":
                    "methodName")+" should not be null");
        }
        Object[] args=exp.getArguments();
        boolean expression=exp.getClass()==Expression.class;
        Object value=(expression)?getValue((Expression)exp):null;
        String tag=(expression&&isArgument)?"object":"void";
        String attributes="";
        ValueData d=getValueData(value);
        // Special cases for targets.
        if(target==outer){
        }else if(target==Array.class&&methodName.equals("newInstance")){
            tag="array";
            attributes=attributes+" class="+quote(((Class)args[0]).getName());
            attributes=attributes+" length="+quote(args[1].toString());
            args=new Object[]{};
        }else if(target.getClass()==Class.class){
            attributes=attributes+" class="+quote(((Class)target).getName());
        }else{
            d.refs=2;
            if(d.name==null){
                getValueData(target).refs++;
                List<Statement> statements=statementList(target);
                if(!statements.contains(exp)){
                    statements.add(exp);
                }
                outputValue(target,outer,false);
            }
            if(expression){
                outputValue(value,outer,isArgument);
            }
            return;
        }
        if(expression&&(d.refs>1)){
            String instanceName=nameGenerator.instanceName(value);
            d.name=instanceName;
            attributes=attributes+" id="+quote(instanceName);
        }
        // Special cases for methods.
        if((!expression&&methodName.equals("set")&&args.length==2&&
                args[0] instanceof Integer)||
                (expression&&methodName.equals("get")&&args.length==1&&
                        args[0] instanceof Integer)){
            attributes=attributes+" index="+quote(args[0].toString());
            args=(args.length==1)?new Object[]{}:new Object[]{args[1]};
        }else if((!expression&&methodName.startsWith("set")&&args.length==1)||
                (expression&&methodName.startsWith("get")&&args.length==0)){
            if(3<methodName.length()){
                attributes=attributes+" property="+
                        quote(Introspector.decapitalize(methodName.substring(3)));
            }
        }else if(!methodName.equals("new")&&!methodName.equals("newInstance")){
            attributes=attributes+" method="+quote(methodName);
        }
        outputXML(tag,attributes,value,args);
    }

    private void outputXML(String tag,String attributes,Object value,Object... args){
        List<Statement> statements=statementList(value);
        // Use XML's short form when there is no body.
        if(args.length==0&&statements.size()==0){
            writeln("<"+tag+attributes+"/>");
            return;
        }
        writeln("<"+tag+attributes+">");
        indentation++;
        for(int i=0;i<args.length;i++){
            outputValue(args[i],null,true);
        }
        while(!statements.isEmpty()){
            Statement s=statements.remove(0);
            outputStatement(s,value,false);
        }
        indentation--;
        writeln("</"+tag+">");
    }

    private class ValueData{
        public int refs=0;
        public boolean marked=false; // Marked -> refs > 0 unless ref was a target.
        public String name=null;
        public Expression exp=null;
    }
}
