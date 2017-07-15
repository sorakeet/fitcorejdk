/**
 * Copyright (c) 2002, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.Permission;

public class MBeanPermission extends Permission{
    private static final long serialVersionUID=-2416928705275160661L;
    private static final int AddNotificationListener=0x00001;
    private static final int GetAttribute=0x00002;
    private static final int GetClassLoader=0x00004;
    private static final int GetClassLoaderFor=0x00008;
    private static final int GetClassLoaderRepository=0x00010;
    private static final int GetDomains=0x00020;
    private static final int GetMBeanInfo=0x00040;
    private static final int GetObjectInstance=0x00080;
    private static final int Instantiate=0x00100;
    private static final int Invoke=0x00200;
    private static final int IsInstanceOf=0x00400;
    private static final int QueryMBeans=0x00800;
    private static final int QueryNames=0x01000;
    private static final int RegisterMBean=0x02000;
    private static final int RemoveNotificationListener=0x04000;
    private static final int SetAttribute=0x08000;
    private static final int UnregisterMBean=0x10000;
    private static final int NONE=0x00000;
    private static final int ALL=
            AddNotificationListener|
                    GetAttribute|
                    GetClassLoader|
                    GetClassLoaderFor|
                    GetClassLoaderRepository|
                    GetDomains|
                    GetMBeanInfo|
                    GetObjectInstance|
                    Instantiate|
                    Invoke|
                    IsInstanceOf|
                    QueryMBeans|
                    QueryNames|
                    RegisterMBean|
                    RemoveNotificationListener|
                    SetAttribute|
                    UnregisterMBean;
    private String actions;
    private transient int mask;
    private transient String classNamePrefix;
    private transient boolean classNameExactMatch;
    private transient String member;
    private transient ObjectName objectName;

    public MBeanPermission(String name,String actions){
        super(name);
        parseName();
        this.actions=actions;
        parseActions();
    }

    private void parseActions(){
        int mask;
        if(actions==null)
            throw new IllegalArgumentException("MBeanPermission: "+
                    "actions can't be null");
        if(actions.equals(""))
            throw new IllegalArgumentException("MBeanPermission: "+
                    "actions can't be empty");
        mask=getMask(actions);
        if((mask&ALL)!=mask)
            throw new IllegalArgumentException("Invalid actions mask");
        if(mask==NONE)
            throw new IllegalArgumentException("Invalid actions mask");
        this.mask=mask;
    }

    private static int getMask(String action){
        /**
         * BE CAREFUL HERE! PARSING ORDER IS IMPORTANT IN THIS ALGORITHM.
         *
         * The 'string length' test must be performed for the lengthiest
         * strings first.
         *
         * In this permission if the "unregisterMBean" string length test is
         * performed after the "registerMBean" string length test the algorithm
         * considers the 'unregisterMBean' action as being the 'registerMBean'
         * action and a parsing error is returned.
         */
        int mask=NONE;
        if(action==null){
            return mask;
        }
        if(action.equals("*")){
            return ALL;
        }
        char[] a=action.toCharArray();
        int i=a.length-1;
        if(i<0)
            return mask;
        while(i!=-1){
            char c;
            // skip whitespace
            while((i!=-1)&&((c=a[i])==' '||
                    c=='\r'||
                    c=='\n'||
                    c=='\f'||
                    c=='\t'))
                i--;
            // check for the known strings
            int matchlen;
            if(i>=25&& /** removeNotificationListener */
                    (a[i-25]=='r')&&
                    (a[i-24]=='e')&&
                    (a[i-23]=='m')&&
                    (a[i-22]=='o')&&
                    (a[i-21]=='v')&&
                    (a[i-20]=='e')&&
                    (a[i-19]=='N')&&
                    (a[i-18]=='o')&&
                    (a[i-17]=='t')&&
                    (a[i-16]=='i')&&
                    (a[i-15]=='f')&&
                    (a[i-14]=='i')&&
                    (a[i-13]=='c')&&
                    (a[i-12]=='a')&&
                    (a[i-11]=='t')&&
                    (a[i-10]=='i')&&
                    (a[i-9]=='o')&&
                    (a[i-8]=='n')&&
                    (a[i-7]=='L')&&
                    (a[i-6]=='i')&&
                    (a[i-5]=='s')&&
                    (a[i-4]=='t')&&
                    (a[i-3]=='e')&&
                    (a[i-2]=='n')&&
                    (a[i-1]=='e')&&
                    (a[i]=='r')){
                matchlen=26;
                mask|=RemoveNotificationListener;
            }else if(i>=23&& /** getClassLoaderRepository */
                    (a[i-23]=='g')&&
                    (a[i-22]=='e')&&
                    (a[i-21]=='t')&&
                    (a[i-20]=='C')&&
                    (a[i-19]=='l')&&
                    (a[i-18]=='a')&&
                    (a[i-17]=='s')&&
                    (a[i-16]=='s')&&
                    (a[i-15]=='L')&&
                    (a[i-14]=='o')&&
                    (a[i-13]=='a')&&
                    (a[i-12]=='d')&&
                    (a[i-11]=='e')&&
                    (a[i-10]=='r')&&
                    (a[i-9]=='R')&&
                    (a[i-8]=='e')&&
                    (a[i-7]=='p')&&
                    (a[i-6]=='o')&&
                    (a[i-5]=='s')&&
                    (a[i-4]=='i')&&
                    (a[i-3]=='t')&&
                    (a[i-2]=='o')&&
                    (a[i-1]=='r')&&
                    (a[i]=='y')){
                matchlen=24;
                mask|=GetClassLoaderRepository;
            }else if(i>=22&& /** addNotificationListener */
                    (a[i-22]=='a')&&
                    (a[i-21]=='d')&&
                    (a[i-20]=='d')&&
                    (a[i-19]=='N')&&
                    (a[i-18]=='o')&&
                    (a[i-17]=='t')&&
                    (a[i-16]=='i')&&
                    (a[i-15]=='f')&&
                    (a[i-14]=='i')&&
                    (a[i-13]=='c')&&
                    (a[i-12]=='a')&&
                    (a[i-11]=='t')&&
                    (a[i-10]=='i')&&
                    (a[i-9]=='o')&&
                    (a[i-8]=='n')&&
                    (a[i-7]=='L')&&
                    (a[i-6]=='i')&&
                    (a[i-5]=='s')&&
                    (a[i-4]=='t')&&
                    (a[i-3]=='e')&&
                    (a[i-2]=='n')&&
                    (a[i-1]=='e')&&
                    (a[i]=='r')){
                matchlen=23;
                mask|=AddNotificationListener;
            }else if(i>=16&& /** getClassLoaderFor */
                    (a[i-16]=='g')&&
                    (a[i-15]=='e')&&
                    (a[i-14]=='t')&&
                    (a[i-13]=='C')&&
                    (a[i-12]=='l')&&
                    (a[i-11]=='a')&&
                    (a[i-10]=='s')&&
                    (a[i-9]=='s')&&
                    (a[i-8]=='L')&&
                    (a[i-7]=='o')&&
                    (a[i-6]=='a')&&
                    (a[i-5]=='d')&&
                    (a[i-4]=='e')&&
                    (a[i-3]=='r')&&
                    (a[i-2]=='F')&&
                    (a[i-1]=='o')&&
                    (a[i]=='r')){
                matchlen=17;
                mask|=GetClassLoaderFor;
            }else if(i>=16&& /** getObjectInstance */
                    (a[i-16]=='g')&&
                    (a[i-15]=='e')&&
                    (a[i-14]=='t')&&
                    (a[i-13]=='O')&&
                    (a[i-12]=='b')&&
                    (a[i-11]=='j')&&
                    (a[i-10]=='e')&&
                    (a[i-9]=='c')&&
                    (a[i-8]=='t')&&
                    (a[i-7]=='I')&&
                    (a[i-6]=='n')&&
                    (a[i-5]=='s')&&
                    (a[i-4]=='t')&&
                    (a[i-3]=='a')&&
                    (a[i-2]=='n')&&
                    (a[i-1]=='c')&&
                    (a[i]=='e')){
                matchlen=17;
                mask|=GetObjectInstance;
            }else if(i>=14&& /** unregisterMBean */
                    (a[i-14]=='u')&&
                    (a[i-13]=='n')&&
                    (a[i-12]=='r')&&
                    (a[i-11]=='e')&&
                    (a[i-10]=='g')&&
                    (a[i-9]=='i')&&
                    (a[i-8]=='s')&&
                    (a[i-7]=='t')&&
                    (a[i-6]=='e')&&
                    (a[i-5]=='r')&&
                    (a[i-4]=='M')&&
                    (a[i-3]=='B')&&
                    (a[i-2]=='e')&&
                    (a[i-1]=='a')&&
                    (a[i]=='n')){
                matchlen=15;
                mask|=UnregisterMBean;
            }else if(i>=13&& /** getClassLoader */
                    (a[i-13]=='g')&&
                    (a[i-12]=='e')&&
                    (a[i-11]=='t')&&
                    (a[i-10]=='C')&&
                    (a[i-9]=='l')&&
                    (a[i-8]=='a')&&
                    (a[i-7]=='s')&&
                    (a[i-6]=='s')&&
                    (a[i-5]=='L')&&
                    (a[i-4]=='o')&&
                    (a[i-3]=='a')&&
                    (a[i-2]=='d')&&
                    (a[i-1]=='e')&&
                    (a[i]=='r')){
                matchlen=14;
                mask|=GetClassLoader;
            }else if(i>=12&& /** registerMBean */
                    (a[i-12]=='r')&&
                    (a[i-11]=='e')&&
                    (a[i-10]=='g')&&
                    (a[i-9]=='i')&&
                    (a[i-8]=='s')&&
                    (a[i-7]=='t')&&
                    (a[i-6]=='e')&&
                    (a[i-5]=='r')&&
                    (a[i-4]=='M')&&
                    (a[i-3]=='B')&&
                    (a[i-2]=='e')&&
                    (a[i-1]=='a')&&
                    (a[i]=='n')){
                matchlen=13;
                mask|=RegisterMBean;
            }else if(i>=11&& /** getAttribute */
                    (a[i-11]=='g')&&
                    (a[i-10]=='e')&&
                    (a[i-9]=='t')&&
                    (a[i-8]=='A')&&
                    (a[i-7]=='t')&&
                    (a[i-6]=='t')&&
                    (a[i-5]=='r')&&
                    (a[i-4]=='i')&&
                    (a[i-3]=='b')&&
                    (a[i-2]=='u')&&
                    (a[i-1]=='t')&&
                    (a[i]=='e')){
                matchlen=12;
                mask|=GetAttribute;
            }else if(i>=11&& /** getMBeanInfo */
                    (a[i-11]=='g')&&
                    (a[i-10]=='e')&&
                    (a[i-9]=='t')&&
                    (a[i-8]=='M')&&
                    (a[i-7]=='B')&&
                    (a[i-6]=='e')&&
                    (a[i-5]=='a')&&
                    (a[i-4]=='n')&&
                    (a[i-3]=='I')&&
                    (a[i-2]=='n')&&
                    (a[i-1]=='f')&&
                    (a[i]=='o')){
                matchlen=12;
                mask|=GetMBeanInfo;
            }else if(i>=11&& /** isInstanceOf */
                    (a[i-11]=='i')&&
                    (a[i-10]=='s')&&
                    (a[i-9]=='I')&&
                    (a[i-8]=='n')&&
                    (a[i-7]=='s')&&
                    (a[i-6]=='t')&&
                    (a[i-5]=='a')&&
                    (a[i-4]=='n')&&
                    (a[i-3]=='c')&&
                    (a[i-2]=='e')&&
                    (a[i-1]=='O')&&
                    (a[i]=='f')){
                matchlen=12;
                mask|=IsInstanceOf;
            }else if(i>=11&& /** setAttribute */
                    (a[i-11]=='s')&&
                    (a[i-10]=='e')&&
                    (a[i-9]=='t')&&
                    (a[i-8]=='A')&&
                    (a[i-7]=='t')&&
                    (a[i-6]=='t')&&
                    (a[i-5]=='r')&&
                    (a[i-4]=='i')&&
                    (a[i-3]=='b')&&
                    (a[i-2]=='u')&&
                    (a[i-1]=='t')&&
                    (a[i]=='e')){
                matchlen=12;
                mask|=SetAttribute;
            }else if(i>=10&& /** instantiate */
                    (a[i-10]=='i')&&
                    (a[i-9]=='n')&&
                    (a[i-8]=='s')&&
                    (a[i-7]=='t')&&
                    (a[i-6]=='a')&&
                    (a[i-5]=='n')&&
                    (a[i-4]=='t')&&
                    (a[i-3]=='i')&&
                    (a[i-2]=='a')&&
                    (a[i-1]=='t')&&
                    (a[i]=='e')){
                matchlen=11;
                mask|=Instantiate;
            }else if(i>=10&& /** queryMBeans */
                    (a[i-10]=='q')&&
                    (a[i-9]=='u')&&
                    (a[i-8]=='e')&&
                    (a[i-7]=='r')&&
                    (a[i-6]=='y')&&
                    (a[i-5]=='M')&&
                    (a[i-4]=='B')&&
                    (a[i-3]=='e')&&
                    (a[i-2]=='a')&&
                    (a[i-1]=='n')&&
                    (a[i]=='s')){
                matchlen=11;
                mask|=QueryMBeans;
            }else if(i>=9&& /** getDomains */
                    (a[i-9]=='g')&&
                    (a[i-8]=='e')&&
                    (a[i-7]=='t')&&
                    (a[i-6]=='D')&&
                    (a[i-5]=='o')&&
                    (a[i-4]=='m')&&
                    (a[i-3]=='a')&&
                    (a[i-2]=='i')&&
                    (a[i-1]=='n')&&
                    (a[i]=='s')){
                matchlen=10;
                mask|=GetDomains;
            }else if(i>=9&& /** queryNames */
                    (a[i-9]=='q')&&
                    (a[i-8]=='u')&&
                    (a[i-7]=='e')&&
                    (a[i-6]=='r')&&
                    (a[i-5]=='y')&&
                    (a[i-4]=='N')&&
                    (a[i-3]=='a')&&
                    (a[i-2]=='m')&&
                    (a[i-1]=='e')&&
                    (a[i]=='s')){
                matchlen=10;
                mask|=QueryNames;
            }else if(i>=5&& /** invoke */
                    (a[i-5]=='i')&&
                    (a[i-4]=='n')&&
                    (a[i-3]=='v')&&
                    (a[i-2]=='o')&&
                    (a[i-1]=='k')&&
                    (a[i]=='e')){
                matchlen=6;
                mask|=Invoke;
            }else{
                // parse error
                throw new IllegalArgumentException("Invalid permission: "+
                        action);
            }
            // make sure we didn't just match the tail of a word
            // like "ackbarfaccept".  Also, skip to the comma.
            boolean seencomma=false;
            while(i>=matchlen&&!seencomma){
                switch(a[i-matchlen]){
                    case ',':
                        seencomma=true;
                        break;
                    case ' ':
                    case '\r':
                    case '\n':
                    case '\f':
                    case '\t':
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid permission: "+
                                action);
                }
                i--;
            }
            // point i at the location of the comma minus one (or -1).
            i-=matchlen;
        }
        return mask;
    }

    private void parseName(){
        String name=getName();
        if(name==null)
            throw new IllegalArgumentException("MBeanPermission name "+
                    "cannot be null");
        if(name.equals(""))
            throw new IllegalArgumentException("MBeanPermission name "+
                    "cannot be empty");
        /** The name looks like "class#member[objectname]".  We subtract
         elements from the right as we parse, so after parsing the
         objectname we have "class#member" and after parsing the
         member we have "class".  Each element is optional.  */
        // Parse ObjectName
        int openingBracket=name.indexOf("[");
        if(openingBracket==-1){
            // If "[on]" missing then ObjectName("*:*")
            //
            objectName=ObjectName.WILDCARD;
        }else{
            if(!name.endsWith("]")){
                throw new IllegalArgumentException("MBeanPermission: "+
                        "The ObjectName in the "+
                        "target name must be "+
                        "included in square "+
                        "brackets");
            }else{
                // Create ObjectName
                //
                try{
                    // If "[]" then ObjectName("*:*")
                    //
                    String on=name.substring(openingBracket+1,
                            name.length()-1);
                    if(on.equals(""))
                        objectName=ObjectName.WILDCARD;
                    else if(on.equals("-"))
                        objectName=null;
                    else
                        objectName=new ObjectName(on);
                }catch(MalformedObjectNameException e){
                    throw new IllegalArgumentException("MBeanPermission: "+
                            "The target name does "+
                            "not specify a valid "+
                            "ObjectName",e);
                }
            }
            name=name.substring(0,openingBracket);
        }
        // Parse member
        int poundSign=name.indexOf("#");
        if(poundSign==-1)
            setMember("*");
        else{
            String memberName=name.substring(poundSign+1);
            setMember(memberName);
            name=name.substring(0,poundSign);
        }
        // Parse className
        setClassName(name);
    }

    private void setClassName(String className){
        if(className==null||className.equals("-")){
            classNamePrefix=null;
            classNameExactMatch=false;
        }else if(className.equals("")||className.equals("*")){
            classNamePrefix="";
            classNameExactMatch=false;
        }else if(className.endsWith(".*")){
            // Note that we include the "." in the required prefix
            classNamePrefix=className.substring(0,className.length()-1);
            classNameExactMatch=false;
        }else{
            classNamePrefix=className;
            classNameExactMatch=true;
        }
    }

    private void setMember(String member){
        if(member==null||member.equals("-"))
            this.member=null;
        else if(member.equals(""))
            this.member="*";
        else
            this.member=member;
    }

    public MBeanPermission(String className,
                           String member,
                           ObjectName objectName,
                           String actions){
        super(makeName(className,member,objectName));
        initName(className,member,objectName);
        this.actions=actions;
        parseActions();
    }

    private void initName(String className,String member,
                          ObjectName objectName){
        setClassName(className);
        setMember(member);
        this.objectName=objectName;
    }

    private static String makeName(String className,String member,
                                   ObjectName objectName){
        final StringBuilder name=new StringBuilder();
        if(className==null)
            className="-";
        name.append(className);
        if(member==null)
            member="-";
        name.append("#"+member);
        if(objectName==null)
            name.append("[-]");
        else
            name.append("[").append(objectName.getCanonicalName()).append("]");
        /** In the interests of legibility for Permission.toString(), we
         transform the empty string into "*".  */
        if(name.length()==0)
            return "*";
        else
            return name.toString();
    }

    public boolean implies(Permission p){
        if(!(p instanceof MBeanPermission))
            return false;
        MBeanPermission that=(MBeanPermission)p;
        // Actions
        //
        // The actions in 'this' permission must be a
        // superset of the actions in 'that' permission
        //
        /** "queryMBeans" implies "queryNames" */
        if((this.mask&QueryMBeans)==QueryMBeans){
            if(((this.mask|QueryNames)&that.mask)!=that.mask){
                //System.out.println("action [with QueryNames] does not imply");
                return false;
            }
        }else{
            if((this.mask&that.mask)!=that.mask){
                //System.out.println("action does not imply");
                return false;
            }
        }
        // Target name
        //
        // The 'className' check is true iff:
        // 1) the className in 'this' permission is omitted or "*", or
        // 2) the className in 'that' permission is omitted or "*", or
        // 3) the className in 'this' permission does pattern
        //    matching with the className in 'that' permission.
        //
        // The 'member' check is true iff:
        // 1) the member in 'this' permission is omitted or "*", or
        // 2) the member in 'that' permission is omitted or "*", or
        // 3) the member in 'this' permission equals the member in
        //    'that' permission.
        //
        // The 'object name' check is true iff:
        // 1) the object name in 'this' permission is omitted or "*:*", or
        // 2) the object name in 'that' permission is omitted or "*:*", or
        // 3) the object name in 'this' permission does pattern
        //    matching with the object name in 'that' permission.
        //
        /** Check if this.className implies that.className.

         If that.classNamePrefix is empty that means the className is
         irrelevant for this permission check.  Otherwise, we do not
         expect that "that" contains a wildcard, since it is a
         needed permission.  So we assume that.classNameExactMatch.  */
        if(that.classNamePrefix==null){
            // bottom is implied
        }else if(this.classNamePrefix==null){
            // bottom implies nothing but itself
            return false;
        }else if(this.classNameExactMatch){
            if(!that.classNameExactMatch)
                return false; // exact never implies wildcard
            if(!that.classNamePrefix.equals(this.classNamePrefix))
                return false; // exact match fails
        }else{
            // prefix match, works even if "that" is also a wildcard
            // e.g. a.* implies a.* and a.b.*
            if(!that.classNamePrefix.startsWith(this.classNamePrefix))
                return false;
        }
        /** Check if this.member implies that.member */
        if(that.member==null){
            // bottom is implied
        }else if(this.member==null){
            // bottom implies nothing but itself
            return false;
        }else if(this.member.equals("*")){
            // wildcard implies everything (including itself)
        }else if(!this.member.equals(that.member)){
            return false;
        }
        /** Check if this.objectName implies that.objectName */
        if(that.objectName==null){
            // bottom is implied
        }else if(this.objectName==null){
            // bottom implies nothing but itself
            return false;
        }else if(!this.objectName.apply(that.objectName)){
            /** ObjectName.apply returns false if that.objectName is a
             wildcard so we also allow equals for that case.  This
             never happens during real permission checks, but means
             the implies relation is reflexive.  */
            if(!this.objectName.equals(that.objectName))
                return false;
        }
        return true;
    }

    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(!(obj instanceof MBeanPermission))
            return false;
        MBeanPermission that=(MBeanPermission)obj;
        return (this.mask==that.mask)&&
                (this.getName().equals(that.getName()));
    }

    public int hashCode(){
        return this.getName().hashCode()+this.getActions().hashCode();
    }

    public String getActions(){
        if(actions==null)
            actions=getActions(this.mask);
        return actions;
    }

    private static String getActions(int mask){
        final StringBuilder sb=new StringBuilder();
        boolean comma=false;
        if((mask&AddNotificationListener)==AddNotificationListener){
            comma=true;
            sb.append("addNotificationListener");
        }
        if((mask&GetAttribute)==GetAttribute){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("getAttribute");
        }
        if((mask&GetClassLoader)==GetClassLoader){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("getClassLoader");
        }
        if((mask&GetClassLoaderFor)==GetClassLoaderFor){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("getClassLoaderFor");
        }
        if((mask&GetClassLoaderRepository)==GetClassLoaderRepository){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("getClassLoaderRepository");
        }
        if((mask&GetDomains)==GetDomains){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("getDomains");
        }
        if((mask&GetMBeanInfo)==GetMBeanInfo){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("getMBeanInfo");
        }
        if((mask&GetObjectInstance)==GetObjectInstance){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("getObjectInstance");
        }
        if((mask&Instantiate)==Instantiate){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("instantiate");
        }
        if((mask&Invoke)==Invoke){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("invoke");
        }
        if((mask&IsInstanceOf)==IsInstanceOf){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("isInstanceOf");
        }
        if((mask&QueryMBeans)==QueryMBeans){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("queryMBeans");
        }
        if((mask&QueryNames)==QueryNames){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("queryNames");
        }
        if((mask&RegisterMBean)==RegisterMBean){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("registerMBean");
        }
        if((mask&RemoveNotificationListener)==RemoveNotificationListener){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("removeNotificationListener");
        }
        if((mask&SetAttribute)==SetAttribute){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("setAttribute");
        }
        if((mask&UnregisterMBean)==UnregisterMBean){
            if(comma) sb.append(',');
            else comma=true;
            sb.append("unregisterMBean");
        }
        return sb.toString();
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        parseName();
        parseActions();
    }
}
