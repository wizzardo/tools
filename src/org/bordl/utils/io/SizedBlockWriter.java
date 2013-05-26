package org.bordl.utils.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Moxa
 */
public class SizedBlockWriter {

    private OutputStream out;
    private long blockLength = 0;
    private long written = 0;

    public SizedBlockWriter(OutputStream out) {
        this.out = out;
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
        BytesUtils.toBytes(blockLength, out);
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
