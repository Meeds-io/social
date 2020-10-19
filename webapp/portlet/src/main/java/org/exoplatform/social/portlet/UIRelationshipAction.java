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
package org.exoplatform.social.portlet;

import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  template = "app:/groovy/social/portlet/UIRelationshipAction.gtmpl",
  events = {
    @EventConfig(listeners = UIRelationshipAction.ConnectActionListener.class),
    @EventConfig(listeners = UIRelationshipAction.CancelActionListener.class),
    @EventConfig(listeners = UIRelationshipAction.AcceptActionListener.class),
    @EventConfig(listeners = UIRelationshipAction.DenyActionListener.class),
    @EventConfig(listeners = UIRelationshipAction.DisconnectActionListener.class)
  }
)
public class UIRelationshipAction extends UIComponent {

  public UIRelationshipAction() {
  }

  public static abstract class AbstractActionListener extends EventListener<UIRelationshipAction> {
    protected Relationship relationship = null;
    protected String msgKey = "UIRelationshipAction.label.ConnectNotExisting";
    @Override
    public void execute(Event<UIRelationshipAction> event) {
      UIRelationshipAction uiAction = event.getSource();
      relationship = Utils.getRelationshipManager().get(Utils.getOwnerIdentity(), Utils.getViewerIdentity());
      if (isValid(event)) {
        doAction(event);
      } else {
        uiAction.getAncestorOfType(UIPortletApplication.class).addMessage(new ApplicationMessage(msgKey, new String[]{}, ApplicationMessage.WARNING));
      }
      //
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAction);
    }
    protected boolean isValid(Event<UIRelationshipAction> event) {
      return (relationship != null);
    }
    protected abstract void doAction(Event<UIRelationshipAction> event);
  }
  
  public static class ConnectActionListener extends AbstractActionListener {
    @Override
    protected boolean isValid(Event<UIRelationshipAction> event) {
      msgKey = "UIRelationshipAction.label.ConnectionExisted";
      return (relationship == null);
    }
    @Override
    protected void doAction(Event<UIRelationshipAction> event) {// sender --> owner
      Utils.getRelationshipManager().inviteToConnect(Utils.getViewerIdentity(), Utils.getOwnerIdentity());
    }
  }

  public static class CancelActionListener extends AbstractActionListener {
    @Override
    protected void doAction(Event<UIRelationshipAction> event) {
      Utils.getRelationshipManager().deny(relationship.getReceiver(), relationship.getSender());
    }
  }

  public static class AcceptActionListener extends AbstractActionListener {
    @Override
    protected boolean isValid(Event<UIRelationshipAction> event) {
      return super.isValid(event) && (relationship.getStatus() != Type.IGNORED);
    }

    @Override
    protected void doAction(Event<UIRelationshipAction> event) {
      Utils.getRelationshipManager().confirm(relationship.getReceiver(), relationship.getSender());
      Utils.updateWorkingWorkSpace();
    }
  }

  public static class DenyActionListener extends AbstractActionListener {
    @Override
    protected boolean isValid(Event<UIRelationshipAction> event) {
      return super.isValid(event);
    }

    @Override
    protected void doAction(Event<UIRelationshipAction> event) {
      Utils.getRelationshipManager().deny(relationship.getReceiver(), relationship.getSender());
      Utils.updateWorkingWorkSpace();
    }
  }

  public static class DisconnectActionListener extends AbstractActionListener {
    @Override
    protected void doAction(Event<UIRelationshipAction> event) {
      Utils.getRelationshipManager().delete(relationship);
      Utils.updateWorkingWorkSpace();
    }
  }
}
