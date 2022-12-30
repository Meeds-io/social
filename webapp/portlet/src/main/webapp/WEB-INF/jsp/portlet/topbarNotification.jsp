<%@page import="org.exoplatform.commons.api.notification.service.WebNotificationService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="java.util.Locale"%>
<%@page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@page import="java.util.ResourceBundle"%>
<%
  WebNotificationService webNotificationService = ExoContainerContext.getService(WebNotificationService.class);
  int badge = webNotificationService.getNumberOnBadge(request.getRemoteUser());
  ResourceBundle bundle;
  try {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", request.getLocale());
  } catch (Exception e) {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", Locale.ENGLISH);
  }
  String tooltip = bundle.getString("UIIntranetNotificationsPortlet.label.tooltip");
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="NotificationPopoverPortlet">
    <div class="v-application--wrap">
      <div class="flex">
        <div class="layout">
          <button type="button"
            class="text-xs-center v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default"
            onclick="Vue.startApp('PORTLET/social-portlet/TopBarNotification', 'init')">
            <span class="v-btn__content">
              <span flat="" class="v-badge v-badge--overlap theme--light">
                <i aria-hidden="true" class="v-icon notranslate fas fa-bell icon-medium-size icon-default-color theme--light" title="<%=tooltip%>"></i>
                <span class="v-badge__wrapper <%=badge > 0? "" : "hidden"%>">
                  <span aria-atomic="true"
                      aria-label="Badge" aria-live="polite" role="status"
                      class="v-badge__badge"
                      style="inset: auto auto calc(100% - 12px) calc(100% - 12px);">
                      <%=badge%>
                  </span>
                </span>
              </span>
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</div>