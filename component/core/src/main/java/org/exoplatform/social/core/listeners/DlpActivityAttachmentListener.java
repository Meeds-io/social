package org.exoplatform.social.core.listeners;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.*;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;

@Asynchronous
public class DlpActivityAttachmentListener extends Listener<Object, Object> {

  private PortalContainer        container;

  private CachedActivityStorage  activityStorage;

  private DlpPositiveItemService dlpPositiveItemService;

  public DlpActivityAttachmentListener(DlpPositiveItemService dlpPositiveItemService,
                                       ActivityStorage activityStorage,
                                       PortalContainer container) {
    this.container = container;
    this.dlpPositiveItemService = dlpPositiveItemService;
    if (activityStorage instanceof CachedActivityStorage) {
      this.activityStorage = (CachedActivityStorage) activityStorage;
    }
  }

  @Override
  public void onEvent(Event<Object, Object> event) throws Exception {
    if (activityStorage == null) {
      return;
    }
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      String attachmentId = null;
      if (event.getData() instanceof DlpPositiveItem) {
        DlpPositiveItem quarantineItem = (DlpPositiveItem) event.getData();
        attachmentId = quarantineItem.getReference();
      } else if (event.getData() instanceof DlpPositiveItemEntity) {
        DlpPositiveItemEntity quarantineItem = (DlpPositiveItemEntity) event.getData();
        if (quarantineItem == null || !StringUtils.equals(quarantineItem.getType(), "file")) {
          return;
        }
        attachmentId = quarantineItem.getReference();
      } else if (event.getData() instanceof Long) {
        long dlpItemId = (Long) event.getData();
        DlpPositiveItem quarantineItem = dlpPositiveItemService.getDlpPositiveItemById(dlpItemId);
        if (quarantineItem == null || !StringUtils.equals(quarantineItem.getType(), "file")) {
          return;
        }
        attachmentId = quarantineItem.getReference();
      } else if (event.getData() instanceof String) {
        String dlpItemReference = (String) event.getData();
        DlpPositiveItem quarantineItem = dlpPositiveItemService.getDlpPositiveItemByReference(dlpItemReference);
        if (quarantineItem == null || !StringUtils.equals(quarantineItem.getType(), "file")) {
          return;
        }
        attachmentId = quarantineItem.getReference();
      }
      if (StringUtils.isBlank(attachmentId)) {
        return;
      }
      activityStorage.clearActivityCachedByAttachmentId(attachmentId);
    } finally {
      RequestLifeCycle.end();
    }
  }
}
