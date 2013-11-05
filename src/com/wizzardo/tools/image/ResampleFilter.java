package com.wizzardo.tools.image;

public interface ResampleFilter {
    public float getSamplingRadius();

    float apply(float v);

    public abstract String getName();
}
