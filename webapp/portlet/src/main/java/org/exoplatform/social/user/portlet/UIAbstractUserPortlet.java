package org.exoplatform.social.user.portlet;

import javax.portlet.MimeResponse;
import javax.portlet.PortletMode;
import javax.portlet.ResourceURL;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIPortletApplication;
import org.json.JSONObject;

public abstract class UIAbstractUserPortlet extends UIPortletApplication {
  protected Profile currentProfile;// current user viewing

  public UIAbstractUserPortlet() throws Exception {
    super();
  }

  @Override
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    beforeProcessRender(context);
    //
    super.processRender(app, context);
    //
    afterProcessRender(context);
  }

  public void beforeProcessRender(WebuiRequestContext context) {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    PortletMode portletMode = portletReqContext.getApplicationMode();
    if (portletMode == PortletMode.VIEW) {
      Identity ownerIdentity = Utils.getOwnerIdentity(true);
      currentProfile = ownerIdentity.getProfile();
    }
  }

  public void afterProcessRender(WebuiRequestContext context) {
  }

  protected boolean isOwner() {
    return Utils.isOwner();
  }

  protected String getCurrentRemoteId() {
    return currentProfile.getIdentity().getRemoteId();
  }

  protected void initProfilePopup() throws Exception {
    JSONObject object = new JSONObject();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    object.put("StatusTitle", UserProfileHelper.getLabel(context, "UserProfilePopup.label.Loading"));
    String[] keys = new String[]{"Connect", "Confirm", "CancelRequest", "RemoveConnection", "Ignore"};
    for (int i = 0; i < keys.length; i++) {
      object.put(keys[i], UserProfileHelper.getLabel(context, "UserProfilePopup.label." + keys[i]));
    }
    //
    context.getJavascriptManager().getRequireJS().require("SHARED/social-ui-profile", "profile" + getId())
           .addScripts("profile" + getId() + ".initUserProfilePopup('" + getId() + "', " + object.toString() + ");");
  }


  public void initSpacePopup() throws Exception {
    JSONObject object = new JSONObject();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    object.put("StatusTitle", UserProfileHelper.getLabel(context, "UserProfilePopup.label.Loading"));
    object.put("members", UserProfileHelper.getLabel(context, "UIActivity.label.Members"));
    object.put("leave", UserProfileHelper.getLabel(context, "UIManageAllSpaces.label.action_leave_space"));
    object.put("join", UserProfileHelper.getLabel(context, "UIManageAllSpaces.label.action_join"));
    String defaultUrl = LinkProvider.SPACE_DEFAULT_AVATAR_URL;

    context.getJavascriptManager().getRequireJS().require("SHARED/social-ui-profile", "socialProfile" + getId())
            .addScripts("profile" + getId() + ".initSpaceInfoPopup('" + getId() + "', " + object.toString() + ", '"+defaultUrl+"');");
  }

  protected String buildResourceURL(String key) {
    try {
      WebuiRequestContext ctx = WebuiRequestContext.getCurrentInstance();
      MimeResponse res = ctx.getResponse();
      ResourceURL rsURL = res.createResourceURL();
      rsURL.setResourceID(key);
      return rsURL.toString();
    } catch (Exception e) {
      return "";
    }
  }
}
