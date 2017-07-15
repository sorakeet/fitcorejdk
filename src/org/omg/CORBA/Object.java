/**
 * Copyright (c) 1995, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public interface Object{
    boolean _is_a(String repositoryIdentifier);

    boolean _is_equivalent(Object other);

    boolean _non_existent();

    int _hash(int maximum);

    Object _duplicate();

    void _release();

    Object _get_interface_def();

    Request _request(String operation);

    Request _create_request(Context ctx,
                            String operation,
                            NVList arg_list,
                            NamedValue result);

    Request _create_request(Context ctx,
                            String operation,
                            NVList arg_list,
                            NamedValue result,
                            ExceptionList exclist,
                            ContextList ctxlist);

    Policy _get_policy(int policy_type);

    DomainManager[] _get_domain_managers();

    Object _set_policy_override(Policy[] policies,
                                SetOverrideType set_add);
}
