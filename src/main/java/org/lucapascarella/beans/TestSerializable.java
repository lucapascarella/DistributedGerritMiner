package org.lucapascarella.beans;

import java.io.Serializable;

public class TestSerializable implements Serializable {

    private static final long serialVersionUID = 1679403982960132297L;
    private String string;

    public TestSerializable(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

}
