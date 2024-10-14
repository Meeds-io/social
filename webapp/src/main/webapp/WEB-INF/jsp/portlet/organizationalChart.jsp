<%@ page import="org.exoplatform.commons.api.settings.SettingService" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%@ page import="org.exoplatform.commons.api.settings.data.Context" %>
<%@ page import="org.exoplatform.commons.api.settings.data.Scope" %>
<%@ page import="org.exoplatform.commons.api.settings.SettingValue" %>
<%@ page import="org.exoplatform.portal.webui.application.UIPortlet" %>
<%@ page import="org.exoplatform.social.core.space.model.Space" %>
<%@ page import="org.exoplatform.social.core.space.spi.SpaceService" %>
<%@ page import="org.exoplatform.social.core.space.SpaceUtils" %>
<%@ page import="org.exoplatform.social.core.profileproperty.ProfilePropertyService" %>
<%@ page import="org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting" %>
<%@ page import="java.util.Objects" %>
<%@ page import="org.exoplatform.social.webui.Utils" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="io.meeds.social.translation.service.TranslationService" %>
<%@ page import="io.meeds.social.translation.model.TranslationField" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects/>
<%
    ProfilePropertyService profilePropertyService = CommonsUtils.getService(ProfilePropertyService.class);
    ProfilePropertySetting propertySetting = profilePropertyService.getProfileSettingByName("manager");
    String appObjectType = "organizationalChart";
    String id = UIPortlet.getCurrentUIPortlet().getStorageId();
    String applicationId =  appObjectType + id;
    SettingService settingsService = CommonsUtils.getService(SettingService.class);
    SettingValue<?> centerUserSettingValue = settingsService
            .get(Context.GLOBAL, Scope.APPLICATION.id(applicationId), "organizationalChartCenterUser");
    String centerUser = centerUserSettingValue != null && centerUserSettingValue.getValue() != null
            ? centerUserSettingValue.getValue().toString() : null;
    SettingValue<?> hasHeaderTitleSettingValue = settingsService
            .get(Context.GLOBAL, Scope.APPLICATION.id(applicationId), "organizationalChartHasHeaderTitle");
    boolean hasHeaderTitle = hasHeaderTitleSettingValue == null || hasHeaderTitleSettingValue.getValue() == null
            || Boolean.parseBoolean(hasHeaderTitleSettingValue.getValue().toString());

    TranslationService translationService = CommonsUtils.getService(TranslationService.class);
    TranslationField translationField = translationService.getTranslationField(appObjectType, Long.parseLong(id), "chartHeaderTitle");
    Map<Locale, String> labels = translationField.getLabels();
    String headerTitle = null;
    String headerTranslations = null;
    if (labels != null && !labels.isEmpty()) {
        headerTitle = labels.get(request.getLocale());
        ObjectMapper mapper = new ObjectMapper();
        headerTranslations = mapper.writeValueAsString(labels);
    }
    PortletPreferences preferences = renderRequest.getPreferences();
    boolean canUpdateCenterUser = Boolean.parseBoolean(preferences.getValue("canUpdateCenterUser", "true"));
    if (centerUser == null) {
        centerUser = preferences.getValue("centerUser", null);
    }
    ResourceBundleService resourceBundleService = CommonsUtils.getService(ResourceBundleService.class);
    ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("locale.portlet.Portlets", request.getLocale());
    if (headerTitle == null) {
        try {
            headerTitle = resourceBundle.getString(preferences.getValue("headerTitle", null));
        } catch (Exception e) {
            headerTitle = "";
        }
    }

    if (Objects.equals(centerUser, "@connected@")) {
        centerUser = Utils.getOwnerIdentityId();
    }

    boolean isManager = false;
    Space space = SpaceUtils.getSpaceByContext();
    if (space != null) {
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      isManager = spaceService.canManageSpace(space, request.getRemoteUser());
    }
%>

<div class="VuetifyApp" id="organizationalChartParent">
    <div data-app="true"
         id="organizationalChart"
         class="v-application transparent v-application--is-ltr theme--light">
        <% if (propertySetting.isActive()) { %>
            <script type="text/javascript">
                require(['PORTLET/social/OrganizationalChart'], app => app.init('<%=id%>', {
                                                                                                       userId: "<%=centerUser%>" !== 'null' && "<%=centerUser%>" || null,
                                                                                                       title: "<%=headerTitle%>" !== 'null' && "<%=headerTitle%>" || null,
                                                                                                       headerTranslations: <%=headerTranslations%>,
                                                                                                       canUpdateCenterUser: <%=canUpdateCenterUser%>,
                                                                                                       hasHeaderTitle: <%=hasHeaderTitle%>,
                                                                                                       isSpaceManager: <%=isManager%>
                                                                                                   }));
            </script>
        <% } else {%>
			<div class="my-auto white border-radius pa-5 card-border-radius v-card v-sheet v-sheet--outlined theme--light" style="margin:0 auto;">
			  <div class="d-flex mb-2 v-sheet theme--light" style="height: 28px;">
			    <p class="my-auto widget-text-header text-truncate">
			      <%=headerTitle%>
			    </p>
			  </div>
              <p class="mt-2">
                <%=resourceBundle.getString("organizationalChart.property.manager.disabled")%>
              </p>
            </div>
        <% } %>
    </div>
</div>
