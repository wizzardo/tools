package com.wizzardo.tools.security;

public class SHA256 extends Hash {
    public SHA256() {
        super("SHA-256");
    }

    @Override
    protected int hexStringLength() {
        return 64;
    }

    public static SHA256 create() {
        return new SHA256();
    }
}
