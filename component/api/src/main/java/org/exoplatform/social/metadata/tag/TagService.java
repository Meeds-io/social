package org.exoplatform.social.metadata.tag;

import java.util.Set;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.metadata.model.*;

public interface TagService {

  public static final MetadataType METADATA_TYPE = new MetadataType(3, "tags");

  /**
   * Updates the list of tags associated to an object.
   * 
   * @param objectType {@link MetadataItem} objectType
   * @param objectId {@link MetadataItem} objectId
   * @param parentObjectId {@link MetadataItem} parentObjectId
   * @param audienceId {@link Metadata} audienceId
   * @param creatorId tag creator {@link Identity} identifier
   * @param tagNames {@link Set} of tag names
   */
  void saveTags(String objectType,
                String objectId,
                String parentObjectId,
                long audienceId,
                long creatorId,
                Set<String> tagNames);

  /**
   * Detects the list of Tag names found in content that are added inside an
   * HTML link using a specific class name 'metadata-tag'.
   * 
   * @param content content used to detected associated tags
   * @return {@link Set} of detected Tag names
   */
  Set<String> detectTags(String content);
}
