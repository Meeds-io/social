package org.exoplatform.social.websocket.entity;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

import groovy.transform.ToString;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ToString
public class WebSocketMessage {

  String              wsEventName;

  Map<String, Object> message;

  public WebSocketMessage(String wsEventName, Object... data) {
    this.wsEventName = wsEventName;
    if (data != null && data.length > 0) {
      message = new HashMap<>();
      for (Object object : data) {
        if (object != null) {
          message.put(object.getClass().getSimpleName().toLowerCase(), object);
        }
      }
    }
  }

  @Override
  public String toString() {
    try {
      return new JsonGeneratorImpl().createJsonObject(this).toString();
    } catch (JsonException e) {
      throw new IllegalStateException("Error parsing current global object to string", e);
    }
  }

}
