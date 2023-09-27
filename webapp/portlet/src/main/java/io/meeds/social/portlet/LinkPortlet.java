package io.meeds.social.portlet;

import org.exoplatform.container.ExoContainerContext;

import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.service.LinkService;

public class LinkPortlet extends BaseCMSPortlet {

  private static LinkService linkService;

  @Override
  protected void saveSettingName(String name, String pageReference) {
    getLinkService().initLinkSetting(name, pageReference);
  }

  @Override
  protected boolean isSettingNameExists(String name) {
    return getLinkSetting(name) != null;
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
