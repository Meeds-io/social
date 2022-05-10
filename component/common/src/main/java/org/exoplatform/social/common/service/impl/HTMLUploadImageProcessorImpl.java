package org.exoplatform.social.common.service.impl;

import org.exoplatform.social.common.service.HTMLUploadImageProcessor;

public class HTMLUploadImageProcessorImpl implements HTMLUploadImageProcessor {
    @Override
    public String processImages(String content, String locationId, String imagesSubLocationPath) {
        return content;
    }

    @Override
    public String processSpaceImages(String content, String spaceGroupId, String imagesSubLocationPath) {
        return content;
    }

    @Override
    public String processUserImages(String content, String userId, String imagesSubLocationPath) {
        return content;
    }

    @Override
    public String processImagesForExport(String content) {
        return content;
    }

    @Override
    public void uploadSpaceFile(String filePath, String spaceGroupId, String fileName, String imagesSubLocationPath) {

    }

    @Override
    public void uploadUserFile(String filePath, String userId, String fileName, String imagesSubLocationPath) {

    }
}
