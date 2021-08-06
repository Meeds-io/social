package org.exoplatform.social.common.service.impl;

import org.exoplatform.social.common.service.HTMLUploadImageProcessor;

public class HTMLUploadImageProcessorImpl implements HTMLUploadImageProcessor {
    @Override
    public String processImages(String content, String locationId, String imagesSubLocationPath) throws Exception {
        return content;
    }

    @Override
    public String processSpaceImages(String content, String spaceGroupId, String imagesSubLocationPath) throws Exception {
        return content;
    }

    @Override
    public String processUserImages(String content, String userId, String imagesSubLocationPath) throws Exception {
        return content;
    }
}
