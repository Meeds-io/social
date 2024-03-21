<%@ page import="org.exoplatform.commons.api.settings.SettingService" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%@ page import="org.exoplatform.commons.api.settings.data.Context" %>
<%@ page import="org.exoplatform.commons.api.settings.data.Scope" %>
<%@ page import="org.exoplatform.commons.api.settings.SettingValue" %>
<%@ page import="org.exoplatform.portal.webui.application.UIPortlet" %>
<%@ page import="org.exoplatform.social.core.space.model.Space" %>
<%@ page import="org.exoplatform.social.core.space.spi.SpaceService" %>
<%@ page import="org.exoplatform.social.core.space.SpaceUtils" %>
<%@ page import="java.util.Objects" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects/>
<%
    String id = UIPortlet.getCurrentUIPortlet().getStorageId();
    String applicationId = "organizationalChart" + id;
    SettingService settingsService = CommonsUtils.getService(SettingService.class);
    SettingValue<?> centerUserSettingValue = settingsService
            .get(Context.GLOBAL, Scope.APPLICATION.id(applicationId), "organizationalChartCenterUser");
    SettingValue<?> headerTitleSettingValue = settingsService
            .get(Context.GLOBAL, Scope.APPLICATION.id(applicationId), "organizationalChartHeaderTitle");

    String centerUser = centerUserSettingValue != null && centerUserSettingValue.getValue() != null
            ? centerUserSettingValue.getValue().toString() : null;
    String headerTitle = headerTitleSettingValue != null && headerTitleSettingValue.getValue() != null
            ? headerTitleSettingValue.getValue().toString() : null;


    if (centerUser == null) {
        PortletPreferences preferences = renderRequest.getPreferences();
        centerUser = preferences.getValue("centerUser", null);
        try {
            ResourceBundleService resourceBundleService = CommonsUtils.getService(ResourceBundleService.class);
            ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("locale.portlet.Portlets", request.getLocale());
            headerTitle = resourceBundle.getString(preferences.getValue("headerTitle", null));
        } catch (Exception e) {
            headerTitle = null;
        }
    }

    if (Objects.equals(centerUser, "@connected@")) {
        centerUser = request.getRemoteUser();
    }

    boolean isManager = false;
    Space space = SpaceUtils.getSpaceByContext();
    if (space != null) {
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      isManager = spaceService.isSuperManager(request.getRemoteUser()) || spaceService.isManager(space, request.getRemoteUser());
    }
%>

<div class="VuetifyApp">
    <div data-app="true"
         id="organizationalChart"
         class="v-application transparent v-application--is-ltr theme--light">
        <script type="text/javascript">
            const settings = {
                user: "<%=centerUser%>" !== 'null' && "'<%=centerUser%>'" || null,
                title: "<%=headerTitle%>" !== 'null' && "<%=headerTitle%>" || null,
                isSpaceManager: <%=isManager%>
            }
            if (eXo?.env?.portal?.organizationalChartEnabled) {
                require(['PORTLET/social-portlet/OrganizationalChart'], app => app.init('<%=applicationId%>', settings));
            }
        </script>
    </div>
</div>
