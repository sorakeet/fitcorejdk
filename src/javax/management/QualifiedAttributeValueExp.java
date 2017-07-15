/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

class QualifiedAttributeValueExp extends AttributeValueExp{
    private static final long serialVersionUID=8832517277410933254L;
    private String className;

    @Deprecated
    public QualifiedAttributeValueExp(){
    }

    public QualifiedAttributeValueExp(String className,String attr){
        super(attr);
        this.className=className;
    }

    public String getAttrClassName(){
        return className;
    }

    @Override
    public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
            BadAttributeValueExpException, InvalidApplicationException{
        try{
            MBeanServer server=QueryEval.getMBeanServer();
            String v=server.getObjectInstance(name).getClassName();
            if(v.equals(className)){
                return super.apply(name);
            }
            throw new InvalidApplicationException("Class name is "+v+
                    ", should be "+className);
        }catch(Exception e){
            throw new InvalidApplicationException("Qualified attribute: "+e);
            /** Can happen if MBean disappears between the time we
             construct the list of MBeans to query and the time we
             evaluate the query on this MBean, or if
             getObjectInstance throws SecurityException.  */
        }
    }

    @Override
    public String toString(){
        if(className!=null){
            return className+"."+super.toString();
        }else{
            return super.toString();
        }
    }
}
