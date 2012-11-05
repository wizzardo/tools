package org.bordl.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

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
                    Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ex) {
                            Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException ex) {
                    Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static boolean isZip(File f) {
        FileInputStream in = null;
        byte[] b = new byte[2];
        try {
            in = new FileInputStream(f);
            in.read(b);
        } catch (IOException ex) {
            Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return b[0] == 80 && b[1] == 75;
    }

    public static void zip(String file) {
        zip(new File(file));
    }

    public static void zip(File toZip) {
        ZipOutputStream zipout = null;
        try {
            File startDir = toZip.getParentFile();
            zipout = new ZipOutputStream(new FileOutputStream(toZip.getAbsolutePath() + ".zip"));
            LinkedList<File> files = new LinkedList<File>();
            getFiles(files, toZip);
            zipping(zipout, files, startDir);
            zipout.close();
        } catch (IOException ex) {
            Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (zipout != null) {
                try {
                    zipout.close();
                } catch (IOException ex) {
                    Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static List<File> getFiles(List<File> files, File f) {
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
                System.out.println(i + "/" + files.size() + " " + entry);
                entry.setMethod(ZipEntry.DEFLATED);
                zipout.putNextEntry(entry);
                int r;
                byte[] b = new byte[1048576];
                while ((r = in.read(b)) != -1) {
                    zipout.write(b, 0, r);
                }
            } catch (IOException ex) {
                Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }
}
