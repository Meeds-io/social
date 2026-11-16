/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.security.rest;

import io.meeds.portal.security.service.SecuritySettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nl.captcha.Captcha;
import nl.captcha.text.producer.DefaultTextProducer;
import nl.captcha.text.renderer.DefaultWordRenderer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.rest.UserFieldValidator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.social.core.profilelabel.ProfileLabelService;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.web.login.recovery.PasswordRecoveryService;
import org.exoplatform.web.register.RegisterUIParamsExtension;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.security.security.RemindPasswordTokenService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import io.meeds.social.core.profileproperty.constant.ProfileFormKind;
import io.meeds.social.core.profileproperty.exception.ProfileFormFieldException;
import io.meeds.social.core.profileproperty.model.ProfilePropertyValue;
import io.meeds.social.core.profileproperty.service.ProfileFormFieldService;
import io.meeds.social.security.rest.model.RegistrationFieldEntity;

@RestController
@RequestMapping("/login")
@Tag(name = "/login", description = "Manage User action around login")
public class LoginRest {

  public static final String             LOGIN_PATH                 = "/portal/login";

  public static final String             ERROR_MESSAGE_PARAM        = "error";

  public static final String             SUCCESS_MESSAGE_PARAM      = "success";

  public static final int                CAPTCHA_WIDTH              = 200;

  public static final int                CAPTCHA_HEIGHT             = 50;

  public static final String             ONBOARDING_EMAIL_SENT_MESSAGE = "onboardingEmailSent";

  public static final String             EMAIL_VERIFICATION_SENT    = "emailVerificationSent";

  public static final String            ADMINISTRATORS_GROUP       = "/platform/administrators";

  public static final String             USERS_GROUP                = "/platform/users";

  public static final String             EXTERNAL_USERS_GROUP       = "/platform/externals";

  public static final String            MEMBER                     = "member";

  public static final String             LOCATION_HEADER            = "Location";

  /** The registration's parameter carrying the additional profile fields */
  public static final String             PROFILE_PROPERTIES_PARAM   = "profileProperties";

  /** The attribute a refused additional field names, beside the error code */
  public static final String             PROPERTY_NAME_PARAM        = "propertyName";

  /**
   * The login page's error code telling a registrant whose account was
   * created that its additional fields were not saved
   */
  public static final String             ADDITIONAL_FIELDS_NOT_SAVED = "additionalFieldsNotSaved";

  public static final UserFieldValidator PASSWORD_VALIDATOR =
      new UserFieldValidator("password", false, false, 8, 255);

  public static final UserFieldValidator EMAIL_VALIDATOR = new UserFieldValidator("email", false, false);

  public static final UserFieldValidator LASTNAME_VALIDATOR                  =
      new UserFieldValidator("lastName", false, true);

  public static final UserFieldValidator FIRSTNAME_VALIDATOR                 =
      new UserFieldValidator("firstName", false, true);

  private static final Log LOG = ExoLogger.getLogger(LoginRest.class);

  @Autowired
  private PasswordRecoveryService passwordRecoveryService;

  @Autowired
  private OrganizationService organizationService;

  @Autowired
  private RegisterUIParamsExtension registerUIParamsExtension;

  @Autowired
  private SecuritySettingService securitySettingService;

  @Autowired
  private RemindPasswordTokenService remindPasswordTokenService;

  @Autowired
  private ProfileFormFieldService profileFormFieldService;

  @Autowired
  private ProfileLabelService profileLabelService;

  @GetMapping(path = "/verify")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Verify user token", method = "GET")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public ResponseEntity<String> verifyToken(HttpServletRequest request,
                                            @Parameter(description = "Token")
                                            @RequestParam("token")
                                            String token,
                                            @Parameter(description = "Token Type")
                                            @RequestParam(name = "tokenType", required = false, defaultValue = "")
                                            String tokenType) {

    String username = passwordRecoveryService.verifyToken(token, tokenType);
    if (username != null) {
      JSONObject body = new JSONObject().put("username", username);
      String email = findEmail(username);
      if (StringUtils.isNotBlank(email)) {
        body.put("email", email);
      }
      return ResponseEntity.ok().body(body.toString());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping(path = "/registrationFields", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "The additional fields of the registration form",
             description = "The profile attributes listed for registration, read from the store, for the holder of a registration token: each attribute's definition only, never a value",
             method = "GET")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "404", description = "The registration token is unknown or expired"),
  })
  public ResponseEntity<List<RegistrationFieldEntity>> getRegistrationFields(HttpServletRequest request,
                                                                             @Parameter(description = "Registration token")
                                                                             @RequestParam("token")
                                                                             String token) {
    if (passwordRecoveryService.verifyToken(token, CookieTokenService.EXTERNAL_REGISTRATION_TOKEN) == null) {
      return ResponseEntity.notFound().cacheControl(CacheControl.noStore()).build();
    }
    Locale locale = request.getLocale();
    List<RegistrationFieldEntity> fields = profileFormFieldService.getSettings(ProfileFormKind.REGISTRATION)
                                                                  .stream()
                                                                  .map(setting -> toRegistrationField(setting, locale))
                                                                  .toList();
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(fields);
  }

  @PostMapping(path = "/finishRegistration")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Finalize external user registration", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public ResponseEntity<String> finishRegistration(HttpServletRequest request,
                                                   @Parameter(description = "Token")
                                                   @RequestParam("token")
                                                   String token,
                                                   @Parameter(description = "Token Type")
                                                   @RequestParam(name = "tokenType", required = false, defaultValue = "")
                                                   String tokenType) {
    String username = passwordRecoveryService.verifyToken(token, tokenType);
    if (username != null) {
      try {
        String email = finishUserCreation(username, request, token, request.getLocale());
        cleanCapcha(request.getSession(),"email-validation");
        return ResponseEntity.status(HttpStatus.FOUND).header(LOCATION_HEADER, LOGIN_PATH).body(new JSONObject()
                                                                                                    .put("username", email).toString());
      } catch (Exception e) {
        LOG.warn("Error while registering external user", e);
        return ResponseEntity.internalServerError().body(new JSONObject()
                                                             .put(ERROR_MESSAGE_PARAM, "external.registration.fail.create.user").toString());
      }
    } else {
      return ResponseEntity.notFound().build();
    }
  }


  @GetMapping(path = "/captcha")
  @Produces("image/png")
  @Operation(summary = "Create a captcha image", method = "GET")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public ResponseEntity<byte[]> serveCaptcha(HttpServletRequest request,
                                             @Parameter(description = "Captcha name")
                                             @RequestParam("name")
                                             String name) {
    HttpSession session = request.getSession();
    Captcha captcha;
    if (session.getAttribute(name) == null) {
      List<Font> textFonts = Arrays.asList(new Font("Arial", Font.BOLD, 40), new Font("Courier", Font.BOLD, 40));
      captcha = new Captcha.Builder(CAPTCHA_WIDTH, CAPTCHA_HEIGHT)
          .addText(new DefaultTextProducer(5),
                   new DefaultWordRenderer(Color.WHITE, textFonts))
          .gimp()
          .addNoise()
          .addBackground()
          .build();

      session.setAttribute(name, captcha);

    } else {
      captcha = (Captcha) session.getAttribute(name);
    }
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(captcha.getImage(), "png", baos);
      byte[] bytes = baos.toByteArray();
      return ResponseEntity.ok()
                           .header("Cache-Control", "private,no-cache,no-store")
                           .contentType(org.springframework.http.MediaType.valueOf("image/png"))
                           .body(bytes);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }

  }

  @PostMapping(path = "/requestResetPassword")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Request a reset password", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public ResponseEntity<String> requestResetPassword(HttpServletRequest request,
                                                     @Parameter(description = "Username")
                                                     @RequestParam("username")
                                                     String username) {


    if (username == null || username.isEmpty()) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, "gatein.forgotPassword.emptyUserOrEmail").toString());
    }

    User user = findUser(username);
    Locale locale = request.getLocale();
    if (user == null || !user.isEnabled() || passwordRecoveryService.sendRecoverPasswordEmail(user, locale, request)) {
      // Send a success message even when user is not found to not inform
      // anonymous users which usernames and emails exists
      return ResponseEntity.ok(new JSONObject()
                                   .put(SUCCESS_MESSAGE_PARAM, "gatein.forgotPassword.emailSendSuccessful")
                                   .toString());
    } else {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, "gatein.forgotPassword.emailSendFailure").toString());
    }
  }

  @PostMapping(path = "/requestRegister")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Request for a new account", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public ResponseEntity<String> requestRegister(HttpServletRequest request,
                                                @Parameter(description = "Email")
                                                @RequestParam("email")
                                                String email,
                                                @Parameter(description = "Captcha")
                                                @RequestParam("captcha")
                                                String captcha,
                                                @RequestParam(value = "initialURI", required = false)
                                                String initialURI) {

    if (request.getRemoteUser() != null) {
      return ResponseEntity.status(HttpStatus.FOUND).header(LOCATION_HEADER, "/portal").build();
    }

    if (!registerUIParamsExtension.isRegisterEnabled()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    if (!isValidCaptch(request.getSession(), captcha,"register")) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, "gatein.forgotPassword.captchaError").toString());
    }
    Locale locale = request.getLocale();
    String errorMessage = validateEmail(email, locale);
    if (errorMessage == null) {
      cleanCapcha(request.getSession(),"register");
      if (!isExistsEmail(email)) {
        if (sendOnboardingEmail(email, initialURI, request)) {
          return ResponseEntity.ok(new JSONObject()
                                      .put(SUCCESS_MESSAGE_PARAM, ONBOARDING_EMAIL_SENT_MESSAGE)
                                      .toString());
        } else {
          return ResponseEntity.badRequest().body(new JSONObject()
                                                      .put(ERROR_MESSAGE_PARAM, "gatein.forgotPassword.emailSendFailure").toString());
        }
      } else {
        //do not expose that the email is already used
        //so return success message without sending email
        return ResponseEntity.ok(new JSONObject()
                                     .put(SUCCESS_MESSAGE_PARAM, ONBOARDING_EMAIL_SENT_MESSAGE)
                                     .toString());
      }
    } else {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, errorMessage).toString());
    }
  }


  @PostMapping(path = "/register")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Register a new account", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public ResponseEntity<String> register(HttpServletRequest request,
                                         @Parameter(description = "Email")
                                         @RequestParam("email")
                                         String email,
                                         @Parameter(description = "Password")
                                         @RequestParam("password")
                                         String password,
                                         @Parameter(description = "Confirm Password")
                                         @RequestParam("password2")
                                         String password2,
                                         @Parameter(description = "Firstname")
                                         @RequestParam("firstName")
                                         String firsname,
                                         @Parameter(description = "Lastname")
                                         @RequestParam("lastName")
                                         String lastname,
                                         @Parameter(description = "Captcha")
                                         @RequestParam("captcha")
                                         String captcha,
                                         @Parameter(description = "token")
                                         @RequestParam("token")
                                         String token,
                                         @Parameter(description = "The additional profile fields: a JSON array of {propertyName, value} or {propertyName, values}")
                                         @RequestParam(name = PROFILE_PROPERTIES_PARAM, required = false)
                                         String profileProperties) {

    if (request.getRemoteUser() != null) {
      return ResponseEntity.status(HttpStatus.FOUND).header(LOCATION_HEADER, "/portal").build();
    }

    String usernameToken = passwordRecoveryService.verifyToken(token, CookieTokenService.EXTERNAL_REGISTRATION_TOKEN);

    if (usernameToken == null) {
      return ResponseEntity.notFound().build();
    }

    if (findUser(usernameToken) != null) {
      // User already exists
      return ResponseEntity.status(HttpStatus.FOUND).header(LOCATION_HEADER, LOGIN_PATH).build();
    }

    if (!isValidCaptch(request.getSession(), captcha,"external-registration")) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, "gatein.forgotPassword.captchaError").toString());
    }
    Locale locale = request.getLocale();
    String errorMessage = validateUsernameAndPassword(usernameToken,
                                                      null,
                                                      email,
                                                      password,
                                                      password2,
                                                      locale);
    String errorMessageName = validateUserFullName(firsname,lastname,locale);
    if (StringUtils.isNotBlank(errorMessage)) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, errorMessage).toString());
    }
    if (StringUtils.isNotBlank(errorMessageName)) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, errorMessageName).toString());
    }

    // The additional fields are checked, Required ones included, before the
    // account exists: a refused value creates nothing
    Map<String, Object> profileValues;
    try {
      profileValues = profileFormFieldService.validate(parseProfileProperties(profileProperties),
                                                       ProfileFormKind.REGISTRATION,
                                                       true,
                                                       null);
    } catch (ProfileFormFieldException e) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, e.getMessage())
                                                  .put(PROPERTY_NAME_PARAM, e.getPropertyName())
                                                  .toString());
    } catch (JSONException e) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, ProfileFormFieldException.INVALID_VALUE)
                                                  .toString());
    }

    String username;
    try {
      HttpSession session = request.getSession();
      username = createUser(email, firsname, lastname, password);
      passwordRecoveryService.sendAccountCreatedConfirmationEmail(username, locale, getUrl(request));
      // The token was created with its type, which its deletion names
      remindPasswordTokenService.deleteToken(token, CookieTokenService.EXTERNAL_REGISTRATION_TOKEN);
      cleanCapcha(session, "external-registration");
    } catch (Exception e) {
      LOG.warn("Error while registering external user", e);
      return ResponseEntity.internalServerError().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, "external.registration.fail.create.user").toString());
    }
    if (!profileValues.isEmpty()) {
      // The account exists: a failed write of its additional fields leaves it
      // as it is, and the registrant is told to fill them from the profile
      try {
        profileFormFieldService.save(username, profileValues, username);
      } catch (Exception e) {
        LOG.warn("Additional profile fields of registered user {} not saved", username, e);
        return ResponseEntity.status(HttpStatus.FOUND)
                             .header(LOCATION_HEADER, LOGIN_PATH + "?" + ERROR_MESSAGE_PARAM + "=" + ADDITIONAL_FIELDS_NOT_SAVED)
                             .build();
      }
    }
    return ResponseEntity.status(HttpStatus.FOUND).header(LOCATION_HEADER, LOGIN_PATH).build();
  }

  @PostMapping(path = "/verifyEmail")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Send email verification", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public ResponseEntity<String> verifyEmail(HttpServletRequest request,
                                         @Parameter(description = "Email")
                                         @RequestParam("email")
                                         String email,
                                         @Parameter(description = "Username")
                                         @RequestParam(name = "username", required = false)
                                         String username,
                                         @Parameter(description = "Password")
                                         @RequestParam("password")
                                         String password,
                                         @Parameter(description = "Confirm Password")
                                         @RequestParam("password2")
                                         String password2,
                                         @Parameter(description = "Firstname")
                                         @RequestParam("firstName")
                                         String firsname,
                                         @Parameter(description = "Lastname")
                                         @RequestParam("lastName")
                                         String lastname,
                                         @Parameter(description = "Captcha")
                                         @RequestParam("captcha")
                                         String captcha,
                                         @Parameter(description = "token")
                                         @RequestParam("token")
                                         String token) {

    if (request.getRemoteUser() != null) {
      return ResponseEntity.status(HttpStatus.FOUND).header(LOCATION_HEADER, "/portal").build();
    }

    String usernameToken = passwordRecoveryService.verifyToken(token, CookieTokenService.EXTERNAL_REGISTRATION_TOKEN);

    if (usernameToken == null) {
      return ResponseEntity.notFound().build();
    }

    if (findUser(usernameToken) != null) {
      // User already exists
      return ResponseEntity.status(HttpStatus.FOUND).header(LOCATION_HEADER, LOGIN_PATH).build();
    }

    if (!isValidCaptch(request.getSession(), captcha,"external-registration")) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, "gatein.forgotPassword.captchaError").toString());
    }
    Locale locale = request.getLocale();
    String errorMessage = validateUsernameAndPassword(usernameToken,
                                                      username,
                                                      email,
                                                      password,
                                                      password2,
                                                      locale);
    String errorMessageName = validateUserFullName(firsname,lastname,locale);
    if (StringUtils.isNotBlank(errorMessage)) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, errorMessage).toString());
    }
    if (StringUtils.isNotBlank(errorMessageName)) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, errorMessageName).toString());
    }

    User user = organizationService.getUserHandler().createUserInstance(username);
    user.setFirstName(firsname);
    user.setLastName(lastname);
    user.setEmail(email);
    user.setPassword(password);
    String data = generateUserDetailCredential(user);
    passwordRecoveryService.sendAccountVerificationEmail(data,
                                                         username,
                                                         firsname,
                                                         lastname,
                                                         email,
                                                         locale,
                                                         getUrl(request));
    return ResponseEntity.ok(new JSONObject().
                                 put(SUCCESS_MESSAGE_PARAM, EMAIL_VERIFICATION_SENT)
                                 .toString());

  }

  @PostMapping(path = "/resetPassword")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Reset password", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public ResponseEntity<String> resetPassword(HttpServletRequest request,
                                              @Parameter(description = "Username")
                                              @RequestParam("username")
                                              String username,
                                              @Parameter(description = "Password")
                                              @RequestParam("password")
                                              String password,
                                              @Parameter(description = "Confirm Password")
                                              @RequestParam("password2")
                                              String password2,
                                              @Parameter(description = "token")
                                              @RequestParam("token")
                                              String token) {

    String usernameToken = passwordRecoveryService.verifyToken(token, CookieTokenService.FORGOT_PASSWORD_TOKEN);

    if (usernameToken == null) {
      return ResponseEntity.notFound().build();
    }
    Locale locale = request.getLocale();
    String errorMessage = validateUserAndPassword(usernameToken, username, password, password2, locale);
    if (errorMessage == null) {
      if (passwordRecoveryService.changePass(token, CookieTokenService.FORGOT_PASSWORD_TOKEN, username, password)) {
        return ResponseEntity.status(HttpStatus.FOUND).header(LOCATION_HEADER, LOGIN_PATH).build();
      } else {
        return ResponseEntity.badRequest().body(new JSONObject()
                                                    .put(ERROR_MESSAGE_PARAM, "gatein.forgotPassword.resetPasswordFailure").toString());
      }
    } else {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, errorMessage).toString());
    }

  }

  @PostMapping(path = "/setPassword")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Set password", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public ResponseEntity<String> setPassword(HttpServletRequest request,
                                            @Parameter(description = "Username")
                                            @RequestParam("username")
                                            String username,
                                            @Parameter(description = "Password")
                                            @RequestParam("password")
                                            String password,
                                            @Parameter(description = "Confirm Password")
                                            @RequestParam("password2")
                                            String password2,
                                            @Parameter(description = "token")
                                            @RequestParam("token")
                                            String token,
                                            @Parameter(description = "captcha")
                                            @RequestParam("captcha")
                                            String captcha) {

    String usernameToken = passwordRecoveryService.verifyToken(token, "onboard");

    if (usernameToken == null) {
      return ResponseEntity.notFound().build();
    }

    if (!isValidCaptch(request.getSession(), captcha,"on-boarding")) {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, "gatein.forgotPassword.captchaError").toString());
    }

    Locale locale = request.getLocale();
    String errorMessage = validateUserAndPassword(usernameToken, username, password, password2, locale);
    if (errorMessage == null) {
      if (passwordRecoveryService.changePass(token, "onboard", username, password)) {
        return ResponseEntity.status(HttpStatus.FOUND).header(LOCATION_HEADER, LOGIN_PATH).build();
      } else {
        return ResponseEntity.badRequest().body(new JSONObject()
                                                    .put(ERROR_MESSAGE_PARAM, "gatein.forgotPassword.resetPasswordFailure").toString());
      }
    } else {
      return ResponseEntity.badRequest().body(new JSONObject()
                                                  .put(ERROR_MESSAGE_PARAM, errorMessage).toString());
    }

  }

  /**
   * The additional fields a registration sends: a JSON array of objects, each
   * naming its attribute and carrying a text value or a list of text values
   *
   * @throws JSONException when the text is not such an array
   */
  private List<ProfilePropertyValue> parseProfileProperties(String profileProperties) {
    if (StringUtils.isBlank(profileProperties)) {
      return Collections.emptyList();
    }
    JSONArray items = new JSONArray(profileProperties);
    List<ProfilePropertyValue> values = new ArrayList<>();
    for (int i = 0; i < items.length(); i++) {
      JSONObject item = items.getJSONObject(i);
      List<String> entries = null;
      if (!item.isNull("values")) {
        JSONArray texts = item.getJSONArray("values");
        entries = new ArrayList<>();
        for (int j = 0; j < texts.length(); j++) {
          entries.add(texts.getString(j));
        }
      }
      values.add(new ProfilePropertyValue(item.getString(PROPERTY_NAME_PARAM),
                                          item.isNull("value") ? null : item.getString("value"),
                                          entries));
    }
    return values;
  }

  private RegistrationFieldEntity toRegistrationField(ProfilePropertySetting setting, Locale locale) {
    return new RegistrationFieldEntity(setting.getId(),
                                       setting.getPropertyName(),
                                       setting.getPropertyType(),
                                       setting.isDropdownList(),
                                       setting.isMultiValued(),
                                       setting.isRequired(),
                                       setting.getOrder(),
                                       profileLabelService.findLabelByObjectTypeAndObjectId(ProfilePropertyService.LABELS_OBJECT_TYPE,
                                                                                            String.valueOf(setting.getId())),
                                       EntityBuilder.toProfilePropertyOptionEntities(setting.getPropertyOptions(), locale));
  }

  /**
   * The email of the account a token names, which the onboarding and the
   * password reset forms show in place of its username; null when the
   * account or its email is not found, and the forms then show the username
   */
  private String findEmail(String username) {
    try {
      User user = organizationService.getUserHandler().findUserByName(username, UserStatus.ANY);
      return user == null ? null : user.getEmail();
    } catch (Exception e) {
      LOG.debug("Email of user {} not found", username, e);
      return null;
    }
  }

  private User findUser(String usernameOrEmail) {
    User user = null;
    try {
      user = organizationService.getUserHandler().findUserByName(usernameOrEmail, UserStatus.ANY);
      if (user == null && usernameOrEmail.contains("@")) {
        Query query = new Query();
        //in passwordRecovery context, we do not allow search by email with wildcard
        usernameOrEmail=usernameOrEmail.replace("*","");
        query.setEmail(usernameOrEmail);
        ListAccess<User> list = organizationService.getUserHandler().findUsersByQuery(query, UserStatus.ANY);
        if (list != null && list.getSize() > 0) {
          user = list.load(0, 1)[0];
        }
      }
    } catch (Exception e) {
      LOG.error("An error occurred while searching for user with username/email: " + usernameOrEmail, e);
    }
    return user;
  }


  private String validateUserAndPassword(String tokenUsername,
                                          String requestedUsername,
                                          String password,
                                          String confirmPass,
                                          Locale locale) {
    if (requestedUsername == null || !requestedUsername.equals(tokenUsername)) {
      return "gatein.forgotPassword.usernameChanged";
    } else if (!StringUtils.equals(password, confirmPass)) {
      return ("gatein.forgotPassword.confirmPasswordNotMatch");
    } else {
      String errorMessage = PASSWORD_VALIDATOR.validate(locale, password);
      if (StringUtils.isNotBlank(errorMessage)) {
        return errorMessage;
      }
    }
    return null;
  }
  private boolean isValidCaptch(HttpSession session, String captchaValue,String name) {
    Captcha captcha = (Captcha) session.getAttribute(name);
    return ((captcha != null) && (captcha.isCorrect(captchaValue)));
  }

  private String validateUsernameAndPassword(String tokenUsernameOrEmail, // NOSONAR
                                             String username,
                                             String email,
                                             String password,
                                             String confirmPass,
                                             Locale locale) {
    boolean isEmailToken = StringUtils.contains(tokenUsernameOrEmail, "@");
    boolean notSameUser =
        StringUtils.isBlank(tokenUsernameOrEmail) || (StringUtils.isBlank(username) && StringUtils.isBlank(email))
            || (isEmailToken && !StringUtils.equals(tokenUsernameOrEmail, email))
            || (!isEmailToken && !StringUtils.equals(tokenUsernameOrEmail, username));
    if (notSameUser) {
      return "gatein.forgotPassword.usernameChanged";
    } else {
      String errorMessage = PASSWORD_VALIDATOR.validate(locale, password);
      if (StringUtils.isNotBlank(errorMessage)) {
        return errorMessage;
      } else if (!isEmailToken) {
        // User added an email in registration form
        // which wasn't provided at first place in token
        // generation
        errorMessage = EMAIL_VALIDATOR.validate(locale, email);
        if (StringUtils.isNotBlank(errorMessage)) {
          return errorMessage;
        }
      } else if (!StringUtils.equals(password, confirmPass)) {
        return "gatein.forgotPassword.confirmPasswordNotMatch";
      }
    }
    return null;
  }

  private String validateUserFullName(String firstName, String lastName, Locale locale) {
    String errorMessage = FIRSTNAME_VALIDATOR.validate(locale, firstName);
    if (StringUtils.isNotBlank(errorMessage)) {
      return errorMessage;
    }

    errorMessage = LASTNAME_VALIDATOR.validate(locale, lastName);
    if (StringUtils.isNotBlank(errorMessage)) {
      return errorMessage;
    }
    return null;
  }

  private String validateEmail(String email,
                               Locale locale) {
    return EMAIL_VALIDATOR.validate(locale, email);
  }

  private boolean isExistsEmail(String email) {
    try {
      // Check if mail address is already used
      Query query = new Query();
      query.setEmail(email);
      ListAccess<User> users = organizationService.getUserHandler().findUsersByQuery(query, UserStatus.ANY);
      if (users != null && users.getSize() > 0) {
        return true;
      } else {
        User user = organizationService.getUserHandler().findUserByName(email);
        if (user != null) {
          return true;
        }
      }
    } catch (Exception e) {
      LOG.error("Error retrieving users list with email {}. Thus, we will consider the email as already used", email, e);
      return true;
    }
    return false;
  }

  private boolean sendOnboardingEmail(String email, String initialUri, HttpServletRequest request) {
    try {
      StringBuilder url = getUrl(request);
      Locale locale = request.getLocale();
      passwordRecoveryService.sendExternalRegisterEmail(null, email, locale, null, url, false, initialUri);
    } catch (Exception e) {
      LOG.error("Unable to send onboarding email",e);
      return false;
    }
    return true;
  }

  private StringBuilder getUrl(HttpServletRequest request) {
    StringBuilder url = new StringBuilder();
    if (request != null) {
      url.append(request.getScheme()).append("://").append(request.getServerName());
      if (request.getServerPort() != 80 && request.getServerPort() != 443) {
        url.append(':').append(request.getServerPort());
      }
      url.append("/").append(PortalContainer.getCurrentPortalContainerName());
    }
    return url;
  }


  private void cleanCapcha(HttpSession session, String name) {
    session.removeAttribute(name);
  }

  private String createUser(String email, String firstName, String lastName, String password) throws Exception {
    User user = organizationService.getUserHandler().createUserInstance(null);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setPassword(password);
    user.setEmail(email);

    return createUser(user);
  }


  private String createUser(User user) throws Exception {
    String login = user.getUserName();
    if (StringUtils.isBlank(login)) {
      login = generateUsername(user.getFirstName(), user.getLastName());
      user.setUserName(login);
    }
    organizationService.getUserHandler().createUser(user, true);
    RequestLifeCycle.restartTransaction();

    if (securitySettingService.isRegistrationExternalUser()) {
      Collection<Membership> memberships = organizationService.getMembershipHandler()
                                                              .findMembershipsByUserAndGroup(login, ADMINISTRATORS_GROUP);
      boolean isAdministrator = CollectionUtils.isNotEmpty(memberships);
      if (!isAdministrator) {
        // Avoid incoherence by indicating an admin user As external
        deleteFromInternalUsersGroup(login);
        RequestLifeCycle.restartTransaction();
        addToExternalUsersGroup(login);
      }
    }
    return login;
  }

  private String generateUsername(String firstname, String lastname) {
    String userNameBase = (firstname.replaceAll("\\s", "") + "."
        + lastname.replaceAll("\\s", ""))
        .toLowerCase();
    userNameBase = unAccent(userNameBase);
    String username = userNameBase;
    Random rand = new Random();// NOSONAR
    // Check if username already existed (with identity manager, need to
    // move the handler to social)
    while (findUser(username) != null) {
      int num = rand.nextInt(89) + 10;// range between 10 and 99.
      username = userNameBase + num;
    }
    return username;
  }

  private void deleteFromInternalUsersGroup(String username) throws Exception {
    Collection<Membership> usersMemberhips = organizationService.getMembershipHandler()
                                                                .findMembershipsByUserAndGroup(username, USERS_GROUP);
    if (CollectionUtils.isNotEmpty(usersMemberhips)) {
      for (Membership usersMemberhip : usersMemberhips) {
        organizationService.getMembershipHandler().removeMembership(usersMemberhip.getId(), true);
      }
    }
  }

  private void addToExternalUsersGroup(String username) throws Exception {
    Collection<Membership> externalsUsersMemberhips = organizationService.getMembershipHandler()
                                                                         .findMembershipsByUserAndGroup(username,
                                                                                                        EXTERNAL_USERS_GROUP);
    if (CollectionUtils.isNotEmpty(externalsUsersMemberhips)) {
      for (Membership usersMemberhip : externalsUsersMemberhips) {
        organizationService.getMembershipHandler().removeMembership(usersMemberhip.getId(), true);
      }
    }

    organizationService.getMembershipHandler()
                       .linkMembership(organizationService.getUserHandler().findUserByName(username),
                                       organizationService.getGroupHandler().findGroupById(EXTERNAL_USERS_GROUP),
                                       organizationService.getMembershipTypeHandler().findMembershipType(MEMBER),
                                       true);
  }

  private String unAccent(String src) {
    return Normalizer.normalize(src, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replace("'", "").replace("`", "");
  }

  private String generateUserDetailCredential(User user) {
    return String.format("%s::%s::%s::%s::%s",
                         user.getUserName(),
                         user.getFirstName(),
                         user.getLastName(),
                         user.getEmail(),
                         user.getPassword());
  }

  private String finishUserCreation(String username, HttpServletRequest request, String token, Locale locale) throws Exception {
    User user = generateUserFromCredential(username);
    username = createUser(user);
    passwordRecoveryService.sendAccountCreatedConfirmationEmail(username, locale, getUrl(request));
    remindPasswordTokenService.deleteToken(token);
    return user.getEmail();
  }

  private User generateUserFromCredential(String data) {
    String[] dataParts = StringUtils.split(data, "::");
    User user = organizationService.getUserHandler().createUserInstance(dataParts[0]);
    user.setFirstName(dataParts[1]);
    user.setLastName(dataParts[2]);
    user.setEmail(dataParts[3]);

    // password could contains '::'
    // to extract it, we must not simply get last part, as we could be a not
    // complete password.
    String firstPart = dataParts[0] + "::" + dataParts[1] + "::" + dataParts[2] + "::" + dataParts[3] + "::";
    String password = data.substring(firstPart.length());
    user.setPassword(password);
    return user;
  }
}
