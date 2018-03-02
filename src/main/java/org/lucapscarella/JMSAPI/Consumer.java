package org.lucapscarella.JMSAPI;

import javax.jms.JMSException;


public interface Consumer {

	public void start() throws JMSException;
	public void close() throws JMSException;
	public void startAndWait();
}
