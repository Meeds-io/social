<%@ page import="io.meeds.social.portlet.PeopleListPortlet" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsUrl" />
<portlet:resourceURL var="settingsResourceUrl" id="<%=PeopleListPortlet.SETTINGS_RESOURCE_ID%>" />
<portlet:resourceURL var="usersResourceUrl" id="<%=PeopleListPortlet.USERS_RESOURCE_ID%>" />
<portlet:resourceURL var="attributeValuesResourceUrl" id="<%=PeopleListPortlet.ATTRIBUTE_VALUES_RESOURCE_ID%>" />
<portlet:resourceURL var="groupsResourceUrl" id="<%=PeopleListPortlet.GROUPS_RESOURCE_ID%>" />
<%
  PortletPreferences preferences = renderRequest.getPreferences();
  JSONObject settings = new JSONObject();
  settings.put("headerTranslations", new JSONObject(PeopleListPortlet.storedHeaderTranslations(preferences)));
  settings.put("pageSize", PeopleListPortlet.storedPageSize(preferences));
  settings.put("filter", PeopleListPortlet.storedFilter(preferences));
  settings.put("directoryFiltered", PeopleListPortlet.isDirectoryFiltered(preferences));
  settings.put("directoryAttributes", new JSONArray(PeopleListPortlet.directoryAttributes(preferences)));
  boolean canEdit = PeopleListPortlet.canEdit(renderRequest.getRemoteUser());
  settings.put("canEdit", canEdit);
  if (canEdit) {
    settings.put("attributes", new JSONArray(PeopleListPortlet.storedAttributes(preferences, renderRequest.getLocale())));
    settings.put("attributeFilterInvalid", PeopleListPortlet.isAttributeFilterInvalid(preferences));
    settings.put("canSetGroupFilter", PeopleListPortlet.canSetGroupFilter(renderRequest.getRemoteUser()));
    JSONObject groups = new JSONObject(PeopleListPortlet.storedGroups(preferences, renderRequest.getRemoteUser()));
    settings.put("groups", groups.getJSONArray("groups"));
    settings.put("otherGroupsApplied", groups.getBoolean("otherGroupsApplied"));
    settings.put("otherGroupsCount", groups.getInt("otherGroupsCount"));
    settings.put("unresolvableGroups", groups.getBoolean("unresolvableGroups"));
    settings.put("groupFilterInvalid", groups.getBoolean("groupFilterInvalid"));
  }

  String portletId = (String) request.getAttribute("portletStorageId");
  String appId = "peopleList" + portletId;
  String settingsDomId = appId + "Settings";
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light peopleList"
    id="<%=appId%>">
    <textarea id="<%=settingsDomId%>" style="display:none;"><%=URLEncoder.encode(settings.toString(), StandardCharsets.UTF_8)%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social/PeopleList'], app => app.init({
        appId: '<%=appId%>',
        <%-- Never a // comment inside this script: the layout renderer strips newlines before
             injecting it, so a line comment swallows the rest of the script. --%>
        settings: JSON.parse(decodeURIComponent(document.getElementById('<%=settingsDomId%>').value.replace(/\+/g, '%20'))),
        saveSettingsUrl: '<%=saveSettingsUrl%>',
        settingsResourceUrl: '<%=settingsResourceUrl%>',
        usersResourceUrl: '<%=usersResourceUrl%>',
        attributeValuesResourceUrl: '<%=attributeValuesResourceUrl%>',
        groupsResourceUrl: '<%=groupsResourceUrl%>'
      }));
    </script>
  </div>
</div>
