package com.wizzardo.tools.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class JpegDecoder {

    static final int[] SCAN_ZIGZAG = {
            0, 1, 8, 16, 9, 2, 3, 10,
            17, 24, 32, 25, 18, 11, 4, 5,
            12, 19, 26, 33, 40, 48, 41, 34,
            27, 20, 13, 6, 7, 14, 21, 28,
            35, 42, 49, 56, 57, 50, 43, 36,
            29, 22, 15, 23, 30, 37, 44, 51,
            58, 59, 52, 45, 38, 31, 39, 46,
            53, 60, 61, 54, 47, 55, 62, 63
    };

    protected int bitsPerPixel;
    protected int height;
    protected int width;
    protected int numberOfComponents;
    protected int currentScan;
    protected int restartInterval;
    protected ComponentInfo[] components;
    protected float[][] quantizationTables;
    protected JFIF jfif;
    protected HuffmanTable[] huffmanTables;
    protected boolean progressive = false;
    protected int[] tmp = new int[64];
    protected float[] tmpColor = new float[3];
    protected float[] tmpCb = new float[256];
    protected float[] tmpCr = new float[256];

    public IccProfileParser.ColorTransform colorTransform;

    static class DecodedData {
        protected final float[] data = new float[64];

        public float get(int x, int y) {
            return data[y * 8 + x];
        }
    }
    public static class ComponentInfo {
        public final Component component;
        public final int quantizationTable;
        public final int samplingFactorVertical;
        public final int samplingFactorHorizontal;

        ComponentInfo(Component component, byte samplingFactor, int quantizationTable) {
            this.component = component;
            this.quantizationTable = quantizationTable;
            this.samplingFactorHorizontal = samplingFactor >> 4;
            this.samplingFactorVertical = samplingFactor & 0x0F;
        }

        @Override
        public String toString() {
            return component +
                    "[v=" + samplingFactorVertical +
                    ", h=" + samplingFactorHorizontal +
                    ']';
        }
    }

    public enum Component {
        Y(1),
        Cb(2),
        Cr(3),
        I(4),
        Q(5),
        ;

        public final int value;

        Component(int i) {
            value = i;
        }

        static Component valueOf(int value) {
            if (value == 1)
                return Y;
            if (value == 2)
                return Cb;
            if (value == 3)
                return Cr;
            if (value == 4)
                return I;
            if (value == 5)
                return Q;

            throw new IllegalArgumentException("Unknown component: " + value);
        }
    }

    public static class JFIF {
        public int majorVersion;
        public int minorVersion;
        public DensityUnit densityUnit;
        public int densityX;
        public int densityY;
    }

    public enum DensityUnit {
        NO_UNIT(0),
        PIXELS_PER_INCH(1),
        PIXELS_PER_CENTIMETER(2),
        ;

        public final int value;

        DensityUnit(int i) {
            value = i;
        }

        public static DensityUnit valueOf(int value) {
            if (value == 1)
                return PIXELS_PER_INCH;
            if (value == 2)
                return PIXELS_PER_CENTIMETER;
            if (value == 0)
                return NO_UNIT;

            return NO_UNIT;
        }
    }

    public enum JFXXThumbnailFormat {
        JPEG(10),
        PALETTIZED(11),
        RGB(13),
        ;

        public final int value;

        JFXXThumbnailFormat(int i) {
            value = i;
        }

        public static JFXXThumbnailFormat valueOf(int value) {
            if (value == 10)
                return JPEG;
            if (value == 11)
                return PALETTIZED;
            if (value == 13)
                return RGB;

            throw new IllegalArgumentException("Unknown format: " + value);
        }
    }

    public enum Marker {
        SOI((byte) 0xD8),
        SOS((byte) 0xDA),
        DRI((byte) 0xDD),
        APP0((byte) 0xE0),
        APP1((byte) 0xE1),
        APP2((byte) 0xE2),
        APP3((byte) 0xE3),
        APP4((byte) 0xE4),
        APP13((byte) 0xED),
        APP14((byte) 0xEE),
        SOF0((byte) 0xC0),
        SOF2((byte) 0xC2),
        DHT((byte) 0xC4),
        COM((byte) 0xFE),
        DQT((byte) 0xDB),
        EOI((byte) 0xD9),
        ;

        public final byte value;

        static final Marker[] markers = new Marker[256];

        static {
            for (Marker marker : Marker.values()) {
                markers[marker.value & 0xff] = marker;
            }
        }

        Marker(byte value) {
            this.value = value;
        }

        public static Marker valueOf(int value) {
            if ((value >> 8) != 0xFF)
                throw new IllegalArgumentException("Marker should start with 0xFF");

            Marker marker = markers[value & 0xFF];
            if (marker == null)
                System.out.println("Unknown marker: " + Integer.toHexString(value) + "(" + (value & 0xFF) + ")");

            return marker;
        }
    }

    static class MCUsHolder {
        float[] curr;
        float[] prev;
        float[] next;
        int index;
        int lastColumn = -1;
        int lastRow = -1;
        int x;
        int y;
        float[] prevRow;
        float prevT0;
        float prevT1;
        int prevXX = -1;

        MCUsHolder() {
            this(new float[64], new float[64], new float[64]);
        }

        MCUsHolder(float[] prev, float[] curr, float[] next) {
            this.prev = prev;
            this.curr = curr;
            this.next = next;
        }

        public int xyToLin(int x, int y) {
            return (y << 3) + x;
        }

        public float get(int x, int y) {
            return curr[xyToLin(x, y)];
        }

        public float getCurrentOrPrev(int x, int y) {
//            if (y == -1)
//                return 0;
//            if (y >= 8)
//                return 0;
            if (x == -1)
                return prev[xyToLin(7, y)];
            if (x == 8)
                return next[xyToLin(0, y)];
            return curr[xyToLin(x, y)];
        }

        void updateIndex(int index, int x, int y, int lastColumn, int lastRow) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.lastColumn = lastColumn;
            this.lastRow = lastRow;
        }

        void shift() {
            float[] arr = prev;
            prev = curr;
            curr = next;
            next = arr;
        }

        public void storeLastRow() {
//            if (index < 0)
//                System.arraycopy(curr, 56, prevRow, prevRow.length - 8, 8);
//            else
//                System.arraycopy(curr, 56, prevRow, index * 8, 8);
            if (index <= 0)
                System.arraycopy(prev, 56, prevRow, prevRow.length - 8, 8);
            else {
                System.arraycopy(prev, 56, prevRow, index * 8 - 8, 8);
            }
        }
    }

    public static class ComponentHT {
        public final Component component;
        public final int huffmanTableIndex;
        public final int quantizationTableIndex;
        public final int componentIndex;
        public final int mcuIndex;
        int prevCoefficientValue;

        ComponentHT(Component component, int huffmanTableIndex, int quantizationTableIndex, int componentIndex, int mcuIndex) {
            this.component = component;
            this.huffmanTableIndex = huffmanTableIndex;
            this.quantizationTableIndex = quantizationTableIndex;
            this.componentIndex = componentIndex;
            this.mcuIndex = mcuIndex;
        }
    }


    static class PixelSetter implements IccProfileParser.ColorConsumer {
        final byte[] rasterBuffer;
        final int width;

        PixelSetter(BufferedImage image) {
            if (image.getType() != BufferedImage.TYPE_3BYTE_BGR)
                throw new IllegalArgumentException("Only TYPE_3BYTE_BGR is supported");
            width = image.getWidth();
            WritableRaster raster = image.getRaster();
            rasterBuffer = ((DataBufferByte) raster.getDataBuffer()).getData(0);
        }

        @Override
        public void set(int x, int y, byte r, byte g, byte b) {
            if (x >= width)
                return;
            int position = (x + y * width) * 3;
            rasterBuffer[position] = b;
            rasterBuffer[position + 1] = g;
            rasterBuffer[position + 2] = r;
        }

        public void set(int x, int y, int rgb) {
            int position = (x + y * width) * 3;
            rasterBuffer[position] = (byte) (rgb & 0xFF);
            rasterBuffer[position + 1] = (byte) ((rgb >> 8) & 0xFF);
            rasterBuffer[position + 2] = (byte) (rgb >> 16);
        }
    }

    public BufferedImage read(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        return read(in);
    }

    public BufferedImage read(InputStream in) throws IOException {
        BitStreamInputStream stream = new BitStreamInputStream(new byte[10240], in);
        return read(stream);
    }

    public BufferedImage read(byte[] in) throws IOException {
        BitStreamByteArray stream = new BitStreamByteArray(in);
        return read(stream);
    }

    public void readMetaInfo(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        readMetaInfo(in);
    }

    public List<ScanReader> getScanReaders(File file) throws IOException {
        BitStreamInputStream inputStream = new BitStreamInputStream(new byte[10240], new FileInputStream(file));
        BitStreamBuffered stream = new BitStreamBuffered(inputStream);
        readMetaInfo(stream);
        stream.unread(2);
        return getScanReaders(stream);
    }

    public void readMetaInfo(InputStream in) throws IOException {
        BitStreamInputStream stream = new BitStreamInputStream(new byte[10240], in);
        readMetaInfo(stream);
    }

    public void readMetaInfo(byte[] in) throws IOException {
        BitStreamByteArray stream = new BitStreamByteArray(in);
        readMetaInfo(stream);
    }

    public boolean isProgressive() {
        return progressive;
    }

    public int getRestartInterval() {
        return restartInterval;
    }

    protected void readMetaInfo(BitStream stream) throws IOException {
        if (Marker.valueOf(stream.readShortUnsafe(true)) != Marker.SOI)
            throw new IllegalStateException("Cannot find SOI marker");

        jfif = new JFIF();
//        stream.offset = readJFIF(buf, stream.offset, jfif);
//        stream.offset = readJFXX(buf, stream.offset, jfif);

        quantizationTables = new float[2][];
        huffmanTables = new HuffmanTable[18];

        do {
            stream.ensureEnoughLength(4);
            Marker marker = Marker.valueOf(stream.readShortUnsafe(true));
            if (marker == Marker.SOS) {
                stream.unread(2);
                return;
            }

            int length = stream.readShortUnsafe(true);
            stream.ensureEnoughLength(Math.min(length, 256));

            if (marker == Marker.APP0) {
                if (jfif.majorVersion == 0)
                    readJFIF(stream, jfif);
                else
                    readJFXX(stream, jfif);
                continue;
            }
            if (marker == Marker.APP2) {
                readIccProfile(stream, length);
                continue;
            }
            if (marker == Marker.DQT) {
                readQuantizationTable(stream, length, quantizationTables);
                continue;
            }
            if (marker == Marker.DHT) {
                huffmanTables = readHuffmanTable(stream, length, huffmanTables);
                continue;
            }
            if (marker == Marker.SOF0) {
                readFrameInfo(stream);
                continue;
            }
            if (marker == Marker.SOF2) {
                progressive = true;
                readFrameInfo(stream);
                continue;
            }
            if (marker == Marker.DRI) {
                readRestartInterval(stream);
                continue;
            }

//            System.out.println("Skipping marker " + marker + "(" + (marker != null ? marker.value & 0xFF : null) + ")" + ", length: " + length);

            stream.skip(length - 2);

        } while (true);
    }

    protected BufferedImage read(BitStream stream) throws IOException {
        if (jfif == null)
            readMetaInfo(stream);

        if (components.length == 1)
            throw new IllegalStateException("Grayscale images are not implemented yet"); //todo
        if (progressive && colorTransform != null)
            throw new IllegalStateException("Progressive JPEG with ICC Profile is not implemented yet"); //todo
        if (progressive && restartInterval != 0)
            throw new IllegalStateException("Progressive JPEG with restart-interval is not implemented yet"); //todo

        stream.ensureEnoughLength(4);

        if (Marker.valueOf(stream.readShortUnsafe(true)) != Marker.SOS)
            throw new IllegalStateException();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
//        WritableRaster raster = image.getRaster();
//        byte[] rasterBuffer = ((DataBufferByte) raster.getDataBuffer()).getData(0);
//        int width = this.width;
//        PixelSetter pixelSetter = (x, y, r, g, b) -> {
////            pixel[0] = rgb >> 16;
////            pixel[1] = (rgb >> 8) & 0xFF;
////            pixel[2] = rgb & 0xFF;
////            raster.setPixel(position % width, position / width, pixel);
//
//            //TYPE_3BYTE_BGR
////            int position = x + y * width;
//            int position = (x + y * width) * 3;
////            rasterBuffer[position * 3] = (byte) (rgb & 0xFF);
////            rasterBuffer[position * 3 + 1] = (byte) ((rgb >> 8) & 0xFF);
////            rasterBuffer[position * 3 + 2] = (byte) (rgb >> 16);
//            rasterBuffer[position] = b;
//            rasterBuffer[position + 1] = g;
//            rasterBuffer[position + 2] = r;
//        };
        PixelSetter pixelSetter = new PixelSetter(image);
        if (progressive) {
            stream.unread(2);
            if (stream instanceof BitStreamInputStream) {
                stream = new BitStreamBuffered((BitStreamInputStream) stream);
            }

            if (!stream.isSeekable())
                throw new IllegalStateException();

//            byte[] sosMarker = new byte[]{(byte) 0xFF, Marker.SOS.value};
//            List<Integer> scans = new ArrayList<>(16);
//            int prev = 0;
//            do {
//                int i = stream.indexOf(sosMarker, prev);
//                if (i == -1)
//                    break;
//                scans.add(i);
//                prev = i + 2;
//            } while (true);
//
//            BitStream finalStream = stream;
//            List<ScanReader> readers = scans.stream().map(offset -> new ScanReader(finalStream.shallowCopy(offset))).collect(Collectors.toList());


            List<ScanReader> readers = getScanReaders(stream);

            if (components.length != 3)
                throw new IllegalStateException("Not implemented yet");

            int blocksWidth = ((width % 8 != 0) ? (int) (width / 8.0 + 1) * 8 : width) / 8;
            int blocksHeight = ((height % 8 != 0) ? (int) (height / 8.0 + 1) * 8 : height) / 8;

            int blocksCount = blocksWidth * blocksHeight;

            int readersCount = readers.size();
            int sh = components[0].samplingFactorHorizontal;
            int sv = components[0].samplingFactorVertical;
            JpegDecoder.MCU mcu = new JpegDecoder.MCU(2 + sh * sv);

            DecodedData[] idcts = new DecodedData[mcu.coeffs.length];
            for (int i = 0; i < idcts.length; i++) {
                idcts[i] = new DecodedData();
            }

//            boolean interleaved = readers.stream().anyMatch(it -> it.componentHTS.length > 1) || true;

            if (sh == 1 && sv == 1) {
                for (int y = 0; y < blocksHeight; y++) {
                    for (int x = 0; x < blocksWidth; x++) {
                        int i = y * blocksWidth + x;
                        mcu.clear();
                        for (int scanIndex = 0; scanIndex < readersCount; scanIndex++) {
                            JpegDecoder.ScanReader reader = readers.get(scanIndex);
                            reader.processMCU(mcu, i, x, y);
                        }

                        processIdct(mcu, idcts, sh, sv);
                        finishDecoding(pixelSetter, i, blocksWidth, idcts);
                    }
                }
            } else if (sh == 2 && sv == 1) {
                UpSampler upSampler = UpSampler.createUpSampler(sv, sh);
                DecodedData[] prevIdcts = new DecodedData[mcu.coeffs.length];
                for (int i = 0; i < prevIdcts.length; i++) {
                    prevIdcts[i] = new DecodedData();
                }
                DecodedData[] nextIdcts = new DecodedData[mcu.coeffs.length];
                for (int i = 0; i < nextIdcts.length; i++) {
                    nextIdcts[i] = new DecodedData();
                }
                int cbIndex = mcu.coeffs.length - 2;
                int crIndex = mcu.coeffs.length - 1;
                MCUsHolder cbData = new MCUsHolder(prevIdcts[cbIndex].data, idcts[cbIndex].data, nextIdcts[cbIndex].data);
                MCUsHolder crData = new MCUsHolder(prevIdcts[crIndex].data, idcts[crIndex].data, nextIdcts[crIndex].data);

                int lastBlockInRow = sh == 1 ? blocksWidth - sh : ((blocksWidth & 1) == 1 ? blocksWidth / sh * sh : blocksWidth - sh);
                int lastRow = height - 1 - (blocksHeight >> 1) * 16;
                if (lastRow == -1)
                    lastRow = 15;
                int lastColumn = (width - 1) & 0xF;
//                readersCount=2;

                for (int y = 0; y < blocksHeight; y += sv) {
                    for (int x = 0; x < blocksWidth; x += sh) {
                        int i = y * blocksWidth + x;
                        mcu.clear();
                        for (int scanIndex = 0; scanIndex < readersCount; scanIndex++) {
                            JpegDecoder.ScanReader reader = readers.get(scanIndex);
                            reader.processMCU(mcu, i, x, y);
                        }

                        int lr = y + sv >= blocksHeight ? lastRow : -1;
                        cbData.updateIndex((x - sh) / sh, x - sh, y, -1, lr);
                        cbData.shift();

                        crData.updateIndex((x - sh) / sh, x - sh, y, -1, lr);
                        crData.shift();

                        {
                            DecodedData[] arr = prevIdcts;
                            prevIdcts = idcts;
                            idcts = nextIdcts;
                            nextIdcts = arr;
                        }

                        processIdct(mcu, nextIdcts, sh, sv);

                        if (cbData.index < 0 && y > 0) {
                            cbData.y -= sv;
//                            cbData.x = blocksWidth - sh;
                            cbData.x = lastBlockInRow;
                            cbData.index = (blocksWidth - 1) >> 1;
                            cbData.lastColumn = lastColumn;
                            cbData.lastRow = -1;

                            crData.y -= sv;
//                            crData.x = blocksWidth - sh;
                            crData.x = lastBlockInRow;
                            crData.index = (blocksWidth - 1) >> 1;
                            crData.lastColumn = lastColumn;
                            crData.lastRow = -1;
                        }

                        if (cbData.index >= 0)
                            finishDecoding(pixelSetter, i, blocksWidth, idcts, sh, sv, upSampler, cbData, crData, null);
                    }
                }

                {
                    DecodedData[] arr = prevIdcts;
                    prevIdcts = idcts;
                    idcts = nextIdcts;
                    nextIdcts = arr;
                }
                int lastRowIndex = (blocksHeight & 1) == 1 ? blocksHeight - 1 : blocksHeight - sv;
                int lastIndex = (blocksWidth - 1) >> 1;
                cbData.updateIndex(lastIndex, lastBlockInRow, lastRowIndex, lastColumn, lastRow);
                cbData.shift();

                crData.updateIndex(lastIndex, lastBlockInRow, lastRowIndex, lastColumn, lastRow);
                crData.shift();
                finishDecoding(pixelSetter, -1, blocksWidth, idcts, sh, sv, upSampler, cbData, crData, null);


            } else if (sv == 2 && sh == 1) {
                UpSampler upSampler = UpSampler.createUpSampler(sv, sh);

                int[][] yRow = new int[blocksWidth][64];
                int[][] yRowNext = new int[blocksWidth][64];
                int[][] cbRow = new int[blocksWidth][64];
                int[][] crRow = new int[blocksWidth][64];

                int cbIndex = mcu.coeffs.length - 2;
                int crIndex = mcu.coeffs.length - 1;
                MCUsHolder cbData = new MCUsHolder(null, idcts[cbIndex].data, null);
                MCUsHolder crData = new MCUsHolder(null, idcts[crIndex].data, null);
                float[] prevRowY = null;


                int lastBlockInRow = sh == 1 ? blocksWidth - sh : ((blocksWidth & 1) == 1 ? blocksWidth / sh * sh : blocksWidth - sh);
                int lastRow = height - 1 - (blocksHeight >> 1) * 16;
                if (lastRow == -1)
                    lastRow = 15;
                int lastColumn = (width - 1) & 0xF;

                for (int y = 0; y < blocksHeight; y += 1) {
                    for (int x = 0; x < blocksWidth; x += sh) {
                        int i = y * blocksWidth + x;
                        if ((y & 1) == 0) {
                            mcu.coeffs[0] = yRow[x];
                            mcu.coeffs[1] = yRowNext[x];
                            mcu.coeffs[2] = cbRow[x];
                            mcu.coeffs[3] = crRow[x];
                            mcu.clear();
                            for (int scanIndex = 0; scanIndex < readersCount; scanIndex++) {
                                JpegDecoder.ScanReader reader = readers.get(scanIndex);
                                reader.processMCU(mcu, i, x, y);
                            }
                        } else {
                            mcu.coeffs[0] = yRowNext[x];
                            mcu.coeffs[1] = null;
                            mcu.coeffs[2] = null;
                            mcu.coeffs[3] = null;
                            for (int scanIndex = 0; scanIndex < readersCount; scanIndex++) {
                                JpegDecoder.ScanReader reader = readers.get(scanIndex);
                                if (reader.dcScan && reader.componentHTS.length == 3)
                                    continue;
                                if (reader.componentHTS[0].componentIndex != 0)
                                    continue;

                                reader.processMCU(mcu, i, x, y);
                            }

                            mcu.coeffs[0] = yRow[x];
                            mcu.coeffs[1] = yRowNext[x];
                            mcu.coeffs[2] = cbRow[x];
                            mcu.coeffs[3] = crRow[x];
                        }

                        if ((y & 1) == 1 || y == blocksHeight - 1) {
                            processIdct(mcu, idcts, sh, sv);

                            int lr;
//                            lr = y + sv > blocksHeight ? lastRow : -1;
                            if ((blocksHeight & 1) == 0)
                                lr = (y + sv >= blocksHeight) ? lastRow : -1;
                            else
                                lr = (y + sv > blocksHeight) ? lastRow : -1;

                            if ((y & 1) == 0 && y == blocksHeight - 1) {
                                cbData.updateIndex(x, x, y, -1, lr);
                                crData.updateIndex(x, x, y, -1, lr);
                            } else {
                                cbData.updateIndex(x, x, y - 1, -1, lr);
                                crData.updateIndex(x, x, y - 1, -1, lr);
                            }

                            finishDecoding(pixelSetter, i, blocksWidth, idcts, sh, sv, upSampler, cbData, crData, prevRowY);
                        }
                    }
                }

            } else if (sh == 2 && sv == 2) {
                UpSampler upSampler = UpSampler.createUpSampler(sv, sh);

                int evenBlockWidth = (blocksWidth & 1) == 1 ? blocksWidth + 1 : blocksWidth;
                int[][] yRow = new int[evenBlockWidth][64];
                int[][] yRowNext = new int[evenBlockWidth][64];
                int[][] cbRow = new int[evenBlockWidth][64];
                int[][] crRow = new int[evenBlockWidth][64];

                DecodedData[] prevIdcts = new DecodedData[mcu.coeffs.length];
                for (int i = 0; i < prevIdcts.length; i++) {
                    prevIdcts[i] = new DecodedData();
                }
                DecodedData[] nextIdcts = new DecodedData[mcu.coeffs.length];
                for (int i = 0; i < nextIdcts.length; i++) {
                    nextIdcts[i] = new DecodedData();
                }

                int cbIndex = mcu.coeffs.length - 2;
                int crIndex = mcu.coeffs.length - 1;
                MCUsHolder cbData = new MCUsHolder(prevIdcts[cbIndex].data, idcts[cbIndex].data, nextIdcts[cbIndex].data);
                MCUsHolder crData = new MCUsHolder(prevIdcts[crIndex].data, idcts[crIndex].data, nextIdcts[crIndex].data);
                cbData.prevRow = new float[(evenBlockWidth) / 2 * 8];
                crData.prevRow = new float[(evenBlockWidth) / 2 * 8];
                float[] prevRowY = new float[blocksWidth * 8];


                int lastBlockInRow = sh == 1 ? blocksWidth - sh : ((blocksWidth & 1) == 1 ? blocksWidth / sh * sh : blocksWidth - sh);
                int lastRow = height - 1 - (blocksHeight >> 1) * 16;
                if (lastRow == -1)
                    lastRow = 15;
                int lastColumn = (width - 1) & 0xF;

                for (int y = 0; y < blocksHeight; y += 1) {
                    for (int x = 0; x < blocksWidth; x += sh) {
                        int i = y * blocksWidth + x;
                        if ((y & 1) == 0) {
                            mcu.coeffs[0] = yRow[x];
//                            mcu.coeffs[1] = x + 1 < blocksWidth ? yRow[x + 1] : null;
                            mcu.coeffs[1] = yRow[x + 1];
                            mcu.coeffs[2] = yRowNext[x];
//                            mcu.coeffs[3] = x + 1 < blocksWidth ? yRowNext[x + 1] : null;
                            mcu.coeffs[3] = yRowNext[x + 1];
                            mcu.coeffs[4] = cbRow[x];
                            mcu.coeffs[5] = crRow[x];
                            mcu.clear();
                            for (int scanIndex = 0; scanIndex < readersCount; scanIndex++) {
                                JpegDecoder.ScanReader reader = readers.get(scanIndex);
                                reader.processMCU(mcu, i, x, y);
                            }
                        } else {
                            mcu.coeffs[0] = yRowNext[x];
//                            mcu.coeffs[1] = x + 1 < blocksWidth ? yRowNext[x + 1] : null;
                            mcu.coeffs[1] = yRowNext[x + 1];
                            mcu.coeffs[2] = null;
                            mcu.coeffs[3] = null;
                            mcu.coeffs[4] = null;
                            mcu.coeffs[5] = null;
                            for (int scanIndex = 0; scanIndex < readersCount; scanIndex++) {
                                JpegDecoder.ScanReader reader = readers.get(scanIndex);
                                if (reader.dcScan && reader.componentHTS.length == 3)
                                    continue;
                                if (reader.componentHTS[0].componentIndex != 0)
                                    continue;

                                reader.processMCU(mcu, i, x, y);
                            }

                            mcu.coeffs[0] = yRow[x];
//                            mcu.coeffs[1] = x + 1 < blocksWidth ? yRow[x + 1] : null;
                            mcu.coeffs[1] = yRow[x + 1];
                            mcu.coeffs[2] = yRowNext[x];
//                            mcu.coeffs[3] = x + 1 < blocksWidth ? yRowNext[x + 1] : null;
                            mcu.coeffs[3] = yRowNext[x + 1];
                            mcu.coeffs[4] = cbRow[x];
                            mcu.coeffs[5] = crRow[x];
                        }

                        if ((y & 1) == 1 || y == blocksHeight - 1) {
                            int lr = y + sv > blocksHeight ? lastRow : -1;
                            int index = (x - sh) / sh;
                            int xx = x - sh;
                            if ((y & 1) == 0 && y == blocksHeight - 1) {
                                cbData.updateIndex(index, xx, y, -1, lr);
                                crData.updateIndex(index, xx, y, -1, lr);
                            } else {
                                cbData.updateIndex(index, xx, y - 1, -1, lr);
                                crData.updateIndex(index, xx, y - 1, -1, lr);
                            }

                            cbData.shift();
                            crData.shift();
                            {
                                DecodedData[] arr = prevIdcts;
                                prevIdcts = idcts;
                                idcts = nextIdcts;
                                nextIdcts = arr;
                            }
                            processIdct(mcu, nextIdcts, sh, sv);


                            if (cbData.index < 0 && y > 0) {
                                cbData.y -= 2;
//                            cbData.x = blocksWidth - sh;
                                cbData.x = lastBlockInRow;
                                cbData.index = (blocksWidth - 1) >> 1;
                                cbData.lastColumn = lastColumn;
                                cbData.lastRow = -1;

                                crData.y -= 2;
//                            crData.x = blocksWidth - sh;
                                crData.x = lastBlockInRow;
                                crData.index = (blocksWidth - 1) >> 1;
                                crData.lastColumn = lastColumn;
                                crData.lastRow = -1;
                            }


                            if (cbData.index >= 0 && cbData.y >= 0)
                                finishDecoding(pixelSetter, i, blocksWidth, idcts, sh, sv, upSampler, cbData, crData, prevRowY);

                            if (x > 0) {
                                System.arraycopy(idcts[2].data, 56, prevRowY, ((x - 2) >> 1) * 16, 8);
                                System.arraycopy(idcts[3].data, 56, prevRowY, ((x - 2) >> 1) * 16 + 8, 8);
                            } else {
                                int destPos = lastBlockInRow * 8;
                                System.arraycopy(idcts[2].data, 56, prevRowY, destPos, Math.min(8, prevRowY.length - destPos));
                                if (prevRowY.length > destPos + 8) {
                                    destPos += 8;
                                    System.arraycopy(idcts[3].data, 56, prevRowY, destPos, Math.min(8, prevRowY.length - destPos));
                                }
                            }

                            cbData.storeLastRow();
                            crData.storeLastRow();
                        }
                    }
                }
                {
                    DecodedData[] arr = prevIdcts;
                    prevIdcts = idcts;
                    idcts = nextIdcts;
                    nextIdcts = arr;
                }
                int lastRowIndex = (blocksHeight & 1) == 1 ? blocksHeight - 1 : blocksHeight - sv;
                int lastIndex = (blocksWidth - 1) >> 1;
                cbData.updateIndex(lastIndex, lastBlockInRow, lastRowIndex, lastColumn, lastRow);
                cbData.shift();

                crData.updateIndex(lastIndex, lastBlockInRow, lastRowIndex, lastColumn, lastRow);
                crData.shift();
                finishDecoding(pixelSetter, -1, blocksWidth, idcts, sh, sv, upSampler, cbData, crData, prevRowY);
            } else {
                UpSampler upSampler = UpSampler.createUpSampler(sv, sh);

//                IDCTFloat[] prevIdcts = new IDCTFloat[mcu.coeffs.length];
//                for (int i = 0; i < prevIdcts.length; i++) {
//                    prevIdcts[i] = new IDCTFloat();
//                }
//                IDCTFloat[] nextIdcts = new IDCTFloat[mcu.coeffs.length];
//                for (int i = 0; i < nextIdcts.length; i++) {
//                    nextIdcts[i] = new IDCTFloat();
//                }
//                IDCTFloat[] yRow = new IDCTFloat[blocksWidth];
//                for (int i = 0; i < yRow.length; i++) {
//                    yRow[i] = new IDCTFloat();
//                }
//
//                int cbIndex = mcu.coeffs.length - 2;
//                int crIndex = mcu.coeffs.length - 1;
//                MCUsHolder cbData = new MCUsHolder(prevIdcts[cbIndex].base, idcts[cbIndex].base, nextIdcts[cbIndex].base);
//                MCUsHolder crData = new MCUsHolder(prevIdcts[crIndex].base, idcts[crIndex].base, nextIdcts[crIndex].base);
//                float[] prevRowY = null;
//
//                if (sv == 2 && sh == 2) {
////                    cbData.prevRow = new float[blocksWidth * 8 / 2];
////                    crData.prevRow = new float[blocksWidth * 8 / 2];
//                    cbData.prevRow = new float[(blocksWidth + 1) / 2 * 8];
//                    crData.prevRow = new float[(blocksWidth + 1) / 2 * 8];
//                    prevRowY = new float[blocksWidth * 8];
//                }
//
//                int lastBlockInRow = sh == 1 ? blocksWidth - sh : ((blocksWidth & 1) == 1 ? blocksWidth / sh * sh : blocksWidth - sh);
//                int lastRow = height - 1 - (blocksHeight >> 1) * 16;
//                if (lastRow == -1)
//                    lastRow = 15;
//                int lastColumn = (width - 1) & 0xF;
//
//                for (int y = 0; y < blocksHeight; y += sv) {
//                    for (int x = 0; x < blocksWidth; x += sh) {
//                        int i = y * blocksWidth + x;
//                        mcu.clear();
//                        for (int scanIndex = 0; scanIndex < readersCount; scanIndex++) {
//                            JpegDecoder.ScanReader reader = readers.get(scanIndex);
//                            reader.processMCU(mcu, i, x, y);
//                        }
//
//                        int lr = y + sv >= blocksHeight ? lastRow : -1;
//                        cbData.updateIndex((x - sh) / sh, x - sh, y, -1, lr);
//                        cbData.shift();
//
//                        crData.updateIndex((x - sh) / sh, x - sh, y, -1, lr);
//                        crData.shift();
//
//                        {
//                            IDCTFloat[] arr = prevIdcts;
//                            prevIdcts = idcts;
//                            idcts = nextIdcts;
//                            nextIdcts = arr;
//                        }
//
//                        processIdct(mcu, nextIdcts, sh, sv);
//
//                        if (cbData.index < 0 && y > 0) {
//                            cbData.y -= sv;
////                            cbData.x = blocksWidth - sh;
//                            cbData.x = lastBlockInRow;
//                            cbData.index = (blocksWidth - 1) >> 1;
//                            cbData.lastColumn = lastColumn;
//                            cbData.lastRow = -1;
//
//                            crData.y -= sv;
////                            crData.x = blocksWidth - sh;
//                            crData.x = lastBlockInRow;
//                            crData.index = (blocksWidth - 1) >> 1;
//                            crData.lastColumn = lastColumn;
//                            crData.lastRow = -1;
//                        }
//
//                        if (cbData.index >= 0)
//                            finishDecoding(pixelSetter, i, blocksWidth, idcts, sh, sv, upSampler, cbData, crData, prevRowY);
//
//                        if (sv == 2 && sh == 2) {
////                        if (x == 0 || x == 2) {
////                            printFloats(matL[2].base, 56, 8);
////                            printFloats(matL[3].base, 56, 8);
////                            printFloats(matLPrev[2].base, 56, 8);
////                            printFloats(matLPrev[3].base, 56, 8);
////                            System.out.println();
////                        }
////                        System.arraycopy(matLPrev[2].base, 56, prevRowY, (x >> 1) * 16, 8);
////                        System.arraycopy(matLPrev[3].base, 56, prevRowY, (x >> 1) * 16 + 8, 8);
//                            if (x > 0) {
//                                System.arraycopy(idcts[2].base, 56, prevRowY, ((x - 2) >> 1) * 16, 8);
//                                System.arraycopy(idcts[3].base, 56, prevRowY, ((x - 2) >> 1) * 16 + 8, 8);
//                            } else {
//                                int destPos = lastBlockInRow * 8;
//                                System.arraycopy(idcts[2].base, 56, prevRowY, destPos, Math.min(8, prevRowY.length - destPos));
//                                if (prevRowY.length > destPos + 8) {
//                                    destPos += 8;
//                                    System.arraycopy(idcts[3].base, 56, prevRowY, destPos, Math.min(8, prevRowY.length - destPos));
//                                }
//                            }
//
//                            cbData.storeLastRow();
//                            crData.storeLastRow();
//                        }
//                    }
//                }
//
//                {
//                    IDCTFloat[] arr = prevIdcts;
//                    prevIdcts = idcts;
//                    idcts = nextIdcts;
//                    nextIdcts = arr;
//                }
//                int lastRowIndex = (blocksHeight & 1) == 1 ? blocksHeight - 1 : blocksHeight - sv;
//                int lastIndex = (blocksWidth - 1) >> 1;
//                cbData.updateIndex(lastIndex, lastBlockInRow, lastRowIndex, lastColumn, lastRow);
//                cbData.shift();
//
//                crData.updateIndex(lastIndex, lastBlockInRow, lastRowIndex, lastColumn, lastRow);
//                crData.shift();
//                finishDecoding(pixelSetter, -1, blocksWidth, idcts, sh, sv, upSampler, cbData, crData, prevRowY);
            }


//            int[] progressiveBuffer = new int[rasterBuffer.length];
//            stream.unread(2);
//            do {
//                stream.ensureEnoughLength(4);
//                Marker marker = Marker.valueOf(stream.readShortUnsafe(true));
//                if (marker == Marker.SOS) {
//                    readProgressiveScan(stream, progressiveBuffer);
//                } else if (marker == Marker.DHT) {
//                    int length = stream.readShortUnsafe();
//                    stream.ensureEnoughLength(length);
//                    huffmanTables = readHuffmanTable(stream, length, huffmanTables);
//                } else if (marker == Marker.EOI) {
//                    finishDecoding(pixelSetter, progressiveBuffer);
//                    return image;
//                } else
//                    throw new IllegalStateException();
//            } while (true);

        } else {
            readScan(stream, pixelSetter);
//            if (colorSpace != null) {
//                ColorTransform[] transformList = new ColorTransform[2];
//                ICC_ColorSpace srgbCS =
//                        (ICC_ColorSpace) ColorSpace.getInstance(ColorSpace.CS_sRGB);
//                PCMM mdl = CMSManager.getModule();
//                transformList[0] = mdl.createTransform(
//                        colorSpace.getProfile(), ColorTransform.Any, ColorTransform.In);
//                transformList[1] = mdl.createTransform(
//                        srgbCS.getProfile(), ColorTransform.Any, ColorTransform.Out);
//                ColorTransform transform = mdl.createTransform(transformList);
//                transform.colorConvert(rasterBuffer, rasterBuffer);
//            }
        }
        return image;
    }

    private List<ScanReader> getScanReaders(BitStream stream) {
        byte[] sosMarker = new byte[]{(byte) 0xFF, Marker.SOS.value};
        byte[] dhtMarker = new byte[]{(byte) 0xFF, Marker.DHT.value};

        List<ScanReader> readers = new ArrayList<>(16);
        int prev = 0;
        int dhtIndex = stream.indexOf(dhtMarker, prev);
        do {
            int scanIndex = stream.indexOf(sosMarker, prev);
            if (scanIndex == -1)
                break;

            while (dhtIndex != -1 && dhtIndex < scanIndex) {
                BitStream streamCopy = stream.shallowCopy(dhtIndex + 2);
                int length = streamCopy.readShortUnsafe();
                huffmanTables = readHuffmanTable(streamCopy, length, huffmanTables);
                dhtIndex = stream.indexOf(dhtMarker, dhtIndex + 2 + length);
            }
            BitStream shallowCopy = stream.shallowCopy(scanIndex + 2);
            readers.add(createScanReader(shallowCopy, readers.size()));

            prev = scanIndex + 2;
        } while (true);
        return readers;
    }

    protected ScanReader createScanReader(BitStream stream, int i) {
        ScanReader reader = new ScanReader(stream, width, height, components, Arrays.copyOf(huffmanTables, huffmanTables.length), i);
        return reader;
    }

    public static class ScanReader {
        final BitStream stream;
        public int index;
        public ComponentHT[] componentHTS;
        public boolean dcScan;
        public boolean refining;
        public int blocksWidth;
        public int blocksHeight;
        public int spectralSelectionStart;
        public int spectralSelectionEnd;
        public int bitPositionPrevScan;
        public int bitPositionCurrenScan;
        public int stepH;
        public int stepV;
        public int sh;
        public int sv;
        public int scanIndex;

        public int width;
        public int height;
        public ComponentInfo[] components;
        public HuffmanTable[] huffmanTables;
        public int eobRun;

        ScanReader(BitStream stream, int width, int height, ComponentInfo[] components, HuffmanTable[] huffmanTables, int scanIndex) {
            this.stream = stream;
            this.width = width;
            this.height = height;
            this.components = components;
            this.huffmanTables = huffmanTables;
            this.scanIndex = scanIndex;
            init();
        }

        void init() {
            int length = stream.readShortUnsafe();
            int numberOfComponents = stream.readByteUnsafe();
            componentHTS = new ComponentHT[numberOfComponents];

            sh = components[0].samplingFactorHorizontal;
            sv = components[0].samplingFactorVertical;

            for (int i = 0; i < numberOfComponents; i++) {
                Component component = Component.valueOf(stream.readByteUnsafe());
                int tableIndex = stream.readByteUnsafe();
                int componentIndex = findComponentIndex(components, component);
                if (componentIndex == -1)
                    throw new IllegalArgumentException("Cannot find component " + component);

                int mcuIndex = componentIndex == 0 ? 0 : componentIndex + (sh * sv - 1);
                int quantizationTableIndex = components[componentIndex].quantizationTable;
                componentHTS[i] = new ComponentHT(component, tableIndex, quantizationTableIndex, componentIndex, mcuIndex);
            }

            spectralSelectionStart = stream.readByteUnsafe();
            spectralSelectionEnd = stream.readByteUnsafe();
            int tmpByte = stream.readByteUnsafe();
            bitPositionPrevScan = tmpByte >> 4;
            bitPositionCurrenScan = tmpByte & 0x0F;

            blocksWidth = ((width % 8 != 0) ? (int) (width / 8.0 + 1) * 8 : width) / 8;
            blocksHeight = ((height % 8 != 0) ? (int) (height / 8.0 + 1) * 8 : height) / 8;

            int refiningLength = 0;
            if (bitPositionPrevScan == 0) {
                refining = false;
            } else if (bitPositionPrevScan - bitPositionCurrenScan == 1) {
                refining = true;
            } else
                throw new IllegalStateException("Progressive JPEG images cannot contain more than 1 bit for each value on a refining scan.");

            if (spectralSelectionStart == 0 && spectralSelectionEnd == 0) {
                dcScan = true;
            } else if (spectralSelectionStart > 0 && spectralSelectionEnd >= spectralSelectionStart) {
                dcScan = false;
            } else
                throw new IllegalStateException("Progressive JPEG images cannot contain both DC and AC values in the same scan.");


            if (!dcScan && componentHTS.length > 1)
                throw new IllegalStateException("An AC progressive scan can only have a single color component.");

            stepH = 8;
            stepV = 8;

            if (componentHTS.length == 1 && componentHTS[0].component != Component.Y && (components[0].samplingFactorHorizontal != 1 || components[0].samplingFactorVertical != 1)) {
                stepH = 8 * components[0].samplingFactorHorizontal;
                stepV = 8 * components[0].samplingFactorVertical;
                blocksWidth = ((width % stepH != 0) ? (int) (width * 1.0 / stepH + 1) * stepH : width) / stepH;
                blocksHeight = ((height % stepV != 0) ? (int) (height * 1.0 / stepV + 1) * stepV : height) / stepV;
            }
        }

        int getIndex() {
            return index;
        }

        void processMCU(MCU mcu, int index, int x, int y) throws IOException {
//            if (index != this.index) {
//                if (index < this.index)
//                    return;
//                else
//                    throw new IllegalStateException();
//            }

            if (dcScan) {
                if (!refining) {
                    if (sv > 1 || sh > 1) {
                        if (componentHTS.length == 3) {
                            for (int i = 0; i < componentHTS.length; i++) {
                                ComponentHT componentHT = componentHTS[i];
                                HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex >> 4];

                                if (i == 0) {
                                    for (int yy = 0; yy < sv; yy++) {
                                        for (int xx = 0; xx < sh; xx++) {
                                            int category = getValue(stream, ht) & 0x0F;
                                            int bits = stream.readBits(category);
                                            int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
                                            componentHT.prevCoefficientValue = dcValue;

                                            dcValue = dcValue << bitPositionCurrenScan;
                                            mcu.coeffs[yy * sh + xx][0] = dcValue;
                                        }
                                    }
                                } else {
                                    int category = getValue(stream, ht) & 0x0F;
                                    int bits = stream.readBits(category);
                                    int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
                                    componentHT.prevCoefficientValue = dcValue;

                                    dcValue = dcValue << bitPositionCurrenScan;
                                    mcu.coeffs[componentHT.mcuIndex][0] = dcValue;
                                }
                            }
                        } else {
                            ComponentHT componentHT = componentHTS[0];
                            HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex >> 4];
                            if (componentHT.mcuIndex == 0) {
                                for (int xx = 0; xx < sh; xx++) {
                                    if (x + xx >= blocksWidth)
                                        continue;
                                    int category = getValue(stream, ht) & 0x0F;
                                    int bits = stream.readBits(category);
                                    int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
                                    componentHT.prevCoefficientValue = dcValue;

                                    dcValue = dcValue << bitPositionCurrenScan;
                                    mcu.coeffs[xx][0] = dcValue;
                                }
                            } else {
                                int category = getValue(stream, ht) & 0x0F;
                                int bits = stream.readBits(category);
                                int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
                                componentHT.prevCoefficientValue = dcValue;

                                dcValue = dcValue << bitPositionCurrenScan;
                                mcu.coeffs[componentHT.mcuIndex][0] = dcValue;
                            }
//                            for (int i = 0; i < componentHTS.length; i++) {
//                                ComponentHT componentHT = componentHTS[i];
//                                HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex >> 4];
//
//                                int category = getValue(stream, ht) & 0x0F;
//                                int bits = stream.readBits(category);
//                                int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
//                                componentHT.prevCoefficientValue = dcValue;
//
//                                dcValue = dcValue << bitPositionCurrenScan;
//                                mcu.coeffs[componentHT.mcuIndex][0] = dcValue;
//                            }
                        }
                    } else {
                        for (int i = 0; i < componentHTS.length; i++) {
                            ComponentHT componentHT = componentHTS[i];
                            HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex >> 4];

                            int category = getValue(stream, ht) & 0x0F;
                            int bits = stream.readBits(category);
                            int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
                            componentHT.prevCoefficientValue = dcValue;

                            dcValue = dcValue << bitPositionCurrenScan;
                            mcu.coeffs[componentHT.mcuIndex][0] = dcValue;
                        }
                    }
                    this.index++;
                } else {
                    if (sv > 1 || sh > 1) {
                        if (componentHTS.length == 3) {
                            for (int i = 0; i < componentHTS.length; i++) {
                                ComponentHT componentHT = componentHTS[i];
                                if (componentHT.mcuIndex == 0) {
                                    for (int yy = 0; yy < sv; yy++) {
                                        for (int xx = 0; xx < sh; xx++) {
                                            int bit = stream.readBit();
                                            mcu.coeffs[yy * sh + xx][0] |= bit << bitPositionCurrenScan;
                                        }
                                    }
                                } else {
                                    int bit = stream.readBit();
                                    mcu.coeffs[componentHT.mcuIndex][0] |= bit << bitPositionCurrenScan;
                                }
                            }
                        } else {
                            ComponentHT componentHT = componentHTS[0];
                            if (componentHT.mcuIndex == 0) {
                                for (int xx = 0; xx < sh; xx++) {
                                    if (x + xx >= blocksWidth)
                                        continue;
                                    int bit = stream.readBit();
                                    mcu.coeffs[xx][0] |= bit << bitPositionCurrenScan;
                                }
                            } else {
                                int bit = stream.readBit();
                                mcu.coeffs[componentHT.mcuIndex][0] |= bit << bitPositionCurrenScan;
                            }
                        }
                    } else {
                        for (int i = 0; i < componentHTS.length; i++) {
                            ComponentHT componentHT = componentHTS[i];
                            int bit = stream.readBit();
                            mcu.coeffs[componentHT.mcuIndex][0] |= bit << bitPositionCurrenScan;
                        }
                    }
                    this.index++;
                }
            } else {
                ComponentHT componentHT = componentHTS[0];
                HuffmanTable ht;
                if (componentHT.huffmanTableIndex < 16)
                    ht = huffmanTables[componentHT.huffmanTableIndex + 16];
                else
                    ht = huffmanTables[componentHT.huffmanTableIndex];

//                boolean printData = scanIndex == 2;
                boolean printData = false;
                int sv = componentHT.componentIndex == 0 ? this.sv : 1;
                int sh = componentHT.componentIndex == 0 ? this.sh : 1;

                if (componentHT.componentIndex != 0) {
                    x /= this.sh;
                    y /= this.sv;
                }

//                for (int yy = 0; yy < sv; yy++) {
                for (int xx = 0; xx < sh; xx++) {
                    if (x + xx >= blocksWidth)
                        continue;

//                        int mcuIndex = componentHT.componentIndex == 0 ? yy * sh + xx : componentHT.mcuIndex;
                    int mcuIndex = componentHT.componentIndex == 0 ? xx : componentHT.mcuIndex;
                    if (mcu.coeffs[mcuIndex] == null)
                        continue;

//                        if (scanIndex == 1) {
//                            System.out.println((x + xx) + "x" + (y + yy));
//                        }
                    if (printData) {
//                            System.out.println("b" + (x + xx) + "x" + (y + yy) + ": " + Arrays.toString(mcu.coeffs[mcuIndex]));
                        System.out.println("b" + (x + xx) + "x" + (y) + ": " + Arrays.toString(mcu.coeffs[mcuIndex]));
                    }
                    if (printData && x == 2 && y == 2) {
                        x = x;
                    }

                    if (!refining) {
                        if (eobRun > 0) {
                            eobRun--;
//                                eobRun -= eobStep;
                            if (printData) {
//                                    System.out.println("a" + (x + xx) + "x" + (y + yy) + ": " + Arrays.toString(mcu.coeffs[mcuIndex]));
                                System.out.println("a" + (x + xx) + "x" + (y) + ": " + Arrays.toString(mcu.coeffs[mcuIndex]));
                            }
                            continue;
                        }
                        int shift = bitPositionCurrenScan;

                        int k = spectralSelectionStart;
                        do {
                            int zig;
                            int c, r, s;
                            int rs = getValue(stream, ht) & 0xFF;
                            s = rs & 15;
                            r = rs >> 4;
                            if (s == 0) {
                                if (r < 15) {
                                    eobRun = (1 << r) - 1;
                                    if (r != 0) {
                                        eobRun += stream.readBits(r);
                                    }
//                                        this.index += eobRun;
                                    break;
                                }
                                k += 16;
                            } else {
                                k += r;
                                int ii = SCAN_ZIGZAG[k++];
                                int xr = ii & 0x07;
                                int yr = ii >> 3;
//                            int position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.mcuIndex;

                                int bits = stream.readBits(s);
                                int value = convertToValue(bits, s);
                                value = value << bitPositionCurrenScan;
//                            buffer[position] = value;
                                mcu.coeffs[mcuIndex][xr + (yr << 3)] = value;
                            }
                        } while (k <= spectralSelectionEnd);
                        this.index++;
                    } else {
                        int bit = 1 << bitPositionCurrenScan;
                        if (eobRun > 0) {
                            eobRun--;
//                                eobRun-=eobStep;

                            for (int k = spectralSelectionStart; k <= spectralSelectionEnd; k++) {
                                int ii = SCAN_ZIGZAG[k];
                                int xr = ii & 0x07;
                                int yr = ii >> 3;
//                            int position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.componentIndex;
//                            int prevValue = buffer[position];
                                int position = xr + (yr << 3);
                                int prevValue = mcu.coeffs[mcuIndex][position];
                                if (prevValue != 0)
                                    if (stream.readBit() != 0) {
                                        if ((prevValue & bit) == 0) {
                                            if (prevValue > 0)
                                                mcu.coeffs[mcuIndex][position] = prevValue + bit;
                                            else
                                                mcu.coeffs[mcuIndex][position] = prevValue - bit;
                                        }
                                    }
                            }
                        } else {
                            int k = spectralSelectionStart;
                            do {
                                int rs = getValue(stream, ht) & 0xFF;
                                int s = rs & 0x0F;
                                int r = rs >> 4;
                                if (s == 0) {
                                    if (r < 15) {
                                        eobRun = (1 << r) - 1;
                                        if (r != 0) {
                                            int value = stream.readBits(r);
                                            eobRun += value;
                                        }
                                        r = 64; // force end of block
                                    } else {
                                        // r=15 s=0 should write 16 0s, so we just do
                                        // a run of 15 0s and then write s (which is 0),
                                        // so we don't have to do anything special here
                                    }
                                } else {
                                    if (s != 1) throw new IllegalStateException();
                                    // sign bit
                                    if (stream.readBit() != 0)
                                        s = bit;
                                    else
                                        s = -bit;
                                }

                                while (k <= spectralSelectionEnd) {
                                    int ii = SCAN_ZIGZAG[k++];
                                    int xr = ii & 0x07;
                                    int yr = ii >> 3;
//                                int position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.componentIndex;
//                                int prevValue = buffer[position];
                                    int position = xr + (yr << 3);
                                    int prevValue = mcu.coeffs[mcuIndex][position];
                                    if (prevValue != 0) {
                                        if (stream.readBit() != 0) {
                                            if ((prevValue & bit) == 0) {
                                                if (prevValue > 0)
                                                    mcu.coeffs[mcuIndex][position] = prevValue + bit;
                                                else
                                                    mcu.coeffs[mcuIndex][position] = prevValue - bit;
                                            }
                                        }
                                    } else {
                                        if (r == 0) {
                                            mcu.coeffs[mcuIndex][position] = s;
                                            break;
                                        }
                                        --r;
                                    }
                                }
                            } while (k <= spectralSelectionEnd);
                        }
                        this.index++;
                    }

                    if (printData) {
//                            System.out.println("a" + (x + xx) + "x" + (y + yy) + ": " + Arrays.toString(mcu.coeffs[mcuIndex]));
                        System.out.println("a" + (x + xx) + "x" + (y) + ": " + Arrays.toString(mcu.coeffs[mcuIndex]));
                    }
                }
//                }

            }
        }

        void processMCU(int index, int[] buffer) throws IOException {
            if (index != this.index) {
                if (index < this.index)
                    return;
                else
                    throw new IllegalStateException();
            }

            if (dcScan) {
                if (!refining) {
                    for (int i = 0; i < componentHTS.length; i++) {
                        ComponentHT componentHT = componentHTS[i];

                        HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex >> 4];
                        int category = getValue(stream, ht) & 0x0F;
                        int bits = stream.readBits(category);
                        int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
                        componentHT.prevCoefficientValue = dcValue;

                        dcValue = dcValue << bitPositionCurrenScan;
//                        mcu.coeffs[componentHT.componentIndex][0] = dcValue;

                        int x = index % blocksWidth;
                        int y = index / blocksWidth;
                        int position = ((x * stepH) + (y * stepV) * width) * 3;
                        buffer[position + componentHT.componentIndex] = dcValue;
                    }
                    this.index++;
                } else {
                    for (int i = 0; i < componentHTS.length; i++) {
                        ComponentHT componentHT = componentHTS[i];
                        int bit = stream.readBit();
//                        mcu.coeffs[componentHT.componentIndex][0] |= bit << bitPositionCurrenScan;
                        int x = index % blocksWidth;
                        int y = index / blocksWidth;
                        int position = ((x * stepH) + (y * stepV) * width) * 3;
//                        mcu.coeffs[componentHT.componentIndex][0] |= bit << bitPositionCurrenScan;
                        buffer[position + componentHT.componentIndex] |= bit << bitPositionCurrenScan;
                    }
                    this.index++;
                }
            } else {
                int y = index / blocksWidth;
                int x = index - y * blocksWidth;
                ComponentHT componentHT = componentHTS[0];
                HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex + 16];

                if (!refining) {
                    int shift = bitPositionCurrenScan;

                    int k = spectralSelectionStart;
                    do {
                        int zig;
                        int c, r, s;
                        int rs = getValue(stream, ht) & 0xFF;
                        s = rs & 15;
                        r = rs >> 4;
                        if (s == 0) {
                            if (r < 15) {
                                int eobRun = (1 << r) - 1;
                                if (r != 0) {
                                    eobRun += stream.readBits(r);
                                }
                                this.index += eobRun;
                                break;
                            }
                            k += 16;
                        } else {
                            k += r;
                            int ii = SCAN_ZIGZAG[k++];
                            int xr = ii & 0x07;
                            int yr = ii >> 3;
                            int position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.componentIndex;

                            int bits = stream.readBits(s);
                            int value = convertToValue(bits, s);
                            value = value << bitPositionCurrenScan;
                            buffer[position] = value;
                        }
                    } while (k <= spectralSelectionEnd);
                    this.index++;
                } else {
                    int bit = 1 << bitPositionCurrenScan;
                    if (eobRun > 0) {
                        eobRun--;
//                        eobRun-=eobStep;
                        for (int k = spectralSelectionStart; k <= spectralSelectionEnd; k++) {
                            int ii = SCAN_ZIGZAG[k];
                            int xr = ii & 0x07;
                            int yr = ii >> 3;
                            int position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.componentIndex;
                            int prevValue = buffer[position];
                            if (prevValue != 0)
                                if (stream.readBit() != 0) {
                                    if ((prevValue & bit) == 0) {
                                        if (prevValue > 0)
                                            buffer[position] = prevValue + bit;
                                        else
                                            buffer[position] = prevValue - bit;
                                    }
                                }
                        }
                    } else {
                        int k = spectralSelectionStart;
                        do {
                            int rs = getValue(stream, ht) & 0xFF;
                            int s = rs & 0x0F;
                            int r = rs >> 4;
                            if (s == 0) {
                                if (r < 15) {
                                    eobRun = (1 << r) - 1;
                                    if (r != 0) {
                                        int value = stream.readBits(r);
                                        eobRun += value;
                                    }
                                    r = 64; // force end of block
                                } else {
                                    // r=15 s=0 should write 16 0s, so we just do
                                    // a run of 15 0s and then write s (which is 0),
                                    // so we don't have to do anything special here
                                }
                            } else {
                                if (s != 1) throw new IllegalStateException();
                                // sign bit
                                if (stream.readBit() != 0)
                                    s = bit;
                                else
                                    s = -bit;
                            }

                            while (k <= spectralSelectionEnd) {
                                int ii = SCAN_ZIGZAG[k++];
                                int xr = ii & 0x07;
                                int yr = ii >> 3;
                                int position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.componentIndex;
                                int prevValue = buffer[position];
                                if (prevValue != 0) {
                                    if (stream.readBit() != 0) {
                                        if ((prevValue & bit) == 0) {
                                            if (prevValue > 0)
                                                buffer[position] = prevValue + bit;
                                            else
                                                buffer[position] = prevValue - bit;
                                        }
                                    }
                                } else {
                                    if (r == 0) {
                                        buffer[position] = s;
                                        break;
                                    }
                                    --r;
                                }
                            }
                        } while (k <= spectralSelectionEnd);
                    }
                    this.index++;
                }
            }

        }
    }

    static class MCU {
        final int[][] coeffs;

        MCU(int components) {
            this.coeffs = new int[components][64];
        }

        public void clear() {
            for (int i = 0; i < coeffs.length; i++) {
                if (coeffs[i] != null)
                    Arrays.fill(coeffs[i], 0);
            }
        }
    }


    protected void processIdct(MCU mcu, DecodedData[] idcts, int sh, int sv) {
        for (int i = 0; i < mcu.coeffs.length; i++) {
            int[] coeff = mcu.coeffs[i];
            if (coeff == null)
                continue;

            DecodedData idct = idcts[i];
            int componentIndex = i < sh * sv ? 0 : i - (sh * sv - 1);
            float[] quantizationTable = quantizationTables[components[componentIndex].quantizationTable];
            for (int j = 0; j < 64; j++) {
//                idct.base[j] = coeff[j] * quantizationTable[ZIGZAG[j]];
                idct.data[j] = coeff[j] * quantizationTable[j];
            }
            IDCT.inverseDCT8x8(idct.data);
        }
    }

    static boolean isAllElementsEqual(float[] arr, float f) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != f)
                return false;
        }
        return true;
    }

    private void finishDecoding(PixelSetter pixelSetter, int index, int blocksWidth, DecodedData[] idcts) {
        int y = index / blocksWidth;
        int x = index - y * blocksWidth;

        int cbIndex = idcts.length - 2;
        int crIndex = idcts.length - 1;

        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {

//                        int position = (x * 8 + xx) + (y * 8 + yy) * width;
                float Y = idcts[0].get(xx, yy);
                float Cb;
                float Cr;
//                        if (sh == 1 && sv == 1) {
                Cb = idcts[1].get(xx, yy);
                Cr = idcts[2].get(xx, yy);
//                        } else {
//                            int cx = (xx + (x % sh) * 8) / sv;
//                            int cy = (yy + (y % sv) * 8) / sh;
//                            Cb = idcts[cbIndex].get(cx, cy);
//                            Cr = idcts[crIndex].get(cx, cy);
//                        }
//                        Y=0;
//                        Cb=0;
//                        Cr=0;
                int rgb = colorConversion(Y, Cb, Cr);
//                        int rgb = colorConversion(clamp(Y+128)-128, clamp(Cb+128)-128, clamp(Cr+128)-128);

//                        {
////                            int R = position >> 16;
////                            int G = (position >> 8) & 0xFF;
////                            int B = position & 0xFF;
//                            int R = position & width;
//                            int G = position / width;
//                            int B = 128;
//
//                            int r = rgb >> 16;
//                            int g = (rgb >> 8) & 0xFF;
//                            int b = rgb & 0xFF;
//                            if (R != r || G != g || B != b) {
//                                        System.out.println();
//                            }
//                        }

                int pixelY = (y << 3) + yy;
                int pixelX = (x << 3) + xx;
                if (pixelY >= height)
                    continue;
                if (pixelX >= width)
                    continue;
//                pixelSetter.set(pixelX, pixelY, rgb);
                pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));

            }
        }
    }

    private void finishDecoding(
            PixelSetter pixelSetter,
            int index,
            int blocksWidth,
            DecodedData[] idcts,
            int sh,
            int sv,
            UpSampler upSampler,
            MCUsHolder cbData,
            MCUsHolder crData,
            float[] prevRowY
    ) {
//        int y = index / blocksWidth;
//        int x = index - y * blocksWidth;
        int x = cbData.x << 3;
        int y = cbData.y << 3;

//        int cbIndex = idcts.length - 2;
//        int crIndex = idcts.length - 1;


        if (sv == 2 && sh == 2) {
            if (y > 0) {
                int lr = cbData.lastRow;
                cbData.lastRow = -1;
                crData.lastRow = -1;

                for (int xx = 0; xx < 16; xx++) {
                    int cy = 15;

                    int pixelY = y - 1;
                    int pixelX = x + xx;
                    if (pixelY >= height)
                        continue;
                    if (pixelX >= width)
                        continue;

                    float Y = prevRowY[xx + cbData.index * 16];

                    float Cb = upSampler.get(cbData, xx, cy);
                    float Cr = upSampler.get(crData, xx, cy);
//                        Y = 0;
//                        Cb = 0;
//                        Cr = 0;

                    int rgb = colorConversion(Y, Cb, Cr);
//                    pixelSetter.set(pixelX, pixelY, rgb);
                    pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));
                }
                cbData.lastRow = lr;
                crData.lastRow = lr;
            }

            boolean renderLastRow = cbData.lastRow == 15;
            for (int j = 0, yi = 0; j < sv; j++) {
                for (int i = 0; i < sh; i++, yi++) {
                    for (int yy = 0; yy < 8 - (renderLastRow ? 0 : j); yy++) {
                        for (int xx = 0; xx < 8; xx++) {
                            DecodedData idct = idcts[yi];
                            int cx = xx + i * 8;
                            int cy = yy + j * 8;

                            int pixelY = y + cy;
                            int pixelX = x + cx;
                            if (pixelY >= height)
                                continue;
                            if (pixelX >= width)
                                continue;

                            if (pixelX == 497 && pixelY == 319) {
                                x = x;
                            }
                            if (pixelX == 926 && pixelY == 983) {
                                x = x;
                            }

                            float Y = idct.get(xx, yy);
                            float Cb = upSampler.get(cbData, cx, cy);
                            float Cr = upSampler.get(crData, cx, cy);
//                                Y = 0;
//                                Cb = 0;
//                                Cr = 0;

                            int rgb = colorConversion(Y, Cb, Cr);
//                            pixelSetter.set(pixelX, pixelY, rgb);
                            pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));
                        }
                    }
                }
            }
        } else {
            for (int sy = 0, yi = 0; sy < sv; sy++) {
                for (int sx = 0; sx < sh; sx++, yi++) {
                    sx = sx;
                    for (int yy = 0; yy < 8; yy++) {
                        for (int xx = 0; xx < 8; xx++) {

//                                        IDCTFloat idct = matL[j + i * sh];
                            int cx = xx + sx * 8;
                            int cy = yy + sy * 8;

//                            int position = (x + cx) + (y + cy) * width;
                            int pixelY = y + cy;
                            int pixelX = x + cx;
                            if (pixelY >= height)
                                continue;
                            if (pixelX >= width)
                                continue;

                            DecodedData idct = idcts[yi];
                            float Y = idct.get(xx, yy);

                            float Cb = upSampler.get(cbData, cx, cy);
                            float Cr = upSampler.get(crData, cx, cy);
//                            Cb = 0;
//                            Cr = 0;

                            if (pixelX == 8 && pixelY == 0) {
                                sx = sx;
                            }

                            int rgb = colorConversion(Y, Cb, Cr);
//                            pixelSetter.set(pixelX, pixelY, rgb);
                            pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));
                        }

                    }
                }
            }

        }


//        for (int sy = 0; sy < sv; sy++) {
//            for (int sx = 0; sx < sh; sx++) {
//
//                for (int yy = 0; yy < 8; yy++) {
//                    for (int xx = 0; xx < 8; xx++) {
//
////                        int position = (x * 8 + xx) + (y * 8 + yy) * width;
//                        float Y = idcts[sx + sy * sh].get(xx, yy);
//                        float Cb;
//                        float Cr;
//                        if (sh == 1 && sv == 1) {
//                            Cb = idcts[1].get(xx, yy);
//                            Cr = idcts[2].get(xx, yy);
//                        } else {
//                            int cx = (xx + (x % sh) * 8) / sv;
//                            int cy = (yy + (y % sv) * 8) / sh;
//                            Cb = idcts[cbIndex].get(cx, cy);
//                            Cr = idcts[crIndex].get(cx, cy);
//                        }
////                        Y=0;
////                        Cb=0;
////                        Cr=0;
//                        int rgb = colorConversion(Y, Cb, Cr);
////                        int rgb = colorConversion(clamp(Y+128)-128, clamp(Cb+128)-128, clamp(Cr+128)-128);
//
////                        {
//////                            int R = position >> 16;
//////                            int G = (position >> 8) & 0xFF;
//////                            int B = position & 0xFF;
////                            int R = position & width;
////                            int G = position / width;
////                            int B = 128;
////
////                            int r = rgb >> 16;
////                            int g = (rgb >> 8) & 0xFF;
////                            int b = rgb & 0xFF;
////                            if (R != r || G != g || B != b) {
////                                        System.out.println();
////                            }
////                        }
//
//                        pixelSetter.set((x + sx) * 8 + xx, (y + sy) * 8 + yy, rgb);
//
//                    }
//                }
//
//            }
//        }
    }

    public ComponentInfo[] getComponents() {
        return components;
    }

    protected void readProgressiveScan(BitStream stream, int[] buffer) throws IOException {
        int length = stream.readShortUnsafe();
        int numberOfComponents = stream.readByteUnsafe();
        ComponentHT[] componentHTS = new ComponentHT[numberOfComponents];
        for (int i = 0; i < numberOfComponents; i++) {
            Component component = Component.valueOf(stream.readByteUnsafe());
            int tableIndex = stream.readByteUnsafe();
            int componentIndex = findComponentIndex(components, component);
            if (componentIndex == -1)
                throw new IllegalArgumentException("Cannot find component " + component);

            int mcuIndex = componentIndex == 0 ? 0 : componentIndex + (components[0].samplingFactorHorizontal * components[0].samplingFactorVertical - 1);
            int quantizationTableIndex = components[componentIndex].quantizationTable;
            componentHTS[i] = new ComponentHT(component, tableIndex, quantizationTableIndex, componentIndex, mcuIndex);
        }

        int spectralSelectionStart = stream.readByteUnsafe();
        int spectralSelectionEnd = stream.readByteUnsafe();
        int tmpByte = stream.readByteUnsafe();
        int successiveApproximationHigh = tmpByte >> 4;
        int successiveApproximationLow = tmpByte & 0x0F;

        int blocksWidth = ((width % 8 != 0) ? (int) (width / 8.0 + 1) * 8 : width) / 8;
        int blocksHeight = ((height % 8 != 0) ? (int) (height / 8.0 + 1) * 8 : height) / 8;

        boolean refining;
        if (successiveApproximationHigh == 0) {
            refining = false;
        } else if (successiveApproximationHigh - successiveApproximationLow == 1) {
            refining = true;
        } else
            throw new IllegalStateException("Progressive JPEG images cannot contain more than 1 bit for each value on a refining scan.");

        boolean dcScan;
        if (spectralSelectionStart == 0 && spectralSelectionEnd == 0) {
            dcScan = true;
        } else if (spectralSelectionStart > 0 && spectralSelectionEnd >= spectralSelectionStart) {
            dcScan = false;
        } else
            throw new IllegalStateException("Progressive JPEG images cannot contain both DC and AC values in the same scan.");

        System.out.println("scan " + (currentScan++));
        System.out.println("Components: " + (Arrays.stream(componentHTS).map(it -> it.component).collect(Collectors.toList())));
        System.out.println("Spectral selection: " + spectralSelectionStart + "-" + spectralSelectionEnd + " " + (dcScan ? "DC" : "AC"));
        System.out.println("Successive approximation: " + successiveApproximationHigh + "-" + successiveApproximationLow + " " + (refining ? "(refining scan)" : "(first scan)"));

        if (!dcScan && componentHTS.length > 1)
            throw new IllegalStateException("An AC progressive scan can only have a single color component.");

        int stepH = 8;
        int stepV = 8;
        if (componentHTS.length == 1 && componentHTS[0].component != Component.Y && (components[0].samplingFactorHorizontal != 1 || components[0].samplingFactorVertical != 1)) {
            stepH = 8 * components[0].samplingFactorHorizontal;
            stepV = 8 * components[0].samplingFactorVertical;
            blocksWidth = ((width % stepH != 0) ? (int) (width * 1.0 / stepH + 1) * stepH : width) / stepH;
            blocksHeight = ((height % stepV != 0) ? (int) (height * 1.0 / stepV + 1) * stepV : height) / stepV;
        }
        if (dcScan) {
            if (!refining) {
                if (componentHTS.length > 3)
                    throw new IllegalStateException();

                int sv = components[0].samplingFactorVertical;
                int sh = components[0].samplingFactorHorizontal;
                if (numberOfComponents == 3 && (sv > 1 || sh > 1)) {
                    for (int y = 0; y < blocksHeight; y += sv) {
                        for (int x = 0; x < blocksWidth; x += sh) {
                            for (int i = 0; i < componentHTS.length; i++) {
                                ComponentHT componentHT = componentHTS[i];
                                HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex >> 4];
                                if (i == 0) {
                                    for (int yy = 0; yy < sv; yy++) {
                                        for (int xx = 0; xx < sh; xx++) {
                                            int category = getValue(stream, ht) & 0x0F;
                                            int bits = stream.readBits(category);
                                            int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
                                            componentHT.prevCoefficientValue = dcValue;

                                            int pixelX = (x + xx) * stepH;
                                            int pixelY = (y + yy) * stepV;
                                            if (pixelX >= width || pixelY >= height)
                                                continue;

                                            int position = (pixelX + pixelY * width) * 3;
                                            dcValue = dcValue << successiveApproximationLow;
                                            buffer[position + componentHT.componentIndex] = dcValue;
                                        }
                                    }
                                } else {
                                    int category = getValue(stream, ht) & 0x0F;
                                    int bits = stream.readBits(category);
                                    int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
                                    componentHT.prevCoefficientValue = dcValue;

                                    int position = ((x * stepH) + (y * stepV) * width) * 3;
                                    dcValue = dcValue << successiveApproximationLow;
                                    buffer[position + componentHT.componentIndex] = dcValue;
                                }
                            }
                        }
                    }
                } else {
                    for (int y = 0; y < blocksHeight; y++) {
                        for (int x = 0; x < blocksWidth; x++) {
                            for (int i = 0; i < componentHTS.length; i++) {
                                ComponentHT componentHT = componentHTS[i];

                                HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex >> 4];
                                int category = getValue(stream, ht) & 0x0F;
                                int bits = stream.readBits(category);
                                int dcValue = convertToValue(bits, category) + componentHT.prevCoefficientValue;
                                componentHT.prevCoefficientValue = dcValue;

                                int position = ((x * stepH) + (y * stepV) * width) * 3;
                                dcValue = dcValue << successiveApproximationLow;
                                buffer[position + componentHT.componentIndex] = dcValue;
                            }
                        }
                    }
                }

            } else {
                int sv = components[0].samplingFactorVertical;
                int sh = components[0].samplingFactorHorizontal;
                if (numberOfComponents == 3 && (sv > 1 || sh > 1)) {
                    for (int y = 0; y < blocksHeight; y += sv) {
                        for (int x = 0; x < blocksWidth; x += sh) {
                            for (int i = 0; i < componentHTS.length; i++) {
                                if (i == 0) {
                                    for (int yy = 0; yy < sv; yy++) {
                                        for (int xx = 0; xx < sh; xx++) {
                                            int position = (((x + xx) * stepH) + ((y + yy) * stepV) * width) * 3;
                                            int bit = stream.readBit();
                                            buffer[position + i] |= bit << successiveApproximationLow;
                                        }
                                    }
                                } else {
                                    int position = ((x * stepH) + (y * stepV) * width) * 3;
                                    int bit = stream.readBit();
                                    buffer[position + i] |= bit << successiveApproximationLow;
//                            System.out.println((position / 3 % width) + "x" + (position / 3 / width) + "x" + i + ": " + buffer[position + i]);
                                }
                            }
                        }
                    }
                } else {
                    for (int y = 0; y < blocksHeight; y++) {
                        for (int x = 0; x < blocksWidth; x++) {
                            for (int i = 0; i < componentHTS.length; i++) {
                                int position = ((x * stepH) + (y * stepV) * width) * 3;
                                int bit = stream.readBit();
                                buffer[position + i] |= bit << successiveApproximationLow;
//                            System.out.println((position / 3 % width) + "x" + (position / 3 / width) + "x" + i + ": " + buffer[position + i]);
                            }
                        }
                    }
                }
            }
        } else {
            processAcScan2(stream,
                    buffer,
                    componentHTS,
                    spectralSelectionStart,
                    spectralSelectionEnd,
                    successiveApproximationLow,
                    blocksWidth,
                    blocksHeight,
                    refining,
                    stepH,
                    stepV
            );

            int x = 0;
            int y = 0;
            int i = 0;

            for (int yy = 0; yy < 8; yy++) {
                for (int xx = 0; xx < 8; xx++) {
                    int position = ((x * 8 + xx) + (y * 8 + yy) * width) * 3 + i;
                    int j = xx + yy * 8;
//                                idct.base[j] = progressiveBuffer[position] * quantizationTable[j];
                    tmp[j] = buffer[position];
                }
            }
            if (componentHTS[0].componentIndex == 0)
                System.out.println();
        }

        stream.setBitsRemaining(0);
    }

    private void processAcScan2(
            BitStream stream,
            int[] buffer,
            ComponentHT[] componentHTS,
            int spectralSelectionStart,
            int spectralSelectionEnd,
            int successiveApproximationLow,
            int blocksWidth,
            int blocksHeight,
            boolean refining,
            int stepH,
            int stepV
    ) throws IOException {
        ComponentHT componentHT = componentHTS[0];
        HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex + 16];
        int eobRun = 0;

        int sv = components[componentHT.componentIndex].samplingFactorVertical;
        int sh = components[componentHT.componentIndex].samplingFactorHorizontal;

        int mcu = 0;
        int mcuCount = blocksHeight * blocksWidth;
        for (int y = 0; y < blocksHeight; y += sv) {
            for (int x = 0; x < blocksWidth; x += sh) {
                for (int yy = 0; yy < sv; yy++) {
                    for (int xx = 0; xx < sh; xx++) {
                        if (x + xx >= blocksWidth)
                            continue;

//        while (mcu < mcuCount) {
//            int y = mcu / blocksWidth;
//            int x = mcu - y * blocksWidth;

                        if (!refining) {
                            int shift = successiveApproximationLow;

                            if (eobRun > 0) {
                                eobRun--;
//                        mcu++;
                                continue;
                            }

                            int k = spectralSelectionStart;
                            do {
                                int zig;
                                int c, r, s;
                                int rs = getValue(stream, ht) & 0xFF;
                                s = rs & 15;
                                r = rs >> 4;
                                if (s == 0) {
                                    if (r < 15) {
                                        eobRun = (1 << r) - 1;
                                        if (r != 0) {
                                            eobRun += stream.readBits(r);
                                        }
                                        break;
                                    }
                                    k += 16;
                                } else {
                                    k += r;
                                    int ii = SCAN_ZIGZAG[k++];
                                    int xr = ii & 0x07;
                                    int yr = ii >> 3;
                                    int bits = stream.readBits(s);
                                    int value = convertToValue(bits, s);
                                    int pixelY = (y + yy) * stepV + yr;
                                    int pixelX = (x + xx) * stepH + xr;
//                                    if (pixelX < width && pixelY < height) {
                                    int position = (pixelX + pixelY * width) * 3 + componentHT.componentIndex;
                                    value = value << successiveApproximationLow;
                                    buffer[position] = value;
//                                    }
                                }
                            } while (k <= spectralSelectionEnd);
                        } else {
                            int bit = 1 << successiveApproximationLow;
                            if (eobRun > 0) {
                                eobRun--;
                                for (int k = spectralSelectionStart; k <= spectralSelectionEnd; k++) {
                                    int ii = SCAN_ZIGZAG[k];
                                    int xr = ii & 0x07;
                                    int yr = ii >> 3;
                                    int pixelX = (x + xx) * stepH + xr;
                                    int pixelY = (y + yy) * stepV + yr;
                                    int position = (pixelX + pixelY * width) * 3 + componentHT.componentIndex;
                                    int prevValue = 0;
//                                    if (pixelX < width && pixelY < height) {
                                    prevValue = buffer[position];
//                                    }else{
//                                        stream.readBit();
//                                    }
                                    if (prevValue != 0)
                                        if (stream.readBit() != 0) {
                                            if ((prevValue & bit) == 0) {
                                                if (prevValue > 0)
                                                    buffer[position] = prevValue + bit;
                                                else
                                                    buffer[position] = prevValue - bit;
                                            }
                                        }
                                }
                            } else {
                                int k = spectralSelectionStart;
                                do {
                                    int rs = getValue(stream, ht) & 0xFF;
                                    int s = rs & 0x0F;
                                    int r = rs >> 4;
                                    if (s == 0) {
                                        if (r < 15) {
                                            eobRun = (1 << r) - 1;
                                            if (r != 0) {
                                                int value = stream.readBits(r);
                                                eobRun += value;
                                            }
                                            r = 64; // force end of block
                                        } else {
                                            // r=15 s=0 should write 16 0s, so we just do
                                            // a run of 15 0s and then write s (which is 0),
                                            // so we don't have to do anything special here
                                        }
                                    } else {
                                        if (s != 1) throw new IllegalStateException();
                                        // sign bit
                                        if (stream.readBit() != 0)
                                            s = bit;
                                        else
                                            s = -bit;
                                    }

                                    while (k <= spectralSelectionEnd) {
                                        int ii = SCAN_ZIGZAG[k++];
                                        int xr = ii & 0x07;
                                        int yr = ii >> 3;
                                        int pixelX = (x + xx) * stepH + xr;
                                        int pixelY = (y + yy) * stepV + yr;
                                        int position = (pixelX + pixelY * width) * 3 + componentHT.componentIndex;

                                        int prevValue = 0;
//                                        if (pixelX < width && pixelY < height) {
                                        prevValue = buffer[position];
//                                        }
                                        if (prevValue != 0) {
                                            if (stream.readBit() != 0) {
                                                if ((prevValue & bit) == 0) {
                                                    if (prevValue > 0)
                                                        buffer[position] = prevValue + bit;
                                                    else
                                                        buffer[position] = prevValue - bit;
                                                }
                                            }
                                        } else {
                                            if (r == 0) {
                                                buffer[position] = s;
                                                break;
                                            }
                                            --r;
                                        }
                                    }
                                } while (k <= spectralSelectionEnd);
                            }
                        }
//            mcu++;
                    }
                }
            }
        }
    }

    private void processAcScan(
            BitStream stream,
            int[] buffer,
            ComponentHT[] componentHTS,
            int spectralSelectionStart,
            int spectralSelectionEnd,
            int successiveApproximationLow,
            int blocksWidth,
            int blocksHeight,
            boolean refining,
            int stepH,
            int stepV
    ) throws IOException {
        ComponentHT componentHT = componentHTS[0];
        HuffmanTable ht = huffmanTables[componentHT.huffmanTableIndex + 16];
        int eobRun = 0;
        int zeroRun = 0;
        int refiningLength = 0;

        int mcu = 0;
        int mcuCount = blocksHeight * blocksWidth;
        while (mcu < mcuCount) {
            int y = mcu / blocksWidth;
            int x = mcu - y * blocksWidth;
            int i = spectralSelectionStart;
            while (i <= spectralSelectionEnd) {
                int code = getValue(stream, ht) & 0xFF;
//                    if (currentScan == 5)
//                        System.out.println(x + "x" + y + ": " + code);
//                    if (currentScan == 5 && x == 5 && y == 4) {
//                        System.out.println();
//                    }
                if (code == 0) {
                    eobRun = 1;
                    break;
                }
                int bitLength = code & 0x0F;
                int runMagnitude = code >> 4;
                if (code == 0xF0) {
                    zeroRun = 16;
                } else if (bitLength == 0) {
                    int value = stream.readBits(runMagnitude);
                    eobRun = (1 << runMagnitude) + value;
                    break;
                } else {
                    zeroRun = runMagnitude;
                }

                if (!refining && zeroRun != 0) {
                    i += zeroRun;
                    zeroRun = 0;
                } else {
                    while (zeroRun > 0) {
                        int ii = SCAN_ZIGZAG[i];
                        int xr = ii & 0x07;
                        int yr = ii >> 3;
                        int position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.componentIndex;
                        int value = buffer[position];

                        if (value == 0) {
                            zeroRun -= 1;
                        } else {
//                                    to_refine.append((x * 8 + xr, y * 8 + yr))
                            tmp[refiningLength++] = position;
//                                System.out.println();
                        }
                        i++;
                    }
                }

                if (bitLength > 0) {
                    int bits = stream.readBits(bitLength);
                    int value = convertToValue(bits, bitLength);
                    value = value << successiveApproximationLow;
//                        if (value >= 256 ) {
//                            throw new IllegalStateException();
//                        }
                    int ii = SCAN_ZIGZAG[i];
                    int xr = ii & 0x07;
                    int yr = ii >> 3;
                    int position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.componentIndex;
                    if (refining) {
                        while (buffer[position] != 0) {
//                                int bit = stream.readBit();
//                                buffer[position] |= bit << bitPositionCurrenScan;
                            tmp[refiningLength++] = position;
                            i++;
                            ii = SCAN_ZIGZAG[i];
                            xr = ii & 0x07;
                            yr = ii >> 3;
                            position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.componentIndex;
                        }
//                            System.out.println();
                    }

//                        if (value > 256) {
//                            System.out.println();
//                        }

//                        if(position + componentHT.componentIndex == 364){
//                            System.out.println();
//                        }

                    if (buffer[position] != 0) {
                        System.out.println("buffer[position] != 0");
                    }
                    buffer[position] = value;
                    i++;
                }
                if (refining) {
                    if (refiningLength > 0) {
                        int bits = stream.readBits(refiningLength);
//                            System.out.println("refining " + Integer.toBinaryString(bits));
                        for (int j = 0; j < refiningLength; j++) {
                            int p = tmp[j];
//                                int bit = stream.readBit();
                            int bit = bits >> (refiningLength - j - 1) & 1;
                            buffer[p] |= bit << successiveApproximationLow;
//                                System.out.println((p / 3 % width) + "x" + (p / 3 / width) + "x" + componentHT.componentIndex + ": " + buffer[p]);
                        }
                        refiningLength = 0;
                    }
                }
            }
            if (i > spectralSelectionEnd) {
                mcu++;
                if (refining) {
//                        System.out.println();
                    y = mcu / blocksWidth;
                    x = mcu - y * blocksWidth;
                }
            }
            if (!refining) {
                mcu += eobRun;
                eobRun = 0;
            } else {
                while (eobRun > 0) {
                    int ii = SCAN_ZIGZAG[i];
                    int xr = ii & 0x07;
                    int yr = ii >> 3;
                    int position = ((x * stepH + xr) + (y * stepV + yr) * width) * 3 + componentHT.componentIndex;
                    if (buffer[position] != 0) {
                        int bit = stream.readBit();
                        buffer[position] |= bit << successiveApproximationLow;
//                            System.out.println((p / 3 % width) + "x" + (p / 3 / width) + "x" + componentHT.componentIndex + ": " + buffer[p]);
                    }
                    i++;
                    if (i > spectralSelectionEnd) {
                        eobRun--;
                        mcu++;
                        y = mcu / blocksWidth;
                        x = mcu - y * blocksWidth;
                        i = spectralSelectionStart;
                    }
                }

//                    if (refiningLength > 0) {
//                        if(refiningLength>32){
//                            System.out.println();
//                        }
//                        int bits = stream.readBits(refiningLength);
////                        System.out.println("refining " + Integer.toBinaryString(bits));
//                        for (int j = 0; j < refiningLength; j++) {
//                            int p = tmp[j];
////                            int bit = stream.readBit();
//                            int bit = bits >> (refiningLength - j - 1) & 1;
//                            buffer[p] |= bit << bitPositionCurrenScan;
////                            System.out.println((p / 3 % width) + "x" + (p / 3 / width) + "x" + componentHT.componentIndex + ": " + buffer[p]);
//                        }
//                        refiningLength = 0;
//                    }
            }
        }
    }

    private static int findComponentIndex(ComponentInfo[] components, Component component) {
        for (int i = 0; i < components.length; i++) {
            ComponentInfo info = components[i];
            if (info.component == component)
                return i;
        }
        return -1;
    }

    private void readScan(BitStream stream, PixelSetter pixelSetter) throws IOException {
        int length = stream.readShortUnsafe();
        int numberOfComponents = stream.readByteUnsafe() & 0xFF;
        ComponentHT[] componentHTS = new ComponentHT[numberOfComponents];
        for (int i = 0; i < numberOfComponents; i++) {
            Component component = Component.valueOf(stream.readByteUnsafe() & 0xFF);
            int tableIndex = stream.readByteUnsafe() & 0xFF;
            int componentIndex = findComponentIndex(components, component);
            if (componentIndex == -1)
                throw new IllegalArgumentException("Cannot find component " + component);

            int mcuIndex = componentIndex == 0 ? 0 : componentIndex + (components[0].samplingFactorHorizontal * components[0].samplingFactorVertical - 1);
            int quantizationTableIndex = components[componentIndex].quantizationTable;
            componentHTS[i] = new ComponentHT(component, tableIndex, quantizationTableIndex, componentIndex, mcuIndex);
        }

        stream.skip(3);
//        int spectralSelectionStart = stream.readByteUnsafe() & 0xFF;
//        int spectralSelectionEnd = stream.readByteUnsafe() & 0xFF;
//        int b = stream.readByteUnsafe();
//        int bitPositionPrevScan = (b & 0xFF) >> 4;
//        int bitPositionCurrenScan = (b & 0x0F);


//        int blocksWidth = ((width % 8 != 0) ? (int) (Math.floor(width / 8.0) + 1) * 8 : width) / 8;
//        int blocksHeight = ((height % 8 != 0) ? (int) (Math.floor(height / 8.0) + 1) * 8 : height) / 8;
        int blocksWidth = ((width % 8 != 0) ? (int) (width / 8.0 + 1) * 8 : width) / 8;
        int blocksHeight = ((height % 8 != 0) ? (int) (height / 8.0 + 1) * 8 : height) / 8;
        if (numberOfComponents == 3) {
            int oldLumDcCoeff = 0;
            int oldCrDcCoeff = 0;
            int oldCbDcCoeff = 0;

            ComponentInfo yComponent = components[0];
            DecodedData[] matL = new DecodedData[yComponent.samplingFactorHorizontal * yComponent.samplingFactorVertical];
            DecodedData[] matLPrev = new DecodedData[yComponent.samplingFactorHorizontal * yComponent.samplingFactorVertical];
            for (int i = 0; i < matL.length; i++) {
                matL[i] = new DecodedData();
                matLPrev[i] = new DecodedData();
            }

            int sv = yComponent.samplingFactorVertical;
            int sh = yComponent.samplingFactorHorizontal;

            MCUsHolder cbData = new MCUsHolder();
            MCUsHolder crData = new MCUsHolder();
            float[] prevRowY = null;

            UpSampler upSampler = null;
            UpSampler upSamplerCenter = null;
            if (sv != 1 || sh != 1) {
                upSampler = UpSampler.createUpSampler(sv, sh);
                if (sv == 2 && sh == 2) {
                    upSamplerCenter = UpSampler.createUpSampler(sv, sh, true);
                    cbData.prevRow = new float[(blocksWidth + 1) / 2 * 8];
                    crData.prevRow = new float[(blocksWidth + 1) / 2 * 8];
                    prevRowY = new float[blocksWidth * 8];
                }
            }

            int lastBlockInRow = sh == 1 ? blocksWidth - sh : ((blocksWidth & 1) == 1 ? blocksWidth / sh * sh : blocksWidth - sh);
            int lastRow = height - 1 - (blocksHeight >> 1) * 16;
            if (lastRow == -1)
                lastRow = 15;
            int lastColumn = (width - 1) & 0xF;
            int restartCounter = restartInterval;

//            int blocksHeightSv = sv == 2 && (blocksHeight & 1) == 0 ? blocksHeight + 1 : blocksHeight;
            outer:
            for (int y = 0; y < blocksHeight; y += sv) {
                for (int x = 0; x < blocksWidth; x += sh) {
//                    if (sv == 2 && sh == 2 && y > 0) {
//                        System.arraycopy(matL[2].base, 56, prevRowY, (x >> 1) * 16, 8);
//                        System.arraycopy(matL[3].base, 56, prevRowY, (x >> 1) * 16 + 8, 8);
//                    }
                    for (int i = 0; i < matL.length; i++) {
                        try {
//                            System.out.print(x + "x" + y + " ");
                            oldLumDcCoeff = buildMatrix(stream, 0, oldLumDcCoeff, quantizationTables[yComponent.quantizationTable], matL[i]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break outer;
                        }
                    }

                    {
                        DecodedData[] arr = matLPrev;
                        matLPrev = matL;
                        matL = arr;
                    }

                    int lr;
                    if ((blocksHeight & 1) == 0)
                        lr = (y + sv >= blocksHeight) ? lastRow : -1;
                    else
                        lr = (y + sv > blocksHeight) ? lastRow : -1;
                    cbData.updateIndex((x - sh) / sh, x - sh, y, -1, lr);
                    cbData.shift();

                    crData.updateIndex((x - sh) / sh, x - sh, y, -1, lr);
                    crData.shift();
                    if (y == 48) {
                        y = y;
                    }
//
//                    if (sv == 2 && sh == 2 && y > 0) {
//                        if (true) {
//                            System.out.print("put " + (cbData.index + 1) * 8 + " ");
//                            printFloats(cbData.next, 56, 8);
////                            System.out.println();
//                        }
//                        cbData.storeLastRowForNext();
//                        crData.storeLastRowForNext();
//                    }

                    oldCbDcCoeff = buildMatrix(stream, 1, oldCbDcCoeff, quantizationTables[components[1].quantizationTable], cbData.next);
                    oldCrDcCoeff = buildMatrix(stream, 1, oldCrDcCoeff, quantizationTables[components[2].quantizationTable], crData.next);

                    if (--restartCounter == 0) {
                        oldLumDcCoeff = 0;
                        oldCbDcCoeff = 0;
                        oldCrDcCoeff = 0;

                        if (y == 122) {
                            y = y;
                        }
//                        System.out.println("reset at " + x + "x" + y);
//                        System.out.println("stream.getBitsRemaining(): " + stream.getBitsRemaining());
                        stream.setBitsRemaining(stream.getBitsRemaining() / 8 * 8);
//                        System.out.println("stream.getBitsRemaining(): " + stream.getBitsRemaining());
//                        System.out.println();

                        if (stream.getBitsRemaining() != 0) {
                            y = y;
                        }
                        int b1 = stream.readByteUnsafe(true);
                        int b2 = stream.readByteUnsafe(true);
                        if (b1 != 255 || !(b2 >= 0xD0 && b2 <= 0xD7)) {
                            if (b2 != 0xD9)
                                throw new IllegalStateException("Reset marker expected but not found");
                        }
//                        if(b1!=)

                        restartCounter = restartInterval;
                    }

//                    if(y==64){
//                        System.out.println();
//                    }
//                    if (x == 60 && y == 42) {
//                        x = x;
//                    }

                    if (cbData.index < 0 && y > 0) {
                        cbData.y -= sv;
//                        cbData.x = blocksWidth - sh;
                        cbData.x = lastBlockInRow;
                        cbData.index = (blocksWidth - 1) >> 1;
//                        cbData.index = (blocksWidth - sh ) / sh;
                        cbData.lastColumn = lastColumn;
                        cbData.lastRow = -1;

                        crData.y -= sv;
//                        crData.x = blocksWidth - sh;
                        crData.x = lastBlockInRow;
                        crData.index = (blocksWidth - 1) >> 1;
//                        crData.index = (blocksWidth - sh ) / sh;
                        crData.lastColumn = lastColumn;
                        crData.lastRow = -1;
                    }
                    if (cbData.index >= 0) {
                        if (sv == 2 && sh == 2 && cbData.lastColumn == -1 && cbData.lastRow == -1 && cbData.y > 0 && cbData.x > 0) {
                            processMCU(pixelSetter, matL, sv, sh, cbData, crData, upSamplerCenter, prevRowY);
                        } else {
                            processMCU(pixelSetter, matL, sv, sh, cbData, crData, upSampler, prevRowY);
                        }
                    }

                    if (sv == 2 && sh == 2) {
//                        if (x == 0 || x == 2) {
//                            printFloats(matL[2].base, 56, 8);
//                            printFloats(matL[3].base, 56, 8);
//                            printFloats(matLPrev[2].base, 56, 8);
//                            printFloats(matLPrev[3].base, 56, 8);
//                            System.out.println();
//                        }
//                        System.arraycopy(matLPrev[2].base, 56, prevRowY, (x >> 1) * 16, 8);
//                        System.arraycopy(matLPrev[3].base, 56, prevRowY, (x >> 1) * 16 + 8, 8);
                        if (x > 0) {
                            System.arraycopy(matL[2].data, 56, prevRowY, ((x - 2) >> 1) * 16, 8);
                            System.arraycopy(matL[3].data, 56, prevRowY, ((x - 2) >> 1) * 16 + 8, 8);
                        } else {
//                            int destPos = ((blocksWidth - 1) >> 1) * 16;
//                            int destPos = (blocksWidth / sh * sh) * 8;
                            int destPos = lastBlockInRow * 8;
                            System.arraycopy(matL[2].data, 56, prevRowY, destPos, Math.min(8, prevRowY.length - destPos));
                            if (prevRowY.length > destPos + 8) {
                                destPos += 8;
                                System.arraycopy(matL[3].data, 56, prevRowY, destPos, Math.min(8, prevRowY.length - destPos));
                            }
                        }
                    }
                    if (sv == 2 && sh == 2) {
//                        if (true) {
//                            System.out.print("put " + (cbData.index + 1) * 8 + " ");
//                            printFloats(cbData.next, 56, 8);
////                            System.out.println();
//                        }
                        cbData.storeLastRow();
                        crData.storeLastRow();
                    }
                }
            }

            {
                DecodedData[] arr = matLPrev;
                matLPrev = matL;
                matL = arr;
            }
            int lastRowIndex = (blocksHeight & 1) == 1 ? blocksHeight - 1 : blocksHeight - sv;
            int lastIndex = (blocksWidth - 1) >> 1;
//            int lastIndex = (blocksWidth - sh ) / sh;
            cbData.updateIndex(lastIndex, lastBlockInRow, lastRowIndex, lastColumn, lastRow);
            cbData.shift();

            crData.updateIndex(lastIndex, lastBlockInRow, lastRowIndex, lastColumn, lastRow);
            crData.shift();
            processMCU(pixelSetter, matL, sv, sh, cbData, crData, upSampler, prevRowY);

        }
    }

    private void processMCU(
            PixelSetter pixelSetter,
            DecodedData[] matL,
            int sv,
            int sh,
            MCUsHolder cbData,
            MCUsHolder crData,
            UpSampler upSampler,
            float[] prevRowY
    ) {
        int x = cbData.x * 8;
        int y = cbData.y * 8;
        if (sv == 1 && sh == 1) {
            DecodedData idct = matL[0];
            for (int yy = 0; yy < 8; yy++) {
                for (int xx = 0; xx < 8; xx++) {
//                    int position = (x * 8 + xx) + (y * 8 + yy) * width;
//                                double Y = idct.get(xx, yy);
//                                double Cb = matCb.get(xx, yy);
//                                double Cr = matCr.get(xx, yy);
//                    float Cb = matCb.get(xx, yy);
//                    float Cr = matCr.get(xx, yy);
//                    int position = (x + xx) + (y + yy) * width;

                    float Y = idct.get(xx, yy);
                    float Cb = cbData.get(xx, yy);
                    float Cr = crData.get(xx, yy);

                    int pixelY = y + yy;
                    int pixelX = x + xx;
                    if (pixelY >= height)
                        continue;
                    if (pixelX >= width)
                        continue;

                    if (colorTransform != null) {
//                            int rgb;
//                            rgb = colorTransform.yCbCr2RGB(Y, Cb, Cr);
//                            colorTransform.yCbCr2RGB(Y, Cb, Cr, (r, g, b) -> pixelSetter.set(pixelX, pixelY, r, g, b));
                        colorTransform.yCbCr2RGB(Y, Cb, Cr, pixelX, pixelY, pixelSetter);
                    } else {
                        int rgb = colorConversion(Y, Cb, Cr);

//                    {
//                        int position = (x + xx) + (y + yy) * width;
//                        int R = position >> 16;
//                        int G = (position >> 8) & 0xFF;
//                        int B = position & 0xFF;
//
//                        int r = rgb >> 16;
//                        int g = (rgb >> 8) & 0xFF;
//                        int b = rgb & 0xFF;
//                        if (R != r || G != g || B != b) {
////                                        System.out.println();
//                        }
//                    }

//                    pixelSetter.set(pixelX, pixelY, rgb);
                        pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));
                    }
                }
            }
        } else {
            if (sv == 2 && sh == 2) {
                if (y > 0) {
//                    printFloats(prevRowY, x, 16);
//                    if (x + 8 < width) {
//                        System.out.print("get " + (x >> 1) + " ");
//                        printFloats(cbData.prevRow, x >> 1, 8);
//                        x = x;
//                        System.out.println();
//                    }
                    int lr = cbData.lastRow;
                    cbData.lastRow = -1;
                    crData.lastRow = -1;

                    for (int xx = 0; xx < 16; xx++) {
                        int cy = 15;

                        int pixelY = y - 1;
                        int pixelX = x + xx;
                        if (pixelY >= height)
                            continue;
                        if (pixelX >= width)
                            continue;

                        float Y = prevRowY[xx + cbData.index * 16];
                        if (pixelX == 383 && pixelY == 399) {
                            x = x;
                        }

                        float Cb = upSampler.get(cbData, xx, cy);
                        float Cr = upSampler.get(crData, xx, cy);
//                        float Cb = upSampler.get(cbData, xx, cy) / 4;
//                        float Cr = upSampler.get(crData, xx, cy) / 4;
//                        Y = 0;
//                        Cb = 0;
//                        Cr = 0;

                        if (colorTransform != null) {
//                            int rgb;
//                            rgb = colorTransform.yCbCr2RGB(Y, Cb, Cr);
//                            colorTransform.yCbCr2RGB(Y, Cb, Cr, (r, g, b) -> pixelSetter.set(pixelX, pixelY, r, g, b));
                            colorTransform.yCbCr2RGB(Y, Cb, Cr, pixelX, pixelY, pixelSetter);
                        } else {
                            int rgb;
//                            rgb = colorConversion(Y, Cb, Cr);
//                            pixelSetter.set(pixelX, pixelY, rgb);
//                            pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));
//                            Y += 128 + 0.5f;
                            Y = Math.max(Math.min(Y + 128 + 0.5f, 255.5f), 0.5f);
                            float R = (Cr) * 1.402f + Y;
                            float B = (Cb) * 1.772f + Y;
                            float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);
                            byte red = (byte) clamp(R);
                            byte green = (byte) clamp(G);
                            byte blue = (byte) clamp(B);
                            pixelSetter.set(pixelX, pixelY, red, green, blue);
                        }
                    }
                    cbData.lastRow = lr;
                    crData.lastRow = lr;
                }

                boolean renderLastRow = cbData.lastRow == 15;
                if (renderLastRow) {
                    renderLastRow = renderLastRow;
                }
//                for (int cy = 0; cy < 16; cy++) {
//                    for (int cx = 0; cx < 16; cx++) {
//                        int yi = (cy >> 3) * 2;
//                        int pixelY = y + cy;
//                        if (pixelY >= height)
//                            continue;
//                        IDCTFloat idct = matL[(cx >> 3) + yi];
//                        int pixelX = x + cx;
//                        if (pixelX >= width)
//                            continue;
//
//                        if (pixelY == 399) {
//                            x = x;
//                        }
//
//                        float Y = idct.get(cx & 0x7, cy & 0x7);
//                        float Cb = upSampler.get(cbData, cx, cy);
//                        float Cr = upSampler.get(crData, cx, cy);
////                                Y = 0;
////                                Cb = 0;
////                                Cr = 0;
//
//                        if (colorTransform != null) {
////                                    Y += 128 + 0.5f;
////                                    float R = (Cr) * 1.402f + Y;
////                                    float B = (Cb) * 1.772f + Y;
////                                    float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);
////                                    tmpColor[0]=R;
////                                    tmpColor[1]=G;
////                                    tmpColor[2]=B;
////                                    float[] rgb1 = colorSpace.toRGB(tmpColor);
////                                    int rgb = colorConversion(Y-128.5f, Cb, Cr);
////                                    int rgb = colorConversion(Y, Cb, Cr);
////                                    int rgb = colorTransform.yCbCr2RGB(Y, Cb, Cr);
////                                    pixelSetter.set(pixelX, pixelY, rgb);
////                                    colorTransform.yCbCr2RGB(Y, Cb, Cr, (r, g, b) -> pixelSetter.set(pixelX, pixelY, r, g, b));
//                            colorTransform.yCbCr2RGB(Y, Cb, Cr, pixelX, pixelY, pixelSetter);
//                        } else {
////                                    int rgb = colorConversion(Y, Cb, Cr);
////                                    pixelSetter.set(pixelX, pixelY, rgb);
////                                    pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));
//
//                            Y = Math.max(Math.min(Y + 128 + 0.5f, 255.5f), 0.5f);
//                            float R = (Cr) * 1.402f + Y;
//                            float B = (Cb) * 1.772f + Y;
//                            float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);
//                            byte red = (byte) clamp(R);
//                            byte green = (byte) clamp(G);
//                            byte blue = (byte) clamp(B);
//                            pixelSetter.set(pixelX, pixelY, red, green, blue);
//                        }
//
//                    }
//                }

//                int cyLimit = renderLastRow ? 16 : 15;
//                for (int cy = 0; cy < cyLimit; cy++) {
//                    for (int cx = 0; cx < 16; cx++) {
//                        float Cb = upSampler.get(cbData, cx, cy);
//                        float Cr = upSampler.get(crData, cx, cy);
//                        tmpCb[cx + (cy << 4)] = Cb;
//                        tmpCr[cx + (cy << 4)] = Cr;
//                    }
//                }
                for (int j = 0, yi = 0; j < sv; j++) {
                    for (int i = 0; i < sh; i++, yi++) {
                        for (int yy = 0; yy < 8 - (renderLastRow ? 0 : j); yy++) {
//                            for (int xx = 0; xx < 8; xx++) {
//                                IDCTFloat idct = matL[yi];
//                                int cx = xx + i * 8;
//                                int cy = yy + j * 8;
//
//                                int pixelY = y + cy;
//                                int pixelX = x + cx;
//                                if (pixelY >= height)
//                                    continue;
//                                if (pixelX >= width)
//                                    continue;
//
//                                if (pixelY == 399) {
//                                    x = x;
//                                }
//
//                                float Y = idct.get(xx, yy);
//                                float Cb = upSampler.get(cbData, cx, cy);
//                                float Cr = upSampler.get(crData, cx, cy);
////                                float Cb = tmpCb[cx + (cy << 4)];
////                                float Cr = tmpCr[cx + (cy << 4)];
////                                Y = 0;
////                                Cb = 0;
////                                Cr = 0;
//
//                                if (colorTransform != null) {
////                                    Y += 128 + 0.5f;
////                                    float R = (Cr) * 1.402f + Y;
////                                    float B = (Cb) * 1.772f + Y;
////                                    float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);
////                                    tmpColor[0]=R;
////                                    tmpColor[1]=G;
////                                    tmpColor[2]=B;
////                                    float[] rgb1 = colorSpace.toRGB(tmpColor);
////                                    int rgb = colorConversion(Y-128.5f, Cb, Cr);
////                                    int rgb = colorConversion(Y, Cb, Cr);
////                                    int rgb = colorTransform.yCbCr2RGB(Y, Cb, Cr);
////                                    pixelSetter.set(pixelX, pixelY, rgb);
////                                    colorTransform.yCbCr2RGB(Y, Cb, Cr, (r, g, b) -> pixelSetter.set(pixelX, pixelY, r, g, b));
//                                    colorTransform.yCbCr2RGB(Y, Cb, Cr, pixelX, pixelY, pixelSetter);
//                                } else {
////                                    int rgb = colorConversion(Y, Cb, Cr);
////                                    pixelSetter.set(pixelX, pixelY, rgb);
////                                    pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));
//
//                                    Y = Math.max(Math.min(Y + 128 + 0.5f, 255.5f), 0.5f);
//                                    float R = (Cr) * 1.402f + Y;
//                                    float B = (Cb) * 1.772f + Y;
//                                    float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);
//                                    byte red = (byte) clamp(R);
//                                    byte green = (byte) clamp(G);
//                                    byte blue = (byte) clamp(B);
//                                    pixelSetter.set(pixelX, pixelY, red, green, blue);
//                                }
//
//                            }

//                            for (int xx = 0; xx < 8; xx++) {
//                                int cx = xx + i * 8;
//                                int cy = yy + j * 8;
//
////                                int pixelY = y + cy;
////                                int pixelX = x + cx;
////                                if (pixelY >= height)
////                                    continue;
////                                if (pixelX >= width)
////                                    continue;
//
//                                tmpCb[cx + (cy << 4)] = upSampler.get(cbData, cx, cy);
//                                tmpCr[cx + (cy << 4)] = upSampler.get(crData, cx, cy);
//                            }

                            int cy = yy + j * 8;

                            int pixelY = y + cy;
                            if (pixelY >= height)
                                continue;

                            float[] tmpCb = this.tmpCb;
                            float[] tmpCr = this.tmpCr;
//                            {
//                                int ii = i * 8;
//                                tmpCb[ii] = upSampler.get(cbData, ii, cy);
//                                tmpCb[ii + 1 ] = upSampler.get(cbData, ii + 1, cy);
//                                tmpCb[ii + 2 ] = upSampler.get(cbData, ii + 2, cy);
//                                tmpCb[ii + 3 ] = upSampler.get(cbData, ii + 3, cy);
//                                tmpCb[ii + 4 ] = upSampler.get(cbData, ii + 4, cy);
//                                tmpCb[ii + 5 ] = upSampler.get(cbData, ii + 5, cy);
//                                tmpCb[ii + 6 ] = upSampler.get(cbData, ii + 6, cy);
//                                tmpCb[ii + 7 ] = upSampler.get(cbData, ii + 7, cy);
//
//                                tmpCr[ii] = upSampler.get(crData, ii, cy);
//                                tmpCr[ii + 1 ] = upSampler.get(crData, ii + 1, cy);
//                                tmpCr[ii + 2 ] = upSampler.get(crData, ii + 2, cy);
//                                tmpCr[ii + 3 ] = upSampler.get(crData, ii + 3, cy);
//                                tmpCr[ii + 4 ] = upSampler.get(crData, ii + 4, cy);
//                                tmpCr[ii + 5 ] = upSampler.get(crData, ii + 5, cy);
//                                tmpCr[ii + 6 ] = upSampler.get(crData, ii + 6, cy);
//                                tmpCr[ii + 7 ] = upSampler.get(crData, ii + 7, cy);
//                            }
////                            {
////                                int ii = i * 8;
////                                tmpCb[ii] = upSampler.get(cbData, ii, cy) / 4f;
////                                tmpCb[ii + 1] = (3 * tmpCb[ii + 0] + upSampler.get(cbData, ii + 1, cy)) / 16f;
////                                tmpCb[ii + 2] = (tmpCb[ii + 1] + 3 * upSampler.get(cbData, ii + 2, cy)) / 16f;
////                                tmpCb[ii + 3] = (3 * tmpCb[ii + 2] + upSampler.get(cbData, ii + 3, cy)) / 16f;
////                                tmpCb[ii + 4] = (tmpCb[ii + 3] + 3 * upSampler.get(cbData, ii + 4, cy)) / 16f;
////                                tmpCb[ii + 5] = (3 * tmpCb[ii + 4] + upSampler.get(cbData, ii + 5, cy)) / 16f;
////                                tmpCb[ii + 6] = (tmpCb[ii + 5] + 3 * upSampler.get(cbData, ii + 6, cy)) / 16f;
////                                tmpCb[ii + 7] = (upSampler.get(cbData, ii + 7, cy)) / 4f;
////
////                                tmpCr[ii] = upSampler.get(crData, ii, cy) / 4f;
////                                tmpCr[ii + 1] = (3 * tmpCr[ii + 0] + upSampler.get(crData, ii + 1, cy)) / 16f;
////                                tmpCr[ii + 2] = (tmpCr[ii + 1] + 3 * upSampler.get(crData, ii + 2, cy)) / 16f;
////                                tmpCr[ii + 3] = (3 * tmpCr[ii + 2] + upSampler.get(crData, ii + 3, cy)) / 16f;
////                                tmpCr[ii + 4] = (tmpCr[ii + 3] + 3 * upSampler.get(crData, ii + 4, cy)) / 16f;
////                                tmpCr[ii + 5] = (3 * tmpCr[ii + 4] + upSampler.get(crData, ii + 5, cy)) / 16f;
////                                tmpCr[ii + 6] = (tmpCr[ii + 5] + 3 * upSampler.get(crData, ii + 6, cy)) / 16f;
////                                tmpCr[ii + 7] = upSampler.get(crData, ii + 7, cy) / 4f;
////                            }
//                            {
//                                IccProfileParser.ColorTransform transform = colorTransform;
//                                if (transform != null) {
//                                    int ii = i * 8;
//                                    IDCTFloat idct = matL[yi];
//                                    transform.yCbCr2RGB(idct.get(0, yy), tmpCb[ii + 0], tmpCr[ii + 0], x + ii + 0, pixelY, pixelSetter);
//                                    transform.yCbCr2RGB(idct.get(1, yy), tmpCb[ii + 1], tmpCr[ii + 1], x + ii + 1, pixelY, pixelSetter);
//                                    transform.yCbCr2RGB(idct.get(2, yy), tmpCb[ii + 2], tmpCr[ii + 2], x + ii + 2, pixelY, pixelSetter);
//                                    transform.yCbCr2RGB(idct.get(3, yy), tmpCb[ii + 3], tmpCr[ii + 3], x + ii + 3, pixelY, pixelSetter);
//                                    transform.yCbCr2RGB(idct.get(4, yy), tmpCb[ii + 4], tmpCr[ii + 4], x + ii + 4, pixelY, pixelSetter);
//                                    transform.yCbCr2RGB(idct.get(5, yy), tmpCb[ii + 5], tmpCr[ii + 5], x + ii + 5, pixelY, pixelSetter);
//                                    transform.yCbCr2RGB(idct.get(6, yy), tmpCb[ii + 6], tmpCr[ii + 6], x + ii + 6, pixelY, pixelSetter);
//                                    transform.yCbCr2RGB(idct.get(7, yy), tmpCb[ii + 7], tmpCr[ii + 7], x + ii + 7, pixelY, pixelSetter);
//                                }
//                            }
                            for (int xx = 0; xx < 8; xx++) {
                                DecodedData idct = matL[yi];
                                int cx = xx + i * 8;
                                int pixelX = x + cx;
                                if (pixelX >= width)
                                    continue;


                                float Y = idct.get(xx, yy);
                                float Cb = upSampler.get(cbData, cx, cy);
                                float Cr = upSampler.get(crData, cx, cy);
//                                float Cb = tmpCb[cx + (cy << 4)];
//                                float Cr = tmpCr[cx + (cy << 4)];
//                                float Cb = tmpCb[cx];
//                                float Cr = tmpCr[cx];
//                                Y = 0;
//                                Cb = 0;
//                                Cr = 0;

                                if (colorTransform != null) {
//                                    Y += 128 + 0.5f;
//                                    float R = (Cr) * 1.402f + Y;
//                                    float B = (Cb) * 1.772f + Y;
//                                    float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);
//                                    tmpColor[0]=R;
//                                    tmpColor[1]=G;
//                                    tmpColor[2]=B;
//                                    float[] rgb1 = colorSpace.toRGB(tmpColor);
//                                    int rgb = colorConversion(Y-128.5f, Cb, Cr);
//                                    int rgb = colorConversion(Y, Cb, Cr);
//                                    int rgb = colorTransform.yCbCr2RGB(Y, Cb, Cr);
//                                    pixelSetter.set(pixelX, pixelY, rgb);
//                                    colorTransform.yCbCr2RGB(Y, Cb, Cr, (r, g, b) -> pixelSetter.set(pixelX, pixelY, r, g, b));
                                    colorTransform.yCbCr2RGB(Y, Cb, Cr, pixelX, pixelY, pixelSetter);
                                } else {
//                                    int rgb = colorConversion(Y, Cb, Cr);
//                                    pixelSetter.set(pixelX, pixelY, rgb);
//                                    pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));

                                    Y = Math.max(Math.min(Y + 128 + 0.5f, 255.5f), 0.5f);
                                    float R = (Cr) * 1.402f + Y;
                                    float B = (Cb) * 1.772f + Y;
                                    float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);
                                    byte red = (byte) clamp(R);
                                    byte green = (byte) clamp(G);
                                    byte blue = (byte) clamp(B);
                                    pixelSetter.set(pixelX, pixelY, red, green, blue);
                                }
                            }

                        }
                    }
                }

            } else {
                for (int j = 0, yi = 0; j < sv; j++) {
                    for (int i = 0; i < sh; i++, yi++) {
                        for (int yy = 0; yy < 8; yy++) {
                            if (yy == 2) {
                                yy = yy;
                            }
                            for (int xx = 0; xx < 8; xx++) {
                                int cy = yy + j * 8;
                                int pixelY = y + cy;
                                if (pixelY >= height)
                                    continue;
                                int cx = xx + i * 8;
                                int pixelX = x + cx;
                                if (pixelX >= width)
                                    continue;

                                DecodedData idct = matL[yi];
                                float Y = idct.get(xx, yy);
//                                        float Cb = matCb.base[tempX + tempY];
//                                        float Cr = matCr.base[tempX + tempY];
//                                        int cx = (xx + i * 8) / sh;
//                                        int cy = (yy + j * 8) / sv;
//                                        float Cb = matCb.get(cx, cy);
//                                        float Cr = matCr.get(cx, cy);
//                            if (pixelX == 511 && pixelY == 110) {
//                                System.out.println();
//                            }
                                if (pixelY == 2) {
                                    Y = idct.get(xx, yy);
                                }

                                float Cb = upSampler.get(cbData, cx, cy);
                                float Cr = upSampler.get(crData, cx, cy);
//
//                                            if (pixelX <= 8 && pixelY == 0) {
//                                                System.out.println();
//                                            }


//                                        float Cb = matCb.get((xx+i*8)/sv, (yy+j*8)/sh);
//                                        float Cr = matCr.get((xx+i*8)/sv, (yy+j*8)/sh);
//                                        float Cb = 0;
//                                        float Cr = 0;
//                                        Y = 0;
//                                        if (pixelY == 16 && pixelX < 128) {
//                                            System.out.println("x: " + pixelX + "\t");
//                                        }
                                float cbShifted = (Cb + 128.5f);
                                float crShifted = (Cr + 128.5f);

                                int rgb = colorConversion(Y, Cb, Cr);
//                            if (position / width == 0 && position % width < 1920) {
//                                System.out.println("x: " + position + "\t" + (rgb >> 16) + " " + ((rgb >> 8) & 0xFF)+" " + (rgb & 0xFF));
//                            }

//                                        if (pixelX == 210 && pixelY == 33) {
////                                            [206, 197, 168]
//                                            for (int yyy = 0; yyy < 8; yyy++) {
//                                                for (int xxx = 0; xxx < 8; xxx++) {
//                                                    System.out.println(xxx+"x"+yyy+": "+Arrays.toString(colorConversionDebug(Y,matCb.get(xxx, yyy), matCr.get(xxx, yyy))));
//                                                }
//                                            }
//                                            System.out.println();
//                                        }
//                                pixelSetter.set(pixelX, pixelY, rgb);
                                pixelSetter.set(pixelX, pixelY, (byte) (rgb >> 16), (byte) ((rgb >> 8) & 0xFF), (byte) (rgb & 0xFF));
                            }

                        }
//                                    x++;
                    }
//                                x -= sh;
//                                y++;
                }

            }
        }
    }

    static void printMCURow(MCUsHolder holder, int y, int l) {
        for (int x = 0; x < l; x++) {
            System.out.print((holder.get(x, y) + 128) + " ");
            if ((x + 1) % 8 == 0)
                System.out.print(" ");
        }
        System.out.println("");
    }

    static void printFloats(float[] floats, int from, int l) {
        for (int x = 0; x < l; x++) {
            System.out.print((floats[from + x] + 128) + " ");
            if ((x + 1) % 8 == 0)
                System.out.print(" ");
        }
        System.out.println("");
    }

    static void printMCURow(MCUsHolder holder, int y, int l, UpSampler upSampler) {
        for (int x = 0; x < l; x++) {
            System.out.print((upSampler.get(holder, x, y) + 128) + " ");
            if ((x + 1) % 8 == 0)
                System.out.print(" ");
        }
        System.out.println("");
    }

    public static int clamp(double col) {
        return Math.max(Math.min((int) col, 255), 0);
    }

    public static int clamp(float col) {
        return Math.max(Math.min((int) col, 255), 0);
    }

    public static int colorConversion(double Y, double Cr, double Cb) {
//        double R = Cr * (2 - 2 * .299) + Y + 128;
//        double B = Cb * (2 - 2 * .114) + Y + 128;
//        double G = (Y - .114 * B - .299 * R) / .587 + 128;
        Y += 128;
        double R = (Cr) * 1.402 + Y;
        double B = (Cb) * 1.772 + Y;
        double G = Y - 0.714136 * (Cr) - 0.344136 * (Cb);


        int red = clamp(R);
        int green = clamp(G);
        int blue = clamp(B);
//        return new int[]{red, green, blue};
        return (red << 16) + (green << 8) + blue;
    }

    public static int colorConversion(float Y, float Cb, float Cr) {
//        double R = Cr * (2 - 2 * .299) + Y + 128;
//        double B = Cb * (2 - 2 * .114) + Y + 128;
//        double G = (Y - .114 * B - .299 * R) / .587 + 128;
//        Y += 128 + 0.5f;
        Y = Math.max(Math.min(Y + 128 + 0.5f, 255.5f), 0.5f);
//        Y += 128 ;
        float R = (Cr) * 1.402f + Y;
        float B = (Cb) * 1.772f + Y;
        float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);


        int red = clamp(R);
        int green = clamp(G);
        int blue = clamp(B);
//        return new int[]{red, green, blue};
        return (red << 16) + (green << 8) + blue;
    }

    public static float[] colorConversionDebug(float Y, float Cb, float Cr) {
//        double R = Cr * (2 - 2 * .299) + Y + 128;
//        double B = Cb * (2 - 2 * .114) + Y + 128;
//        double G = (Y - .114 * B - .299 * R) / .587 + 128;
        Y += 128 + 0.5f;
        float R = (Cr) * 1.402f + Y;
        float B = (Cb) * 1.772f + Y;
        float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);

        return new float[]{R, G, B};
    }

    public static float[] colorConversionDebug2(float Y, float Cb, float Cr) {
//        double R = Cr * (2 - 2 * .299) + Y + 128;
//        double B = Cb * (2 - 2 * .114) + Y + 128;
//        double G = (Y - .114 * B - .299 * R) / .587 + 128;
        Y += 128 + 0.5f;
        Y = clamp(Y);
        Cb = clamp(Cb + 128) - 128;
        Cr = clamp(Cr + 128) - 128;

        float R = (Cr) * 1.402f + Y;
        float B = (Cb) * 1.772f + Y;
        float G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);

        return new float[]{R, G, B};
    }


    private int buildMatrix(BitStream stream, int idx, int oldDcCoeff, float[] quant, DecodedData i) throws IOException {
        boolean safeRead = stream.ensureEnoughLength(256);
        if (safeRead)
            oldDcCoeff = readMatrixUnsafe(stream, idx, oldDcCoeff);
        else
            oldDcCoeff = readMatrix(stream, idx, oldDcCoeff);


        for (int j = 0; j < 64; j++) {
            i.data[j] = tmp[IDCT.ZIGZAG[j]] * quant[j];
//            i.base[j] = tmp[ZIGZAG[j]] * quant[ZIGZAG[j]];
        }

//        if (idx == 0) {
//            printFloats(i.base, 0, 64);
//        }

        IDCT.inverseDCT8x8(i.data);

        return oldDcCoeff;
    }

    private int buildMatrix(BitStream stream, int idx, int oldDcCoeff, float[] quant, float[] data) throws IOException {
        boolean safeRead = stream.ensureEnoughLength(256);
        if (safeRead)
            oldDcCoeff = readMatrixUnsafe(stream, idx, oldDcCoeff);
        else
            oldDcCoeff = readMatrix(stream, idx, oldDcCoeff);


        for (int j = 0; j < 64; j++) {
            data[j] = tmp[IDCT.ZIGZAG[j]] * quant[j];
//            data[j] = tmp[ZIGZAG[j]] * quant[ZIGZAG[j]];
        }

        IDCT.inverseDCT8x8(data);

        return oldDcCoeff;
    }

    private int readMatrix(BitStream stream, int idx, int oldDcCoeff) throws IOException {
        Arrays.fill(tmp, 0);
        HuffmanTable ht = huffmanTables[idx];
//        boolean safeRead = stream.ensureEnoughLength(64);
//        boolean safeRead = false;
        int category;
//        if (safeRead)
//            category = getValueUnsafe(stream, ht.huffmanTree) & 0x0F;
//        else
        category = getValue(stream, ht) & 0x0F;
//        int zeroes = value >>> 4;
//        int category = value & 0x0F;
        int bits = stream.readBits(category);
        int dcValue = convertToValue(bits, category);

        oldDcCoeff += dcValue;
//        i.addZigZag(0, oldDcCoeff * quant[0]);
        tmp[0] = oldDcCoeff;

        int l = 1;
        ht = huffmanTables[idx + 16];
        while (l < 64) {
            int code;
//            if (safeRead)
//                code = getValueUnsafe(stream, ht.huffmanTree) & 0xFF;
//            else
            code = getValue(stream, ht) & 0xFF;

            if (code == 0) {
                break;
            }

            l += (code >> 4);

            if ((code & 0x0F) == 0 && (code >> 4) == 0) {
                break;
            }

            bits = stream.readBits(code & 0x0F);
            if (l < 64) {
                int coeff = convertToValue(bits, code & 0x0F);
//                int coeff = convertToValue(bits, code);
//                i.addZigZag(l, coeff * quant[l]);
                tmp[l] = coeff;
                l++;
            }
        }
        return oldDcCoeff;
    }

    private int readMatrixUnsafe(BitStream stream, int idx, int oldDcCoeff) throws IOException {
        Arrays.fill(tmp, 0);
        HuffmanTable ht = huffmanTables[idx];
        int category = getValueUnsafe(stream, ht) & 0x0F;
        int bits = stream.readBitsUnsafe(category);
        int dcValue = convertToValue(bits, category);

        oldDcCoeff += dcValue;
//        i.addZigZag(0, oldDcCoeff * quant[0]);
        tmp[0] = oldDcCoeff;

        int l = 1;
        ht = huffmanTables[idx + 16];
        while (l < 64) {
            int code;
            code = getValueUnsafe(stream, ht) & 0xFF;

            if (code == 0) {
                break;
            }

            l += (code >> 4);

            if ((code & 0x0F) == 0 && (code >> 4) == 0) {
                break;
            }

            bits = stream.readBitsUnsafe(code & 0x0F);
            if (l < 64) {
                int coeff = convertToValue(bits, code & 0x0F);
//                int coeff = convertToValue(bits, code);
//                i.addZigZag(l, coeff * quant[l]);
                tmp[l] = coeff;
                l++;
            }
        }
        return oldDcCoeff;
    }

    private static byte getValue(BitStream stream, HuffmanTable huffmanTable) throws IOException {
//        HuffmanTree node = huffmanTable.huffmanTree;
        int code = stream.readBit();
//        if (code == 1) {
//            node = node.right;
//        } else if (code == 0) {
//            node = node.left;
//        } else
//            throw new IllegalStateException();
        int length = 1;

        int[] maxCode = huffmanTable.maxCode;
        while (code > maxCode[length]) {
            int bit = stream.readBit();
            code = (code << 1) + bit;
            length++;

//            if (bit == 1) {
//                node = node.right;
//            } else if (bit == 0) {
//                node = node.left;
//            } else
//                throw new IllegalStateException();

//            if (node == null) {
//                return 0;
//            }

//            if (node.value != null) {
//                if(huffmanTable.get(code, length)!= node.value){
//                    System.out.println();
//                }
////                if (key < 256) {
////                    if (huffmanTable.ht1[key] != (node.value & 0xFF)) {
////                        System.out.println();
////                    }
////                } else {
////                    if (huffmanTable.ht2[key & 0xFF] != (node.value & 0xFF)) {
////                        System.out.println();
////                    }
////                }
//                return node.value.byteValue();
//            }
        }
        return huffmanTable.get(code, length);
    }

    private byte getValueUnsafe(BitStream stream, HuffmanTable huffmanTable) {
        int bitsRemaining = stream.getBitsRemaining();
        int buffer = stream.getBitsBuffer();

        int code;

        if (bitsRemaining == 0) {
            bitsRemaining = stream.fillBitsBuffer();
            buffer = stream.getBitsBuffer();
//            stream.setBitsBuffer(buffer = stream.readIntUnsafe());
//            bitsRemaining = 32;
        }
        bitsRemaining--;
        code = (buffer >>> bitsRemaining) & 1;

//        code = stream.readBitUnsafe();
        int length = 1;

        int[] maxCode = huffmanTable.maxCode;
        while (code > maxCode[length]) {
//            int bit = stream.readBitUnsafe();

            if (bitsRemaining == 0) {
                bitsRemaining = stream.fillBitsBuffer();
                buffer = stream.getBitsBuffer();
//                stream.setBitsBuffer(buffer = stream.readIntUnsafe());
//                bitsRemaining = 32;
            }
            bitsRemaining--;
            int bit = (buffer >>> bitsRemaining) & 1;

            code = (code << 1) + bit;
            length++;

        }
        stream.setBitsRemaining(bitsRemaining);

        byte b = huffmanTable.get(code, length);
        return b;
    }

    private static int convertToValue(int bits, int length) {
        int vt = (1 << (length - 1));
        if (bits < vt) {
            vt = (-1 << length) + 1;
            bits += vt;
        }
        return bits;
    }

    private void readFrameInfo(BitStream stream) {
        bitsPerPixel = stream.readByteUnsafe();
        height = stream.readShortUnsafe(true);
        width = stream.readShortUnsafe(true);
        numberOfComponents = stream.readByteUnsafe();
        components = new ComponentInfo[numberOfComponents];
        for (int i = 0; i < numberOfComponents; i++) {
            components[i] = new ComponentInfo(
                    Component.valueOf(stream.readByteUnsafe()),
                    (byte) stream.readByteUnsafe(),
                    stream.readByteUnsafe()
            );
        }
    }

    protected void readRestartInterval(BitStream stream) {
        restartInterval = stream.readShortUnsafe(true);
    }

    protected void readIccProfile(BitStream stream, int length) throws IOException {
        length -= 2;
        int header1 = stream.readIntUnsafe();
        int header2 = stream.readIntUnsafe();
        int header3 = stream.readIntUnsafe();
        length -= 12;

        if ((header1 >> 24) != 'I' || ((header1 >> 16) & 0xFF) != 'C' || ((header1 >> 8) & 0xFF) != 'C' || (header1 & 0xFF) != '_'
                || (header2 >> 24) != 'P' || ((header2 >> 16) & 0xFF) != 'R' || ((header2 >> 8) & 0xFF) != 'O' || (header2 & 0xFF) != 'F'
                || (header3 >> 24) != 'I' || ((header3 >> 16) & 0xFF) != 'L' || ((header3 >> 8) & 0xFF) != 'E' || (header3 & 0xFF) != 0
        ) {
            stream.skip(length);
            return;
        }

        int chunkNumber = stream.readByteUnsafe();
        int chunks = stream.readByteUnsafe();
        length -= 2;


        stream.ensureEnoughLength(length);
        colorTransform = IccProfileParser.parseIccProfile(stream, length);
    }

    private void readQuantizationTable(BitStream stream, int length, float[][] quantizationTables) {
        if (length == 67) {
            int i = stream.readByteUnsafe();
            byte[] bytes = stream.readBytesUnsafe(new byte[64]);
//            quantizationTables[i] = toInts(bytes);
//            quantizationTables[i] = toFloats(bytes);
            quantizationTables[i] = IDCT.scaleDequantizationMatrix(bytes);
        } else if (length == 132) {
            for (int j = 0; j < 2; j++) {
                int i = stream.readByteUnsafe();
                byte[] bytes = stream.readBytesUnsafe(new byte[64]);
//                quantizationTables[i] = toInts(bytes);
//                quantizationTables[i] = toFloats(bytes);
                quantizationTables[i] = IDCT.scaleDequantizationMatrix(bytes);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private int[] toInts(byte[] bytes) {
        int[] ints = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            ints[i] = bytes[i] & 0xFF;
        }
        return ints;
    }

    private float[] toFloats(byte[] bytes) {
        float[] ints = new float[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            ints[i] = bytes[i] & 0xFF;
        }
        return ints;
    }

    protected HuffmanTable[] readHuffmanTable(BitStream stream, int length, HuffmanTable[] tables) {
        int i = stream.readByteUnsafe();
        byte[] header = stream.readBytesUnsafe(new byte[16]);
        int sum = 0;
        for (int j = 0; j < header.length; j++) {
            sum += header[j] & 0xff;
        }
        byte[] values = stream.readBytesUnsafe(new byte[sum]);
        if (length != 3 + 16 + sum) {
            int l = 1 + 16 + sum;
            tables = readHuffmanTable(stream, length - l, tables);
        }
        HuffmanTable table = new HuffmanTable(header, values);
        if (i >= tables.length) {
            HuffmanTable[] tmp = new HuffmanTable[i + 1];
            System.arraycopy(tables, 0, tmp, 0, tables.length);
            tables = tmp;
        }
        tables[i] = table;
        return tables;
    }

    private int getSegmentLength(byte[] buf, int offset) {
        return ((buf[offset] & 0xFF) << 8) + (buf[offset + 1] & 0xFF);
    }

    private int readEXIF(byte[] buf, int offset) {
        int segmentLength = ((buf[offset] & 0xFF) << 8) + (buf[offset + 1] & 0xFF);
        return offset + segmentLength;
    }

    private int skipSegment(byte[] buf, int offset) {
        int segmentLength = ((buf[offset] & 0xFF) << 8) + (buf[offset + 1] & 0xFF);
        return offset + segmentLength;
    }

    private int checkSOIMarker(byte[] buf, int offset) {
        if (buf[offset] != (byte) 0xFF || buf[offset + 1] != (byte) 0xD8)
            throw new IllegalStateException("Cannot find SOI marker");
        return offset + 2;
    }

    private void readJFIF(BitStream stream, JFIF jfif) throws IOException {
        int header = stream.readIntUnsafe();
        int zero = stream.readByteUnsafe();
        if ((header >> 24) != 'J' || ((header >> 16) & 0xFF) != 'F' || ((header >> 8) & 0xFF) != 'I' || (header & 0xFF) != 'F' || zero != 0)
            throw new IllegalStateException("Cannot find JFIF marker");

        jfif.majorVersion = stream.readByteUnsafe();
        jfif.minorVersion = stream.readByteUnsafe();

        jfif.densityUnit = DensityUnit.valueOf(stream.readByteUnsafe());
        jfif.densityX = stream.readShortUnsafe();
        jfif.densityY = stream.readShortUnsafe();

        int thumbnailX = stream.readByteUnsafe();
        int thumbnailY = stream.readByteUnsafe();

        //skip thumbnail
        stream.skip(thumbnailX * thumbnailY * 3);
    }

    private void readJFXX(BitStream stream, JFIF jfif) {
        int header = stream.readIntUnsafe();
        int zero = stream.readByteUnsafe();
        if ((header >> 24) != 'J' || ((header >> 16) & 0xFF) != 'F' || ((header >> 8) & 0xFF) != 'X' || (header & 0xFF) != 'X' || zero != 0)
            throw new IllegalStateException("Cannot find JFIF marker");

        JFXXThumbnailFormat thumbnailFormat = JFXXThumbnailFormat.valueOf(stream.readByteUnsafe());

        if (thumbnailFormat != null) {
            throw new IllegalStateException("Not implemented yet");
        }
    }
}
