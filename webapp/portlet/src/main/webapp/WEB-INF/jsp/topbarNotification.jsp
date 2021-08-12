<%@page import="org.exoplatform.commons.api.notification.service.WebNotificationService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%
  WebNotificationService webNotificationService = ExoContainerContext.getService(WebNotificationService.class);
  int badge = webNotificationService.getNumberOnBadge(request.getRemoteUser());
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
                <i aria-hidden="true" class="v-icon notranslate grey-color mdi mdi-bell theme--light" style="font-size: 21px;"></i>
                <span class="v-badge__wrapper <%=badge > 0? "" : "hidden"%>">
                  <span aria-atomic="true"
                      aria-label="Badge" aria-live="polite" role="status"
                      class="v-badge__badge"
                      style="inset: auto auto calc(100% - 12px) calc(100% - 12px); background-color: var(- -allPagesBadgePrimaryColor, #ff5335); border-color: var(- -allPagesBadgePrimaryColor, #ff5335);">
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