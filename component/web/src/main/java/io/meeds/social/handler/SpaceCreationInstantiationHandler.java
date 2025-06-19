/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.handler;

import io.meeds.social.space.model.SpaceCreationInstance;
import io.meeds.social.util.JsonUtils;
import io.meeds.spring.web.localization.HttpRequestLocaleWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Component
public class SpaceCreationInstantiationHandler extends WebRequestHandler {

  private static final Log LOG = ExoLogger.getLogger(SpaceCreationInstantiationHandler.class);

  public static final String NAME = "space-creation-instantiation";

  public static final String TOKEN_TYPE = "SPACE_CREATION_INSTANCE";

  private final SpaceService spaceService;

  private final WebAppController webAppController;

  private final PortalContainer container;

  private final ServletContext servletContext;

  private final UploadService uploadService;


  @Autowired
  public SpaceCreationInstantiationHandler(WebAppController webAppController,
                                           SpaceService spaceService,
                                           PortalContainer container,
                                           UploadService uploadService) {
    this.webAppController = webAppController;
    this.spaceService = spaceService;
    this.container = container;
    this.servletContext = container.getPortalContext();
    this.uploadService = uploadService;
    }

  @PostConstruct
  public void init() {
    webAppController.register(this);
  }

  @Override
  public String getHandlerName() {
    return NAME;
  }

  @Override
  protected boolean getRequiresLifeCycle() {
    return true;
  }

  @Override
  public boolean execute(ControllerContext controllerContext) throws Exception {
    HttpServletRequest request = new HttpRequestLocaleWrapper(controllerContext.getRequest());
    HttpServletResponse response = controllerContext.getResponse();
    String user = request.getRemoteUser();
    if (user != null) {
      CookieTokenService tokenService = AbstractTokenService.getInstance(CookieTokenService.class);
      String token = getTokenCookie(request);
      if (token != null) {
        Space space = new Space();
        String spaceData = tokenService.getToken(token, TOKEN_TYPE).getUsername();
        SpaceCreationInstance model = JsonUtils.fromJsonString(spaceData, SpaceCreationInstance.class);
        fillSpaceFromModel(space, model);
        space.setEditor(user);
        space = spaceService.createSpace(space, user);
        saveSpaceAvatar(space, model);
        saveSpaceBanner(model, space);
        RequestLifeCycle.restartTransaction();
        removeTokenCookie(request, response);
        tokenService.deleteToken(token, TOKEN_TYPE);
        String path = servletContext.getContextPath() + "/s/" + space.getSpaceId();
        response.sendRedirect(path);
        return true;
      }
    }
    return false;
  }

  private void removeTokenCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie cookie = new Cookie("spaceCreationCookie", "");
    cookie.setPath("/");
    cookie.setMaxAge(0);
    cookie.setHttpOnly(true);
    cookie.setSecure(request.isSecure());
    response.addCookie(cookie);
  }

  private String getTokenCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("spaceCreationCookie".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  private void fillSpaceFromModel(Space space, SpaceCreationInstance model) throws IOException {
    if (StringUtils.isNotBlank(model.getDisplayName())) {
      space.setDisplayName(model.getDisplayName());
      space.setDescription(model.getDescription());

      if (StringUtils.isBlank(space.getPrettyName())) {
        space.setPrettyName(model.getDisplayName());
      }
    } else if (StringUtils.isNotBlank(model.getPrettyName())) {
      space.setPrettyName(model.getPrettyName());
      space.setDescription(model.getDescription());
      space.setDisplayName(model.getPrettyName());
    }

    if (space.getSpaceId() == 0 && model.getTemplateId() > 0) {
      space.setTemplateId(model.getTemplateId());
    }

    if (StringUtils.isNotBlank(model.getId()) && StringUtils.isNotBlank(model.getBannerId())) {
      updateProfileField(space, Profile.BANNER, model.getBannerId());
    }

    if (StringUtils.isNotBlank(model.getId()) && StringUtils.isNotBlank(model.getAvatarId())) {
      updateProfileField(space, Profile.AVATAR, model.getAvatarId());
    }

    if (StringUtils.equalsIgnoreCase(Space.HIDDEN, model.getVisibility())) {
      space.setVisibility(Space.HIDDEN);
    } else if (StringUtils.equalsIgnoreCase(Space.PRIVATE, model.getVisibility())) {
      space.setVisibility(Space.PRIVATE);
    } else if (StringUtils.equalsIgnoreCase(Space.PUBLIC, model.getVisibility())) {
      space.setVisibility(Space.PUBLIC);
    } else if (StringUtils.isBlank(model.getVisibility()) && space.getId() == null) {
      space.setVisibility(Space.PRIVATE);
    }

    if (StringUtils.equalsIgnoreCase(Space.OPEN, model.getSubscription())) {
      space.setRegistration(Space.OPEN);
    } else if (StringUtils.equalsIgnoreCase(Space.CLOSED, model.getSubscription())) {
      space.setRegistration(Space.CLOSED);
    } else if (StringUtils.equalsIgnoreCase(Space.VALIDATION, model.getSubscription())) {
      space.setRegistration(Space.VALIDATION);
    } else if (StringUtils.isBlank(model.getSubscription()) && space.getId() == null) {
      space.setRegistration(Space.VALIDATION);
    }
  }

  private void updateProfileField(Space space,
                                  String name,
                                  String value) throws IOException {
    if (Profile.BANNER.equals(name) && StringUtils.equals(value, "DEFAULT_BANNER")) {
      space.setBannerAttachment(null);
      spaceService.updateSpaceBanner(space, RestUtils.getCurrentUser());
    } else if (Profile.AVATAR.equals(name) || Profile.BANNER.equals(name)) {
      UploadResource uploadResource = uploadService.getUploadResource(value);
      if (uploadResource == null) {
        throw new IllegalStateException("No uploaded resource found with uploadId = " + value);
      }
      String storeLocation = uploadResource.getStoreLocation();
      try (FileInputStream inputStream = new FileInputStream(storeLocation)) {
        if (Profile.AVATAR.equals(name)) {
          AvatarAttachment attachment = new AvatarAttachment(null,
                                                            uploadResource.getFileName(),
                                                            uploadResource.getMimeType(),
                                                            inputStream,
                                                            System.currentTimeMillis());
          space.setAvatarAttachment(attachment);
          spaceService.updateSpaceAvatar(space, RestUtils.getCurrentUser());
        } else {
          BannerAttachment attachment = new BannerAttachment(null,
                                                            uploadResource.getFileName(),
                                                            uploadResource.getMimeType(),
                                                            inputStream,
                                                            System.currentTimeMillis());
          space.setBannerAttachment(attachment);
          spaceService.updateSpaceBanner(space, RestUtils.getCurrentUser());
        }
      } finally {
        uploadService.removeUploadResource(value);
      }
    }
  }

  private void saveSpaceAvatar(Space space, SpaceCreationInstance model) {
    if (StringUtils.isNotBlank(model.getAvatarId())) {
      try {
        updateProfileField(space, Profile.AVATAR, model.getAvatarId());
      } catch (IOException e) {
        LOG.warn("Error adding Space Avatar. Avoid stopping space creation process and continue", e);
      }
    }
  }

  private void saveSpaceBanner(SpaceCreationInstance model, Space space) {
    if (StringUtils.isNotBlank(model.getBannerId())) {
      try {
        updateProfileField(space, Profile.BANNER, model.getBannerId());
      } catch (IOException e) {
        LOG.warn("Error adding Space Banner. Avoid stopping space creation process and continue", e);
      }
    }
  }
}
