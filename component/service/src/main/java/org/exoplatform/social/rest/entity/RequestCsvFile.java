package org.exoplatform.social.rest.entity;

public class RequestCsvFile {

    private String uploadId;
    private boolean progress;
    private boolean clean;
    private boolean sync;


    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public boolean getProgress() {
        return progress;
    }

    public void setProgress(boolean progress) {
        this.progress = progress;
    }

    public boolean getClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public boolean getSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }
}
