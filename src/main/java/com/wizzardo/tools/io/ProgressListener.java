package com.wizzardo.tools.io;

/**
 * @author: moxa
 * Date: 5/26/13
 */
public class ProgressListener {
    private volatile int progress = -1;
    private OnChange onChange;

    public ProgressListener() {
    }

    public ProgressListener(OnChange onChange) {
        this.onChange = onChange;
    }

    public static interface OnChange {
        public void onProgressChanged(int progress);
    }

    protected void onComplete() {
    }

    protected void onProgressChanged(int progress) {
        if (onChange != null)
            onChange.onProgressChanged(progress);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (this.progress != progress) {
            this.progress = progress;
            onProgressChanged(progress);
        }
        if (progress >= 100) {
            onComplete();
        }
    }
}
