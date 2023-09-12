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
            class="text-xs-center v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default">
            <span class="v-btn__content">
              <span flat="" class="v-badge v-badge--overlap theme--light">
                <i aria-hidden="true" class="v-icon notranslate fas fa-bell icon-medium-size icon-default-color theme--light"></i>
                <script type="text/javascript">
                  require(['PORTLET/social-portlet/TopBarNotification'], app => app.init(<%=badge%>));
                </script>
              </span>
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</div>

