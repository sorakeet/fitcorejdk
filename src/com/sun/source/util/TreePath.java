/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.util;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

import java.util.Iterator;

@jdk.Exported
public class TreePath implements Iterable<Tree>{
    private CompilationUnitTree compilationUnit;
    private Tree leaf;
    private TreePath parent;

    public TreePath(CompilationUnitTree t){
        this(null,t);
    }

    public TreePath(TreePath p,Tree t){
        if(t.getKind()==Tree.Kind.COMPILATION_UNIT){
            compilationUnit=(CompilationUnitTree)t;
            parent=null;
        }else{
            compilationUnit=p.compilationUnit;
            parent=p;
        }
        leaf=t;
    }

    public static TreePath getPath(CompilationUnitTree unit,Tree target){
        return getPath(new TreePath(unit),target);
    }

    public static TreePath getPath(TreePath path,Tree target){
        path.getClass();
        target.getClass();
        class Result extends Error{
            static final long serialVersionUID=-5942088234594905625L;
            TreePath path;

            Result(TreePath path){
                this.path=path;
            }
        }
        class PathFinder extends TreePathScanner<TreePath,Tree>{
            public TreePath scan(Tree tree,Tree target){
                if(tree==target){
                    throw new Result(new TreePath(getCurrentPath(),target));
                }
                return super.scan(tree,target);
            }
        }
        if(path.getLeaf()==target){
            return path;
        }
        try{
            new PathFinder().scan(path,target);
        }catch(Result result){
            return result.path;
        }
        return null;
    }

    public Tree getLeaf(){
        return leaf;
    }

    public CompilationUnitTree getCompilationUnit(){
        return compilationUnit;
    }

    public TreePath getParentPath(){
        return parent;
    }

    @Override
    public Iterator<Tree> iterator(){
        return new Iterator<Tree>(){
            private TreePath next=TreePath.this;

            @Override
            public boolean hasNext(){
                return next!=null;
            }

            @Override
            public Tree next(){
                Tree t=next.leaf;
                next=next.parent;
                return t;
            }

            @Override
            public void remove(){
                throw new UnsupportedOperationException();
            }
        };
    }
}
