package com.wizzardo.tools.bytecode;

import com.wizzardo.tools.bytecode.fields.LongFieldSetter;

public class PrivateFieldSetter implements LongFieldSetter<PrivateFieldHolder> {

    public void set(PrivateFieldHolder holder, long i) {
        holder.l = i;
    }
}
