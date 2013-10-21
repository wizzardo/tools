package com.wizzardo.tools.io;

/**
 * @author: moxa
 * Date: 5/26/13
 */
public class ProgressListener {
    private int progress;

    protected void onComplete() {
    }

    protected void onProgressChanged(int progress) {
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
