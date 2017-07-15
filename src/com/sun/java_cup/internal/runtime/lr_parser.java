/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java_cup.internal.runtime;

import java.util.Stack;

public abstract class lr_parser{
    /**-----------------------------------------------------------*/
    /**--- (Access to) Static (Class) Variables ------------------*/
    protected final static int _error_sync_size=3;
    protected boolean _done_parsing=false;
    /**-----------------------------------------------------------*/
    /** Global parse state shared by parse(), error recovery, and
     * debugging routines */
    protected int tos;
    protected Symbol cur_token;
    /**-----------------------------------------------------------*/
    protected Stack stack=new Stack();
    protected short[][] production_tab;
    protected short[][] action_tab;
    protected short[][] reduce_tab;
    protected Symbol lookahead[];
    protected int lookahead_pos;
    private Scanner _scanner;

    public lr_parser(Scanner s){
        this(); /** in case default constructor someday does something */
        setScanner(s);
    }

    /**--- Constructor(s) ----------------------------------------*/
    public lr_parser(){
        /** nothing to do here */
    }
    /**. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    protected static short[][] unpackFromStrings(String[] sa){
        // Concatanate initialization strings.
        StringBuffer sb=new StringBuffer(sa[0]);
        for(int i=1;i<sa.length;i++)
            sb.append(sa[i]);
        int n=0; // location in initialization string
        int size1=(((int)sb.charAt(n))<<16)|((int)sb.charAt(n+1));
        n+=2;
        short[][] result=new short[size1][];
        for(int i=0;i<size1;i++){
            int size2=(((int)sb.charAt(n))<<16)|((int)sb.charAt(n+1));
            n+=2;
            result[i]=new short[size2];
            for(int j=0;j<size2;j++)
                result[i][j]=(short)(sb.charAt(n++)-2);
        }
        return result;
    }

    protected int error_sync_size(){
        return _error_sync_size;
    }

    /**--- (Access to) Instance Variables ------------------------*/
    public abstract short[][] production_table();

    public abstract short[][] action_table();

    public abstract short[][] reduce_table();

    public abstract int start_state();

    public abstract int start_production();

    public abstract int EOF_sym();

    public abstract int error_sym();
    /**-----------------------------------------------------------*/

    public void done_parsing(){
        _done_parsing=true;
    }

    public Scanner getScanner(){
        return _scanner;
    }

    public void setScanner(Scanner s){
        _scanner=s;
    }

    /**--- General Methods ---------------------------------------*/
    public abstract Symbol do_action(
            int act_num,
            lr_parser parser,
            Stack stack,
            int top)
            throws Exception;

    public void user_init() throws Exception{
    }

    protected abstract void init_actions() throws Exception;

    public Symbol scan() throws Exception{
        return getScanner().next_token();
    }

    public void report_fatal_error(
            String message,
            Object info)
            throws Exception{
        /** stop parsing (not really necessary since we throw an exception, but) */
        done_parsing();
        /** use the normal error message reporting to put out the message */
        report_error(message,info);
        /** throw an exception */
        throw new Exception("Can't recover from previous error(s)");
    }

    public void report_error(String message,Object info){
        System.err.print(message);
        if(info instanceof Symbol)
            if(((Symbol)info).left!=-1)
                System.err.println(" at character "+((Symbol)info).left+
                        " of input");
            else System.err.println("");
        else System.err.println("");
    }

    public void syntax_error(Symbol cur_token){
        report_error("Syntax error",cur_token);
    }

    public void unrecovered_syntax_error(Symbol cur_token)
            throws Exception{
        report_fatal_error("Couldn't repair and continue parse",cur_token);
    }

    protected final short get_action(int state,int sym){
        short tag;
        int first, last, probe;
        short[] row=action_tab[state];
        /** linear search if we are < 10 entries */
        if(row.length<20)
            for(probe=0;probe<row.length;probe++){
                /** is this entry labeled with our Symbol or the default? */
                tag=row[probe++];
                if(tag==sym||tag==-1){
                    /** return the next entry */
                    return row[probe];
                }
            }
        /** otherwise binary search */
        else{
            first=0;
            last=(row.length-1)/2-1;  /** leave out trailing default entry */
            while(first<=last){
                probe=(first+last)/2;
                if(sym==row[probe*2])
                    return row[probe*2+1];
                else if(sym>row[probe*2])
                    first=probe+1;
                else
                    last=probe-1;
            }
            /** not found, use the default at the end */
            return row[row.length-1];
        }
        /** shouldn't happened, but if we run off the end we return the
         default (error == 0) */
        return 0;
    }

    protected final short get_reduce(int state,int sym){
        short tag;
        short[] row=reduce_tab[state];
        /** if we have a null row we go with the default */
        if(row==null)
            return -1;
        for(int probe=0;probe<row.length;probe++){
            /** is this entry labeled with our Symbol or the default? */
            tag=row[probe++];
            if(tag==sym||tag==-1){
                /** return the next entry */
                return row[probe];
            }
        }
        /** if we run off the end we return the default (error == -1) */
        return -1;
    }

    public Symbol parse() throws Exception{
        /** the current action code */
        int act;
        /** the Symbol/stack element returned by a reduce */
        Symbol lhs_sym=null;
        /** information about production being reduced with */
        short handle_size, lhs_sym_num;
        /** set up direct reference to tables to drive the parser */
        production_tab=production_table();
        action_tab=action_table();
        reduce_tab=reduce_table();
        /** initialize the action encapsulation object */
        init_actions();
        /** do user initialization */
        user_init();
        /** get the first token */
        cur_token=scan();
        /** push dummy Symbol with start state to get us underway */
        stack.removeAllElements();
        stack.push(new Symbol(0,start_state()));
        tos=0;
        /** continue until we are told to stop */
        for(_done_parsing=false;!_done_parsing;){
            /** Check current token for freshness. */
            if(cur_token.used_by_parser)
                throw new Error("Symbol recycling detected (fix your scanner).");
            /** current state is always on the top of the stack */
            /** look up action out of the current state with the current input */
            act=get_action(((Symbol)stack.peek()).parse_state,cur_token.sym);
            /** decode the action -- > 0 encodes shift */
            if(act>0){
                /** shift to the encoded state by pushing it on the stack */
                cur_token.parse_state=act-1;
                cur_token.used_by_parser=true;
                stack.push(cur_token);
                tos++;
                /** advance to the next Symbol */
                cur_token=scan();
            }
            /** if its less than zero, then it encodes a reduce action */
            else if(act<0){
                /** perform the action for the reduce */
                lhs_sym=do_action((-act)-1,this,stack,tos);
                /** look up information about the production */
                lhs_sym_num=production_tab[(-act)-1][0];
                handle_size=production_tab[(-act)-1][1];
                /** pop the handle off the stack */
                for(int i=0;i<handle_size;i++){
                    stack.pop();
                    tos--;
                }
                /** look up the state to go to from the one popped back to */
                act=get_reduce(((Symbol)stack.peek()).parse_state,lhs_sym_num);
                /** shift to that state */
                lhs_sym.parse_state=act;
                lhs_sym.used_by_parser=true;
                stack.push(lhs_sym);
                tos++;
            }
            /** finally if the entry is zero, we have an error */
            else if(act==0){
                /** call user syntax error reporting routine */
                syntax_error(cur_token);
                /** try to error recover */
                if(!error_recovery(false)){
                    /** if that fails give up with a fatal syntax error */
                    unrecovered_syntax_error(cur_token);
                    /** just in case that wasn't fatal enough, end parse */
                    done_parsing();
                }else{
                    lhs_sym=(Symbol)stack.peek();
                }
            }
        }
        return lhs_sym;
    }

    public void dump_stack(){
        if(stack==null){
            debug_message("# Stack dump requested, but stack is null");
            return;
        }
        debug_message("============ Parse Stack Dump ============");
        /** dump the stack */
        for(int i=0;i<stack.size();i++){
            debug_message("Symbol: "+((Symbol)stack.elementAt(i)).sym+
                    " State: "+((Symbol)stack.elementAt(i)).parse_state);
        }
        debug_message("==========================================");
    }

    public void debug_message(String mess){
        System.err.println(mess);
    }

    public void debug_reduce(int prod_num,int nt_num,int rhs_size){
        debug_message("# Reduce with prod #"+prod_num+" [NT="+nt_num+
                ", "+"SZ="+rhs_size+"]");
    }
    /**. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    public void debug_shift(Symbol shift_tkn){
        debug_message("# Shift under term #"+shift_tkn.sym+
                " to state #"+shift_tkn.parse_state);
    }

    public void debug_stack(){
        StringBuffer sb=new StringBuffer("## STACK:");
        for(int i=0;i<stack.size();i++){
            Symbol s=(Symbol)stack.elementAt(i);
            sb.append(" <state "+s.parse_state+", sym "+s.sym+">");
            if((i%3)==2||(i==(stack.size()-1))){
                debug_message(sb.toString());
                sb=new StringBuffer("         ");
            }
        }
    }

    public Symbol debug_parse()
            throws Exception{
        /** the current action code */
        int act;
        /** the Symbol/stack element returned by a reduce */
        Symbol lhs_sym=null;
        /** information about production being reduced with */
        short handle_size, lhs_sym_num;
        /** set up direct reference to tables to drive the parser */
        production_tab=production_table();
        action_tab=action_table();
        reduce_tab=reduce_table();
        debug_message("# Initializing parser");
        /** initialize the action encapsulation object */
        init_actions();
        /** do user initialization */
        user_init();
        /** the current Symbol */
        cur_token=scan();
        debug_message("# Current Symbol is #"+cur_token.sym);
        /** push dummy Symbol with start state to get us underway */
        stack.removeAllElements();
        stack.push(new Symbol(0,start_state()));
        tos=0;
        /** continue until we are told to stop */
        for(_done_parsing=false;!_done_parsing;){
            /** Check current token for freshness. */
            if(cur_token.used_by_parser)
                throw new Error("Symbol recycling detected (fix your scanner).");
            /** current state is always on the top of the stack */
            //debug_stack();
            /** look up action out of the current state with the current input */
            act=get_action(((Symbol)stack.peek()).parse_state,cur_token.sym);
            /** decode the action -- > 0 encodes shift */
            if(act>0){
                /** shift to the encoded state by pushing it on the stack */
                cur_token.parse_state=act-1;
                cur_token.used_by_parser=true;
                debug_shift(cur_token);
                stack.push(cur_token);
                tos++;
                /** advance to the next Symbol */
                cur_token=scan();
                debug_message("# Current token is "+cur_token);
            }
            /** if its less than zero, then it encodes a reduce action */
            else if(act<0){
                /** perform the action for the reduce */
                lhs_sym=do_action((-act)-1,this,stack,tos);
                /** look up information about the production */
                lhs_sym_num=production_tab[(-act)-1][0];
                handle_size=production_tab[(-act)-1][1];
                debug_reduce((-act)-1,lhs_sym_num,handle_size);
                /** pop the handle off the stack */
                for(int i=0;i<handle_size;i++){
                    stack.pop();
                    tos--;
                }
                /** look up the state to go to from the one popped back to */
                act=get_reduce(((Symbol)stack.peek()).parse_state,lhs_sym_num);
                debug_message("# Reduce rule: top state "+
                        ((Symbol)stack.peek()).parse_state+
                        ", lhs sym "+lhs_sym_num+" -> state "+act);
                /** shift to that state */
                lhs_sym.parse_state=act;
                lhs_sym.used_by_parser=true;
                stack.push(lhs_sym);
                tos++;
                debug_message("# Goto state #"+act);
            }
            /** finally if the entry is zero, we have an error */
            else if(act==0){
                /** call user syntax error reporting routine */
                syntax_error(cur_token);
                /** try to error recover */
                if(!error_recovery(true)){
                    /** if that fails give up with a fatal syntax error */
                    unrecovered_syntax_error(cur_token);
                    /** just in case that wasn't fatal enough, end parse */
                    done_parsing();
                }else{
                    lhs_sym=(Symbol)stack.peek();
                }
            }
        }
        return lhs_sym;
    }

    /** Error recovery code */
    protected boolean error_recovery(boolean debug)
            throws Exception{
        if(debug) debug_message("# Attempting error recovery");
        /** first pop the stack back into a state that can shift on error and
         do that shift (if that fails, we fail) */
        if(!find_recovery_config(debug)){
            if(debug) debug_message("# Error recovery fails");
            return false;
        }
        /** read ahead to create lookahead we can parse multiple times */
        read_lookahead();
        /** repeatedly try to parse forward until we make it the required dist */
        for(;;){
            /** try to parse forward, if it makes it, bail out of loop */
            if(debug) debug_message("# Trying to parse ahead");
            if(try_parse_ahead(debug)){
                break;
            }
            /** if we are now at EOF, we have failed */
            if(lookahead[0].sym==EOF_sym()){
                if(debug) debug_message("# Error recovery fails at EOF");
                return false;
            }
            /** otherwise, we consume another Symbol and try again */
            if(debug)
                debug_message("# Consuming Symbol #"+cur_err_token().sym);
            restart_lookahead();
        }
        /** we have consumed to a point where we can parse forward */
        if(debug) debug_message("# Parse-ahead ok, going back to normal parse");
        /** do the real parse (including actions) across the lookahead */
        parse_lookahead(debug);
        /** we have success */
        return true;
    }

    protected boolean shift_under_error(){
        /** is there a shift under error Symbol */
        return get_action(((Symbol)stack.peek()).parse_state,error_sym())>0;
    }

    protected boolean find_recovery_config(boolean debug){
        Symbol error_token;
        int act;
        if(debug) debug_message("# Finding recovery state on stack");
        /** Remember the right-position of the top symbol on the stack */
        int right_pos=((Symbol)stack.peek()).right;
        int left_pos=((Symbol)stack.peek()).left;
        /** pop down until we can shift under error Symbol */
        while(!shift_under_error()){
            /** pop the stack */
            if(debug)
                debug_message("# Pop stack by one, state was # "+
                        ((Symbol)stack.peek()).parse_state);
            left_pos=((Symbol)stack.pop()).left;
            tos--;
            /** if we have hit bottom, we fail */
            if(stack.empty()){
                if(debug) debug_message("# No recovery state found on stack");
                return false;
            }
        }
        /** state on top of the stack can shift under error, find the shift */
        act=get_action(((Symbol)stack.peek()).parse_state,error_sym());
        if(debug){
            debug_message("# Recover state found (#"+
                    ((Symbol)stack.peek()).parse_state+")");
            debug_message("# Shifting on error to state #"+(act-1));
        }
        /** build and shift a special error Symbol */
        error_token=new Symbol(error_sym(),left_pos,right_pos);
        error_token.parse_state=act-1;
        error_token.used_by_parser=true;
        stack.push(error_token);
        tos++;
        return true;
    }

    protected void read_lookahead() throws Exception{
        /** create the lookahead array */
        lookahead=new Symbol[error_sync_size()];
        /** fill in the array */
        for(int i=0;i<error_sync_size();i++){
            lookahead[i]=cur_token;
            cur_token=scan();
        }
        /** start at the beginning */
        lookahead_pos=0;
    }

    protected Symbol cur_err_token(){
        return lookahead[lookahead_pos];
    }

    protected boolean advance_lookahead(){
        /** advance the input location */
        lookahead_pos++;
        /** return true if we didn't go off the end */
        return lookahead_pos<error_sync_size();
    }

    protected void restart_lookahead() throws Exception{
        /** move all the existing input over */
        for(int i=1;i<error_sync_size();i++)
            lookahead[i-1]=lookahead[i];
        /** read a new Symbol into the last spot */
        cur_token=scan();
        lookahead[error_sync_size()-1]=cur_token;
        /** reset our internal position marker */
        lookahead_pos=0;
    }

    protected boolean try_parse_ahead(boolean debug)
            throws Exception{
        int act;
        short lhs, rhs_size;
        /** create a virtual stack from the real parse stack */
        virtual_parse_stack vstack=new virtual_parse_stack(stack);
        /** parse until we fail or get past the lookahead input */
        for(;;){
            /** look up the action from the current state (on top of stack) */
            act=get_action(vstack.top(),cur_err_token().sym);
            /** if its an error, we fail */
            if(act==0) return false;
            /** > 0 encodes a shift */
            if(act>0){
                /** push the new state on the stack */
                vstack.push(act-1);
                if(debug) debug_message("# Parse-ahead shifts Symbol #"+
                        cur_err_token().sym+" into state #"+(act-1));
                /** advance simulated input, if we run off the end, we are done */
                if(!advance_lookahead()) return true;
            }
            /** < 0 encodes a reduce */
            else{
                /** if this is a reduce with the start production we are done */
                if((-act)-1==start_production()){
                    if(debug) debug_message("# Parse-ahead accepts");
                    return true;
                }
                /** get the lhs Symbol and the rhs size */
                lhs=production_tab[(-act)-1][0];
                rhs_size=production_tab[(-act)-1][1];
                /** pop handle off the stack */
                for(int i=0;i<rhs_size;i++)
                    vstack.pop();
                if(debug)
                    debug_message("# Parse-ahead reduces: handle size = "+
                            rhs_size+" lhs = #"+lhs+" from state #"+vstack.top());
                /** look up goto and push it onto the stack */
                vstack.push(get_reduce(vstack.top(),lhs));
                if(debug)
                    debug_message("# Goto state #"+vstack.top());
            }
        }
    }

    protected void parse_lookahead(boolean debug)
            throws Exception{
        /** the current action code */
        int act;
        /** the Symbol/stack element returned by a reduce */
        Symbol lhs_sym=null;
        /** information about production being reduced with */
        short handle_size, lhs_sym_num;
        /** restart the saved input at the beginning */
        lookahead_pos=0;
        if(debug){
            debug_message("# Reparsing saved input with actions");
            debug_message("# Current Symbol is #"+cur_err_token().sym);
            debug_message("# Current state is #"+
                    ((Symbol)stack.peek()).parse_state);
        }
        /** continue until we accept or have read all lookahead input */
        while(!_done_parsing){
            /** current state is always on the top of the stack */
            /** look up action out of the current state with the current input */
            act=
                    get_action(((Symbol)stack.peek()).parse_state,cur_err_token().sym);
            /** decode the action -- > 0 encodes shift */
            if(act>0){
                /** shift to the encoded state by pushing it on the stack */
                cur_err_token().parse_state=act-1;
                cur_err_token().used_by_parser=true;
                if(debug) debug_shift(cur_err_token());
                stack.push(cur_err_token());
                tos++;
                /** advance to the next Symbol, if there is none, we are done */
                if(!advance_lookahead()){
                    if(debug) debug_message("# Completed reparse");
                    /** scan next Symbol so we can continue parse */
                    // BUGFIX by Chris Harris <ckharris@ucsd.edu>:
                    //   correct a one-off error by commenting out
                    //   this next line.
                    /**cur_token = scan();*/
                    /** go back to normal parser */
                    return;
                }
                if(debug)
                    debug_message("# Current Symbol is #"+cur_err_token().sym);
            }
            /** if its less than zero, then it encodes a reduce action */
            else if(act<0){
                /** perform the action for the reduce */
                lhs_sym=do_action((-act)-1,this,stack,tos);
                /** look up information about the production */
                lhs_sym_num=production_tab[(-act)-1][0];
                handle_size=production_tab[(-act)-1][1];
                if(debug) debug_reduce((-act)-1,lhs_sym_num,handle_size);
                /** pop the handle off the stack */
                for(int i=0;i<handle_size;i++){
                    stack.pop();
                    tos--;
                }
                /** look up the state to go to from the one popped back to */
                act=get_reduce(((Symbol)stack.peek()).parse_state,lhs_sym_num);
                /** shift to that state */
                lhs_sym.parse_state=act;
                lhs_sym.used_by_parser=true;
                stack.push(lhs_sym);
                tos++;
                if(debug) debug_message("# Goto state #"+act);
            }
            /** finally if the entry is zero, we have an error
             (shouldn't happen here, but...)*/
            else if(act==0){
                report_fatal_error("Syntax error",lhs_sym);
                return;
            }
        }
    }
}
