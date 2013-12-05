package com.wizzardo.tools.image;

interface ResampleFilter {
    public float getSamplingRadius();

    float apply(float v);

    public abstract String getName();
}
