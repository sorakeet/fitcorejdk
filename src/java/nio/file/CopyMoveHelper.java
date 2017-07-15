/**
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

class CopyMoveHelper{
    private CopyMoveHelper(){
    }

    static void moveToForeignTarget(Path source,Path target,
                                    CopyOption... options) throws IOException{
        copyToForeignTarget(source,target,convertMoveToCopyOptions(options));
        Files.delete(source);
    }

    private static CopyOption[] convertMoveToCopyOptions(CopyOption... options)
            throws AtomicMoveNotSupportedException{
        int len=options.length;
        CopyOption[] newOptions=new CopyOption[len+2];
        for(int i=0;i<len;i++){
            CopyOption option=options[i];
            if(option==StandardCopyOption.ATOMIC_MOVE){
                throw new AtomicMoveNotSupportedException(null,null,
                        "Atomic move between providers is not supported");
            }
            newOptions[i]=option;
        }
        newOptions[len]=LinkOption.NOFOLLOW_LINKS;
        newOptions[len+1]=StandardCopyOption.COPY_ATTRIBUTES;
        return newOptions;
    }

    static void copyToForeignTarget(Path source,Path target,
                                    CopyOption... options)
            throws IOException{
        CopyOptions opts=CopyOptions.parse(options);
        LinkOption[] linkOptions=(opts.followLinks)?new LinkOption[0]:
                new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        // attributes of source file
        BasicFileAttributes attrs=Files.readAttributes(source,
                BasicFileAttributes.class,
                linkOptions);
        if(attrs.isSymbolicLink())
            throw new IOException("Copying of symbolic links not supported");
        // delete target if it exists and REPLACE_EXISTING is specified
        if(opts.replaceExisting){
            Files.deleteIfExists(target);
        }else if(Files.exists(target))
            throw new FileAlreadyExistsException(target.toString());
        // create directory or copy file
        if(attrs.isDirectory()){
            Files.createDirectory(target);
        }else{
            try(InputStream in=Files.newInputStream(source)){
                Files.copy(in,target);
            }
        }
        // copy basic attributes to target
        if(opts.copyAttributes){
            BasicFileAttributeView view=
                    Files.getFileAttributeView(target,BasicFileAttributeView.class);
            try{
                view.setTimes(attrs.lastModifiedTime(),
                        attrs.lastAccessTime(),
                        attrs.creationTime());
            }catch(Throwable x){
                // rollback
                try{
                    Files.delete(target);
                }catch(Throwable suppressed){
                    x.addSuppressed(suppressed);
                }
                throw x;
            }
        }
    }

    private static class CopyOptions{
        boolean replaceExisting=false;
        boolean copyAttributes=false;
        boolean followLinks=true;

        private CopyOptions(){
        }

        static CopyOptions parse(CopyOption... options){
            CopyOptions result=new CopyOptions();
            for(CopyOption option : options){
                if(option==StandardCopyOption.REPLACE_EXISTING){
                    result.replaceExisting=true;
                    continue;
                }
                if(option==LinkOption.NOFOLLOW_LINKS){
                    result.followLinks=false;
                    continue;
                }
                if(option==StandardCopyOption.COPY_ATTRIBUTES){
                    result.copyAttributes=true;
                    continue;
                }
                if(option==null)
                    throw new NullPointerException();
                throw new UnsupportedOperationException("'"+option+
                        "' is not a recognized copy option");
            }
            return result;
        }
    }
}
