package com.wizzardo.tools.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Moxa
 */
public class SizedBlockOutputStream extends OutputStream {

    protected OutputStream out;
    protected long blockLength = 0;
    protected long written = 0;
    protected BlockSizeType sizeType;

    public SizedBlockOutputStream(OutputStream out) {
        this(out, BlockSizeType.LONG);
    }

    public SizedBlockOutputStream(OutputStream out, BlockSizeType sizeType) {
        this.out = out;
        this.sizeType = sizeType;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b});
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int offset, int l) throws IOException {
        if (l + written > blockLength) {
            throw new IllegalStateException("you are trying to write more bytes than was declared. " + (l + written) + " > " + blockLength);
        }
        out.write(b, offset, l);
        written += l;
    }

    public void setBlockLength(long l) throws IOException {
        if (written != blockLength) {
            throw new IllegalStateException("not all data was written. " + written + " != " + blockLength);
        }
        blockLength = l;
        written = 0;
        BytesTools.toBytes(blockLength, out, sizeType.bytesCount);
    }

    public long left() {
        return blockLength - written;
    }

    public void close() throws IOException {
        out.close();
    }

    public void flush() throws IOException {
        out.flush();
    }
}
