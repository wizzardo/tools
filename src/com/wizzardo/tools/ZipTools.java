package com.wizzardo.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipTools {

    public static class ZipBuilder {
        private List<ZipBuilderEntry> entries = new ArrayList<ZipBuilderEntry>();

        public ZipBuilder append(String name, byte[] bytes) {
            entries.add(new BytesEntry(name, bytes));
            return this;
        }

        public ZipBuilder append(File f) {
            entries.add(new FileEntry(f));
            return this;
        }

        public void zip(OutputStream out) throws IOException {
            ZipOutputStream zipout = new ZipOutputStream(out);
            for (ZipBuilderEntry entry : entries) {
                entry.write(zipout);
            }
            zipout.close();
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
                FileInputStream in = new FileInputStream(file);
                ZipEntry entry = new ZipEntry(file.getName());
                entry.setMethod(ZipEntry.DEFLATED);
                out.putNextEntry(entry);
                int r;
                byte[] b = new byte[10240];
                while ((r = in.read(b)) != -1) {
                    out.write(b, 0, r);
                }
                in.close();
            } else {
                zip(out, file);
            }
        }
    }

    public static void unzip(File zipFile, File outDir) {
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            byte[] b = new byte[10240];
            int r;
            while ((entry = zip.getNextEntry()) != null) {
                File outFile = new File(outDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(outFile);
                    while ((r = zip.read(b)) != -1) {
                        out.write(b, 0, r);
                    }
                } catch (IOException ex) {
                    throw new WrappedException(ex);
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new WrappedException(ex);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public static boolean isZip(File f) {
        FileInputStream in = null;

        try {
            in = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            throw new WrappedException(e);
        }

        byte[] b = new byte[2];
        try {
            in.read(b);
        } catch (IOException ex) {
            throw new WrappedException(ex);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
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
            zipout.close();
        } catch (IOException ex) {
            throw new WrappedException(ex);
        } finally {
            if (zipout != null) {
                try {
                    zipout.close();
                } catch (IOException ignore) {
                }
            }
        }
        return zip;
    }

    public static void zip(ZipOutputStream out, File toZip) {
        File startDir = toZip.getParentFile();
        LinkedList<File> files = new LinkedList<File>();
        getFiles(files, toZip);
        zipping(out, files, startDir);
    }

    public static List<File> getFiles(List<File> files, File f) {
        if (f == null)
            return files;

        if (!f.isDirectory()) {
            files.add(f);
        } else {
            for (File file : f.listFiles()) {
                getFiles(files, file);
            }
        }
        return files;
    }

    public static void zipping(ZipOutputStream zipout, List<File> files, File startDir) {
        FileInputStream in = null;
        for (int i = 1; i <= files.size(); i++) {
            try {
                File f = files.get(i - 1);
                in = new FileInputStream(f);
                ZipEntry entry = new ZipEntry(f.getAbsolutePath().substring(startDir.getAbsolutePath().length() + 1));
                entry.setMethod(ZipEntry.DEFLATED);
                zipout.putNextEntry(entry);
                int r;
                byte[] b = new byte[1048576];
                while ((r = in.read(b)) != -1) {
                    zipout.write(b, 0, r);
                }
            } catch (IOException ex) {
                throw new WrappedException(ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }
}
