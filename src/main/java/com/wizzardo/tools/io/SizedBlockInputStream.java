package com.wizzardo.tools.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Moxa
 */
public class SizedBlockInputStream extends InputStream {

    protected InputStream in;
    protected long blockLength = 0;
    protected long readed = 0;
    protected ProgressListener listener;
    protected BlockSizeType sizeType;

    public SizedBlockInputStream(InputStream in) {
        this(in, BlockSizeType.LONG);
    }

    public SizedBlockInputStream(InputStream in, BlockSizeType sizeType) {
        this.in = in;
        this.sizeType = sizeType;
    }

    public boolean hasNext() throws IOException {
        if (readed != blockLength) {
            throw new IllegalStateException("not all data has been read. " + readed + " != " + blockLength);
        }
        int r = 0, t = 0;
        byte[] b = new byte[sizeType.bytesCount];
        while (t != sizeType.bytesCount && (r = in.read(b, t, sizeType.bytesCount - t)) != -1) {
            t += r;
        }
        if (t == sizeType.bytesCount) {
            blockLength = BytesTools.toNumber(b, 0, sizeType.bytesCount);
            readed = 0;
            return true;
        }
        return false;
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

    public long lenght() {
        return blockLength;
    }

    public int read(byte[] b, int offset, int l) throws IOException {
        if (blockLength == readed) {
            return -1;
        }
        if (blockLength - readed < l) {
            l = (int) (blockLength - readed);
        }
        int r = in.read(b, offset, l);
        if (r == -1) {
            return -1;
        }
        readed += r;

        if (listener != null)
            listener.setProgress((int) (readed * 100 / blockLength));
        return r;
    }

    @Override
    public int read() throws IOException {
        if (readed == blockLength) {
            return -1;
        }

        byte[] bytes = new byte[1];
        int r = read(bytes);
        if (r == 0)
            return -1;

        return bytes[0] & 0xff;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public void close() throws IOException {
        in.close();
    }
}
