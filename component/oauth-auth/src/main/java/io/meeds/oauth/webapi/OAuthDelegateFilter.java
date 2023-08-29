/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.oauth.webapi;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.gatein.sso.agent.filter.api.SSOInterceptor;
import org.gatein.sso.integration.SSODelegateFilter;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Filter will delegate the work to configured OAuth interceptors
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthDelegateFilter extends SSODelegateFilter {

  private volatile Map<SSOInterceptor, String> oauthInterceptors;

  private static final Log                     log = ExoLogger.getLogger(OAuthDelegateFilter.class);

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    Map<SSOInterceptor, String> oauthInterceptors = getInterceptors();

    // skip this filter if no oauthInterceptors are declared
    if (!oauthInterceptors.isEmpty()) {
      OAuthFilterChain oauthChain = new OAuthFilterChain(chain, getInterceptors(), this);
      oauthChain.doFilter(request, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  private Map<SSOInterceptor, String> getInterceptors() {
    if (oauthInterceptors == null) {
      synchronized (this) {
        if (oauthInterceptors == null) {
          OAuthFilterIntegrator oauthFilterIntegrator =
                                                      (OAuthFilterIntegrator) getContainer().getComponentInstanceOfType(OAuthFilterIntegrator.class);
          oauthInterceptors = oauthFilterIntegrator.getOAuthInterceptors();
          if (!oauthInterceptors.isEmpty()) {
            log.info("Initialized OAuth integrator with interceptors: " + oauthInterceptors);
          }
        }
      }
    }

    return oauthInterceptors;
  }

  private static class OAuthFilterChain extends SSODelegateFilter.SSOFilterChain {

    private OAuthFilterChain(FilterChain containerChain,
                             Map<SSOInterceptor, String> interceptors,
                             OAuthDelegateFilter oauthDelegateFilter) {
      super(containerChain, interceptors, oauthDelegateFilter);
    }
  }
}
