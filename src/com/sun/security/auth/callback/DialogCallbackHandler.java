/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth.callback;
/** JAAS imports */

import javax.security.auth.callback.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@jdk.Exported(false)
@Deprecated
public class DialogCallbackHandler implements CallbackHandler{
    private static final int JPasswordFieldLen=8;
    private static final int JTextFieldLen=8;
    /** -- Fields -- */
    private Component parentComponent;

    public DialogCallbackHandler(){
    }

    public DialogCallbackHandler(Component parentComponent){
        this.parentComponent=parentComponent;
    }

    public void handle(Callback[] callbacks)
            throws UnsupportedCallbackException{
        /** Collect messages to display in the dialog */
        final List<Object> messages=new ArrayList<>(3);
        /** Collection actions to perform if the user clicks OK */
        final List<Action> okActions=new ArrayList<>(2);
        ConfirmationInfo confirmation=new ConfirmationInfo();
        for(int i=0;i<callbacks.length;i++){
            if(callbacks[i] instanceof TextOutputCallback){
                TextOutputCallback tc=(TextOutputCallback)callbacks[i];
                switch(tc.getMessageType()){
                    case TextOutputCallback.INFORMATION:
                        confirmation.messageType=JOptionPane.INFORMATION_MESSAGE;
                        break;
                    case TextOutputCallback.WARNING:
                        confirmation.messageType=JOptionPane.WARNING_MESSAGE;
                        break;
                    case TextOutputCallback.ERROR:
                        confirmation.messageType=JOptionPane.ERROR_MESSAGE;
                        break;
                    default:
                        throw new UnsupportedCallbackException(
                                callbacks[i],"Unrecognized message type");
                }
                messages.add(tc.getMessage());
            }else if(callbacks[i] instanceof NameCallback){
                final NameCallback nc=(NameCallback)callbacks[i];
                JLabel prompt=new JLabel(nc.getPrompt());
                final JTextField name=new JTextField(JTextFieldLen);
                String defaultName=nc.getDefaultName();
                if(defaultName!=null){
                    name.setText(defaultName);
                }
                /**
                 * Put the prompt and name in a horizontal box,
                 * and add that to the set of messages.
                 */
                Box namePanel=Box.createHorizontalBox();
                namePanel.add(prompt);
                namePanel.add(name);
                messages.add(namePanel);
                /** Store the name back into the callback if OK */
                okActions.add(new Action(){
                    public void perform(){
                        nc.setName(name.getText());
                    }
                });
            }else if(callbacks[i] instanceof PasswordCallback){
                final PasswordCallback pc=(PasswordCallback)callbacks[i];
                JLabel prompt=new JLabel(pc.getPrompt());
                final JPasswordField password=
                        new JPasswordField(JPasswordFieldLen);
                if(!pc.isEchoOn()){
                    password.setEchoChar('*');
                }
                Box passwordPanel=Box.createHorizontalBox();
                passwordPanel.add(prompt);
                passwordPanel.add(password);
                messages.add(passwordPanel);
                okActions.add(new Action(){
                    public void perform(){
                        pc.setPassword(password.getPassword());
                    }
                });
            }else if(callbacks[i] instanceof ConfirmationCallback){
                ConfirmationCallback cc=(ConfirmationCallback)callbacks[i];
                confirmation.setCallback(cc);
                if(cc.getPrompt()!=null){
                    messages.add(cc.getPrompt());
                }
            }else{
                throw new UnsupportedCallbackException(
                        callbacks[i],"Unrecognized Callback");
            }
        }
        /** Display the dialog */
        int result=JOptionPane.showOptionDialog(
                parentComponent,
                messages.toArray(),
                "Confirmation",                     /** title */
                confirmation.optionType,
                confirmation.messageType,
                null,                               /** icon */
                confirmation.options,               /** options */
                confirmation.initialValue);         /** initialValue */
        /** Perform the OK actions */
        if(result==JOptionPane.OK_OPTION
                ||result==JOptionPane.YES_OPTION){
            Iterator<Action> iterator=okActions.iterator();
            while(iterator.hasNext()){
                iterator.next().perform();
            }
        }
        confirmation.handleResult(result);
    }

    private static interface Action{
        void perform();
    }

    private static class ConfirmationInfo{
        int optionType=JOptionPane.OK_CANCEL_OPTION;
        Object[] options=null;
        Object initialValue=null;
        int messageType=JOptionPane.QUESTION_MESSAGE;
        private int[] translations;
        private ConfirmationCallback callback;

        void setCallback(ConfirmationCallback callback)
                throws UnsupportedCallbackException{
            this.callback=callback;
            int confirmationOptionType=callback.getOptionType();
            switch(confirmationOptionType){
                case ConfirmationCallback.YES_NO_OPTION:
                    optionType=JOptionPane.YES_NO_OPTION;
                    translations=new int[]{
                            JOptionPane.YES_OPTION,ConfirmationCallback.YES,
                            JOptionPane.NO_OPTION,ConfirmationCallback.NO,
                            JOptionPane.CLOSED_OPTION,ConfirmationCallback.NO
                    };
                    break;
                case ConfirmationCallback.YES_NO_CANCEL_OPTION:
                    optionType=JOptionPane.YES_NO_CANCEL_OPTION;
                    translations=new int[]{
                            JOptionPane.YES_OPTION,ConfirmationCallback.YES,
                            JOptionPane.NO_OPTION,ConfirmationCallback.NO,
                            JOptionPane.CANCEL_OPTION,ConfirmationCallback.CANCEL,
                            JOptionPane.CLOSED_OPTION,ConfirmationCallback.CANCEL
                    };
                    break;
                case ConfirmationCallback.OK_CANCEL_OPTION:
                    optionType=JOptionPane.OK_CANCEL_OPTION;
                    translations=new int[]{
                            JOptionPane.OK_OPTION,ConfirmationCallback.OK,
                            JOptionPane.CANCEL_OPTION,ConfirmationCallback.CANCEL,
                            JOptionPane.CLOSED_OPTION,ConfirmationCallback.CANCEL
                    };
                    break;
                case ConfirmationCallback.UNSPECIFIED_OPTION:
                    options=callback.getOptions();
                    /**
                     * There's no way to know if the default option means
                     * to cancel the login, but there isn't a better way
                     * to guess this.
                     */
                    translations=new int[]{
                            JOptionPane.CLOSED_OPTION,callback.getDefaultOption()
                    };
                    break;
                default:
                    throw new UnsupportedCallbackException(
                            callback,
                            "Unrecognized option type: "+confirmationOptionType);
            }
            int confirmationMessageType=callback.getMessageType();
            switch(confirmationMessageType){
                case ConfirmationCallback.WARNING:
                    messageType=JOptionPane.WARNING_MESSAGE;
                    break;
                case ConfirmationCallback.ERROR:
                    messageType=JOptionPane.ERROR_MESSAGE;
                    break;
                case ConfirmationCallback.INFORMATION:
                    messageType=JOptionPane.INFORMATION_MESSAGE;
                    break;
                default:
                    throw new UnsupportedCallbackException(
                            callback,
                            "Unrecognized message type: "+confirmationMessageType);
            }
        }

        void handleResult(int result){
            if(callback==null){
                return;
            }
            for(int i=0;i<translations.length;i+=2){
                if(translations[i]==result){
                    result=translations[i+1];
                    break;
                }
            }
            callback.setSelectedIndex(result);
        }
    }
}
