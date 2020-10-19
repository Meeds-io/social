/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.user.form;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.settings.IMType;
import org.exoplatform.social.core.profile.settings.UserProfileSettingsService;
import org.exoplatform.social.user.form.UIInputSection.ActionData;
import org.exoplatform.social.user.portlet.UserProfileHelper;
import org.exoplatform.social.user.profile.AboutMeComparator;
import org.exoplatform.social.user.profile.ContactComparator;
import org.exoplatform.social.user.profile.ExperiencesComparator;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.profile.settings.UserProfileRenderingService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.*;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.*;
import org.exoplatform.webui.utils.TimeConvertUtils;

@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "war:/groovy/social/portlet/user/UIEditUserProfileForm.gtmpl",
   events = {
     @EventConfig(listeners = UIEditUserProfileForm.AddExperienceActionListener.class, phase = Phase.DECODE),
     @EventConfig(listeners = UIEditUserProfileForm.RemoveExperienceActionListener.class, phase = Phase.DECODE),
     @EventConfig(listeners = UIEditUserProfileForm.RemoveActionListener.class, phase = Phase.DECODE),
     @EventConfig(listeners = UIEditUserProfileForm.CancelActionListener.class, phase = Phase.DECODE),
     @EventConfig(listeners = UIEditUserProfileForm.SaveActionListener.class, csrfCheck = true)
   }
)
public class UIEditUserProfileForm extends UIForm {
  public static final String PLACEHOLDER_KEY = "placeholder";

  public static final String FIELD_ABOUT_SECTION = "AboutSection";
  public static final String FIELD_BASE_SECTION = "BaseSection";
  public static final String FIELD_EXPERIENCE_SECTION = "ExperienceSection";
  public static final String OPTION_MALE = "Male";
  public static final String OPTION_FEMALE = "Female";
  public static final String FIELD_EMPTY_EXPERIENCE_SECTION = "emprySelection";
  public static final String FIELD_INFO_EMPTY_EXPERIENCE = "infoExperien";
  public static final String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";
  /** PHONE_TYPES. */
  public static final String[] PHONE_TYPES = new String[] {"work","home","other"};
  /** PHONE REGEX EXPRESSION. */
  public static final String PHONE_REGEX_EXPRESSION = "^[\\d\\s ().+-]{3,25}+$";
  /** URL REGEX EXPRESSION. */
  public static final String URL_REGEX_EXPRESSION ="^(?i)(" +
      "((?:(?:ht)tp(?:s?)\\:\\/\\/)?" +                                                         // protocol
      "(?:\\w+:\\w+@)?" +                                                                       // username password
      "(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +  // IPAddress
      "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))|" +     // IPAddress
      "(?:(?:[-\\p{L}\\p{Digit}\\+\\$\\-\\*\\=]+\\.)+" +
      "(?:[a-z]{2,}))))|" + //Domain
      "(?:(?:(?:ht)tp(?:s?)\\:\\/\\/)(?:\\w+:\\w+@)?(?:[-\\p{L}\\p{Digit}\\+\\$\\-\\*\\=]+))" + // Protocol with hostname
      ")" +
      "(?::[\\d]{1,5})?" +                                                                      // port
      "(?:[\\/|\\?|\\#].*)?$";
  
  private Profile currentProfile;
  private List<String> experiens = new LinkedList<String>();
  private int index = 0;
  
  public UIEditUserProfileForm() throws Exception {
    if (getId() == null) {
      setId("UIEditUserProfileForm");
    }
    UIInputSection aboutSection = new UIInputSection(FIELD_ABOUT_SECTION, Profile.ABOUT_ME);
    aboutSection.useGroupControl(false)
                .addUIFormInput(new UIFormTextAreaInput(Profile.ABOUT_ME, Profile.ABOUT_ME, "")
                .addValidator(StringLengthValidator.class, 1500));
    //
    UIInputSection baseSection = new UIInputSection(FIELD_BASE_SECTION, "ContactInfomation");
    baseSection.addUIFormInput(createUIFormStringInput(Profile.FIRST_NAME, true)
                               .addValidator(PersonalNameValidator.class).addValidator(StringLengthValidator.class, 3, 45));
    //
    baseSection.addUIFormInput(createUIFormStringInput(Profile.LAST_NAME, true)
                               .addValidator(PersonalNameValidator.class).addValidator(StringLengthValidator.class, 3, 45));
    //
    baseSection.addUIFormInput(createUIFormStringInput(Profile.EMAIL, true).addValidator(EmailAddressValidator.class)
                                                                           .addValidator(StringLengthValidator.class, 5, 100));
    //
    UIChangeAvatarContainer avatarContainer = createUIComponent(UIChangeAvatarContainer.class, null, "Avatar");
    baseSection.addUIFormInput(avatarContainer);
    //
    baseSection.addUIFormInput(createUIFormStringInput(Profile.POSITION, false).addValidator(StringLengthValidator.class, 100));
    //
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>("", ""));
    options.add(new SelectItemOption<String>(getLabel(OPTION_MALE), Profile.MALE));
    options.add(new SelectItemOption<String>(getLabel(OPTION_FEMALE), Profile.FEMALE));
    UIFormSelectBox genderSelectBox = new UIFormSelectBox(Profile.GENDER, Profile.GENDER, options);
    genderSelectBox.setLabel(Profile.GENDER);
    baseSection.addUIFormInput(genderSelectBox);
    //
    UIMultiValueSelection phoneSelection = new UIMultiValueSelection(Profile.CONTACT_PHONES, getId(), Arrays.asList(PHONE_TYPES));
    baseSection.addUIFormInput(phoneSelection.addValidator(ExpressionValidator.class, PHONE_REGEX_EXPRESSION, "UIEditUserProfileForm.msg.Invalid-phone"));
    //
    UserProfileSettingsService profileSettings = CommonsUtils.getService(UserProfileSettingsService.class);
    Collection<IMType> imTypes = profileSettings.getIMTypes();
    List<String> imTypesValues = new ArrayList<>(imTypes.size());
    for (IMType imt : imTypes) {
      imTypesValues.add(imt.getId());
    }
    UIMultiValueSelection imsSelection = new UIMultiValueSelection(Profile.CONTACT_IMS, getId(), imTypesValues);
    UserProfileRenderingService settingsRenderers = CommonsUtils.getService(UserProfileRenderingService.class);
    imsSelection.withValueControl(settingsRenderers.getIMControl());
    baseSection.addUIFormInput(imsSelection.addValidator(StringLengthValidator.class, 100));
    //
    UIFormMultiValueInputSet urlMultiValueInput = new UIFormMultiValueInputSet(Profile.CONTACT_URLS, Profile.CONTACT_URLS);
    urlMultiValueInput.addValidator(ExpressionValidator.class, URL_REGEX_EXPRESSION, "UIEditUserProfileForm.msg.Invalid-url");
    urlMultiValueInput.addValidator(StringLengthValidator.class, 5, 100);
    urlMultiValueInput.setType(UIFormStringInput.class);
    urlMultiValueInput.setValue(Arrays.asList(""));
    urlMultiValueInput.setLabel(Profile.CONTACT_URLS);
    baseSection.addUIFormInput(urlMultiValueInput);
    //
    addUIFormInput(aboutSection);
    addUIFormInput(baseSection);
  }
  
  private List<ActionData> createExperienceActions(String experienId, boolean hasAdd) {
    List<ActionData> actions = new ArrayList<UIInputSection.ActionData>();
    ActionData removeAction = new ActionData();
    removeAction.setAction("RemoveExperience").setIcon("uiIconClose")
                .setTooltip(UserProfileHelper.getLabel(null, "UIEditUserProfileForm.title.RemoveExperience")).setObjectId(experienId);
    actions.add(removeAction);
    if(hasAdd) {
      ActionData addAction = new ActionData();
      addAction.setAction("AddExperience").setIcon("uiIconPlus")
               .setTooltip(UserProfileHelper.getLabel(null, "UIEditUserProfileForm.title.AddExperience")).setObjectId(experienId);
      actions.add(addAction);
    }
    return actions;
  }

  private UIInputSection getOrCreateExperienceSection(String id) throws Exception {
    UIInputSection experienceSection = getChildById(id);
    if(experienceSection != null) {
      return experienceSection;
    }
    String label = (experiens.size() == 0) ? "Experience" : "";
    experienceSection = new UIInputSection(id, label, "uiExperien");

    //
    UIFormStringInput company = createUIFormStringInput(Profile.EXPERIENCES_COMPANY + id, true);
    company.addValidator(StringLengthValidator.class, 100);
    company.setLabel(Profile.EXPERIENCES_COMPANY);
    experienceSection.addUIFormInput(company);
    experienceSection.addUIFormInput(new UIFormHiddenInput(Profile.EXPERIENCES_ID + id, ""));
    //
    experienceSection.addUIFormInput(createUIFormStringInput(Profile.EXPERIENCES_POSITION + id, true)
                                       .addValidator(StringLengthValidator.class, 100), Profile.EXPERIENCES_POSITION);
    //
    experienceSection.addUIFormInput(new UIFormTextAreaInput(Profile.EXPERIENCES_DESCRIPTION + id, Profile.EXPERIENCES_DESCRIPTION + id, "")
                                       .addValidator(StringLengthValidator.class, 1500), Profile.EXPERIENCES_DESCRIPTION);
    //
    experienceSection.addUIFormInput(new UIFormTextAreaInput(Profile.EXPERIENCES_SKILLS + id, Profile.EXPERIENCES_SKILLS + id, "")
                                       .addValidator(StringLengthValidator.class, 1500), Profile.EXPERIENCES_SKILLS);
    //
    experienceSection.addUIFormInput(new UIFormDateTimeInput(Profile.EXPERIENCES_START_DATE + id,
                                                             Profile.EXPERIENCES_START_DATE + id, null, false), Profile.EXPERIENCES_START_DATE);
    //
    experienceSection.addUIFormInput(new UIFormDateTimeInput(Profile.EXPERIENCES_END_DATE + id,
                                                             Profile.EXPERIENCES_END_DATE + id, null, false), Profile.EXPERIENCES_END_DATE);
    //
    experienceSection.addUIFormInput(new UICheckBoxInput(Profile.EXPERIENCES_IS_CURRENT + id,
                                                         Profile.EXPERIENCES_IS_CURRENT + id, false), "CurrentPosition");
    //
    addUIFormInput(experienceSection);
    //
    ++index;
    experiens.add(id);
    return experienceSection;
  }

  protected UIInputSection setValueExperienceSection(String id, Map<String, String> experience) throws Exception {
    UIInputSection experienceSection = getOrCreateExperienceSection(id);
    experienceSection.getUIInput(Profile.EXPERIENCES_ID + id).setValue(experience.get(Profile.EXPERIENCES_ID));
    experienceSection.getUIStringInput(Profile.EXPERIENCES_COMPANY + id).setValue(getValueExperience(experience, Profile.EXPERIENCES_COMPANY));
    experienceSection.getUIStringInput(Profile.EXPERIENCES_POSITION + id).setValue(getValueExperience(experience, Profile.EXPERIENCES_POSITION));
    experienceSection.getUIFormTextAreaInput(Profile.EXPERIENCES_DESCRIPTION + id).setValue(getValueExperience(experience, Profile.EXPERIENCES_DESCRIPTION));
    experienceSection.getUIFormTextAreaInput(Profile.EXPERIENCES_SKILLS + id).setValue(getValueExperience(experience, Profile.EXPERIENCES_SKILLS));
    experienceSection.getUIFormDateTimeInput(Profile.EXPERIENCES_START_DATE + id).setCalendar(stringToCalendar(experience.get(Profile.EXPERIENCES_START_DATE)));
    experienceSection.getUIFormDateTimeInput(Profile.EXPERIENCES_END_DATE + id).setCalendar(stringToCalendar(experience.get(Profile.EXPERIENCES_END_DATE)));
    experienceSection.getUICheckBoxInput(Profile.EXPERIENCES_IS_CURRENT + id).setChecked(Boolean.valueOf(String.valueOf(experience.get(Profile.EXPERIENCES_IS_CURRENT))));
    //
    return experienceSection;
  }

  private String getValueExperience(Map<String, String> experience, String key) {
    return experience.get(key);
  }

  private String getStringValueProfile(String key) {
    return (String) currentProfile.getProperty(key);
  }

  protected void setValueBasicInfo() throws Exception {
    //about me
    getUIInputSection(FIELD_ABOUT_SECTION).getUIFormTextAreaInput(Profile.ABOUT_ME)
                                          .setValue(getStringValueProfile(Profile.ABOUT_ME));
      
    //Basic information
    UIInputSection baseSection = getUIInputSection(FIELD_BASE_SECTION);
    baseSection.getUIStringInput(Profile.FIRST_NAME).setValue(getStringValueProfile(Profile.FIRST_NAME));
    baseSection.getUIStringInput(Profile.LAST_NAME).setValue(getStringValueProfile(Profile.LAST_NAME));
    baseSection.getUIStringInput(Profile.EMAIL).setValue(getStringValueProfile(Profile.EMAIL));
    baseSection.getUIStringInput(Profile.POSITION).setValue(getStringValueProfile(Profile.POSITION));
    //
    baseSection.getUIFormSelectBox(Profile.GENDER).setValue(getStringValueProfile(Profile.GENDER));
    if (getStringValueProfile(Profile.GENDER) != null) {
      baseSection.getUIFormSelectBox(Profile.GENDER).setSelectedValues(new String[] {getStringValueProfile(Profile.GENDER)});
    }
    //
    List<Map<String, String>> phones = UserProfileHelper.getMultiValues(currentProfile, Profile.CONTACT_PHONES);
    baseSection.getUIMultiValueSelection(Profile.CONTACT_PHONES).setValues(phones);
    //
    List<Map<String, String>> ims = UserProfileHelper.getMultiValues(currentProfile, Profile.CONTACT_IMS);
    baseSection.getUIMultiValueSelection(Profile.CONTACT_IMS).setValues(ims);
    
    baseSection.getUIFormMultiValueInputSet(Profile.CONTACT_URLS).setValue(UserProfileHelper.getURLValues(currentProfile));
    //Experience
    List<Map<String, String>> experiences = UserProfileHelper.getSortedExperiences(currentProfile);
    if(experiences != null && !experiences.isEmpty()) {
      int i = 0;
      String experienId;
      removeChildById(FIELD_EMPTY_EXPERIENCE_SECTION);
      int experienCurrentSize = experiens.size();
      for (Map<String, String> experience : experiences) {
        if (i < experienCurrentSize) {
          experienId = experiens.get(i);
          ++i;
        } else {
          experienId = FIELD_EXPERIENCE_SECTION + index;
        }
        setValueExperienceSection(experienId, experience);
      }
      resetActionFileds();
    } else if (experiens.isEmpty()) {
      addEmptyExperienceSection();
    }
  }
  
  private void addEmptyExperienceSection() throws Exception {
    //
    UIInputSection emptySelection = getChildById(FIELD_EMPTY_EXPERIENCE_SECTION);
    if(emptySelection == null) {
      emptySelection = new UIInputSection(FIELD_EMPTY_EXPERIENCE_SECTION, "Experience", "uiExperien").useGroupControl(false);
      ActionData addAction = new ActionData();
      addAction.setAction("AddExperience").setIcon("uiIconPlus")
               .setTooltip(UserProfileHelper.getLabel(null, "UIEditUserProfileForm.title.AddExperience"))
               .setObjectId(FIELD_EXPERIENCE_SECTION + "empty");
      emptySelection.addUIFormInput(new UIFormInputInfo(FIELD_INFO_EMPTY_EXPERIENCE, FIELD_INFO_EMPTY_EXPERIENCE,
                                                        getLabel("AddExperience")), Arrays.asList(addAction));
      //
      addUIFormInput(emptySelection);
    }
  }
  
  protected Calendar stringToCalendar(String sDate) {
    try {
      SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(sd.parse(sDate));
      return calendar;
    } catch (Exception e) {
      return null;
    }
  }

  private UIFormStringInput createUIFormStringInput(String name, boolean isMandatory) throws Exception {
    UIFormStringInput firstName = new UIFormStringInput(name, name, "");
    if (isMandatory) {
      firstName.addValidator(MandatoryValidator.class);
    }
    firstName.setLabel(name);
    return firstName;
  }
  /**
   * @throws Exception
   */
  private void initPlaceholder() throws Exception {
    //
    getUIInputSection(FIELD_ABOUT_SECTION).getUIFormTextAreaInput(Profile.ABOUT_ME)
                   .setHTMLAttribute(PLACEHOLDER_KEY, getLabel("aboutMePlaceholder"));
    //
    UIInputSection baseSection = getUIInputSection(FIELD_BASE_SECTION);
    UIFormMultiValueInputSet urlMulti = baseSection.getChildById(Profile.CONTACT_URLS);
    List<UIComponent> children = urlMulti.getChildren();
    for (UIComponent uiComponent : children) {
      if(uiComponent instanceof UIFormInputBase) {
        ((UIFormInputBase<?>)uiComponent).setHTMLAttribute(PLACEHOLDER_KEY, getLabel("sampleUrl"));
      }
    }
    //
    List<UIInputSection> experienceSections = getExperienceSections();
    for (UIInputSection uiInputSection : experienceSections) {
      List<UIFormDateTimeInput> dateInputs = new ArrayList<UIFormDateTimeInput>();
      uiInputSection.findComponentOfType(dateInputs, UIFormDateTimeInput.class);
      for (UIFormDateTimeInput uiFormDateTimeInput : dateInputs) {
        uiFormDateTimeInput.setHTMLAttribute(PLACEHOLDER_KEY, DATE_FORMAT_MMDDYYYY);
      }
    }
  }
  /**
   * @return
   */
  private List<UIInputSection> getExperienceSections() {
    List<UIInputSection> experienceSections = new ArrayList<UIInputSection>();
    List<UIComponent> children = this.getChildren();
    for (UIComponent uiComponent : children) {
      if (uiComponent.getId().startsWith(FIELD_EXPERIENCE_SECTION)) {
        experienceSections.add((UIInputSection) uiComponent);
      }
    }
    return experienceSections;
  }
  
  private void clearExperiences() {
    for (String string : experiens) {
      removeChildById(string);
    }
    //
    experiens.clear();
    index = 0;
  }
  
  /**
   * @param id
   * @return
   */
  private UIInputSection getUIInputSection(String id) {
    return (UIInputSection) getChildById(id);
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    //
    if (!Util.getPortalRequestContext().useAjax()) {
      resetForm();
      //
      initPlaceholder();
    }
    
    
    //
    if(this.currentProfile == null) {
      this.currentProfile = Utils.getViewerIdentity(true).getProfile();
      this.setValueBasicInfo();
    }
    //
    super.processRender(context);
  }
  
  /**
   * Reset actions on experience
   */
  private void resetActionFileds() {
    List<UIInputSection> experienceSections = getExperienceSections();
    int i = 1;
    int experienceSize = experienceSections.size();
    for (UIInputSection uiInputSection : experienceSections) {
      boolean hasAdd = (i ==  experienceSize);
      String experienId = uiInputSection.getName();
      uiInputSection.setActionField(Profile.EXPERIENCES_COMPANY + experienId, createExperienceActions(uiInputSection.getName(), hasAdd));
      ++i;
    }
  }
  /**
   * Reset form
   */
  private void resetForm() {
    currentProfile = null;
    clearExperiences();
  }
  
  /**
   * @param messageKey
   * @param args
   */
  private void warning(String messageKey, String... args) {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    context.getUIApplication().addMessage(new ApplicationMessage(messageKey, args, ApplicationMessage.WARNING));
    ((PortalRequestContext) context.getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
  }
  /**
   * @param map
   * @param key
   * @param value
   */
  private void putData(Map<String, String> map, String key, String value) {
    map.put(key, value);
  }
  /**
   * @param experienceSection
   * @return
   * @throws Exception
   */
  private Map<String, String> getValueExperience(UIInputSection experienceSection) throws Exception {
    String id = experienceSection.getId();
    Map<String, String> map = new HashMap<String, String>();
    putData(map, Profile.EXPERIENCES_ID, (String) experienceSection.getUIInput(Profile.EXPERIENCES_ID + id).getValue());
    putData(map, Profile.EXPERIENCES_COMPANY, experienceSection.getUIStringInput(Profile.EXPERIENCES_COMPANY + id).getValue());
    putData(map, Profile.EXPERIENCES_POSITION, experienceSection.getUIStringInput(Profile.EXPERIENCES_POSITION + id).getValue());
    putData(map, Profile.EXPERIENCES_DESCRIPTION, experienceSection.getUIFormTextAreaInput(Profile.EXPERIENCES_DESCRIPTION + id).getValue());
    putData(map, Profile.EXPERIENCES_SKILLS, experienceSection.getUIFormTextAreaInput(Profile.EXPERIENCES_SKILLS + id).getValue());
    //
    UIFormDateTimeInput startDate = experienceSection.getUIFormDateTimeInput(Profile.EXPERIENCES_START_DATE + id);
    UIFormDateTimeInput endDate = experienceSection.getUIFormDateTimeInput(Profile.EXPERIENCES_END_DATE + id);
    boolean isCurrent = experienceSection.getUICheckBoxInput(Profile.EXPERIENCES_IS_CURRENT + id).isChecked();
    
    
    // start empty
    if ((startDate.getValue() == null || startDate.getValue().isEmpty())) {
      // current is checked or end date not empty
      if ((isCurrent || (endDate.getValue() != null && !endDate.getValue().isEmpty()))) {
        warning("EmptyFieldValidator.msg.empty-input", getLabel(Profile.EXPERIENCES_START_DATE));
        return null;
      }
    } else {
      // Invalid-format-date
      if (startDate.getCalendar() == null || (startDate.getValue().indexOf("-") >= 0)) {
        warning("UIEditUserProfileForm.msg.Invalid-format-date", getLabel(Profile.EXPERIENCES_START_DATE), DATE_FORMAT_MMDDYYYY);
        return null;
      }
      //
      putData(map, Profile.EXPERIENCES_START_DATE, TimeConvertUtils.getFormatDate(startDate.getCalendar().getTime(), DATE_FORMAT_MMDDYYYY, Locale.ENGLISH));
      // start after today
      if (startDate.getCalendar().after(Calendar.getInstance())) {
        warning("UIEditUserProfileForm.msg.TheDateBeforeToday", getLabel(Profile.EXPERIENCES_START_DATE));
        return null;
      }
      if (!isCurrent) {
        // end date empty
        if (endDate.getValue() == null || endDate.getValue().isEmpty()) {
          warning("EmptyFieldValidator.msg.empty-input", getLabel(Profile.EXPERIENCES_END_DATE));
          return null;
        }
        // Invalid-format-date
        if (endDate.getCalendar() == null || (endDate.getValue().indexOf("-") >= 0)) {
          warning("UIEditUserProfileForm.msg.Invalid-format-date", getLabel(Profile.EXPERIENCES_END_DATE), DATE_FORMAT_MMDDYYYY);
          return null;
        }
        // end after today
        if (endDate.getCalendar().after(Calendar.getInstance())) {
          warning("UIEditUserProfileForm.msg.TheDateBeforeToday", getLabel(Profile.EXPERIENCES_END_DATE));
          return null;
        }
        // end date before start date
        if (endDate.getCalendar().before(startDate.getCalendar())) {
          warning("UIEditUserProfileForm.msg.FromDateBeforeToDate",
                  getLabel(Profile.EXPERIENCES_START_DATE), getLabel(Profile.EXPERIENCES_END_DATE));
          return null;
        }
        putData(map, Profile.EXPERIENCES_END_DATE, TimeConvertUtils.getFormatDate(endDate.getCalendar().getTime(), DATE_FORMAT_MMDDYYYY, Locale.ENGLISH));
        map.put(Profile.EXPERIENCES_IS_CURRENT, "false");
      } else {
        map.put(Profile.EXPERIENCES_IS_CURRENT, "true");
        map.put(Profile.EXPERIENCES_END_DATE, null);
      }
    }
    //
    return map;
  }

  /**
   * @return
   */
  protected String getViewProfileURL() {
    String portalOwner = Util.getPortalRequestContext().getPortalOwner();
    return this.currentProfile.getUrl().replace("intranet", portalOwner);
  }

  public static class SaveActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm uiForm = event.getSource();

      Profile profile = Utils.getViewerIdentity(true).getProfile();

      if(profile == null) {
        return;
      }

      UIInputSection baseSection = uiForm.getUIInputSection(FIELD_BASE_SECTION);
      String email = baseSection.getUIStringInput(Profile.EMAIL).getValue();
      // Checks input email existing or not 
      String oldEmail = profile.getEmail();
      if(oldEmail != null && !oldEmail.equals(email) && UserProfileHelper.isExistingEmail(email)) {
        uiForm.warning("UIEditUserProfileForm.msg.email-exist", uiForm.getLabel(Profile.EMAIL));
        return;
      }
      // About me
      String aboutMe = uiForm.getUIInputSection(FIELD_ABOUT_SECTION).getUIFormTextAreaInput(Profile.ABOUT_ME).getValue();
      // Basic information
      String firstName = baseSection.getUIStringInput(Profile.FIRST_NAME).getValue();
      String lastName = baseSection.getUIStringInput(Profile.LAST_NAME).getValue();
      //
      String position =  baseSection.getUIStringInput(Profile.POSITION).getValue();
      String gender = baseSection.getUIFormSelectBox(Profile.GENDER).getValue();
      //
      List<Map<String, String>> phones = baseSection.getUIMultiValueSelection(Profile.CONTACT_PHONES).getValues();
      //
      List<Map<String, String>> ims = baseSection.getUIMultiValueSelection(Profile.CONTACT_IMS).getValues();
      //
      List<Map<String, String>> mapUrls = new ArrayList<>();
      List<?> urls = baseSection.getUIFormMultiValueInputSet(Profile.CONTACT_URLS).getValue();
      for (Object url : urls) {
        String urlStr = (String)url;
        if (urlStr == null || urlStr.isEmpty() || urlStr.trim().isEmpty()) {
          continue;
        }
        Map<String, String> mUrl = new HashMap<>();
        mUrl.put(UserProfileHelper.KEY, UserProfileHelper.URL_KEY);
        mUrl.put(UserProfileHelper.VALUE, urlStr);
        mapUrls.add(mUrl);
      }
      //Experiences
      List<Map<String, String>> experiences = new ArrayList<Map<String, String>>();
      List<UIInputSection> experienceSections = uiForm.getExperienceSections();
      for (UIInputSection experienSection : experienceSections) {
        Map<String, String> map = uiForm.getValueExperience(experienSection);
        if (map == null) {
          return;
        }
        if (map.size() > 0) {
          experiences.add(map);
        }
      }

      List<Profile.UpdateType> list = new ArrayList<>();
      //
      AboutMeComparator aboutMeSection = new AboutMeComparator(getValue(aboutMe), profile);
      if (aboutMeSection.hasChanged()) {
        list.add(Profile.UpdateType.ABOUT_ME);
      }
      profile = aboutMeSection.getProfile();
      //
      ContactComparator contactSection = new ContactComparator(firstName, lastName, email, getValue(position), gender, phones, ims, mapUrls, profile);
      if (contactSection.hasChanged()) {
        list.add(Profile.UpdateType.CONTACT);
      }
      profile = contactSection.getProfile();
      //
      ExperiencesComparator experiencesSection = new ExperiencesComparator(experiences, profile);
      if (experiencesSection.hasChanged()) {
        list.add(Profile.UpdateType.EXPERIENCES);
      }
      profile = experiencesSection.getProfile();
      //
      profile.setListUpdateTypes(list);
      Utils.getIdentityManager().updateProfile(profile);
      //
      uiForm.resetForm();
      //
      String portalOwner = Util.getPortalRequestContext().getPortalOwner();
      event.getRequestContext().getJavascriptManager().getRequireJS()
           .addScripts("setTimeout(function() {window.open(window.location.origin + '" +
            profile.getUrl().replace("intranet", portalOwner) + "', '_self')}, 200);");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
    
    private String getValue(String value) {
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
      return null;
    }
  }
  
  public static class CancelActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm editUserProfile = event.getSource();
      //
      editUserProfile.resetForm();
      //
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }

  public static class AddExperienceActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm editUserProfile = event.getSource();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (objectId != null && !objectId.startsWith(FIELD_EXPERIENCE_SECTION)) {
        return;
      }
      if (objectId.contains("empty")) {
        editUserProfile.removeChildById(FIELD_EMPTY_EXPERIENCE_SECTION);
      }
      //
      editUserProfile.getOrCreateExperienceSection(FIELD_EXPERIENCE_SECTION + editUserProfile.index);
      //
      editUserProfile.resetActionFileds();
      RequireJS requireJs = event.getRequestContext().getJavascriptManager().getRequireJS();
      requireJs.require("SHARED/user-profile", "profile")
               .addScripts("profile.chechboxUtil('" + (FIELD_EXPERIENCE_SECTION + editUserProfile.index) + "');");
      editUserProfile.initAboutMeTextArea(event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(editUserProfile);
    }
  }

  public static class RemoveExperienceActionListener extends EventListener<UIEditUserProfileForm> {
    @Override
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      UIEditUserProfileForm editUserProfile = event.getSource();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      editUserProfile.removeChildById(objectId);
      editUserProfile.experiens.remove(objectId);
      List<UIInputSection> experienceSections = editUserProfile.getExperienceSections();
      if(experienceSections.size() > 0) {
        experienceSections.get(0).setTitle("Experience");
      } else {
        editUserProfile.addEmptyExperienceSection();
      }
      //
      editUserProfile.resetActionFileds();
      editUserProfile.initAboutMeTextArea(event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(editUserProfile);
    }
  }

  /**
   * The action support to reload only form when remove URLs.
   */
  public static class RemoveActionListener extends EventListener<UIEditUserProfileForm> {
    public void execute(Event<UIEditUserProfileForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getUIInputSection(FIELD_BASE_SECTION));
    }
  }
  
  private void initAboutMeTextArea(WebuiRequestContext context) throws Exception {
    String aboutMe = getUIInputSection(FIELD_ABOUT_SECTION).getUIFormTextAreaInput(Profile.ABOUT_ME).getValue();
    if (aboutMe == null || getLabel("aboutMePlaceholder").equals(aboutMe) || aboutMe.length() == 0) {
      String scripts = new StringBuilder("(function(jq){jq(\"textarea#").append(Profile.ABOUT_ME)
                                                                        .append("\").val('')")
                                                                        .append(".attr(\"placeholder\", \"")
                                                                        .append(getLabel("aboutMePlaceholder"))
                                                                        .append("\");})(jq);").toString();
      
      context.getJavascriptManager().getRequireJS()
             .require("SHARED/jquery", "jq")
             .addScripts(scripts);
    }
  }   
}
