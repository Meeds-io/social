package org.exoplatform.social.metadata.thumbnail;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.social.core.identity.model.Identity;

public interface ImageThumbnailService {

    /**
     * Retrieves a thumbnail by given width and height or creates a thumbnail image and get it
     * if not exist
     * @param file Image file
     * @param identity User social identity
     * @param width target thumbnail width
     * @param height target thumbnail height
     * @return {@link FileItem}
     */
    FileItem getOrCreateThumbnail(FileItem file, Identity identity, int width, int height) throws Exception;
}
