package io.meeds.social.portlet;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.service.LinkService;

public class LinkPortlet extends BaseCMSPortlet {

  private static LinkService linkService;

  @Override
  protected void saveSettingName(String name, String pageReference, long spaceId) {
    getLinkService().initLinkSetting(name, pageReference, spaceId);
  }

  @Override
  protected boolean isSettingNameExists(String name) {
    return getLinkSetting(name) != null;
  }

  @Override
  protected boolean canEditSettings(String name) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    return getLinkService().hasEditPermission(identity, getPageReference(), getSpaceId());
  }

  private LinkSetting getLinkSetting(String name) {
    return getLinkService().getLinkSetting(name);
  }

  private static LinkService getLinkService() {
    if (linkService == null) {
      linkService = ExoContainerContext.getService(LinkService.class);
    }
    return linkService;
  }

}
