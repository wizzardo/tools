package com.wizzardo.tools.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IccProfileParser {

    static class Tag {
        final String signature;
        final int offset;
        final int size;
        public Object value;

        Tag(String signature, int offset, int size) {
            this.signature = signature;
            this.offset = offset;
            this.size = size;
        }

        @Override
        public String toString() {
            return "Tag{" +
                    "signature='" + signature + '\'' +
                    ", offset=" + offset +
                    ", size=" + size +
                    ", value=" + value +
                    '}';
        }
    }

    static ColorTransform parseIccProfile(BitStream stream, int length) throws IOException {
        int l = stream.readIntUnsafe();
        if (l != length)
            throw new IllegalStateException();

        int preferredCMMType = stream.readIntUnsafe();
        int profileVersion = stream.readIntUnsafe();
        int profileDeviceClass = stream.readIntUnsafe();
        int colourSpace = stream.readIntUnsafe();
        int pcs = stream.readIntUnsafe();
        stream.skip(12); //date
        int profileSignature = stream.readIntUnsafe();
        int platformSignature = stream.readIntUnsafe();
        int flags = stream.readIntUnsafe();
        int deviceManufacturer = stream.readIntUnsafe();
        int deviceModel = stream.readIntUnsafe();
        stream.skip(8); //deviceAttributes
        int renderingIntent = stream.readIntUnsafe();
        stream.skip(12); //XYZNumber - The nCIEXYZ values of the illuminant of the PCS
        int profileCreatorSignature = stream.readIntUnsafe();
        stream.skip(16); // profileId
        stream.skip(28); // reserved
        l -= 128;

        int tagsCount = stream.readIntUnsafe();
        l -= 4;

        List<Tag> tags = new ArrayList<>(tagsCount);
        for (int i = 0; i < tagsCount; i++) {
            String tagSignature = new String(stream.readBytesUnsafe(new byte[4]));
            int tagDataOffset = stream.readIntUnsafe(true);
            int tagDataSize = stream.readIntUnsafe(true);
            tags.add(new Tag(tagSignature, tagDataOffset, tagDataSize));
        }
        l -= tagsCount * 3 * 4;

        byte[] data = stream.readBytes(new byte[l]);
        tags.sort(Comparator.comparingInt(o -> o.offset));

        Curve[] curves = new Curve[0];

        int offset = tags.get(0).offset;
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (i > 0) {
                Tag prev = tags.get(i - 1);
                if (prev.offset == tag.offset) {
                    tag.value = prev.value;
                    continue;
                }
            }
            BitStreamByteArray tagStream = new BitStreamByteArray(data);
            tagStream.skip(tag.offset - offset);

//            if (tag.offset > offset) {
//                stream.skip(tag.offset - offset);
//                l -= tag.offset - offset;
//                offset = tag.offset;
//            } else if (tag.offset < offset) {
//                tag.value = tags.get(i - 1).value;
//                continue;
//            }
//
//            offset += tag.size;
//            l -= tag.size;

            switch (tag.signature) {
//                case "desc":{
//                    int signature = tagStream.readIntUnsafe(true);
////                    int reserved = tagStream.readIntUnsafe(true);
//                    tagStream.skip(4); // reserved
//                    int numberOfRecords = tagStream.readIntUnsafe(true);
//                    int[] recordLengths = new int[numberOfRecords];
//                    String[] records = new String[numberOfRecords];
//                    for (int j = 0; j < numberOfRecords; j++) {
//                        int recordSize = tagStream.readIntUnsafe(true);
//                        int lang = tagStream.readShortUnsafe(true);
//                        int country = tagStream.readShortUnsafe(true);
//                        int recordLength = tagStream.readIntUnsafe(true);
//                        int recordOffset = tagStream.readIntUnsafe(true);
//                        recordLengths[j]=recordLength;
//                    }
//
//                    for (int j = 0; j < numberOfRecords; j++) {
//                        records[j] = new String(tagStream.readBytes(new byte[recordLengths[j]]), StandardCharsets.UTF_16BE);
//                    }
//                    tag.value=records;
//                    break;
//                }
                case "rXYZ":
                case "gXYZ":
                case "bXYZ": {
                    int signature = tagStream.readIntUnsafe(true);
                    if (signature == 1482250784) { // ‘XYZ ’ (58595A20h)
                        tagStream.skip(4); // reserved
                        double[] xyz = new double[3];
                        for (int j = 0; j < xyz.length; j++) {
                            xyz[j] = read32bitSignedFloat(tagStream);
                        }
                        tag.value = new XYZNumber(xyz);
                    } else
                        throw new IllegalStateException();
                    break;
                }
                case "rTRC":
                case "gTRC":
                case "bTRC": {
                    int signature = tagStream.readIntUnsafe(true);
                    if (signature == 1885434465) { // 'para' (70617261h)
                        tagStream.skip(4); // reserved
                        int type = tagStream.readShortUnsafe(true);
                        tagStream.skip(2); // reserved
                        double[] parameters;
                        if (type == 0) {
                            parameters = new double[1];
                        } else if (type == 1) {
                            parameters = new double[3];
                        } else if (type == 2) {
                            parameters = new double[4];
                        } else if (type == 3) {
                            parameters = new double[5];
                        } else if (type == 4) {
                            parameters = new double[7];
                        } else
                            throw new IllegalStateException();

                        for (int j = 0; j < parameters.length; j++) {
                            parameters[j] = read32bitSignedFloat(tagStream);
                        }
                        curves = new Curve[]{
                                ParametricCurve.ofType(type, parameters),
                                ParametricCurve.ofTypeInverse(type, parameters)
                        };
                        tag.value = curves;
                        break;
                    }
                    if (signature == 1668641398) { // ‘curv’ (63757276h)
                        tagStream.skip(4); // reserved
                        int entriesCount = tagStream.readIntUnsafe(true);
                        if (entriesCount == 1) {
                            double d = read16bitUnsignedFloat(tagStream);
//                            curves = new Curve[]{
//                                    x -> Math.pow(x, d),
//                                    x -> Math.pow(x, 1 / d)
//                            };
                            curves = new Curve[]{
                                    ParametricCurve.ofType(0, new double[]{d}),
                                    ParametricCurve.ofTypeInverse(0, new double[]{d})
                            };
                        } else {
                            int[] table = new int[entriesCount];
                            double[] doubles = new double[entriesCount];
                            for (int j = 0; j < entriesCount; j++) {
                                int value = tagStream.readShortUnsafe(true);
                                table[j] = value;
                                doubles[j] = value / 65535.0;
                            }

                            int entriesMinusOne = entriesCount - 1;
                            double indexIncrement = 1.0 / entriesMinusOne;
                            curves = new Curve[]{
                                    x -> {
                                        double index = x / indexIncrement;
                                        int left = (int) index;
                                        double ratio = (index - left);
                                        double value1 = doubles[left];
                                        double value2 = left < entriesMinusOne ? doubles[left + 1] : 1.0;
                                        return value1 + (value2 - value1) * ratio;
                                    },
                                    x -> {
                                        int left = findClosestLeftIndex(doubles, x);
                                        double value1 = doubles[left];
                                        double value2 = left < entriesMinusOne ? doubles[left + 1] : 1.0;
                                        double ratio = (x - value1) / (value2 - value1);
                                        return (ratio + left) / entriesMinusOne;
                                    }
                            };
                        }
                        tag.value = curves;
                        break;
                    }
                    tagStream.unread(4);
//                    else
//                        throw new IllegalStateException();
//                    break;
                }
                default: {
//                    System.out.println("skipping tag " + tag.signature + " " + tag.size);
                    tagStream.skip(tag.size);
                    break;
                }
            }
        }


        XYZNumber rXYZ = (XYZNumber) tags.stream().filter(tag -> tag.signature.equals("rXYZ")).findFirst().get().value;
        XYZNumber gXYZ = (XYZNumber) tags.stream().filter(tag -> tag.signature.equals("gXYZ")).findFirst().get().value;
        XYZNumber bXYZ = (XYZNumber) tags.stream().filter(tag -> tag.signature.equals("bXYZ")).findFirst().get().value;
        if (rXYZ == null || gXYZ == null || bXYZ == null)
            throw new IllegalStateException();

////            sRGB to ciexyz
////            0.4358587014572366	0.38533607995727476	0.14302281223773555
////            0.22238498512245364	0.7170519569695583	0.06059357595178149
////            0.013916227969787138	0.09713893339436942	0.713847562371252

        double diffWithSRGB = 0;
        diffWithSRGB += Math.abs(rXYZ.value[0] - 0.4358587014572366);
        diffWithSRGB += Math.abs(rXYZ.value[1] - 0.22238498512245364);
        diffWithSRGB += Math.abs(rXYZ.value[2] - 0.013916227969787138);
        diffWithSRGB += Math.abs(gXYZ.value[0] - 0.38533607995727476);
        diffWithSRGB += Math.abs(gXYZ.value[1] - 0.7170519569695583);
        diffWithSRGB += Math.abs(gXYZ.value[2] - 0.09713893339436942);
        diffWithSRGB += Math.abs(bXYZ.value[0] - 0.14302281223773555);
        diffWithSRGB += Math.abs(bXYZ.value[1] - 0.06059357595178149);
        diffWithSRGB += Math.abs(bXYZ.value[2] - 0.713847562371252);
        if (diffWithSRGB <= 0.002)
            return null;


        Curve[] rTRC = (Curve[]) tags.stream().filter(tag -> tag.signature.equals("rTRC")).findFirst().get().value;
        Curve[] gTRC = (Curve[]) tags.stream().filter(tag -> tag.signature.equals("gTRC")).findFirst().get().value;
        Curve[] bTRC = (Curve[]) tags.stream().filter(tag -> tag.signature.equals("bTRC")).findFirst().get().value;
        if (rTRC == null || gTRC == null || bTRC == null)
            throw new IllegalStateException();

//        if (rTRC != gTRC || rTRC != bTRC)
//            throw new IllegalStateException();

        Curve srgbGamma;
        {
            double[] gammaTable = new double[256];
            double[] gammaTableInverted = new double[4096];
            int gammaTableInvertedLengthMinusOne = gammaTableInverted.length - 1;
            {
                for (int i = 0; i < gammaTable.length; i += 1) {
                    gammaTable[i] = Math.pow(i / 255.0, 2.4);
//                    if (i > 0)
//                        System.out.println(i + ": " + Math.pow(i, 2.4) + " diff: " + (Math.pow(i, 2.4) - Math.pow(i - 1, 2.4)));
                }
                for (int i = 0; i < gammaTableInverted.length; i += 1) {
                    double input = i * 1.0 / gammaTableInvertedLengthMinusOne;
//                    gammaTableInverted[i] = Math.pow(input, 1 / 2.4) * 1.055 - 0.055;
                    gammaTableInverted[i] = applySRGBGamma(input) * 255;
                }
            }

            // [13] / 128
            // [56] / 192
            // [134] / 192
            // [223] / 192
            // [255] / 129

            Curve srgbGamma0_13;
            Curve srgbGamma13_56;
            Curve srgbGamma56_134;
            Curve srgbGamma134_223;
            Curve srgbGamma223_255;
            double gamma13 = gammaTable[13];
            {
                double[] table = new double[128];
                int entriesMinusOne = table.length - 1;
                double indexIncrement = gamma13 / entriesMinusOne;
                {
                    for (int i = 0; i < table.length; i += 1) {
                        table[i] = Math.pow(i * indexIncrement, 1 / 2.4);
                    }
                }
                srgbGamma0_13 = x -> {
                    if (x < 0)
                        return 0;
                    double index = x / indexIncrement;
                    int left = (int) index;
                    double ratio = (index - left);
                    double value1 = table[left];
//                    double value2 = left < entriesMinusOne ? table[left + 1] : gamma13;
                    double value2 = table[left + 1]; // left is always < table.length-1
                    return value1 + (value2 - value1) * ratio;
                };
            }
            double gamma56 = gammaTable[56];
            {
                double[] table = new double[192];
                int entriesMinusOne = table.length - 1;
                double indexIncrement = (gamma56 - gamma13) / entriesMinusOne;
                {
                    for (int i = 0; i < table.length; i += 1) {
                        table[i] = Math.pow(i * indexIncrement + gamma13, 1 / 2.4);
                    }
                }
                srgbGamma13_56 = x -> {
                    double index = (x - gamma13) / indexIncrement;
                    int left = (int) index;
                    double ratio = (index - left);
                    double value1 = table[left];
//                    double value2 = left < entriesMinusOne ? table[left + 1] : 1.0;
                    double value2 = table[left + 1]; // left is always < table.length-1
                    return value1 + (value2 - value1) * ratio;
                };
            }
            double gamma134 = gammaTable[134];
            {
                double[] table = new double[192];
                int entriesMinusOne = table.length - 1;
                double indexIncrement = (gamma134 - gamma56) / entriesMinusOne;
                {
                    for (int i = 0; i < table.length; i += 1) {
                        table[i] = Math.pow(i * indexIncrement + gamma56, 1 / 2.4);
                    }
                }
                srgbGamma56_134 = x -> {
                    double index = (x - gamma56) / indexIncrement;
                    int left = (int) index;
                    double ratio = (index - left);
                    double value1 = table[left];
//                    double value2 = left < entriesMinusOne ? table[left + 1] : 1.0;
                    double value2 = table[left + 1]; // left is always < table.length-1 because of ifs in parent curve
                    return value1 + (value2 - value1) * ratio;
                };
            }
            double gamma223 = gammaTable[223];
            {
                double[] table = new double[192];
                int entriesMinusOne = table.length - 1;
                double indexIncrement = (gamma223 - gamma134) / entriesMinusOne;
                {
                    for (int i = 0; i < table.length; i += 1) {
                        table[i] = Math.pow(i * indexIncrement + gamma134, 1 / 2.4);
                    }
                }
                srgbGamma134_223 = x -> {
                    double index = (x - gamma134) / indexIncrement;
                    int left = (int) index;
                    double ratio = (index - left);
                    double value1 = table[left];
//                    double value2 = left < entriesMinusOne ? table[left + 1] : 1.0;
                    double value2 = table[left + 1]; // left is always < table.length-1
                    return value1 + (value2 - value1) * ratio;
                };
            }
            {
                double[] table = new double[129];
                int entriesMinusOne = table.length - 1;
                double gamma255 = gammaTable[255];
                double indexIncrement = (gamma255 - gamma223) / entriesMinusOne;
                {
                    for (int i = 0; i < table.length; i += 1) {
                        table[i] = Math.pow(i * indexIncrement + gamma223, 1 / 2.4);
                    }
                }
                srgbGamma223_255 = x -> {
                    double index = (x - gamma223) / indexIncrement;
                    int left = (int) index;
                    if (left >= table.length)
                        left = table.length - 1;
                    double ratio = (index - left);
                    double value1 = table[left];
                    double value2 = left < entriesMinusOne ? table[left + 1] : 1.0;
                    return value1 + (value2 - value1) * ratio;
                };
            }

//            List<Spline.Point> points = new ArrayList<>();
//            float v = 0;
//            float step = 1;
//            while (v <= 256) {
////            System.out.println("add " + (int)v);
//                System.out.println("add " + v);
//                points.add(new Spline.Point(Math.pow(v, 2.4), v));
////            step += Math.max(v / 25, 1);
////                step += v / 12;
////                step += v / 16;
////                step += v / 64;
//                step += v / 8;
//                v += step;
//            }
//            points.add(new Spline.Point(Math.pow(255, 2.4), 255));
//            System.out.println("spline.point: " + points.size());
//            Spline spline = new Spline(points);

//            srgbGamma = x -> spline.getY(x) * 1.055 - 0.055;
//            srgbGamma = x -> clamp(x) * 1.055 - 0.055;

//            srgbGamma = x -> {
//                int left = findClosestLeftIndex(gammaTable, x);
//                double value1 = gammaTable[left];
//                double value2 = left < 255 ? gammaTable[left + 1] : left;
//                double ratio = (x - value1) / (value2 - value1);
//                return (ratio + left) / 255.0 * 1.055 - 0.055;
//            };
//            srgbGamma = x -> {
//                if (x < gamma56) {
//                    if (x < gamma13)
//                        return srgbGamma0_13.get(x) * 1.055 - 0.055;
//                    else
//                        return srgbGamma13_56.get(x) * 1.055 - 0.055;
//                } else {
//                    if (x < gamma134) {
//                        return srgbGamma56_134.get(x) * 1.055 - 0.055;
//                    } else {
//                        if (x < gamma223)
//                            return srgbGamma134_223.get(x) * 1.055 - 0.055;
//                        else
//                            return srgbGamma223_255.get(x) * 1.055 - 0.055;
//                    }
//                }
//            };

            srgbGamma = x -> {
                int index = (int) (x * gammaTableInvertedLengthMinusOne);
                if (index < 0)
                    return 0;
                if (index > gammaTableInvertedLengthMinusOne)
                    return 255;
                return gammaTableInverted[index];
            };
//            System.out.println(srgbGamma.get(4.210208143703044E-4));
//            System.out.println(srgbGamma0_13.get(4.210208143703044E-4)* 1.055 - 0.055);
//            System.out.println(srgbGamma.get(7.90255654362185E-4));
//            System.out.println(srgbGamma0_13.get(7.90255654362185E-4)* 1.055 - 0.055);
//            System.out.println(srgbGamma.get(0.005880233585761174));
//            System.out.println(srgbGamma13_56.get(0.005880233585761174)* 1.055 - 0.055);
//            System.out.println(srgbGamma.get(0.10323525479338815));
//            System.out.println(srgbGamma56_134.get(0.10323525479338815)* 1.055 - 0.055);
//            System.out.println(srgbGamma.get(0.377903555740975));
//            System.out.println(srgbGamma134_223.get(0.377903555740975)* 1.055 - 0.055);
//            System.out.println();
        }


        Curve rTrcApproximation;
        Curve gTrcApproximation;
        Curve bTrcApproximation;
        {
            rTrcApproximation = getTrcApproximation(rTRC);
            if (rTRC == gTRC)
                gTrcApproximation = rTrcApproximation;
            else
                gTrcApproximation = getTrcApproximation(gTRC);
            if (rTRC == bTRC)
                bTrcApproximation = rTrcApproximation;
            else
                bTrcApproximation = getTrcApproximation(bTRC);
        }

        MatrixMultiplication rMM;
        MatrixMultiplication gMM;
        MatrixMultiplication bMM;
        {
//            R = x * 3.135888146102320303 + y * -1.6186919574161555234 + z * -0.49089080908292703386;
            double R = 3.135888146102320303 * rXYZ.value[0] + (-1.6186919574161555234) * rXYZ.value[1] + (-0.49089080908292703386) * rXYZ.value[2];
            double G = 3.135888146102320303 * gXYZ.value[0] + (-1.6186919574161555234) * gXYZ.value[1] + (-0.49089080908292703386) * gXYZ.value[2];
            double B = 3.135888146102320303 * bXYZ.value[0] + (-1.6186919574161555234) * bXYZ.value[1] + (-0.49089080908292703386) * bXYZ.value[2];
            rMM = (r, g, b) -> r * R + g * G + b * B;
        }
        {
//            G = x * -0.97864531205244014713 + y * 1.9159822781158407685 + z * 0.033441855488639160342;
            double R = (-0.97864531205244014713) * rXYZ.value[0] + (1.9159822781158407685) * rXYZ.value[1] + (0.033441855488639160342) * rXYZ.value[2];
            double G = (-0.97864531205244014713) * gXYZ.value[0] + (1.9159822781158407685) * gXYZ.value[1] + (0.033441855488639160342) * gXYZ.value[2];
            double B = (-0.97864531205244014713) * bXYZ.value[0] + (1.9159822781158407685) * bXYZ.value[1] + (0.033441855488639160342) * bXYZ.value[2];
            gMM = (r, g, b) -> r * R + g * G + b * B;
        }
        {
//            B = x * 0.072038948041565506726 + y * -0.22916711806510855193 + z * 1.4058783627429984365;
            double R = 0.072038948041565506726 * rXYZ.value[0] + (-0.22916711806510855193) * rXYZ.value[1] + (1.4058783627429984365) * rXYZ.value[2];
            double G = 0.072038948041565506726 * gXYZ.value[0] + (-0.22916711806510855193) * gXYZ.value[1] + (1.4058783627429984365) * gXYZ.value[2];
            double B = 0.072038948041565506726 * bXYZ.value[0] + (-0.22916711806510855193) * bXYZ.value[1] + (1.4058783627429984365) * bXYZ.value[2];
            bMM = (r, g, b) -> r * R + g * G + b * B;
        }


        return (Y, Cb, Cr, pixelX, pixelY, colorConsumer) -> {
            Y += 128 + 0.5f;
//            Y = Math.max(Math.min(Y + 128 + 0.5f, 255.5f), 0.5f);
            double R = (Cr) * 1.402f + Y;
            double B = (Cb) * 1.772f + Y;
            double G = Y - 0.714136f * (Cr) - 0.344136f * (Cb);

//            R = R / 255;
//            G = G / 255;
//            B = B / 255;

            double beforeTrcR = R;
            double beforeTrcG = G;
            double beforeTrcB = B;
//            R = clamp(R);
//            G = clamp(G);
//            B = clamp(B);

//            double rt = trcApproximation.get(R);
//            double gt = trcApproximation.get(G);
//            double bt = trcApproximation.get(B);
//            R = rTRC[0].get(R / 255);
//            G = gTRC[0].get(G / 255);
//            B = bTRC[0].get(B / 255);
            R = rTrcApproximation.get(R);
            G = gTrcApproximation.get(G);
            B = bTrcApproximation.get(B);

            double afterTrcR = R;
            double afterTrcG = G;
            double afterTrcB = B;

            R = R;

//            {
//                double r = R;
//                double g = G;
//                double b = B;
//                R = r * rXYZ.value[0] + g * gXYZ.value[0] + b * bXYZ.value[0];
//                G = r * rXYZ.value[1] + g * gXYZ.value[1] + b * bXYZ.value[1];
//                B = r * rXYZ.value[2] + g * gXYZ.value[2] + b * bXYZ.value[2];
//            }
//
//            double afterXyzR = R;
//            double afterXyzG = G;
//            double afterXyzB = B;
//
//            {
//                double x = R;
//                double y = G;
//                double z = B;
////            input[0] = r * 3.2404542 + g * -1.5371385 + b * -0.4985314;
////            input[1] = r * -0.9692660 + g * 1.8760108 + b * 0.0415560;
////            input[2] = r * 0.0556434 + g * -0.2040259 + b * 1.0572252;
////            input[0] = r * 3.2410 + g * -1.5374 + b * -0.4986;
////            input[1] = r * -0.9692 + g * 1.8760 + b * 0.0416;
////            input[2] = r * 0.0556 + g * -0.2040 + b * 1.0570;
//
////            sRGB to ciexyz
////            0.4358587014572366	0.38533607995727476	0.14302281223773555
////            0.22238498512245364	0.7170519569695583	0.06059357595178149
////            0.013916227969787138	0.09713893339436942	0.713847562371252
//
//                R = x * 3.135888146102320303 + y * -1.6186919574161555234 + z * -0.49089080908292703386;
//                G = x * -0.97864531205244014713 + y * 1.9159822781158407685 + z * 0.033441855488639160342;
//                B = x * 0.072038948041565506726 + y * -0.22916711806510855193 + z * 1.4058783627429984365;
//                if (B < 0 || G < 0 || R < 0) {
//                    B = B;
////                    System.out.println(counter++);
//                }
//            }
            {
                double r = rMM.get(afterTrcR, afterTrcG, afterTrcB);
                double g = gMM.get(afterTrcR, afterTrcG, afterTrcB);
                double b = bMM.get(afterTrcR, afterTrcG, afterTrcB);
//                if (R != r || G != g || B != b) {
//                    r = r;
//                }
                R = r;
                G = g;
                B = b;
            }


            double afterSrgbR = R;
            double afterSrgbG = G;
            double afterSrgbB = B;

            {
//                double r = srgbGamma.get(R);
                if (B < 0 || G < 0 || R < 0) {
                    B = B;
//                    System.out.println(counter++);
                }
                if (B > 1 || G > 1 || R > 1) {
                    B = B;
//                    System.out.println(counter++);
                }


//               double afterGammaR = applySRGBGamma(R) ;
//               double afterGammaG = applySRGBGamma(G) ;
//               double afterGammaB = applySRGBGamma(B) ;
//                R = afterGammaR * 255;
//                G = afterGammaG * 255;
//                B = afterGammaB * 255;
//
////                R = applySRGBGamma(R) * 255;
////                G = applySRGBGamma(G) * 255;
////                B = applySRGBGamma(B) * 255;
//
//                double tr = srgbGamma.get(afterSrgbR);
//                double tg = srgbGamma.get(afterSrgbG);
//                double tb = srgbGamma.get(afterSrgbB);
//                if ((int) tr != (int) R || (int) tg != (int) G || (int) tb != (int) B) {
//                    B = B;
////                    System.out.println(counter++);
//                }
//                int delta = 1;
//                if (Math.abs((int) tr - clamp(R)) > delta || Math.abs((int) tg - clamp(G)) > delta || Math.abs((int) tb - clamp(B)) > delta) {
//                    B = B;
////                    System.out.println(counter++);
//                }

//                diff 0: 43918044 (87.257%)
//                diff 1: 11666759 (23.180%)
//                diff 2: 647514 (1.286%)
//                diff 3: 29822 (0.059%)
//                diff 4: 5577 (0.011%)
//                diff 5: 1358 (0.003%)
//                diff 6: 424 (0.001%)
//                diff 7: 185 (0.000%)
//                diff 8: 84 (0.000%)
//                diff 9: 43 (0.000%)
//                diff 10: 20 (0.000%)

                R = srgbGamma.get(R);
                G = srgbGamma.get(G);
                B = srgbGamma.get(B);
            }
//            R = R * 255;
//            G = G * 255;
//            B = B * 255;


//            int red = clamp(R);
//            int green = clamp(G);
//            int blue = clamp(B);
            byte red = (byte) R;
            byte green = (byte) G;
            byte blue = (byte) B;
            colorConsumer.set(pixelX, pixelY, red, green, blue);
//            return (red << 16) + (green << 8) + blue;
        };
//        System.out.println();

//        float R = 137.25694f;
//        float G = 129.92612f;
//        float B = 113.51303f;
//
//        double[] input = new double[]{R / 255.0, G / 255.0, B / 255.0};
//        System.out.println("input:      " + Arrays.toString(input));
//        for (int i = 0; i < input.length; i++) {
//            input[i] = curves[0].get(input[i]);
//        }
//        System.out.println("linearized: " + Arrays.toString(input));
//
//        XYZNumber rXYZ = (XYZNumber) tags.stream().filter(tag -> tag.signature.equals("rXYZ")).findFirst().get().value;
//        XYZNumber gXYZ = (XYZNumber) tags.stream().filter(tag -> tag.signature.equals("gXYZ")).findFirst().get().value;
//        XYZNumber bXYZ = (XYZNumber) tags.stream().filter(tag -> tag.signature.equals("bXYZ")).findFirst().get().value;
//
//        {
//            double r = input[0];
//            double g = input[1];
//            double b = input[2];
//            input[0] = r * rXYZ.value[0] + g * gXYZ.value[0] + b * bXYZ.value[0];
//            input[1] = r * rXYZ.value[1] + g * gXYZ.value[1] + b * bXYZ.value[1];
//            input[2] = r * rXYZ.value[2] + g * gXYZ.value[2] + b * bXYZ.value[2];
//        }
//        System.out.println("CIEXYZ:     " + Arrays.toString(input));
//
////        input = new double[]{0.2206726, 0.22601318, 0.13980103};
////        System.out.println("CIEXYZ:     " + Arrays.toString(input));
//
////        {
////            input[0]=input[0]/input[1];
////            input[2]=input[2]/input[1];
////            input[1]=input[1]/input[1];
////        }
////        System.out.println("CIEXYZ:     "+ Arrays.toString(input));
//
//        {
//            double r = input[0];
//            double g = input[1];
//            double b = input[2];
////            input[0] = r * 3.2404542 + g * -1.5371385 + b * -0.4985314;
////            input[1] = r * -0.9692660 + g * 1.8760108 + b * 0.0415560;
////            input[2] = r * 0.0556434 + g * -0.2040259 + b * 1.0572252;
////            input[0] = r * 3.2410 + g * -1.5374 + b * -0.4986;
////            input[1] = r * -0.9692 + g * 1.8760 + b * 0.0416;
////            input[2] = r * 0.0556 + g * -0.2040 + b * 1.0570;
//
////            sRGB to ciexyz
////            0.4358587014572366	0.38533607995727476	0.14302281223773555
////            0.22238498512245364	0.7170519569695583	0.06059357595178149
////            0.013916227969787138	0.09713893339436942	0.713847562371252
//
//            input[0] = r * 3.135888146102320303 + g * -1.6186919574161555234 + b * -0.49089080908292703386;
//            input[1] = r * -0.97864531205244014713 + g * 1.9159822781158407685 + b * 0.033441855488639160342;
//            input[2] = r * 0.072038948041565506726 + g * -0.22916711806510855193 + b * 1.4058783627429984365;
//        }
//        System.out.println("RGB:        " + Arrays.toString(input));
//
//        {
//            double pow = 1 / 2.4;
//            for (int i = 0; i < input.length; i++) {
//                if (input[i] > 0.0031308)
//                    input[i] = Math.pow(input[i], pow) * 1.055 - 0.055;
//                else
//                    input[i] = input[i] * 12.92;
//            }
//        }
//        System.out.println("RGB:        " + Arrays.toString(input));
//        System.out.println();
//
////        ciexyz: [0.2206726, 0.22601318, 0.13980103]
////        [0.5444724, 0.50826275, 0.4374609]
//        // linear rbg 0.25751126, 0.22177462, 0.16066225
//        //54.3 50.9 44.0
//        //138.5 129.7 112.2
    }

    private static Curve getTrcApproximation(Curve[] curves) {
        Curve trcApproximation;
        if (curves[0] instanceof ParametricCurve) {
            double[] trcTable = new double[768];
            {
                for (int i = 0; i < trcTable.length; i += 1) {
                    trcTable[i] = curves[0].get((i - 300) / 256.0);
                }
            }
            trcApproximation = x -> {
//                double index = x/255 / indexIncrement;
                int left = (int) x + 300;
                double ratio = (x + 300 - left);
                double value1 = trcTable[left];
                double value2 = left < 767 ? trcTable[left + 1] : left;
                return value1 + (value2 - value1) * ratio;
            };
        } else {
            trcApproximation = x -> curves[0].get(x / 255);
        }
        return trcApproximation;
    }

    public static int clamp(double col) {
        return Math.max(Math.min((int) col, 255), 0);
    }

    static final double SRGB_GAMMA_24 = 1 / 2.4;

    private static double applySRGBGamma(double input) {
        if (input > 0.0031308)
            return Math.pow(input, SRGB_GAMMA_24) * 1.055 - 0.055;
        else
            return input * 12.92;
    }

    public interface ColorTransform {
        void yCbCr2RGB(float y, float cb, float cr, int pixelX, int pixelY, ColorConsumer colorConsumer);
    }

    public interface ColorConsumer {
        void set(int x, int y, byte r, byte g, byte b);
    }

    private static int findClosestLeftIndex(double[] table, double x) {
        int left = 0;
        int right = table.length - 1;
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (table[mid] < x) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return Math.max(left - 1, 0);
    }


    static class XYZNumber {
        final double[] value;
        final float[] valueF;

        XYZNumber(double[] value) {
            this.value = value;
            valueF = new float[value.length];
            for (int i = 0; i < value.length; i++) {
                valueF[i] = (float) value[i];
            }
        }
    }

    interface MatrixMultiplication {
        double get(double r, double g, double b);
    }

    interface Curve {
        double get(double x);
    }

    interface ParametricCurve extends Curve {

        static ParametricCurve ofType(int type, double[] parameters) {
            if (type == 0) {
                double g = parameters[0];
                return (x) -> Math.pow(x, g);
            } else if (type == 1) {
                double g = parameters[0];
                double a = parameters[1];
                double b = parameters[2];
                double s = -b / a;
                return (x) -> x >= s ? Math.pow(a * x + b, g) : 0;
            } else if (type == 2) {
                double g = parameters[0];
                double a = parameters[1];
                double b = parameters[2];
                double c = parameters[3];
                double s = -b / a;
                return (x) -> x >= s ? Math.pow(a * x + b, g) + c : c;
            } else if (type == 3) {
                double g = parameters[0];
                double a = parameters[1];
                double b = parameters[2];
                double c = parameters[3];
                double d = parameters[4];
                return (x) -> x >= d ? Math.pow(a * x + b, g) : c * x;
            } else if (type == 4) {
                double g = parameters[0];
                double a = parameters[1];
                double b = parameters[2];
                double c = parameters[3];
                double d = parameters[4];
                double e = parameters[5];
                double f = parameters[6];
                return (x) -> x >= d ? Math.pow(a * x + b, g) + c : c * x + f;
            } else
                throw new IllegalStateException();
        }

        static ParametricCurve ofTypeInverse(int type, double[] parameters) {
            if (type == 0) {
                double g = parameters[0];
                double gi = 1 / g;
                return (y) -> Math.pow(y, gi);
            } else
//                if (type == 1) {
//                double g = parameters[0];
//                double a = parameters[1];
//                double b = parameters[2];
//                double s = -b / a;
//                return (x) -> x >= s ? Math.pow(a * x + b, g) : 0;
//            } else if (type == 2) {
//                double g = parameters[0];
//                double a = parameters[1];
//                double b = parameters[2];
//                double c = parameters[3];
//                double s = -b / a;
//                return (x) -> x >= s ? Math.pow(a * x + b, g) + c : c;
//            } else
                if (type == 3) {
                    double g = parameters[0];
                    double gi = 1 / g;
                    double a = parameters[1];
                    double b = parameters[2];
                    double c = parameters[3];
                    double d = parameters[4];
                    return (y) -> {
                        double x = y / c;
                        if (x < d)
                            return x;
                        else
                            return (Math.pow(y, gi) - b) / a;
                    };
                } else if (type == 4) {
                    double g = parameters[0];
                    double gi = 1 / g;
                    double a = parameters[1];
                    double b = parameters[2];
                    double c = parameters[3];
                    double d = parameters[4];
                    double e = parameters[5];
                    double f = parameters[6];
                    return (y) -> {
                        double x = (y - f) / c;
                        if (x < d)
                            return x;
                        else
                            return (Math.pow(y, gi) - b) / a;
                    };
                } else
                    throw new IllegalStateException();
        }
    }

    private static double read32bitSignedFloat(BitStream stream) {
        short a = (short) stream.readShortUnsafe(true);
        int b = stream.readShortUnsafe(true);
        return a + b / 65535.0;
    }

    private static double read16bitUnsignedFloat(BitStream stream) {
        int value = stream.readShortUnsafe(true);
        return (value >> 8) + (value & 0xFF) / 256.0;
    }

    public static void main(String[] args) {
        List<Spline.Point> points = new ArrayList<>();
//        points.add(new Point(0, Math.pow(0, 2.4)));
//        points.add(new Point(128, Math.pow(128, 2.4)));
//        points.add(new Point(255, Math.pow(255, 2.4)));
        float v = 0;
        float step = 1;
        while (v <= 256) {
//            System.out.println("add " + (int)v);
            System.out.println("add " + v);
            points.add(new Spline.Point(Math.pow(v, 2.4), v));
//            step += Math.max(v / 25, 1);
            step += v / 12;
            v += step;
        }
        points.add(new Spline.Point(Math.pow(255, 2.4), 255));

        Spline spline = new Spline(points);
        System.out.println("spline.polynomials.length: " + spline.polynomials.length);
//        System.out.println(spline.getX(10));
//        System.out.println(Math.pow(10, 2.4));


        double maxDiff = 0;
        for (int j = 0; j < 256; j++) {
//            System.out.println("expected: " + Math.pow(j, 2.4) + ", actual: " + spline.getY(j) + ", diff: " + (Math.pow(j, 2.4) - spline.getY(j)));
            double diff = j - spline.getY(Math.pow(j, 2.4));
            maxDiff = Math.max(maxDiff, Math.abs(diff));
            System.out.println("expected: " + j + ", actual: " + spline.getY(Math.pow(j, 2.4)) + ", diff: " + (diff * 100));
        }
        System.out.println("maxDiff: " + (maxDiff * 100) + "%");
    }

    static class Spline {

        final ThirdDegreePolynomial[] polynomials;

        public double getY(double x) {
//            int i = 0;
//            while (i < polynomials.length - 1 && x > polynomials[i + 1].x) {
//                i++;
//            }
//            ThirdDegreePolynomial polynomial = polynomials[i];
            ThirdDegreePolynomial polynomial = findPolynomial(x);

            return polynomial.getY(x);
        }

        ThirdDegreePolynomial findPolynomial(double x) {
            int left = 0;
            int right = polynomials.length - 1;
            while (left < right) {
                int mid = left + (right - left) / 2;
                if (polynomials[mid].x < x) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
            return polynomials[Math.max(left - 1, 0)];
        }

        static double[][] toArrays(List<Point> points) {
            double[][] arrays = new double[2][];
            arrays[0] = new double[points.size()];
            arrays[1] = new double[points.size()];
            for (int i = 0; i < points.size(); i++) {
                arrays[0][i] = points.get(i).x;
                arrays[1][i] = points.get(i).y;
            }
            return arrays;
        }

        public Spline(List<Point> points) {
            points.sort(Comparator.comparingDouble(o -> o.x));
            double[][] temp = toArrays(points);
            double[] x = temp[0];
            double[] y = temp[1];
            int n = points.size();
            polynomials = new ThirdDegreePolynomial[n];
            for (int i = 0; i < n; ++i) {
                polynomials[i] = new ThirdDegreePolynomial();
                polynomials[i].x = x[i];
                polynomials[i].a = y[i];
            }
            double[] h = new double[n];
            for (int i = 0; i < n - 1; i++) {
                h[i] = x[i + 1] - x[i];
            }
            double[] alpha = new double[n];
            for (int i = 1; i < n - 1; i++) {
                alpha[i] = 3 / h[i] * (polynomials[i + 1].a - polynomials[i].a) - 3 / h[i - 1] * (polynomials[i].a - polynomials[i - 1].a);
            }

            double[] l = new double[n];
            double[] m = new double[n];
            double[] z = new double[n];
            l[0] = 1;
            for (int i = 1; i < n - 1; i++) {
                l[i] = 2 * (x[i + 1] - x[i - 1]) - h[i - 1] * m[i - 1];
                m[i] = h[i] / l[i];
                z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
            }
            l[n - 1] = 1;
            for (int i = n - 2; i >= 0; i--) {
                polynomials[i].c = z[i] - m[i] * polynomials[i + 1].c;
                polynomials[i].b = (polynomials[i + 1].a - polynomials[i].a) / h[i] - (polynomials[i + 1].c + 2 * polynomials[i].c) * h[i] / 3;
                polynomials[i].d = (polynomials[i + 1].c - polynomials[i].c) / (3 * h[i]);
            }
        }

        static public class Point {
            final public double x, y;

            public Point(double x, double y) {
                this.x = x;
                this.y = y;
            }
        }

        static class ThirdDegreePolynomial {
            public double a, b, c, d, x;

            public double getY(double X) {
                double h = (X - x);
                double h2 = h * h;
                double h3 = h2 * h;
                return a + b * h + c * h2 + d * h3;
            }
        }
    }
}
