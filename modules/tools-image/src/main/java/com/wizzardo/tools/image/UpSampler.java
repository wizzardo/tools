package com.wizzardo.tools.image;

interface UpSampler {
    static UpSampler createUpSampler(int sv, int sh) {
        return createUpSampler(sv, sh, false);
    }

    static UpSampler createUpSampler(int sv, int sh, boolean center) {
        UpSampler upSampler;
//        if (true) {
//            return (data, x, y) -> {
//                return data.get(x>>1, y >> 1);
//            };
//        }
        if (sh == 2 && sv == 1) {
            upSampler = (data, x, y) -> {
                if (x == 0) {
                    if (data.index == 0)
                        return data.get(x, y);
                    else
                        return (data.get(0, y) * 3 + data.prev[data.xyToLin(7, y)]) / 4;
                }

                int xx = x >> 1;
                if (x == data.lastColumn) {
                    return data.get(xx, y);
                }
                if (x == 15) {
                    return (data.get(7, y) * 3 + data.next[data.xyToLin(0, y)]) / 4;
                }

                if (x == xx << 1)
                    return (data.get(xx, y) * 3 + data.get(xx - 1, y)) / 4;
                else
                    return (data.get(xx, y) * 3 + data.get(xx + 1, y)) / 4;
            };
        } else if (sh == 1 && sv == 2) {
            upSampler = (data, x, y) -> {
                return data.get(x, y >> 1);
            };
        } else if (sh == 2 && sv == 2) {
            if (center && false) {
                return (data, x, y) -> {
//                    int xx = x >> 1;
//                    if ((x & 1) == 1)
//                        xx++;

                    int xx = (x & 1) == 0 ? x >> 1 : (x + 1) >> 1;
                    int yy = y >> 1;

                    if ((y & 1) == 1) {
                        float t0;
                        float t1;
                        if (y == 15) {
                            t0 = 3 * data.prevRow[data.xyToLin(xx - 1, data.index)] + data.getCurrentOrPrev(xx - 1, 0);
                            t1 = 3 * data.prevRow[data.xyToLin(xx, data.index)] + data.getCurrentOrPrev(xx, 0);
                        } else {
                            if (xx > 0) {
                                t0 = 3 * data.get(xx - 1, yy) + data.get(xx - 1, yy + 1);
                                if (xx == 8) {
                                    t1 = 3 * data.next[data.xyToLin(0, yy)] + data.next[data.xyToLin(0, yy + 1)];
                                } else {
                                    t1 = 3 * data.get(xx, yy) + data.get(xx, yy + 1);
                                }
                            } else {
                                t0 = 3 * data.prev[data.xyToLin(7, yy)] + data.prev[data.xyToLin(7, yy + 1)];
                                t1 = 3 * data.get(xx, yy) + data.get(xx, yy + 1);
                            }
                        }

                        if ((x & 1) == 1)
                            return (3 * t0 + t1) / 16;
                        else
                            return (3 * t1 + t0) / 16;
                    } else {
                        float t0;
                        float t1;
                        if (y == 0) {
                            t0 = 3 * data.getCurrentOrPrev(xx - 1, yy) + data.prevRow[data.xyToLin(xx - 1, data.index)];
                            t1 = 3 * data.getCurrentOrPrev(xx, yy) + data.prevRow[data.xyToLin(xx, data.index)];
                        } else {
                            if (xx > 0) {
                                t0 = 3 * data.get(xx - 1, yy) + data.get(xx - 1, yy - 1);
                                if (xx == 8) {
                                    t1 = 3 * data.next[data.xyToLin(0, yy)] + data.next[data.xyToLin(0, yy - 1)];
                                } else {
                                    t1 = 3 * data.get(xx, yy) + data.get(xx, yy - 1);
                                }
                            } else {
                                t0 = 3 * data.prev[data.xyToLin(7, yy)] + data.prev[data.xyToLin(7, yy - 1)];
                                t1 = 3 * data.get(xx, yy) + data.get(xx, yy - 1);
                            }
                        }

                        if ((x & 1) == 1)
                            return (3 * t0 + t1) / 16;
                        else
                            return (3 * t1 + t0) / 16;
                    }
                };
            }
            upSampler = (data, x, y) -> {
//                int xx = x >> 1;
//                if ((x & 1) == 1)
//                    xx++;
                int xx = (x & 1) == 0 ? x >> 1 : (x + 1) >> 1;
                int yy = y >> 1;

//                if (x >= 0) {//testing perf
//                    return data.get(Math.min(xx, 7), Math.min(yy, 7));
//                }

//                if (x == 0) {
//                    data.prevXX = -1;
//                }

                if ((y == 0 && data.y == 0) || y == data.lastRow) {
                    if (x == 0 && data.index == 0) {
                        return data.get(xx, yy);
                    }
                    if (x == data.lastColumn) {
                        return data.get(x >> 1, yy);
                    }
                    float t0;
                    float t1;
                    t0 = data.getCurrentOrPrev(xx - 1, yy);
                    t1 = data.getCurrentOrPrev(xx, yy);
                    if ((x & 1) == 1)
                        return (3 * t0 + t1) / 4;
                    else
                        return (t0 + 3 * t1) / 4;
                }
                if (x == 0 && data.index == 0) {
                    if ((y & 1) == 0)
                        if (y == 0 && data.y > 0)
                            return (3 * data.getCurrentOrPrev(xx, 0) + data.prevRow[data.xyToLin(xx, data.index)]) / 4;
                        else
                            return (3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy - 1)) / 4;
                    else if (y == 15)
                        return (3 * data.prevRow[data.xyToLin(xx, data.index)] + data.getCurrentOrPrev(xx, 0)) / 4;
                    else
                        return (3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy + 1)) / 4;
                }

                if ((y & 1) == 1) {
                    if (x == data.lastColumn) {
                        xx = x >> 1;
                        if (y == 15)
                            return (3 * data.prevRow[data.xyToLin(xx, data.index)] + data.getCurrentOrPrev(xx, 0)) / 4;
                        else
                            return (3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy + 1)) / 4;
                    }

                    float t0;
                    float t1;
//                    if (data.prevXX == xx) {
//                        t0 = data.prevT0;
//                        t1 = data.prevT1;
//                    } else {
                    if (y == 15) {
//                            if (data.prevXX >= 0)
//                                t0 = data.prevT1;
//                            else
                        t0 = 3 * data.prevRow[data.xyToLin(xx - 1, data.index)] + data.getCurrentOrPrev(xx - 1, 0);
//                                if (t0 != data.prevT1) {
//                                    t0 = t0;
//                                }
                        t1 = 3 * data.prevRow[data.xyToLin(xx, data.index)] + data.getCurrentOrPrev(xx, 0);
                    } else {
//                            if (data.prevXX >= 0)
//                                t0 = data.prevT1;
//                            else
                        t0 = 3 * data.getCurrentOrPrev(xx - 1, yy) + data.getCurrentOrPrev(xx - 1, yy + 1);
//                            if (t0 != data.prevT1 && data.prevXX >= 0) {
//                                t0 = t0;
//                            }
                        t1 = 3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy + 1);
                    }
//                        data.prevXX = xx;
//                        data.prevT0 = t0;
//                        data.prevT1 = t1;
//                    }

                    if ((x & 1) == 1)
                        return (3 * t0 + t1) / 16;
                    else
                        return (3 * t1 + t0) / 16;
                } else {
                    if (x == data.lastColumn) {
                        xx = x >> 1;
                        if (y == 0)
                            return (3 * data.getCurrentOrPrev(xx, yy) + data.prevRow[data.xyToLin(xx, data.index)]) / 4;
                        else
                            return (3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy - 1)) / 4;
                    }

                    float t0;
                    float t1;

//                    if (data.prevXX == xx) {
//                        t0 = data.prevT0;
//                        t1 = data.prevT1;
//                    } else {
                    if (y == 0) {
//                            if (data.prevXX >= 0)
//                                t0 = data.prevT1;
//                            else
                        t0 = 3 * data.getCurrentOrPrev(xx - 1, yy) + data.prevRow[data.xyToLin(xx - 1, data.index)];
                        t1 = 3 * data.getCurrentOrPrev(xx, yy) + data.prevRow[data.xyToLin(xx, data.index)];
                    } else {
//                            if (data.prevXX >= 0)
//                                t0 = data.prevT1;
//                            else
                        t0 = 3 * data.getCurrentOrPrev(xx - 1, yy) + data.getCurrentOrPrev(xx - 1, yy - 1);
                        t1 = 3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy - 1);
                    }
//                        data.prevXX = xx;
//                        data.prevT0 = t0;
//                        data.prevT1 = t1;
//                    }

                    if ((x & 1) == 1)
                        return (3 * t0 + t1) / 16;
                    else
                        return (3 * t1 + t0) / 16;
                }
            };
//            upSampler = (data, x, y) -> {
////                int xx = x >> 1;
////                if ((x & 1) == 1)
////                    xx++;
//                int xx = (x & 1) == 0 ? x >> 1 : (x + 1) >> 1;
//                int yy = y >> 1;
//
////                if (x >= 0) {//testing perf
////                    return data.get(Math.min(xx, 7), Math.min(yy, 7));
////                }
//
////                if (x == 0) {
////                    data.prevXX = -1;
////                }
//
//                if ((y == 0 && data.y == 0) || y == data.lastRow) {
//                    if (x == 0 && data.index == 0) {
//                        return data.get(xx, yy)*4;
//                    }
//                    if (x == data.lastColumn) {
//                        return data.get(x >> 1, yy)*4;
//                    }
//                    float t0;
//                    float t1;
////                    t0 = data.getCurrentOrPrev(xx - 1, yy);
//                    return data.getCurrentOrPrev(xx, yy)*4;
////                    if ((x & 1) == 1)
////                        return (3 * t0 + t1) / 4;
////                    else
////                        return (t0 + 3 * t1) / 4;
//                }
//                if (x == 0 && data.index == 0) {
//                    if ((y & 1) == 0)
//                        if (y == 0 && data.y > 0)
//                            return (3 * data.getCurrentOrPrev(xx, 0) + data.prevRow[data.xyToLin(xx, data.index)]);
//                        else
//                            return (3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy - 1));
//                    else if (y == 15)
//                        return (3 * data.prevRow[data.xyToLin(xx, data.index)] + data.getCurrentOrPrev(xx, 0));
//                    else
//                        return (3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy + 1));
//                }
//
//                if ((y & 1) == 1) {
//                    if (x == data.lastColumn) {
//                        xx = x >> 1;
//                        if (y == 15)
//                            return (3 * data.prevRow[data.xyToLin(xx, data.index)] + data.getCurrentOrPrev(xx, 0));
//                        else
//                            return (3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy + 1));
//                    }
//
//                    float t0;
//                    float t1;
////                    if (data.prevXX == xx) {
////                        t0 = data.prevT0;
////                        t1 = data.prevT1;
////                    } else {
//                    if (y == 15) {
////                            if (data.prevXX >= 0)
////                                t0 = data.prevT1;
////                            else
////                        t0 = 3 * data.prevRow[data.xyToLin(xx - 1, data.index)] + data.getCurrentOrPrev(xx - 1, 0);
////                                if (t0 != data.prevT1) {
////                                    t0 = t0;
////                                }
//                        return 3 * data.prevRow[data.xyToLin(xx, data.index)] + data.getCurrentOrPrev(xx, 0);
//                    } else {
////                            if (data.prevXX >= 0)
////                                t0 = data.prevT1;
////                            else
////                        t0 = 3 * data.getCurrentOrPrev(xx - 1, yy) + data.getCurrentOrPrev(xx - 1, yy + 1);
////                            if (t0 != data.prevT1 && data.prevXX >= 0) {
////                                t0 = t0;
////                            }
//                        return 3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy + 1);
//                    }
////                        data.prevXX = xx;
////                        data.prevT0 = t0;
////                        data.prevT1 = t1;
////                    }
//
////                    if ((x & 1) == 1)
////                        return (3 * t0 + t1) / 16;
////                    else
////                        return (3 * t1 + t0) / 16;
//                } else {
//                    if (x == data.lastColumn) {
//                        xx = x >> 1;
//                        if (y == 0)
//                            return (3 * data.getCurrentOrPrev(xx, yy) + data.prevRow[data.xyToLin(xx, data.index)]);
//                        else
//                            return (3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy - 1));
//                    }
//
//                    float t0;
//                    float t1;
//
////                    if (data.prevXX == xx) {
////                        t0 = data.prevT0;
////                        t1 = data.prevT1;
////                    } else {
//                    if (y == 0) {
////                            if (data.prevXX >= 0)
////                                t0 = data.prevT1;
////                            else
////                        t0 = 3 * data.getCurrentOrPrev(xx - 1, yy) + data.prevRow[data.xyToLin(xx - 1, data.index)];
//                        return 3 * data.getCurrentOrPrev(xx, yy) + data.prevRow[data.xyToLin(xx, data.index)];
//                    } else {
////                            if (data.prevXX >= 0)
////                                t0 = data.prevT1;
////                            else
////                        t0 = 3 * data.getCurrentOrPrev(xx - 1, yy) + data.getCurrentOrPrev(xx - 1, yy - 1);
//                        return 3 * data.getCurrentOrPrev(xx, yy) + data.getCurrentOrPrev(xx, yy - 1);
//                    }
////                        data.prevXX = xx;
////                        data.prevT0 = t0;
////                        data.prevT1 = t1;
////                    }
//
////                    if ((x & 1) == 1)
////                        return (3 * t0 + t1) / 16;
////                    else
////                        return (3 * t1 + t0) / 16;
//                }
//            };
        } else
            throw new UnsupportedOperationException("Subsampling " + sh + " horizontal and " + sv + " vertical is not supported");
        return upSampler;
    }

    float get(JpegDecoder.MCUsHolder data, int x, int y);
}
