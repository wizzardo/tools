package com.wizzardo.tools.image;

class HuffmanTable {
    final byte[] values;
    final int[] codeSizes;
    final int[] codes;
    final int[] minCode = new int[17];
    final int[] maxCode = new int[17];
    final int[] pointers = new int[17];


    HuffmanTable(byte[] header, byte[] values) {
        this.values = values;
        codeSizes = new int[values.length];
        {
            int k = 0;
            for (int i = 0; i < header.length; i++) {
                int l = header[i] & 0xFF;
                for (int j = 0; j < l; j++) {
                    codeSizes[k++] = i;
                }
            }
        }

        {
            int code = 0;
            int si = codeSizes[0];
            codes = new int[values.length];
            for (int k = 0; k < codeSizes.length; k++) {
                while (codeSizes[k] != si) {
                    code <<= 1;
                    si++;
                }

                codes[k] = code++;
            }
        }

        {
            for (int i = 1, j = 0; i < maxCode.length; i++) {
                if (header[i - 1] == 0) {
                    maxCode[i] = -1;
                } else {
                    pointers[i] = j;
                    minCode[i] = codes[j];
                    j += (header[i - 1] & 0xFF) - 1;
                    maxCode[i] = codes[j];
                    j++;
                }
            }
        }
    }

    public byte get(int code, int length) {
        int i = pointers[length] + code - minCode[length];
        return values[i];
    }
}
