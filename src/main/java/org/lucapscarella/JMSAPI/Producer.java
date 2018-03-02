package org.lucapscarella.JMSAPI;

import java.io.Serializable;

import javax.jms.JMSException;

public interface Producer {

    public boolean sendTextMessage(String text);
    public boolean sendObject(Serializable obj);

    public void close() throws JMSException;

    public void waitTermination();
}
