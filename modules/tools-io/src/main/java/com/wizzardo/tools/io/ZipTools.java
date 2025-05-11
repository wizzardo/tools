package com.wizzardo.tools.io;

import com.wizzardo.tools.misc.Unchecked;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipTools {

    public static class ZipWriter implements Closeable {
        protected ZipOutputStream out;

        public ZipWriter(String file) throws IOException {
            this(new FileOutputStream(file));
        }

        public ZipWriter(File file) throws IOException {
            this(new FileOutputStream(file));
        }

        public ZipWriter(OutputStream outputStream) throws IOException {
            out = new ZipOutputStream(outputStream);
        }

        public ZipWriter setLevel(int level) {
            out.setLevel(level);
            return this;
        }

        public ZipWriter setMethod(int method) {
            out.setMethod(method);
            return this;
        }

        public ZipWriter write(ZipBuilderEntry entry) throws IOException {
            out.closeEntry();
            entry.write(out);
            return this;
        }

        public ZipWriter flush() throws IOException {
            out.flush();
            return this;
        }

        public void close() throws IOException {
            out.close();
        }
    }

    public static class ZipBuilder {
        private List<ZipBuilderEntry> entries = new ArrayList<ZipBuilderEntry>();
        private int level = -1;
        private int method = ZipOutputStream.DEFLATED;

        public ZipBuilder append(String name, byte[] bytes) {
            return append(new BytesEntry(name, bytes));
        }

        public ZipBuilder append(File f) {
            return append(new FileEntry(f));
        }


        public ZipBuilder append(String name, InputStream in) {
            return append(new StreamEntry(name, in));
        }

        public ZipBuilder append(ZipBuilderEntry entry) {
            entries.add(entry);
            return this;
        }

        public void zip(File out) throws IOException {
            zip(new FileOutputStream(out));
        }

        public ZipBuilder level(int level) {
            if (level < 0 || level > 9)
                throw new IllegalArgumentException("compression level must be 0-9");
            this.level = level;
            return this;
        }

        public ZipBuilder method(int method) {
            if (method != ZipOutputStream.DEFLATED && method != ZipOutputStream.STORED)
                throw new IllegalArgumentException("compression method must be on of [DEFLATED, STORED]");
            this.method = method;
            return this;
        }

        public void zip(OutputStream out) throws IOException {
            ZipWriter writer = new ZipWriter(out)
                    .setLevel(level)
                    .setMethod(method);
            try {
                for (ZipBuilderEntry entry : entries) {
                    writer.write(entry);
                }
            } finally {
                IOTools.close(writer);
            }
        }
    }

    public interface ZipBuilderEntry {
        void write(ZipOutputStream out) throws IOException;
    }

    public static class BytesEntry implements ZipBuilderEntry {
        private String name;
        private byte[] bytes;
        private int offset;
        private int length;

        public BytesEntry(String name, byte[] bytes) {
            this(name, bytes, 0, bytes.length);
        }

        public BytesEntry(String name, byte[] bytes, int offset, int length) {
            this.name = name;
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public void write(ZipOutputStream out) throws IOException {
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.DEFLATED);
            out.putNextEntry(entry);
            out.write(bytes, offset, length);
        }
    }

    public static class StreamEntry implements ZipBuilderEntry {
        private String name;
        private InputStream stream;

        public StreamEntry(String name, InputStream stream) {
            this.name = name;
            this.stream = stream;
        }

        @Override
        public void write(ZipOutputStream out) throws IOException {
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.DEFLATED);
            out.putNextEntry(entry);
            IOTools.copy(stream, out);
        }
    }

    public static class FileEntry implements ZipBuilderEntry {
        private File file;

        public FileEntry(String file) {
            this(new File(file));
        }

        public FileEntry(File file) {
            this.file = file;
        }

        @Override
        public void write(ZipOutputStream out) throws IOException {
            if (file.isFile()) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    ZipEntry entry = new ZipEntry(file.getName());
                    entry.setMethod(ZipEntry.DEFLATED);
                    out.putNextEntry(entry);
                    IOTools.copy(in, out);
                } finally {
                    IOTools.close(in);
                }
            } else {
                zip(out, file);
            }
        }
    }

    public interface ZipEntryFilter {

        boolean accept(ZipEntry entry);
    }

    public static List<File> unzip(File zipFile, File outDir) {
        return unzip(zipFile, outDir, null);
    }

    public static List<File> unzip(File zipFile, final File outDir, ZipEntryFilter filter) {
        final List<File> l = new ArrayList<File>();
        String outDirCanonicalPath = Unchecked.call(outDir::getCanonicalPath);
        unzip(zipFile, new ZipEntryConsumer() {
            byte[] b = new byte[1024 * 50];

            @Override
            public void consume(String name, InputStream in) {
                File outFile = new File(outDir, name);

                FileOutputStream out = null;
                try {
                    String canonicalDestinationPath = outFile.getCanonicalPath();
                    if (!canonicalDestinationPath.startsWith(outDirCanonicalPath)) {
                        throw new IllegalStateException("Zip entry '" + name + "' tries to escape to another directory");
                    }

                    outFile.getParentFile().mkdirs();

                    out = new FileOutputStream(outFile);
                    IOTools.copy(in, out, b);
                    l.add(outFile);
                } catch (IOException ex) {
                    outFile.delete();
                    throw Unchecked.rethrow(ex);
                } finally {
                    IOTools.close(out);
                }
            }
        }, filter);
        return l;
    }

    public interface ZipEntryConsumer {
        void consume(String name, InputStream in);
    }

    public static void unzip(File zipFile, ZipEntryConsumer consumer) {
        unzip(zipFile, consumer, null);
    }

    public static void unzip(File zipFile, ZipEntryConsumer consumer, ZipEntryFilter filter) {
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory())
                    continue;

                if (filter != null && !filter.accept(entry))
                    continue;

                consumer.consume(entry.getName(), zip);
            }
        } catch (IOException ex) {
            throw Unchecked.rethrow(ex);
        } finally {
            IOTools.close(zip);
        }
    }

    public static boolean isZip(File f) {
        FileInputStream in = null;

        try {
            in = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            throw Unchecked.rethrow(e);
        }

        byte[] b = new byte[2];
        try {
            in.read(b);
        } catch (IOException ex) {
            throw Unchecked.rethrow(ex);
        } finally {
            IOTools.close(in);
        }
        return isZip(b);
    }

    public static boolean isZip(byte[] bytes) {
        return bytes != null && bytes.length >= 2 && bytes[0] == 80 && bytes[1] == 75;
    }

    public static File zip(String file) {
        return zip(new File(file));
    }

    public static File zip(File toZip) {
        ZipOutputStream zipout = null;
        File zip = new File(toZip.getAbsolutePath() + ".zip");
        try {
            zipout = new ZipOutputStream(new FileOutputStream(zip));
            zip(zipout, toZip);
        } catch (IOException ex) {
            throw Unchecked.rethrow(ex);
        } finally {
            IOTools.close(zipout);
        }
        return zip;
    }

    public static void zip(ZipOutputStream out, File toZip) {
        File startDir = toZip.getParentFile();
        zipping(out, FileTools.listRecursive(toZip), startDir);
    }

    public static void zipping(ZipOutputStream zipout, List<File> files, File startDir) {
        FileInputStream in = null;
        byte[] b = new byte[50 * 1024];
        for (int i = 1; i <= files.size(); i++) {
            try {
                File f = files.get(i - 1);
                in = new FileInputStream(f);
                ZipEntry entry = new ZipEntry(f.getAbsolutePath().substring(startDir.getAbsolutePath().length() + 1));
                entry.setMethod(ZipEntry.DEFLATED);
                zipout.putNextEntry(entry);
                IOTools.copy(in, zipout, b);
            } catch (IOException ex) {
                throw Unchecked.rethrow(ex);
            } finally {
                IOTools.close(in);
            }
        }
    }
}
