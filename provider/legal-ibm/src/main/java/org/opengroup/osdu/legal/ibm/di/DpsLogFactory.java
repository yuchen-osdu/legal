/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/


package org.opengroup.osdu.legal.ibm.di;

import org.opengroup.osdu.core.common.logging.ILogger;
import org.opengroup.osdu.core.ibm.logging.logger.IBMLoggingProvider;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author mbayser
 *
 */
//@Component
public class DpsLogFactory extends AbstractFactoryBean<ILogger> {

	private IBMLoggingProvider ibmLoggingProvider = new IBMLoggingProvider();

	@Override
	protected ILogger createInstance() throws Exception {
		return ibmLoggingProvider.getLogger();
	}

	@Override
	public Class<?> getObjectType() {
		return ILogger.class;
	}
}