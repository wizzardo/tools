package com.wizzardo.tools.bytecode;

public class PrivateFieldHolder {
   static long l;
//    int i;
//    byte b;
//    short s;
//    float f;
//    double d;
//    char c;
//    boolean z;

    public PrivateFieldHolder(long l) {
        this.l = l;
    }

    public long getL() {
        return l;
    }

    public void setL(long l) {
        this.l = l;
    }
}
