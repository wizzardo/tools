package org.bordl.xml;

import java.io.IOException;

/**
 *
 * @author moxa
 */
public class XmlParseException extends IOException {

    private String message;

    public XmlParseException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
