package org.exoplatform.social.core.listeners;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.attachment.AttachmentService;
import org.exoplatform.social.metadata.model.MetadataItem;

import java.util.List;

public class ActivityAttachmentMetadataListener extends ActivityListenerPlugin {
  private MetadataService metadataService;

  private FileService fileService;

  public ActivityAttachmentMetadataListener(MetadataService metadataService, FileService fileService) {
    this.metadataService = metadataService;
    this.fileService = fileService;
  }

  @Override
  public void deleteActivity(ActivityLifeCycleEvent event) {
    // Cleanup all related attachments of the activity once deleted
    if (event.getActivity() != null && MapUtils.isNotEmpty(event.getActivity().getMetadatas())
        && CollectionUtils.isNotEmpty(event.getActivity().getMetadatas().get(AttachmentService.METADATA_TYPE.getName()))) {
      List<MetadataItem> attachmentMetadataItems =
          event.getActivity().getMetadatas().get(AttachmentService.METADATA_TYPE.getName());
      attachmentMetadataItems.forEach(attachment -> {
        long metadataId = attachment.getMetadata().getId();
        metadataService.deleteMetadataById(metadataId);
        long fileId = Long.parseLong(attachment.getMetadata().getName());
        fileService.deleteFile(fileId);
      });
    }
  }
}
