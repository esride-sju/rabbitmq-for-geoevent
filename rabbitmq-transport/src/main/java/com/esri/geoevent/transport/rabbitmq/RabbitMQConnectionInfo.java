/*
  Copyright 1995-2015 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
*/

package com.esri.geoevent.transport.rabbitmq;

import com.esri.ges.core.validation.Validatable;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.util.Converter;

enum RabbitMQAuthenticationType
{
  userpass, certificate
}

public class RabbitMQConnectionInfo implements Validatable
{
	private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQConnectionInfo.class);
	private String	host = "localhost";
	private int 	port = 5672;
	private String 	virtualHost = null;
	
	private RabbitMQAuthenticationType authenticationType = RabbitMQAuthenticationType.userpass;
	private String 	username = null;
	private String 	password = null;
	private String clientCert;
	private String clientCertPassword;
	
	private boolean ssl = true;
	private boolean useProvidedServerCert = false;
	private String 	serverCert = null;
			
	public RabbitMQConnectionInfo(String host, String port, String virtualHost, boolean ssl)
	{
		this.host = host;
		this.port = Converter.convertToInteger(port, 5672);
		this.virtualHost = virtualHost;	
		this.ssl = ssl;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public String getVirtualHost()
	{
		return virtualHost;
	}
		
	public RabbitMQAuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	public void setAuthenticationType(RabbitMQAuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
	}	

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername()
	{
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword()
	{
		return password;
	}

	public String getClientCert() {
		return clientCert;
	}
	
	public void setClientCert(String clientCert) {
		this.clientCert = clientCert;
	}
	
	public String getClientCertPassword() {
		return clientCertPassword;
	}
	
	public void setClientCertPassword(String clientCertPassword) {
		this.clientCertPassword = clientCertPassword;
	}
	
	public boolean isSsl()
	{
		return ssl;
	}				
		
	public boolean isUseProvidedServerCert() {
		return useProvidedServerCert;
	}

	public void setUseProvidedServerCert(boolean useProvidedServerCert) {
		this.useProvidedServerCert = useProvidedServerCert;
	}
		
	public String getServerCert() {
		return serverCert;
	}

	public void setServerCert(String serverCert) {
		this.serverCert = serverCert;
	}

	@Override
	public void validate() throws ValidationException
	{
		if (host == null || host.isEmpty())
			throw new ValidationException(LOGGER.translate("CONNECTION_HOST_VALIDATE_ERROR"));
		if (port <= 0)
			throw new ValidationException(LOGGER.translate("CONNECTION_PORT_VALIDATE_ERROR"));
	}
}
