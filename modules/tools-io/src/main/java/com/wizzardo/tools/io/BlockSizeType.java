package com.wizzardo.tools.io;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public enum BlockSizeType {
    BYTE(1), SHORT(2), INTEGER(4), LONG(8);

    public final int bytesCount;

    private BlockSizeType(int bytesCount) {
        this.bytesCount = bytesCount;
    }
}
