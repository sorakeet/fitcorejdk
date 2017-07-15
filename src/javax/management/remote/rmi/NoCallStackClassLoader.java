/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote.rmi;

import java.security.ProtectionDomain;

class NoCallStackClassLoader extends ClassLoader{
    private final String[] classNames;
    private final byte[][] byteCodes;
    private final String[] referencedClassNames;
    private final ClassLoader referencedClassLoader;
    private final ProtectionDomain protectionDomain;
    public NoCallStackClassLoader(String className,
                                  byte[] byteCode,
                                  String[] referencedClassNames,
                                  ClassLoader referencedClassLoader,
                                  ProtectionDomain protectionDomain){
        this(new String[]{className},new byte[][]{byteCode},
                referencedClassNames,referencedClassLoader,protectionDomain);
    }
    public NoCallStackClassLoader(String[] classNames,
                                  byte[][] byteCodes,
                                  String[] referencedClassNames,
                                  ClassLoader referencedClassLoader,
                                  ProtectionDomain protectionDomain){
        super(null);
        /** Validation. */
        if(classNames==null||classNames.length==0
                ||byteCodes==null||classNames.length!=byteCodes.length
                ||referencedClassNames==null||protectionDomain==null)
            throw new IllegalArgumentException();
        for(int i=0;i<classNames.length;i++){
            if(classNames[i]==null||byteCodes[i]==null)
                throw new IllegalArgumentException();
        }
        for(int i=0;i<referencedClassNames.length;i++){
            if(referencedClassNames[i]==null)
                throw new IllegalArgumentException();
        }
        this.classNames=classNames;
        this.byteCodes=byteCodes;
        this.referencedClassNames=referencedClassNames;
        this.referencedClassLoader=referencedClassLoader;
        this.protectionDomain=protectionDomain;
    }

    public static byte[] stringToBytes(String s){
        final int slen=s.length();
        byte[] bytes=new byte[slen];
        for(int i=0;i<slen;i++)
            bytes[i]=(byte)s.charAt(i);
        return bytes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException{
        // Note: classNames is guaranteed by the constructor to be non-null.
        for(int i=0;i<classNames.length;i++){
            if(name.equals(classNames[i])){
                return defineClass(classNames[i],byteCodes[i],0,
                        byteCodes[i].length,protectionDomain);
            }
        }
        /** If the referencedClassLoader is null, it is the bootstrap
         * class loader, and there's no point in delegating to it
         * because it's already our parent class loader.
         */
        if(referencedClassLoader!=null){
            for(int i=0;i<referencedClassNames.length;i++){
                if(name.equals(referencedClassNames[i]))
                    return referencedClassLoader.loadClass(name);
            }
        }
        throw new ClassNotFoundException(name);
    }
}
/**

 You can use the following Emacs function to convert class files into
 strings to be used by the stringToBytes method above.  Select the
 whole (defun...) with the mouse and type M-x eval-region, or save it
 to a file and do M-x load-file.  Then visit the *.class file and do
 M-x class-string.

 ;; class-string.el
 ;; visit the *.class file with emacs, then invoke this function

 (defun class-string ()
 "Construct a Java string whose bytes are the same as the current
 buffer.  The resultant string is put in a buffer called *string*,
 possibly with a numeric suffix like <2>.  From there it can be
 insert-buffer'd into a Java program."
 (interactive)
 (let* ((s (buffer-string))
 (slen (length s))
 (i 0)
 (buf (generate-new-buffer "*string*")))
 (set-buffer buf)
 (insert "\"")
 (while (< i slen)
 (if (> (current-column) 61)
 (insert "\"+\n\""))
 (let ((c (aref s i)))
 (insert (cond
 ((> c 126) (format "\\%o" c))
 ((= c ?\") "\\\"")
 ((= c ?\\) "\\\\")
 ((< c 33)
 (let ((nextc (if (< (1+ i) slen)
 (aref s (1+ i))
 ?\0)))
 (cond
 ((and (<= nextc ?7) (>= nextc ?0))
 (format "\\%03o" c))
 (t
 (format "\\%o" c)))))
 (t c))))
 (setq i (1+ i)))
 (insert "\"")
 (switch-to-buffer buf)))

 Alternatively, the following class reads a class file and outputs a string
 that can be used by the stringToBytes method above.

 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;

 public class BytesToString {

 public static void main(String[] args) throws IOException {
 File f = new File(args[0]);
 int len = (int)f.length();
 byte[] classBytes = new byte[len];

 FileInputStream in = new FileInputStream(args[0]);
 try {
 int pos = 0;
 for (;;) {
 int n = in.read(classBytes, pos, (len-pos));
 if (n < 0)
 throw new RuntimeException("class file changed??");
 pos += n;
 if (pos >= n)
 break;
 }
 } finally {
 in.close();
 }

 int pos = 0;
 boolean lastWasOctal = false;
 for (int i=0; i<len; i++) {
 int value = classBytes[i];
 if (value < 0)
 value += 256;
 String s = null;
 if (value == '\\')
 s = "\\\\";
 else if (value == '\"')
 s = "\\\"";
 else {
 if ((value >= 32 && value < 127) && ((!lastWasOctal ||
 (value < '0' || value > '7')))) {
 s = Character.toString((char)value);
 }
 }
 if (s == null) {
 s = "\\" + Integer.toString(value, 8);
 lastWasOctal = true;
 } else {
 lastWasOctal = false;
 }
 if (pos > 61) {
 System.out.print("\"");
 if (i<len)
 System.out.print("+");
 System.out.println();
 pos = 0;
 }
 if (pos == 0)
 System.out.print("                \"");
 System.out.print(s);
 pos += s.length();
 }
 System.out.println("\"");
 }
 }

 */
