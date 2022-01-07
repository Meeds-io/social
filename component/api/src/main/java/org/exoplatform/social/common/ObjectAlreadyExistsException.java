package org.exoplatform.social.common;


@Deprecated
public class ObjectAlreadyExistsException extends org.exoplatform.commons.ObjectAlreadyExistsException {

    public ObjectAlreadyExistsException(Object existingObject) {
        super(existingObject);
    }

    public ObjectAlreadyExistsException(Object existingObject, String message) {
        super(existingObject, message);
    }

    public ObjectAlreadyExistsException(Object existingObject, String message, Throwable e) {
        super(existingObject, message, e);
    }
}
