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
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;
import jakarta.servlet.Filter;
import lombok.SneakyThrows;
import nl.captcha.Captcha;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.idm.UserDAOImpl;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.model.ProfileLabel;
import org.exoplatform.social.core.profilelabel.ProfileLabelService;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.web.login.recovery.PasswordRecoveryService;
import org.exoplatform.web.register.RegisterUIParamsExtension;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.security.security.RemindPasswordTokenService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import io.meeds.social.core.profileproperty.constant.ProfileFormKind;
import io.meeds.social.core.profileproperty.exception.ProfileFormFieldException;
import io.meeds.social.core.profileproperty.model.ProfilePropertyValue;
import io.meeds.social.core.profileproperty.service.ProfileFormFieldService;
import io.meeds.social.translation.service.TranslationService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = { LoginRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class LoginRestTest {

  private MockMvc mockMvc;

  @Autowired
  private SecurityFilterChain filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private PasswordRecoveryService passwordRecoveryService;

  @MockitoBean
  private OrganizationService organizationService;

  @MockitoBean
  private UserDAOImpl userHandler;

  @MockitoBean
  private IdentityStorage identityStorage;

  @MockitoBean
  private RegisterUIParamsExtension registerUIParamsExtension;

  @MockitoBean
  private SecuritySettingService securitySettingService;

  @MockitoBean
  private RemindPasswordTokenService remindPasswordTokenService;

  @MockitoBean
  private Captcha captcha;

  @MockitoBean
  private ProfileFormFieldService profileFormFieldService;

  @MockitoBean
  private ProfileLabelService profileLabelService;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();


  }

  @Test
  @SneakyThrows
  public void testVerifyWorkingToken() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(get("/login/verify?token=123456789"));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("username", "john").toString()));
  }

  /**
   * The verified token answers the account's email with its username, so the
   * onboarding and password reset forms show the email in place of the username
   */
  @Test
  @SneakyThrows
  public void testVerifyWorkingTokenAnswersTheEmail() {
    UserImpl user = new UserImpl("john");
    user.setEmail("john@acme.com");
    when(userHandler.findUserByName("john", UserStatus.ANY)).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken("123456789", "onboard")).thenReturn("john");
    ResultActions response = mockMvc.perform(get("/login/verify?token=123456789&tokenType=onboard"));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("john"))
            .andExpect(jsonPath("$.email").value("john@acme.com"));
  }

  /**
   * An account without an email, or one the lookup fails on, answers the
   * username alone
   */
  @Test
  @SneakyThrows
  public void testVerifyWorkingTokenWithoutEmail() {
    when(userHandler.findUserByName("john", UserStatus.ANY)).thenReturn(new UserImpl("john"));
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken("123456789", "onboard")).thenReturn("john");
    mockMvc.perform(get("/login/verify?token=123456789&tokenType=onboard"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.username").value("john"))
           .andExpect(jsonPath("$.email").doesNotExist());

    when(userHandler.findUserByName("john", UserStatus.ANY)).thenThrow(new IllegalStateException("store down"));
    mockMvc.perform(get("/login/verify?token=123456789&tokenType=onboard"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.username").value("john"))
           .andExpect(jsonPath("$.email").doesNotExist());
  }

  @Test
  @SneakyThrows
  public void testVerifyNotWorkingToken() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);
    ResultActions response = mockMvc.perform(get("/login/verifyToken?token=123456789"));
    response.andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  public void testRequestResetPasswordSuccess() {

    UserImpl user = new UserImpl("john");
    user.setUserName("john");
    user.setEnabled(true);
    when(userHandler.findUserByName(any(),any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.sendRecoverPasswordEmail(any(),any(),any())).thenReturn(true);

    ResultActions response = mockMvc.perform(post("/login/requestResetPassword")
                                                 .content("username=john")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "gatein.forgotPassword.emailSendSuccessful").toString()));

  }

  @Test
  @SneakyThrows
  public void testRequestResetPasswordEmptyUsername() {
    ResultActions response = mockMvc.perform(post("/login/requestResetPassword")
                                                 .content("username=")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject()
                                            .put("error", "gatein.forgotPassword.emptyUserOrEmail").toString()));

  }

  @Test
  @SneakyThrows
  public void testRequestResetPasswordProblemSendingEmail() {

    UserImpl user = new UserImpl("john");
    user.setUserName("john");
    user.setEnabled(true);
    when(userHandler.findUserByName(any(),any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.sendRecoverPasswordEmail(any(),any(),any())).thenReturn(false);

    ResultActions response = mockMvc.perform(post("/login/requestResetPassword")
                                                 .content("username=john")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.emailSendFailure").toString()));

  }

  @Test
  @SneakyThrows
  public void testResetPasswordTokenNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=test&password2=test&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  public void testResetPasswordUsernameDifferent() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("mary");
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=test&password2=test&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
        .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.usernameChanged").toString()));
  }

  @Test
  @SneakyThrows
  public void testResetPasswordPasswordDifferent() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=test1&password2=test2&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.confirmPasswordNotMatch").toString()));
  }

  @Test
  @SneakyThrows
  public void testResetPasswordNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=test&password2=test&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  public void testResetPasswordErrorOnChange() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.changePass(any(),any(),any(),any())).thenReturn(false);
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=Test1234@&password2=Test1234@&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.resetPasswordFailure").toString()));
  }

  @Test
  @SneakyThrows
  public void testResetPasswordSuccess() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.changePass(any(),any(),any(),any())).thenReturn(true);
    ResultActions response = mockMvc.perform(post("/login/resetPassword")
                                                 .content("username=john&password=Test1234@&password2=Test1234@&token=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal/login"));
  }

  @Test
  @SneakyThrows
  public void testSetPasswordTokenNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .content("username=john&password=test&password2=test&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  public void testSetPasswordCaptchNotValid() {
    when(captcha.isCorrect(any())).thenReturn(false);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=test&password2=test&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.captchaError").toString()));  }

  @Test
  @SneakyThrows
  public void testSetPasswordUsernameDifferent() {
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);

    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("mary");
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=test&password2=test&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.usernameChanged").toString()));
  }

  @Test
  @SneakyThrows
  public void testSetPasswordPasswordDifferent() {
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=test1&password2=test2&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.confirmPasswordNotMatch").toString()));
  }

  @Test
  @SneakyThrows
  public void testSetPasswordNotValid() {
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=test&password2=test&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  public void testSetPasswordErrorOnChange() {
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.changePass(any(),any(),any(),any())).thenReturn(false);
    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=Test1234@&password2=Test1234@&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.resetPasswordFailure").toString()));
  }

  @Test
  @SneakyThrows
  public void testSetPasswordSuccess() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.changePass(any(),any(),any(),any())).thenReturn(true);

    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);

    UserImpl user = new UserImpl("john");
    user.setUserName("john");
    user.setEnabled(true);
    user.setEmail("jsmith@acme.com");
    when(userHandler.findUserByName(any(),any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);

    ResultActions response = mockMvc.perform(post("/login/setPassword")
                                                 .sessionAttrs(sessionattr)
                                                 .content("username=john&password=Test1234@&password2=Test1234@&token=123456&captcha=abcde")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal/login"));
  }


  @Test
  @SneakyThrows
  public void testServeCaptcha() {
    ResultActions response = mockMvc.perform(get("/login/captcha?name=register"));
    response.andExpect(status().isOk())
            .andExpect(header().string("Content-Type","image/png"));
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterAlreadyConnected() {
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON)
                                                 .with(testSimpleUser()));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal"));
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterWithRegisterDisabled() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(false);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterWithNotValidCaptcha() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(false);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.captchaError").toString()));
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterWithNotValidEmail() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterWithExistingEmail() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(true);

    ListAccess<User> users = new ListAccess<>() {
      @Override
      public User[] load(int i, int i1) throws Exception, IllegalArgumentException {
        return null;
      }

      @Override
      public int getSize() throws Exception {
        return 1;
      }
    };

    when(userHandler.findUsersByQuery(any(),any())).thenReturn(users);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "onboardingEmailSent").toString()));

  }

  @Test
  @SneakyThrows
  public void testRequestRegisterSuccessSendOnboardingEmail() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(passwordRecoveryService.sendExternalRegisterEmail(eq(null),any(),any(),eq(null),any(),anyBoolean())).thenReturn("ok");
    when(captcha.isCorrect(any())).thenReturn(true);

    ListAccess<User> users = new ListAccess<>() {
      @Override
      public User[] load(int i, int i1) throws Exception, IllegalArgumentException {
        return null;
      }

      @Override
      public int getSize() throws Exception {
        return 0;
      }
    };

    when(userHandler.findUsersByQuery(any(),any())).thenReturn(users);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "onboardingEmailSent").toString()));

  }

  @Test
  @SneakyThrows
  public void testRequestRegisterFailedSendOnboardingEmail() {
    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
   	when(passwordRecoveryService.sendExternalRegisterEmail(eq(null), any(), any(), eq(null), any(), anyBoolean(), any()))
			.thenThrow(new Exception("Error"));
    when(captcha.isCorrect(any())).thenReturn(true);

    ListAccess<User> users = new ListAccess<>() {
      @Override
      public User[] load(int i, int i1) throws Exception, IllegalArgumentException {
        return null;
      }

      @Override
      public int getSize() throws Exception {
        return 0;
      }
    };

    when(userHandler.findUsersByQuery(any(),any())).thenReturn(users);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);
    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john@acme.com&captcha=123456")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.emailSendFailure").toString()));

  }

  @Test
  @SneakyThrows
  public void testFinishRegistrationTokenNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);
    ResultActions response = mockMvc.perform(post("/login/finishRegistration")
                                                 .content("token=123456798&tokenType=email-validation")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  public void testFinishRegistrationTokenSuccess() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john::John::Smith::jsmith@acme.com::Password1234");

    when(securitySettingService.isRegistrationExternalUser()).thenReturn(false);

    User user = new UserImpl();
    user.setUserName("john");
    when(userHandler.createUserInstance(any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    ResultActions response = mockMvc.perform(post("/login/finishRegistration")
                                                 .content("token=123456798&tokenType=email-validation")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal/login"));
    // the link is used once: its token is deleted with the type it was created with
    verify(passwordRecoveryService).deleteToken("123456798", "email-validation");
  }

  @Test
  @SneakyThrows
  public void testRegisterAlreadyConnected() {
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON)
                                                 .with(testSimpleUser()));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal"));
  }

  @Test
  @SneakyThrows
  public void testRegisterTokenNotValid() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);

    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());

  }

  @Test
  @SneakyThrows
  public void testRegisterExistingUser() {
    User user = new UserImpl();
    user.setUserName("john");

    when(userHandler.findUserByName(any(), any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON)
                                                 .with(testSimpleUser()));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal"));
  }

  @Test
  @SneakyThrows
  public void testRegisterExistingNotValidCaptcha() {
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("jsmith@acme.com");


    when(captcha.isCorrect(any())).thenReturn(false);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(content().string(new JSONObject().put("error", "gatein.forgotPassword.captchaError").toString()));

  }

  @Test
  @SneakyThrows
  public void testRegisterNotValidPassword() {
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("jsmith@acme.com");


    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=&password=pass&password2=pass&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());

  }

  @Test
  @SneakyThrows
  public void testRegisterNotFirstName() {
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("jsmith@acme.com");


    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=&password=Password1234&password2=Password1234&firstName=123John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());

  }

  @Test
  @SneakyThrows
  public void testRegisterSuccessAccountVerificationEmail() {
    User john = new UserImpl();
    john.setUserName("john");
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(userHandler.createUserInstance(any())).thenReturn(john);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.sendAccountVerificationEmail(any(),any(),any(),any(),any(),any(),any())).thenReturn(true);

    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/verifyEmail")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=john&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "emailVerificationSent").toString()));
    // the registration goes on through the verification email's link
    verify(remindPasswordTokenService).deleteToken("123456789", "external-registration");
  }

  @Test
  @SneakyThrows
  public void testRegisterAccountVerificationEmailNotSentKeepsTheLink() {
    User john = new UserImpl();
    john.setUserName("john");
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(userHandler.createUserInstance(any())).thenReturn(john);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("john");
    when(passwordRecoveryService.sendAccountVerificationEmail(any(),any(),any(),any(),any(),any(),any())).thenReturn(false);

    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/verifyEmail")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&username=john&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "emailVerificationSent").toString()));
    verify(remindPasswordTokenService, never()).deleteToken(anyString(), anyString());
  }

  @Test
  @SneakyThrows
  public void testRegisterUsedLinkIsRefusedWithoutBody() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);

    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .content("email=jsmith@acme.com&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    // the registration form shows the expired link message on this status, since the answer has no body
    response.andExpect(status().isNotFound())
            .andExpect(content().string(""));
  }

  @Test
  @SneakyThrows
  public void testVerifyEmailUsedLinkIsRefusedWithoutBody() {
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn(null);

    ResultActions response = mockMvc.perform(post("/login/verifyEmail")
                                                 .content("email=jsmith@acme.com&username=john&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    // the registration form shows the expired link message on this status, since the answer has no body
    response.andExpect(status().isNotFound())
            .andExpect(content().string(""));
  }

  @Test
  @SneakyThrows
  public void testVerifyEmailRefusesNonConformingUsername() {
    for (String username : new String[] { "First.Second", "first second", "first#second", "first/second", "first:second" }) {
      clearInvocations(userHandler, passwordRecoveryService);
      assertVerifyEmailRefusesUsername(username);
    }
  }

  @Test
  @SneakyThrows
  public void testVerifyEmailAcceptsValidUsername() {
    ResultActions response = performVerifyEmail("first.second");
    response.andExpect(status().isOk())
            .andExpect(content().string(new JSONObject().put("success", "emailVerificationSent").toString()));
    verify(passwordRecoveryService).sendAccountVerificationEmail(any(), eq("first.second"), any(), any(), any(), any(), any());
  }

  @Test
  @SneakyThrows
  public void testVerifyEmailRefusesAbsentUsername() {
    assertVerifyEmailRefusesUsername(null);
  }

  @Test
  @SneakyThrows
  public void testVerifyEmailRefusesEmptyUsername() {
    assertVerifyEmailRefusesUsername("");
  }

  @SneakyThrows
  private void assertVerifyEmailRefusesUsername(String username) {
    ResultActions response = performVerifyEmail(username);
    response.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").isNotEmpty());
    verify(userHandler, never()).createUserInstance(any());
    verify(passwordRecoveryService, never()).sendAccountVerificationEmail(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  @SneakyThrows
  public void testVerifyEmailRefusesTakenUsername() {
    User existing = new UserImpl();
    existing.setUserName("first.second");
    ResultActions response = performVerifyEmail("first.second", existing);
    response.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(LoginRest.USERNAME_TAKEN_MESSAGE));
    verify(passwordRecoveryService, never()).sendAccountVerificationEmail(any(), any(), any(), any(), any(), any(), any());
    verify(remindPasswordTokenService, never()).deleteToken(anyString(), anyString());
  }

  @Test
  @SneakyThrows
  public void testVerifyEmailRefusesUsernameOfDeletedAccount() {
    Identity deleted = new Identity(OrganizationIdentityProvider.NAME, "first.second");
    deleted.setDeleted(true);
    when(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, "first.second")).thenReturn(deleted);
    ResultActions response = performVerifyEmail("first.second");
    response.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(LoginRest.USERNAME_TAKEN_MESSAGE));
    verify(passwordRecoveryService, never()).sendAccountVerificationEmail(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  @SneakyThrows
  public void testFinishRegistrationRefusesUsernameOfDeletedAccount() {
    Identity deleted = new Identity(OrganizationIdentityProvider.NAME, "john");
    deleted.setDeleted(true);
    when(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, "john")).thenReturn(deleted);
    when(passwordRecoveryService.verifyToken(any(), any())).thenReturn("john::John::Smith::jsmith@acme.com::Password1234");
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    ResultActions response = mockMvc.perform(post("/login/finishRegistration")
                                                 .content("token=123456798&tokenType=email-validation")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(LoginRest.USERNAME_TAKEN_MESSAGE));
    verify(userHandler, never()).createUser(any(), anyBoolean());
  }

  @Test
  @SneakyThrows
  public void testFinishRegistrationRefusesUsernameTakenInBetween() {
    User existing = new UserImpl();
    existing.setUserName("john");
    when(userHandler.findUserByName(eq("john"), any())).thenReturn(existing);
    when(passwordRecoveryService.verifyToken(any(), any())).thenReturn("john::John::Smith::jsmith@acme.com::Password1234");
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    ResultActions response = mockMvc.perform(post("/login/finishRegistration")
                                                 .content("token=123456798&tokenType=email-validation")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(LoginRest.USERNAME_TAKEN_MESSAGE));
    verify(userHandler, never()).createUser(any(), anyBoolean());
    verify(remindPasswordTokenService, never()).deleteToken(anyString(), anyString());
  }

  private ResultActions performVerifyEmail(String username) {
    return performVerifyEmail(username, null);
  }

  @SneakyThrows
  private ResultActions performVerifyEmail(String username, User takenUser) {
    User user = new UserImpl();
    user.setUserName(username);
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    if (takenUser != null) {
      when(userHandler.findUserByName(eq(takenUser.getUserName()), any())).thenReturn(takenUser);
    }
    when(userHandler.createUserInstance(any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(), any())).thenReturn("jsmith@acme.com");
    when(passwordRecoveryService.sendAccountVerificationEmail(any(), any(), any(), any(), any(), any(), any())).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("external-registration", captcha);
    String usernameParam = username == null ? "" : "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&";
    return mockMvc.perform(post("/login/verifyEmail")
                               .sessionAttrs(sessionattr)
                               .content("email=jsmith@acme.com&" + usernameParam
                                        + "password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                               .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                               .accept(MediaType.APPLICATION_JSON));
  }

  @Test
  @SneakyThrows
  public void testRegisterSuccess() {
    User john = new UserImpl();
    john.setUserName("john");
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(userHandler.createUserInstance(any())).thenReturn(john);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken(any(),any())).thenReturn("jsmith@acme.com");
    when(passwordRecoveryService.sendAccountCreatedConfirmationEmail(any(),any(),any())).thenReturn(true);

    when(captcha.isCorrect(any())).thenReturn(true);
    HashMap<String, Object> sessionattr = new HashMap<>();

    sessionattr.put("external-registration", captcha);
    ResultActions response = mockMvc.perform(post("/login/register")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=jsmith@acme.com&password=Password1234&password2=Password1234&firstName=John&lastName=Smith&captcha=abcde&token=123456789")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isFound())
            .andExpect(header().string("Location","/portal/login"));
    // the link is used once: its token is deleted with the type it was created with
    verify(remindPasswordTokenService).deleteToken("123456789", "external-registration");
  }

  @Test
  @SneakyThrows
  public void testRequestRegisterSuccessSendOnboardingEmailWithInitialUri() {

    when(registerUIParamsExtension.isRegisterEnabled()).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(true);

    // No existing users
    ListAccess<User> users = new ListAccess<>() {
      @Override
      public User[] load(int i, int i1) {
        return null;
      }

      @Override
      public int getSize() {
        return 0;
      }
    };

    when(userHandler.findUsersByQuery(any(), any())).thenReturn(users);
    when(organizationService.getUserHandler()).thenReturn(userHandler);

    when(passwordRecoveryService.sendExternalRegisterEmail(
        eq(null),
        any(),
        any(),
        eq(null),
        any(),
        anyBoolean(),
        eq("/spaces/dev")
    )).thenReturn("ok");

    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("register", captcha);

    ResultActions response = mockMvc.perform(post("/login/requestRegister")
                                                 .sessionAttrs(sessionattr)
                                                 .content("email=john@acme.com&captcha=123456&initialUri=/spaces/dev")
                                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                 .accept(MediaType.APPLICATION_JSON));

    response.andExpect(status().isOk())
            .andExpect(content().string(
                new JSONObject().put("success", "onboardingEmailSent").toString()
            ));
  }

  /**
   * The registration form's definitions are read with a registration token
   * only: an unknown one reads nothing, and its answer is kept by no cache
   */
  @Test
  @SneakyThrows
  public void testGetRegistrationFieldsWithUnknownToken() {
    when(passwordRecoveryService.verifyToken("unknown", CookieTokenService.EXTERNAL_REGISTRATION_TOKEN)).thenReturn(null);
    when(passwordRecoveryService.verifyToken("unknown", "onboard")).thenReturn("john");

    mockMvc.perform(get("/login/registrationFields?token=unknown"))
           .andExpect(status().isNotFound())
           .andExpect(header().string("Cache-Control", "no-store"));
    verify(profileFormFieldService, never()).getSettings(any());
  }

  /**
   * The definitions are read with a valid registration token, whether or not
   * an account already exists for its e-mail: the registration itself sends
   * such a registrant to the login page
   */
  @Test
  @SneakyThrows
  public void testGetRegistrationFieldsWhenTheTokenAccountExists() {
    when(passwordRecoveryService.verifyToken("123456789", CookieTokenService.EXTERNAL_REGISTRATION_TOKEN)).thenReturn("jsmith@acme.com");
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    UserImpl registered = new UserImpl("john.smith");
    registered.setEmail("jsmith@acme.com");
    @SuppressWarnings("unchecked")
    ListAccess<User> byEmail = mock(ListAccess.class);
    when(byEmail.getSize()).thenReturn(1);
    when(byEmail.load(0, 1)).thenReturn(new User[] { registered });
    when(userHandler.findUsersByQuery(any(), any())).thenReturn(byEmail);
    when(userHandler.findUserByName("jsmith@acme.com", UserStatus.ANY)).thenReturn(registered);
    when(profileFormFieldService.getSettings(ProfileFormKind.REGISTRATION)).thenReturn(List.of());

    mockMvc.perform(get("/login/registrationFields?token=123456789"))
           .andExpect(status().isOk())
           .andExpect(header().string("Cache-Control", "no-store"))
           .andExpect(jsonPath("$.length()").value(0));
    verify(profileFormFieldService).getSettings(ProfileFormKind.REGISTRATION);
  }

  /**
   * An anonymous registrant holding the token reads the registration
   * allow-list, each attribute with the fields the form needs and none of its
   * other settings, its labels in every language and its options in the
   * request's language, and the answer is kept by no cache
   */
  @Test
  @SneakyThrows
  public void testGetRegistrationFields() {
    when(passwordRecoveryService.verifyToken("123456789", CookieTokenService.EXTERNAL_REGISTRATION_TOKEN)).thenReturn("jsmith@acme.com");
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setId(5L);
    setting.setPropertyName("team");
    setting.setPropertyType("text");
    setting.setDropdownList(true);
    setting.setRequired(true);
    setting.setOrder(3L);
    setting.setActive(true);
    setting.setEditable(true);
    setting.setVisible(true);
    setting.setGroupSynchronized(true);
    setting.setListedOnRegistration(true);
    setting.setPropertyOptions(List.of(new ProfilePropertyOption(7L, "Sales", 5L)));
    when(profileFormFieldService.getSettings(ProfileFormKind.REGISTRATION)).thenReturn(List.of(setting));
    when(profileLabelService.findLabelByObjectTypeAndObjectId(ProfilePropertyService.LABELS_OBJECT_TYPE,
                                                              "5")).thenReturn(List.of(new ProfileLabel(1L,
                                                                                                        ProfilePropertyService.LABELS_OBJECT_TYPE,
                                                                                                        "5",
                                                                                                        "Équipe",
                                                                                                        "fr")));
    TranslationService translationService = mock(TranslationService.class);
    when(translationService.getTranslationLabelOrDefault(anyString(), anyLong(), anyString(), eq(Locale.FRENCH))).thenReturn("Ventes");
    Object cachedTranslationService = ReflectionTestUtils.getField(EntityBuilder.class, "translationService");
    ReflectionTestUtils.setField(EntityBuilder.class, "translationService", translationService);
    try {
      String content = mockMvc.perform(get("/login/registrationFields?token=123456789").locale(Locale.FRENCH))
                              .andExpect(status().isOk())
                              .andExpect(header().string("Cache-Control", "no-store"))
                              .andExpect(jsonPath("$.length()").value(1))
                              .andExpect(jsonPath("$[0].id").value(5))
                              .andExpect(jsonPath("$[0].propertyName").value("team"))
                              .andExpect(jsonPath("$[0].propertyType").value("text"))
                              .andExpect(jsonPath("$[0].dropdownList").value(true))
                              .andExpect(jsonPath("$[0].multiValued").value(false))
                              .andExpect(jsonPath("$[0].required").value(true))
                              .andExpect(jsonPath("$[0].order").value(3))
                              .andExpect(jsonPath("$[0].labels[0].label").value("Équipe"))
                              .andExpect(jsonPath("$[0].labels[0].language").value("fr"))
                              .andExpect(jsonPath("$[0].propertyOptions[0].id").value(7))
                              .andExpect(jsonPath("$[0].propertyOptions[0].translatedValue").value("Ventes"))
                              .andReturn()
                              .getResponse()
                              .getContentAsString();
      assertEquals(Set.of("id",
                          "propertyName",
                          "propertyType",
                          "dropdownList",
                          "multiValued",
                          "required",
                          "order",
                          "labels",
                          "propertyOptions"),
                   new JSONArray(content).getJSONObject(0).keySet());
    } finally {
      ReflectionTestUtils.setField(EntityBuilder.class, "translationService", cachedTranslationService);
    }
  }

  /**
   * Each refusal of an additional field refuses the registration before the
   * account exists, with its code and the attribute it names
   */
  @Test
  @SneakyThrows
  public void testRegisterRefusesAnAdditionalFieldBeforeCreatingTheAccount() {
    prepareRegistration();
    for (String code : List.of(ProfileFormFieldException.NOT_ALLOWED,
                               ProfileFormFieldException.INVALID_OPTION,
                               ProfileFormFieldException.REQUIRED,
                               ProfileFormFieldException.INVALID_EMAIL,
                               ProfileFormFieldException.TOO_LONG,
                               ProfileFormFieldException.INVALID_VALUE)) {
      when(profileFormFieldService.validate(any(), eq(ProfileFormKind.REGISTRATION), eq(true), isNull()))
                                                                                                        .thenThrow(new ProfileFormFieldException(code,
                                                                                                                                                 "team"));

      performRegistration("[{\"propertyName\":\"team\",\"value\":\"x\"}]")
                                                                          .andExpect(status().isBadRequest())
                                                                          .andExpect(jsonPath("$.error").value(code))
                                                                          .andExpect(jsonPath("$.propertyName").value("team"));
    }
    verify(userHandler, never()).createUser(any(), anyBoolean());
    verify(profileFormFieldService, never()).save(any(), any(), any());
    verify(passwordRecoveryService, never()).sendAccountCreatedConfirmationEmail(any(), any(), any());
  }

  /**
   * Additional fields that are not a JSON array of attribute values refuse the
   * registration before the account exists
   */
  @Test
  @SneakyThrows
  public void testRegisterRefusesMalformedAdditionalFields() {
    prepareRegistration();

    for (String profileProperties : List.of("not json",
                                            "{\"propertyName\":\"team\",\"value\":\"A\"}",
                                            "[\"team\"]",
                                            "[{\"value\":\"A\"}]",
                                            "[{\"propertyName\":\"team\",\"value\":{\"id\":1}}]",
                                            "[{\"propertyName\":\"team\",\"values\":\"A\"}]")) {
      performRegistration(profileProperties).andExpect(status().isBadRequest())
                                            .andExpect(jsonPath("$.error").value("profile.formField.invalidValue"));
    }
    verify(profileFormFieldService, never()).validate(any(), any(), anyBoolean(), any());
    verify(userHandler, never()).createUser(any(), anyBoolean());
  }

  /**
   * The additional fields are checked as a complete registration form before
   * the account is created, then written in the created account's profile,
   * by its own login
   */
  @Test
  @SneakyThrows
  public void testRegisterSavesAdditionalFields() {
    prepareRegistration();
    Map<String, Object> profileValues = Map.of("team", "7");
    when(profileFormFieldService.validate(any(), eq(ProfileFormKind.REGISTRATION), eq(true), isNull())).thenReturn(profileValues);

    performRegistration("[{\"propertyName\":\"team\",\"value\":\"7\"},{\"propertyName\":\"skills\",\"values\":[\"Java\",null,\"Vue\"]}]")
                                                                                                                                  .andExpect(status().isFound())
                                                                                                                                  .andExpect(header().string("Location",
                                                                                                                                                             "/portal/login"));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ProfilePropertyValue>> sentValues = ArgumentCaptor.forClass(List.class);
    InOrder inOrder = inOrder(profileFormFieldService, userHandler);
    inOrder.verify(profileFormFieldService).validate(sentValues.capture(), eq(ProfileFormKind.REGISTRATION), eq(true), isNull());
    inOrder.verify(userHandler).createUser(any(), eq(true));
    inOrder.verify(profileFormFieldService).save("john", profileValues, "john");
    // Written once, after the account exists: never before it
    verify(profileFormFieldService).save(any(), any(), any());
    // The registration token is used: deleted with the type it was created
    // with, so that it no longer reads the registration form's fields
    verify(remindPasswordTokenService).deleteToken("123456789", CookieTokenService.EXTERNAL_REGISTRATION_TOKEN);
    verify(remindPasswordTokenService, never()).deleteToken("123456789");
    assertEquals(List.of(new ProfilePropertyValue("team", "7", null),
                         // A null entry reaches the validation, which drops it
                         new ProfilePropertyValue("skills", null, Arrays.asList("Java", null, "Vue"))),
                 sentValues.getValue());
  }

  /**
   * A registration listing no additional field, or a client sending none,
   * still has the Required attributes checked, and writes nothing more
   */
  @Test
  @SneakyThrows
  public void testRegisterWithoutAdditionalFields() {
    prepareRegistration();

    performRegistration(null).andExpect(status().isFound())
                             .andExpect(header().string("Location", "/portal/login"));

    verify(profileFormFieldService).validate(eq(List.of()), eq(ProfileFormKind.REGISTRATION), eq(true), isNull());
    verify(userHandler).createUser(any(), eq(true));
    verify(profileFormFieldService, never()).save(any(), any(), any());
  }

  /**
   * A failed write of the additional fields keeps the created account and
   * completes the registration, telling the registrant on the login page
   */
  @Test
  @SneakyThrows
  public void testRegisterCompletesWhenAdditionalFieldsAreNotSaved() {
    prepareRegistration();
    Map<String, Object> profileValues = Map.of("team", "7");
    when(profileFormFieldService.validate(any(), eq(ProfileFormKind.REGISTRATION), eq(true), isNull())).thenReturn(profileValues);
    doThrow(new IllegalStateException("store down")).when(profileFormFieldService).save("john", profileValues, "john");

    performRegistration("[{\"propertyName\":\"team\",\"value\":\"7\"}]").andExpect(status().isFound())
                                                                       .andExpect(header().string("Location",
                                                                                                  "/portal/login?error=additionalFieldsNotSaved"));
    verify(passwordRecoveryService).sendAccountCreatedConfirmationEmail(eq("john"), any(), any());
    verify(remindPasswordTokenService).deleteToken("123456789", CookieTokenService.EXTERNAL_REGISTRATION_TOKEN);
    verify(userHandler, never()).removeUser(any(), anyBoolean());
  }

  /**
   * The additional fields are checked after the registration's own checks: a
   * caller without a valid token, the session's captcha or valid names learns
   * nothing of the registration form's attributes from their refusals
   */
  @Test
  @SneakyThrows
  public void testRegisterChecksAdditionalFieldsAfterItsOwnChecks() {
    prepareRegistration();
    when(profileFormFieldService.validate(any(),
                                          any(),
                                          anyBoolean(),
                                          any())).thenThrow(new ProfileFormFieldException(ProfileFormFieldException.NOT_ALLOWED,
                                                                                          "external"));
    String refused = "[{\"propertyName\":\"external\",\"value\":\"true\"}]";

    performRegistration(refused, "John", "unknown").andExpect(status().isNotFound());

    when(captcha.isCorrect(any())).thenReturn(false);
    performRegistration(refused, "John", "123456789").andExpect(status().isBadRequest())
                                                     .andExpect(jsonPath("$.error").value("gatein.forgotPassword.captchaError"))
                                                     .andExpect(jsonPath("$.propertyName").doesNotExist());

    when(captcha.isCorrect(any())).thenReturn(true);
    performRegistration(refused, "123John", "123456789").andExpect(status().isBadRequest())
                                                        .andExpect(jsonPath("$.propertyName").doesNotExist());

    verify(profileFormFieldService, never()).validate(any(), any(), anyBoolean(), any());
    verify(userHandler, never()).createUser(any(), anyBoolean());
  }

  /**
   * A token the onboarding token service does not accept, an unknown or
   * expired one, or a registration token, reads nothing: a 404 with no body,
   * kept by no cache
   */
  @Test
  @SneakyThrows
  public void testGetOnboardingFieldsWithARefusedToken() {
    when(passwordRecoveryService.verifyToken("unknown", CookieTokenService.ONBOARD_TOKEN)).thenReturn(null);
    when(passwordRecoveryService.verifyToken("registration", CookieTokenService.ONBOARD_TOKEN)).thenReturn(null);
    when(passwordRecoveryService.verifyToken("registration", CookieTokenService.EXTERNAL_REGISTRATION_TOKEN)).thenReturn("john");
    when(passwordRecoveryService.verifyToken("registration", "")).thenReturn("john");

    for (String token : List.of("unknown", "registration")) {
      mockMvc.perform(get("/login/onboardingFields?token=" + token))
             .andExpect(status().isNotFound())
             .andExpect(header().string("Cache-Control", "no-store"))
             .andExpect(content().string(""));
    }
    verify(profileFormFieldService, never()).getProfile(any());
    verify(profileFormFieldService, never()).getSettings(any());
  }

  /**
   * The holder of an onboarding token reads the registration allow-list and
   * the values the token's account holds for those attributes, read from that
   * account's profile and from no other, and the answer is kept by no cache
   */
  @Test
  @SneakyThrows
  public void testGetOnboardingFields() {
    Profile profile = prepareOnboarding(Map.of("team", "7", "secret", "x"));
    ProfilePropertySetting team = registrationSetting(5L, "team");
    team.setDropdownList(true);
    team.setRequired(true);
    ProfilePropertySetting skills = registrationSetting(6L, "skills");
    skills.setMultiValued(true);
    when(profileFormFieldService.getSettings(ProfileFormKind.REGISTRATION)).thenReturn(List.of(team, skills));
    when(profileFormFieldService.getStoredValues(List.of(team, skills), profile)).thenReturn(Map.of("team", List.of("7")));

    String content = mockMvc.perform(get("/login/onboardingFields?token=123456"))
                            .andExpect(status().isOk())
                            .andExpect(header().string("Cache-Control", "no-store"))
                            .andExpect(jsonPath("$.fields.length()").value(2))
                            .andExpect(jsonPath("$.fields[0].propertyName").value("team"))
                            .andExpect(jsonPath("$.fields[0].dropdownList").value(true))
                            .andExpect(jsonPath("$.fields[0].required").value(true))
                            .andExpect(jsonPath("$.fields[1].propertyName").value("skills"))
                            .andExpect(jsonPath("$.fields[1].multiValued").value(true))
                            .andExpect(jsonPath("$.values.team[0]").value("7"))
                            .andReturn()
                            .getResponse()
                            .getContentAsString();
    JSONObject answer = new JSONObject(content);
    assertEquals(Set.of("fields", "values"), answer.keySet());
    assertEquals(Set.of("team"), answer.getJSONObject("values").keySet());
    assertEquals(Set.of("id",
                        "propertyName",
                        "propertyType",
                        "dropdownList",
                        "multiValued",
                        "required",
                        "order",
                        "labels",
                        "propertyOptions"),
                 answer.getJSONArray("fields").getJSONObject(0).keySet());
    verify(passwordRecoveryService).verifyToken("123456", CookieTokenService.ONBOARD_TOKEN);
    verify(profileFormFieldService).getProfile("john");
    verify(profileFormFieldService).getProfile(any());
  }

  /**
   * An account whose password the link does not set, one an LDAP or Active
   * Directory store holds or a disabled one, for which the check answers
   * false or throws, reads no field and no value
   */
  @Test
  @SneakyThrows
  public void testGetOnboardingFieldsWhenThePasswordCannotBeSet() {
    prepareOnboarding(Map.of("team", "7"));
    when(passwordRecoveryService.allowChangePassword("john")).thenReturn(false);
    assertOnboardingFieldsEmpty();

    when(passwordRecoveryService.allowChangePassword("john")).thenThrow(new IllegalStateException("store down"));
    assertOnboardingFieldsEmpty();
  }

  /**
   * An account that has already signed in reads no field and no value through
   * an onboarding link
   */
  @Test
  @SneakyThrows
  public void testGetOnboardingFieldsOfAnAccountThatSignedIn() {
    prepareOnboarding(Map.of("team", "7", Profile.LAST_LOGIN_TIME, "1700000000000"));
    assertOnboardingFieldsEmpty();
  }

  /**
   * An account with no social identity, which has never signed in, reads the
   * list with no value
   */
  @Test
  @SneakyThrows
  public void testGetOnboardingFieldsOfAnAccountWithoutIdentity() {
    prepareOnboarding(Map.of());
    when(profileFormFieldService.getProfile("john")).thenThrow(new ObjectNotFoundException("no identity"));
    ProfilePropertySetting team = registrationSetting(5L, "team");
    when(profileFormFieldService.getSettings(ProfileFormKind.REGISTRATION)).thenReturn(List.of(team));
    when(profileFormFieldService.getStoredValues(List.of(team), null)).thenReturn(Map.of());

    mockMvc.perform(get("/login/onboardingFields?token=123456"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.fields.length()").value(1))
           .andExpect(jsonPath("$.fields[0].propertyName").value("team"))
           .andExpect(jsonPath("$.values.length()").value(0));
    verify(profileFormFieldService).getStoredValues(List.of(team), null);
  }

  /**
   * An external user the Users management created is stored by the platform,
   * so portal's check answers that the link sets its password (it reads the
   * store, never the group: PasswordRecoveryServiceImpl#allowChangePassword):
   * the user reads the list and its values, and its onboarding checks and
   * writes them, the profile's external flag adding no rule of its own
   */
  @Test
  @SneakyThrows
  public void testOnboardingOfAnExternalUser() {
    Profile profile = prepareOnboarding(Map.of("team", "7", Profile.EXTERNAL, "true"));
    ProfilePropertySetting team = registrationSetting(5L, "team");
    when(profileFormFieldService.getSettings(ProfileFormKind.REGISTRATION)).thenReturn(List.of(team));
    when(profileFormFieldService.getStoredValues(List.of(team), profile)).thenReturn(Map.of("team", List.of("7")));
    Map<String, Object> profileValues = Map.of("team", "8");
    when(profileFormFieldService.validate(any(), eq(ProfileFormKind.REGISTRATION), eq(true), eq(profile))).thenReturn(profileValues);

    mockMvc.perform(get("/login/onboardingFields?token=123456"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.fields[0].propertyName").value("team"))
           .andExpect(jsonPath("$.values.team[0]").value("7"));
    performOnboarding("[{\"propertyName\":\"team\",\"value\":\"8\"}]").andExpect(status().isFound())
                                                                    .andExpect(header().string("Location", "/portal/login"));
    verify(profileFormFieldService).save("john", profileValues, "john");
  }

  /**
   * Each refusal of an additional field refuses the onboarding before the
   * password is set, with its code and the attribute it names: the link is
   * kept and nothing is written
   */
  @Test
  @SneakyThrows
  public void testSetPasswordRefusesAnAdditionalFieldBeforeSettingThePassword() {
    Profile profile = prepareOnboarding(Map.of("team", "7"));
    for (String code : List.of(ProfileFormFieldException.NOT_ALLOWED,
                               ProfileFormFieldException.INVALID_OPTION,
                               ProfileFormFieldException.REQUIRED,
                               ProfileFormFieldException.TOO_LONG)) {
      when(profileFormFieldService.validate(any(), eq(ProfileFormKind.REGISTRATION), eq(true), eq(profile)))
                                                                                                        .thenThrow(new ProfileFormFieldException(code,
                                                                                                                                                 "team"));

      performOnboarding("[{\"propertyName\":\"team\",\"value\":\"x\"}]").andExpect(status().isBadRequest())
                                                                       .andExpect(jsonPath("$.error").value(code))
                                                                       .andExpect(jsonPath("$.propertyName").value("team"));
    }
    verify(passwordRecoveryService, never()).changePass(any(), any(), any(), any());
    verify(profileFormFieldService, never()).save(any(), any(), any());
  }

  /**
   * Additional fields that are not a JSON array of attribute values refuse the
   * onboarding before the password is set
   */
  @Test
  @SneakyThrows
  public void testSetPasswordRefusesMalformedAdditionalFields() {
    prepareOnboarding(Map.of());

    performOnboarding("not json").andExpect(status().isBadRequest())
                                 .andExpect(jsonPath("$.error").value(ProfileFormFieldException.INVALID_VALUE))
                                 .andExpect(jsonPath("$.propertyName").doesNotExist());
    verify(profileFormFieldService, never()).validate(any(), any(), anyBoolean(), any());
    verify(passwordRecoveryService, never()).changePass(any(), any(), any(), any());
  }

  /**
   * The values the user changed are checked as a complete form against the
   * account's stored profile, then the password is set, then the values are
   * written once, in the token's account, by its own login
   */
  @Test
  @SneakyThrows
  public void testSetPasswordSavesAdditionalFieldsAfterThePassword() {
    Profile profile = prepareOnboarding(Map.of("team", "7"));
    Map<String, Object> profileValues = Map.of("team", "8");
    when(profileFormFieldService.validate(any(), eq(ProfileFormKind.REGISTRATION), eq(true), eq(profile))).thenReturn(profileValues);

    performOnboarding("[{\"propertyName\":\"team\",\"value\":\"8\"},{\"propertyName\":\"skills\",\"values\":[\"Java\",null]}]")
                                                                                                                      .andExpect(status().isFound())
                                                                                                                      .andExpect(header().string("Location",
                                                                                                                                                 "/portal/login"));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ProfilePropertyValue>> sentValues = ArgumentCaptor.forClass(List.class);
    InOrder inOrder = inOrder(profileFormFieldService, passwordRecoveryService);
    inOrder.verify(profileFormFieldService).validate(sentValues.capture(), eq(ProfileFormKind.REGISTRATION), eq(true), eq(profile));
    inOrder.verify(passwordRecoveryService).changePass("123456", CookieTokenService.ONBOARD_TOKEN, "john", "Test1234@");
    inOrder.verify(profileFormFieldService).save("john", profileValues, "john");
    // Written once, after the password is set: never before it
    verify(profileFormFieldService).save(any(), any(), any());
    verify(passwordRecoveryService).changePass(any(), any(), any(), any());
    verify(passwordRecoveryService).verifyToken("123456", CookieTokenService.ONBOARD_TOKEN);
    assertEquals(List.of(new ProfilePropertyValue("team", "8", null), new ProfilePropertyValue("skills", null, Arrays.asList("Java", null))),
                 sentValues.getValue());
  }

  /**
   * A password the link fails to set answers today's refusal, and no value is
   * written
   */
  @Test
  @SneakyThrows
  public void testSetPasswordFailureWritesNoAdditionalField() {
    Profile profile = prepareOnboarding(Map.of());
    when(profileFormFieldService.validate(any(), eq(ProfileFormKind.REGISTRATION), eq(true), eq(profile))).thenReturn(Map.of("team", "8"));
    when(passwordRecoveryService.changePass(any(), any(), any(), any())).thenReturn(false);

    performOnboarding("[{\"propertyName\":\"team\",\"value\":\"8\"}]").andExpect(status().isBadRequest())
                                                                    .andExpect(content().string(new JSONObject().put("error",
                                                                                                                     "gatein.forgotPassword.resetPasswordFailure")
                                                                                                                .toString()));
    verify(profileFormFieldService).validate(any(), any(), anyBoolean(), any());
    verify(profileFormFieldService, never()).save(any(), any(), any());
  }

  /**
   * Once the password is set, a failed write of the values leaves it set and
   * tells the user on the login page
   */
  @Test
  @SneakyThrows
  public void testSetPasswordReportsAdditionalFieldsNotSaved() {
    Profile profile = prepareOnboarding(Map.of());
    Map<String, Object> profileValues = Map.of("team", "8");
    when(profileFormFieldService.validate(any(), eq(ProfileFormKind.REGISTRATION), eq(true), eq(profile))).thenReturn(profileValues);
    doThrow(new IllegalStateException("store down")).when(profileFormFieldService).save("john", profileValues, "john");

    performOnboarding("[{\"propertyName\":\"team\",\"value\":\"8\"}]").andExpect(status().isFound())
                                                                    .andExpect(header().string("Location",
                                                                                               "/portal/login?error=onboardingFieldsNotSaved"));
    verify(passwordRecoveryService).changePass("123456", CookieTokenService.ONBOARD_TOKEN, "john", "Test1234@");
  }

  /**
   * An onboarding that sends no additional field still has the Required
   * attributes checked against the stored profile: it sets the password and
   * writes nothing when they are held, and is refused before the password is
   * set when one is not
   */
  @Test
  @SneakyThrows
  public void testSetPasswordWithoutAdditionalFieldsChecksTheRequiredOnes() {
    Profile profile = prepareOnboarding(Map.of("team", "7"));
    when(profileFormFieldService.validate(List.of(), ProfileFormKind.REGISTRATION, true, profile)).thenReturn(Map.of());

    performOnboarding(null).andExpect(status().isFound()).andExpect(header().string("Location", "/portal/login"));
    verify(profileFormFieldService).validate(List.of(), ProfileFormKind.REGISTRATION, true, profile);
    verify(profileFormFieldService, never()).save(any(), any(), any());

    when(profileFormFieldService.validate(List.of(), ProfileFormKind.REGISTRATION, true, profile))
                                                                                                .thenThrow(new ProfileFormFieldException(ProfileFormFieldException.REQUIRED,
                                                                                                                                         "skills"));
    performOnboarding(null).andExpect(status().isBadRequest())
                           .andExpect(jsonPath("$.error").value(ProfileFormFieldException.REQUIRED))
                           .andExpect(jsonPath("$.propertyName").value("skills"));
    verify(passwordRecoveryService).changePass(any(), any(), any(), any());
  }

  /**
   * An account with no social identity is checked with no stored profile
   */
  @Test
  @SneakyThrows
  public void testSetPasswordOfAnAccountWithoutIdentity() {
    prepareOnboarding(Map.of());
    when(profileFormFieldService.getProfile("john")).thenThrow(new ObjectNotFoundException("no identity"));

    performOnboarding("[{\"propertyName\":\"team\",\"value\":\"8\"}]").andExpect(status().isFound());
    verify(profileFormFieldService).validate(any(), eq(ProfileFormKind.REGISTRATION), eq(true), isNull());
  }

  /**
   * For an account whose password the link does not set, an LDAP or Active
   * Directory one, which then fails at the change, or a disabled one, whose
   * password is set, the values are ignored with no check, a malformed
   * parameter included, and the onboarding answers as without them
   */
  @Test
  @SneakyThrows
  public void testSetPasswordIgnoresAdditionalFieldsWhenThePasswordCannotBeSet() {
    prepareOnboarding(Map.of());
    when(passwordRecoveryService.allowChangePassword("john")).thenReturn(false);
    when(passwordRecoveryService.changePass(any(), any(), any(), any())).thenReturn(false);
    performOnboarding("not json").andExpect(status().isBadRequest())
                                 .andExpect(content().string(new JSONObject().put("error",
                                                                                  "gatein.forgotPassword.resetPasswordFailure")
                                                                             .toString()));

    when(passwordRecoveryService.changePass(any(), any(), any(), any())).thenReturn(true);
    performOnboarding("[{\"propertyName\":\"team\",\"value\":\"8\"}]").andExpect(status().isFound())
                                                                    .andExpect(header().string("Location", "/portal/login"));

    when(passwordRecoveryService.allowChangePassword("john")).thenThrow(new IllegalStateException("store down"));
    performOnboarding("[{\"propertyName\":\"team\",\"value\":\"8\"}]").andExpect(status().isFound())
                                                                    .andExpect(header().string("Location", "/portal/login"));

    verify(profileFormFieldService, never()).validate(any(), any(), anyBoolean(), any());
    verify(profileFormFieldService, never()).save(any(), any(), any());
  }

  /**
   * An account that has already signed in sets its password through the link
   * as today: the values are ignored with no check, and nothing is written
   */
  @Test
  @SneakyThrows
  public void testSetPasswordIgnoresAdditionalFieldsOfAnAccountThatSignedIn() {
    prepareOnboarding(Map.of(Profile.LAST_LOGIN_TIME, "1700000000000"));

    performOnboarding("[{\"propertyName\":\"team\",\"value\":\"8\"}]").andExpect(status().isFound())
                                                                    .andExpect(header().string("Location", "/portal/login"));
    verify(passwordRecoveryService).changePass("123456", CookieTokenService.ONBOARD_TOKEN, "john", "Test1234@");
    verify(profileFormFieldService, never()).validate(any(), any(), anyBoolean(), any());
    verify(profileFormFieldService, never()).save(any(), any(), any());
  }

  @SneakyThrows
  private void assertOnboardingFieldsEmpty() {
    mockMvc.perform(get("/login/onboardingFields?token=123456"))
           .andExpect(status().isOk())
           .andExpect(header().string("Cache-Control", "no-store"))
           .andExpect(jsonPath("$.fields.length()").value(0))
           .andExpect(jsonPath("$.values.length()").value(0));
    verify(profileFormFieldService, never()).getSettings(any());
    verify(profileFormFieldService, never()).getStoredValues(any(), any());
  }

  /**
   * An onboarding link of john, who has never signed in and whose password
   * the link sets, with a valid captcha
   *
   * @return john's stored profile, holding the given properties
   */
  @SneakyThrows
  private Profile prepareOnboarding(Map<String, Object> properties) {
    when(passwordRecoveryService.verifyToken("123456", CookieTokenService.ONBOARD_TOKEN)).thenReturn("john");
    when(passwordRecoveryService.allowChangePassword("john")).thenReturn(true);
    when(passwordRecoveryService.changePass(any(), any(), any(), any())).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(true);
    Profile profile = new Profile(new Identity(OrganizationIdentityProvider.NAME, "john"));
    properties.forEach(profile::setProperty);
    when(profileFormFieldService.getProfile("john")).thenReturn(profile);
    return profile;
  }

  @SneakyThrows
  private ResultActions performOnboarding(String profileProperties) {
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("on-boarding", captcha);
    String content = "username=john&password=Test1234@&password2=Test1234@&token=123456&captcha=abcde";
    if (profileProperties != null) {
      content += "&profileProperties=" + URLEncoder.encode(profileProperties, StandardCharsets.UTF_8);
    }
    return mockMvc.perform(post("/login/setPassword").sessionAttrs(sessionattr)
                                                     .content(content)
                                                     .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                     .accept(MediaType.APPLICATION_JSON));
  }

  private ProfilePropertySetting registrationSetting(long id, String name) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setId(id);
    setting.setPropertyName(name);
    setting.setPropertyType("text");
    setting.setActive(true);
    setting.setEditable(true);
    setting.setListedOnRegistration(true);
    setting.setPropertyOptions(List.of());
    return setting;
  }

  @SneakyThrows
  private void prepareRegistration() {
    User john = new UserImpl();
    john.setUserName("john");
    when(userHandler.findUserByName(any(), any())).thenReturn(null);
    when(userHandler.createUserInstance(any())).thenReturn(john);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(passwordRecoveryService.verifyToken("123456789", CookieTokenService.EXTERNAL_REGISTRATION_TOKEN)).thenReturn("jsmith@acme.com");
    when(passwordRecoveryService.sendAccountCreatedConfirmationEmail(any(), any(), any())).thenReturn(true);
    when(captcha.isCorrect(any())).thenReturn(true);
  }

  private ResultActions performRegistration(String profileProperties) {
    return performRegistration(profileProperties, "John", "123456789");
  }

  @SneakyThrows
  private ResultActions performRegistration(String profileProperties, String firstName, String token) {
    HashMap<String, Object> sessionattr = new HashMap<>();
    sessionattr.put("external-registration", captcha);
    String content = "email=jsmith@acme.com&password=Password1234&password2=Password1234&firstName=" + firstName
        + "&lastName=Smith&captcha=abcde&token=" + token;
    if (profileProperties != null) {
      content += "&profileProperties=" + URLEncoder.encode(profileProperties, StandardCharsets.UTF_8);
    }
    return mockMvc.perform(post("/login/register").sessionAttrs(sessionattr)
                                                  .content(content)
                                                  .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                  .accept(MediaType.APPLICATION_JSON));
  }

  private RequestPostProcessor testSimpleUser() {
    return user("john").password("password")
                            .authorities(new SimpleGrantedAuthority("users"));
  }



}
