/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.reflect;

import sun.reflect.annotation.AnnotationParser;
import sun.reflect.annotation.AnnotationSupport;
import sun.reflect.annotation.TypeAnnotation;
import sun.reflect.annotation.TypeAnnotationParser;
import sun.reflect.generics.repository.ConstructorRepository;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;

public abstract class Executable extends AccessibleObject
        implements Member, GenericDeclaration{
    private transient volatile boolean hasRealParameterData;
    private transient volatile Parameter[] parameters;
    private transient Map<Class<? extends Annotation>,Annotation> declaredAnnotations;

    Executable(){
    }

    boolean equalParamTypes(Class<?>[] params1,Class<?>[] params2){
        /** Avoid unnecessary cloning */
        if(params1.length==params2.length){
            for(int i=0;i<params1.length;i++){
                if(params1[i]!=params2[i])
                    return false;
            }
            return true;
        }
        return false;
    }

    String sharedToString(int modifierMask,
                          boolean isDefault,
                          Class<?>[] parameterTypes,
                          Class<?>[] exceptionTypes){
        try{
            StringBuilder sb=new StringBuilder();
            printModifiersIfNonzero(sb,modifierMask,isDefault);
            specificToStringHeader(sb);
            sb.append('(');
            separateWithCommas(parameterTypes,sb);
            sb.append(')');
            if(exceptionTypes.length>0){
                sb.append(" throws ");
                separateWithCommas(exceptionTypes,sb);
            }
            return sb.toString();
        }catch(Exception e){
            return "<"+e+">";
        }
    }

    void separateWithCommas(Class<?>[] types,StringBuilder sb){
        for(int j=0;j<types.length;j++){
            sb.append(types[j].getTypeName());
            if(j<(types.length-1))
                sb.append(",");
        }
    }

    void printModifiersIfNonzero(StringBuilder sb,int mask,boolean isDefault){
        int mod=getModifiers()&mask;
        if(mod!=0&&!isDefault){
            sb.append(Modifier.toString(mod)).append(' ');
        }else{
            int access_mod=mod&Modifier.ACCESS_MODIFIERS;
            if(access_mod!=0)
                sb.append(Modifier.toString(access_mod)).append(' ');
            if(isDefault)
                sb.append("default ");
            mod=(mod&~Modifier.ACCESS_MODIFIERS);
            if(mod!=0)
                sb.append(Modifier.toString(mod)).append(' ');
        }
    }

    abstract void specificToStringHeader(StringBuilder sb);

    String sharedToGenericString(int modifierMask,boolean isDefault){
        try{
            StringBuilder sb=new StringBuilder();
            printModifiersIfNonzero(sb,modifierMask,isDefault);
            TypeVariable<?>[] typeparms=getTypeParameters();
            if(typeparms.length>0){
                boolean first=true;
                sb.append('<');
                for(TypeVariable<?> typeparm : typeparms){
                    if(!first)
                        sb.append(',');
                    // Class objects can't occur here; no need to test
                    // and call Class.getName().
                    sb.append(typeparm.toString());
                    first=false;
                }
                sb.append("> ");
            }
            specificToGenericStringHeader(sb);
            sb.append('(');
            Type[] params=getGenericParameterTypes();
            for(int j=0;j<params.length;j++){
                String param=params[j].getTypeName();
                if(isVarArgs()&&(j==params.length-1)) // replace T[] with T...
                    param=param.replaceFirst("\\[\\]$","...");
                sb.append(param);
                if(j<(params.length-1))
                    sb.append(',');
            }
            sb.append(')');
            Type[] exceptions=getGenericExceptionTypes();
            if(exceptions.length>0){
                sb.append(" throws ");
                for(int k=0;k<exceptions.length;k++){
                    sb.append((exceptions[k] instanceof Class)?
                            ((Class)exceptions[k]).getName():
                            exceptions[k].toString());
                    if(k<(exceptions.length-1))
                        sb.append(',');
                }
            }
            return sb.toString();
        }catch(Exception e){
            return "<"+e+">";
        }
    }

    abstract void specificToGenericStringHeader(StringBuilder sb);

    public abstract TypeVariable<?>[] getTypeParameters();

    public Type[] getGenericParameterTypes(){
        if(hasGenericInformation())
            return getGenericInfo().getParameterTypes();
        else
            return getParameterTypes();
    }

    abstract boolean hasGenericInformation();

    abstract ConstructorRepository getGenericInfo();

    public abstract Class<?>[] getParameterTypes();

    public Type[] getGenericExceptionTypes(){
        Type[] result;
        if(hasGenericInformation()&&
                ((result=getGenericInfo().getExceptionTypes()).length>0))
            return result;
        else
            return getExceptionTypes();
    }

    public abstract Class<?>[] getExceptionTypes();

    public boolean isVarArgs(){
        return (getModifiers()&Modifier.VARARGS)!=0;
    }

    public int getParameterCount(){
        throw new AbstractMethodError();
    }

    Type[] getAllGenericParameterTypes(){
        final boolean genericInfo=hasGenericInformation();
        // Easy case: we don't have generic parameter information.  In
        // this case, we just return the result of
        // getParameterTypes().
        if(!genericInfo){
            return getParameterTypes();
        }else{
            final boolean realParamData=hasRealParameterData();
            final Type[] genericParamTypes=getGenericParameterTypes();
            final Type[] nonGenericParamTypes=getParameterTypes();
            final Type[] out=new Type[nonGenericParamTypes.length];
            final Parameter[] params=getParameters();
            int fromidx=0;
            // If we have real parameter data, then we use the
            // synthetic and mandate flags to our advantage.
            if(realParamData){
                for(int i=0;i<out.length;i++){
                    final Parameter param=params[i];
                    if(param.isSynthetic()||param.isImplicit()){
                        // If we hit a synthetic or mandated parameter,
                        // use the non generic parameter info.
                        out[i]=nonGenericParamTypes[i];
                    }else{
                        // Otherwise, use the generic parameter info.
                        out[i]=genericParamTypes[fromidx];
                        fromidx++;
                    }
                }
            }else{
                // Otherwise, use the non-generic parameter data.
                // Without method parameter reflection data, we have
                // no way to figure out which parameters are
                // synthetic/mandated, thus, no way to match up the
                // indexes.
                return genericParamTypes.length==nonGenericParamTypes.length?
                        genericParamTypes:nonGenericParamTypes;
            }
            return out;
        }
    }

    public Parameter[] getParameters(){
        // TODO: This may eventually need to be guarded by security
        // mechanisms similar to those in Field, Method, etc.
        //
        // Need to copy the cached array to prevent users from messing
        // with it.  Since parameters are immutable, we can
        // shallow-copy.
        return privateGetParameters().clone();
    }

    private Parameter[] synthesizeAllParams(){
        final int realparams=getParameterCount();
        final Parameter[] out=new Parameter[realparams];
        for(int i=0;i<realparams;i++)
            // TODO: is there a way to synthetically derive the
            // modifiers?  Probably not in the general case, since
            // we'd have no way of knowing about them, but there
            // may be specific cases.
            out[i]=new Parameter("arg"+i,0,this,i);
        return out;
    }

    private void verifyParameters(final Parameter[] parameters){
        final int mask=Modifier.FINAL|Modifier.SYNTHETIC|Modifier.MANDATED;
        if(getParameterTypes().length!=parameters.length)
            throw new MalformedParametersException("Wrong number of parameters in MethodParameters attribute");
        for(Parameter parameter : parameters){
            final String name=parameter.getRealName();
            final int mods=parameter.getModifiers();
            if(name!=null){
                if(name.isEmpty()||name.indexOf('.')!=-1||
                        name.indexOf(';')!=-1||name.indexOf('[')!=-1||
                        name.indexOf('/')!=-1){
                    throw new MalformedParametersException("Invalid parameter name \""+name+"\"");
                }
            }
            if(mods!=(mods&mask)){
                throw new MalformedParametersException("Invalid parameter modifiers");
            }
        }
    }

    private Parameter[] privateGetParameters(){
        // Use tmp to avoid multiple writes to a volatile.
        Parameter[] tmp=parameters;
        if(tmp==null){
            // Otherwise, go to the JVM to get them
            try{
                tmp=getParameters0();
            }catch(IllegalArgumentException e){
                // Rethrow ClassFormatErrors
                throw new MalformedParametersException("Invalid constant pool index");
            }
            // If we get back nothing, then synthesize parameters
            if(tmp==null){
                hasRealParameterData=false;
                tmp=synthesizeAllParams();
            }else{
                hasRealParameterData=true;
                verifyParameters(tmp);
            }
            parameters=tmp;
        }
        return tmp;
    }

    boolean hasRealParameterData(){
        // If this somehow gets called before parameters gets
        // initialized, force it into existence.
        if(parameters==null){
            privateGetParameters();
        }
        return hasRealParameterData;
    }

    private native Parameter[] getParameters0();

    // Needed by reflectaccess
    byte[] getTypeAnnotationBytes(){
        return getTypeAnnotationBytes0();
    }

    native byte[] getTypeAnnotationBytes0();

    public abstract String toGenericString();

    public abstract Annotation[][] getParameterAnnotations();

    Annotation[][] sharedGetParameterAnnotations(Class<?>[] parameterTypes,
                                                 byte[] parameterAnnotations){
        int numParameters=parameterTypes.length;
        if(parameterAnnotations==null)
            return new Annotation[numParameters][0];
        Annotation[][] result=parseParameterAnnotations(parameterAnnotations);
        if(result.length!=numParameters)
            handleParameterNumberMismatch(result.length,numParameters);
        return result;
    }

    Annotation[][] parseParameterAnnotations(byte[] parameterAnnotations){
        return AnnotationParser.parseParameterAnnotations(
                parameterAnnotations,
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                getDeclaringClass());
    }

    public abstract Class<?> getDeclaringClass();

    public abstract String getName();

    public abstract int getModifiers();

    public boolean isSynthetic(){
        return Modifier.isSynthetic(getModifiers());
    }

    abstract void handleParameterNumberMismatch(int resultLength,int numParameters);

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass){
        Objects.requireNonNull(annotationClass);
        return annotationClass.cast(declaredAnnotations().get(annotationClass));
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass){
        Objects.requireNonNull(annotationClass);
        return AnnotationSupport.getDirectlyAndIndirectlyPresent(declaredAnnotations(),annotationClass);
    }

    public Annotation[] getDeclaredAnnotations(){
        return AnnotationParser.toArray(declaredAnnotations());
    }

    private synchronized Map<Class<? extends Annotation>,Annotation> declaredAnnotations(){
        if(declaredAnnotations==null){
            Executable root=getRoot();
            if(root!=null){
                declaredAnnotations=root.declaredAnnotations();
            }else{
                declaredAnnotations=AnnotationParser.parseAnnotations(
                        getAnnotationBytes(),
                        sun.misc.SharedSecrets.getJavaLangAccess().
                                getConstantPool(getDeclaringClass()),
                        getDeclaringClass());
            }
        }
        return declaredAnnotations;
    }

    abstract byte[] getAnnotationBytes();

    abstract Executable getRoot();

    public abstract AnnotatedType getAnnotatedReturnType();

    AnnotatedType getAnnotatedReturnType0(Type returnType){
        return TypeAnnotationParser.buildAnnotatedType(getTypeAnnotationBytes0(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                this,
                getDeclaringClass(),
                returnType,
                TypeAnnotation.TypeAnnotationTarget.METHOD_RETURN);
    }

    public AnnotatedType getAnnotatedReceiverType(){
        if(Modifier.isStatic(this.getModifiers()))
            return null;
        return TypeAnnotationParser.buildAnnotatedType(getTypeAnnotationBytes0(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                this,
                getDeclaringClass(),
                getDeclaringClass(),
                TypeAnnotation.TypeAnnotationTarget.METHOD_RECEIVER);
    }

    public AnnotatedType[] getAnnotatedParameterTypes(){
        return TypeAnnotationParser.buildAnnotatedTypes(getTypeAnnotationBytes0(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                this,
                getDeclaringClass(),
                getAllGenericParameterTypes(),
                TypeAnnotation.TypeAnnotationTarget.METHOD_FORMAL_PARAMETER);
    }

    public AnnotatedType[] getAnnotatedExceptionTypes(){
        return TypeAnnotationParser.buildAnnotatedTypes(getTypeAnnotationBytes0(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                        getConstantPool(getDeclaringClass()),
                this,
                getDeclaringClass(),
                getGenericExceptionTypes(),
                TypeAnnotation.TypeAnnotationTarget.THROWS);
    }
}
