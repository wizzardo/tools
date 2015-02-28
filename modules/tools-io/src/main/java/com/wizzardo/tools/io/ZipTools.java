package com.wizzardo.tools.io;

import com.wizzardo.tools.misc.UncheckedThrow;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipTools {

    public static class ZipBuilder {
        private List<ZipBuilderEntry> entries = new ArrayList<ZipBuilderEntry>();
        private int level = -1;
        private int method = ZipOutputStream.DEFLATED;

        public ZipBuilder append(String name, byte[] bytes) {
            entries.add(new BytesEntry(name, bytes));
            return this;
        }

        public ZipBuilder append(File f) {
            entries.add(new FileEntry(f));
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
            ZipOutputStream zipout = new ZipOutputStream(out);
            zipout.setMethod(method);
            zipout.setLevel(level);
            try {
                for (ZipBuilderEntry entry : entries) {
                    entry.write(zipout);
                }
            } finally {
                IOTools.close(zipout);
            }
        }
    }

    private static interface ZipBuilderEntry {
        public void write(ZipOutputStream out) throws IOException;
    }

    private static class BytesEntry implements ZipBuilderEntry {
        private String name;
        private byte[] bytes;

        private BytesEntry(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        @Override
        public void write(ZipOutputStream out) throws IOException {
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.DEFLATED);
            out.putNextEntry(entry);
            out.write(bytes);
        }
    }

    private static class FileEntry implements ZipBuilderEntry {
        private File file;

        private FileEntry(File file) {
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

    public static interface ZipEntryFilter {

        boolean accept(ZipEntry entry);
    }

    public static List<File> unzip(File zipFile, File outDir) {
        return unzip(zipFile, outDir, null);
    }

    public static List<File> unzip(File zipFile, File outDir, ZipEntryFilter filter) {
        ZipInputStream zip = null;
        outDir.mkdirs();
        List<File> l = new ArrayList<File>();
        try {
            zip = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            byte[] b = new byte[1024 * 50];
            while ((entry = zip.getNextEntry()) != null) {
                File outFile = new File(outDir, entry.getName());
                if (entry.isDirectory())
                    continue;

                if (filter != null && !filter.accept(entry))
                    continue;

                outFile.getParentFile().mkdirs();

                FileOutputStream out = new FileOutputStream(outFile);
                try {
                    IOTools.copy(zip, out, b);
                    l.add(outFile);
                } catch (IOException ex) {
                    outFile.delete();
                    throw UncheckedThrow.rethrow(ex);
                } finally {
                    IOTools.close(out);
                }
            }
        } catch (IOException ex) {
            throw UncheckedThrow.rethrow(ex);
        } finally {
            IOTools.close(zip);
        }
        return l;
    }

    public static boolean isZip(File f) {
        FileInputStream in = null;

        try {
            in = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            throw UncheckedThrow.rethrow(e);
        }

        byte[] b = new byte[2];
        try {
            in.read(b);
        } catch (IOException ex) {
            throw UncheckedThrow.rethrow(ex);
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
            throw UncheckedThrow.rethrow(ex);
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
                throw UncheckedThrow.rethrow(ex);
            } finally {
                IOTools.close(in);
            }
        }
    }
}
