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

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.Connections;
import net.jodah.lyra.config.Config;
import net.jodah.lyra.config.RecoveryPolicies;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeoutException;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class RabbitMQConnectionBroker extends RabbitMQObservable implements Observer
{
  private static final BundleLogger  LOGGER  = BundleLoggerFactory.getLogger(RabbitMQComponentBase.class);
  private Connection                 connection;
  private RabbitMQConnectionListener connectionListener;
  private RabbitMQChannelListener    channelListener;
  private RabbitMQConsumerListener   consumerListener;
  private RabbitMQConnectionMonitor  monitor;
  private int                        timeout = 5000;

  public RabbitMQConnectionBroker(RabbitMQConnectionInfo connectionInfo)
  {
    connectionListener = new RabbitMQConnectionListener(connectionInfo);
    connectionListener.addObserver(this);
    channelListener = new RabbitMQChannelListener();
    channelListener.addObserver(this);
    consumerListener = new RabbitMQConsumerListener();
    consumerListener.addObserver(this);
    monitor = new RabbitMQConnectionMonitor(connectionInfo);
    monitor.addObserver(this);
    new Thread(monitor).start();
  }

  public Channel createChannel() throws RabbitMQTransportException
  {
    if (isConnected())
    {
      try
      {
        return connection.createChannel();
      }
      catch (IOException e)
      {
        String msg = LOGGER.translate("CHANNEL_CREATE_ERROR", e.getMessage());
        LOGGER.error(msg, e);
        throw new RabbitMQTransportException(msg);
      }
    }
    String cause = LOGGER.translate("CONNECTION_BROKEN_ERROR", monitor.connectionInfo.getHost());
    String msg = LOGGER.translate("CHANNEL_CREATE_ERROR", cause);
    LOGGER.error(msg);
    throw new RabbitMQTransportException(msg);
  }

  public boolean isConnected()
  {
    return connection != null && connection.isOpen();
  }

  public void shutdown()
  {
    monitor.deleteObserver(this);
    monitor.stop();
    connectionListener.deleteObserver(this);
    channelListener.deleteObserver(this);
    consumerListener.deleteObserver(this);
    if (isConnected())
    {
      try
      {
        connection.close(timeout);
      }
      catch (IOException e)
      {
        String msg = LOGGER.translate("CONNECTION_CLOSE_ERROR", monitor.connectionInfo.getHost(), e.getMessage());
        LOGGER.error(msg, e);
      }
      finally
      {
        connection = null;
      }
    }
  }

  @Override
  public void update(Observable observable, Object obj)
  {
    if (obj instanceof RabbitMQTransportEvent)
    {
      RabbitMQTransportEvent event = (RabbitMQTransportEvent) obj;
      notifyObservers(event.getStatus(), event.getDetails());
    }
  }

  public RabbitMQConnectionInfo getConnectionInfo()
  {
    return monitor.connectionInfo;
  }

  private class RabbitMQConnectionMonitor extends RabbitMQObservable implements Runnable
  {
    private RabbitMQConnectionInfo connectionInfo;
    private volatile boolean       running    = false;
    private volatile boolean       errorState = false;

    public RabbitMQConnectionMonitor(RabbitMQConnectionInfo connectionInfo)
    {
      this.connectionInfo = connectionInfo;
    }

    @Override
    public void run()
    {
      running = true;
      while (running)
      {
        if (!isConnected())
        {
          try
          {
            ConnectionFactory factory = new ConnectionFactory(); // Create factory
            factory.setHost(connectionInfo.getHost());
	        factory.setPort(connectionInfo.getPort());
	                    
	        if (connectionInfo.getVirtualHost() != null) {
	        	  factory.setVirtualHost(connectionInfo.getVirtualHost());
	        }
	        
            TrustManager[] clientTrustManagerList = null;
            
            if (connectionInfo.isUseProvidedServerCert()) {             	 	            	            	
        		/*
        		 // TRUST ALL CERTIFICATES 
  	              TrustManager[] clientTrustManagerListTemp = {
  	  	            new X509TrustManager() {
  	  	                @Override
  	  	                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
  	  	                @Override
  	  	                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
  	  	                @Override
  	  	                public X509Certificate[] getAcceptedIssuers() { return null;}
  	  	            }
  	  	          };
  	  	          clientTrustManagerList = clientTrustManagerListTemp;
        		*/
        		
        		CertificateFactory cf = CertificateFactory.getInstance("X.509");  
        		LOGGER.info("Read Client Cert: " + connectionInfo.getServerCert());
	      		FileInputStream is = new FileInputStream(new File(connectionInfo.getServerCert()));		    	
	          	InputStream caInput = new BufferedInputStream(is);
	          	Certificate ca;
	          	
	      		try {
	      			ca = cf.generateCertificate(caInput);
	      			LOGGER.info("ca=" + ((X509Certificate) ca).getSubjectDN());
	      		} finally {
	      			caInput.close();
	      		}

	          	// Create a KeyStore containing our trusted CAs
        		String keyStoreType = KeyStore.getDefaultType();
        		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        		keyStore.load(null, null);
        		keyStore.setCertificateEntry("ca", ca);

        		// Create a TrustManager that trusts the CAs in our KeyStore
        		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        		tmf.init(keyStore);
        				
        		clientTrustManagerList = tmf.getTrustManagers();            
            } 
        	                    
            KeyManager[] clientKeyManagerList = null;
            
            switch(connectionInfo.getAuthenticationType()) {                        
            	case certificate:                                    	            
                    try {
                    	LOGGER.info("read certificat...: "+ connectionInfo.getClientCert());
                    	FileInputStream clientCertificateInputStream = new FileInputStream(new File(connectionInfo.getClientCert()));
                    	LOGGER.info("create clean keystore instance...");
                        KeyStore clientKeStore = KeyStore.getInstance("PKCS12");
                        LOGGER.info("load client certificate into keystore...");
                        clientKeStore.load(clientCertificateInputStream, connectionInfo.getClientCertPassword().toCharArray());
                        LOGGER.info("create KeyManagerFactory (SunX509)... ");
                        KeyManagerFactory clientSSLKeyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                        LOGGER.info("initialize KeyManagerFactory... ");
                        clientSSLKeyManagerFactory.init(clientKeStore, connectionInfo.getClientCertPassword().toCharArray());
                        LOGGER.info("get list of key managers..."); // (in essence, only the keystore with the client certificate)
                        clientKeyManagerList = clientSSLKeyManagerFactory.getKeyManagers();                             
                    } catch(Exception e) {
                    	LOGGER.error("Exception client certificate", e);
                    }
            		            		
            		break;
            	case userpass:
            	default:
      	          if (connectionInfo.getUsername() != null && connectionInfo.getPassword() != null)
    	          {
    	        	  factory.setUsername(connectionInfo.getUsername());
    	        	  factory.setPassword(connectionInfo.getPassword());
    	          }
            }
            
            if (connectionInfo.isSsl()) {
            	
            	// TODO Kann SSL Context gesetzt werden wenn nur isSSL aktiv ist aber weder Server noch Client Zert?
            	if (connectionInfo.isUseProvidedServerCert()) {
            		// TLS Version konfigurierbar machen? 1.2 oder 1.3?        	  
                	SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); 
            		
                	LOGGER.info("initialize ssl context");
                    //Initialize SSL context with the key and trust managers we've created/loaded before
                	sslContext.init(clientKeyManagerList, clientTrustManagerList, null);
                	

                	LOGGER.info("set Sasl Config to EXTERNAL");
      	          	factory.setSaslConfig(DefaultSaslConfig.EXTERNAL);  //Set authentication method as SSL auth
      	          
      	          	LOGGER.info("use ssl context as SSLProtocol");
      	          	factory.useSslProtocol(sslContext);   //Set the created SSL context as the one to use            		
            	} else {
            		factory.useSslProtocol();	
            	}
        	}
               	                    
	          ConnectionOptions options = new ConnectionOptions().withConnectionFactory(factory);
	          Config config = new Config().withRecoveryPolicy(RecoveryPolicies.recoverAlways()).withChannelListeners(channelListener).withConnectionListeners(connectionListener).withConsumerListeners(consumerListener).withConsumerRecovery(true);
	          connection = Connections.create(options, config);
	          connection.addShutdownListener(new ShutdownListener()
	          {
	            @Override
	            public void shutdownCompleted(ShutdownSignalException cause)
	            {
	              LOGGER.error("CONNECTION_BROKEN_WITH_CAUSE_ERROR", connectionInfo.getHost(), cause.getMessage());
	              notifyObservers(RabbitMQConnectionStatus.DISCONNECTED, cause.getMessage());
	            }
	          });
	          errorState = false;
	          String msg = LOGGER.translate("CONNECTION_ESTABLISH_SUCCESS", connectionInfo.getHost());
	          LOGGER.info(msg);
	          notifyObservers(RabbitMQConnectionStatus.CREATED, msg);	        
          }
          catch (Throwable th)
          {
            // only log the error message once
            if (!errorState)
            {
              String msg = LOGGER.translate("CONNECTION_ESTABLISH_FAILURE", connectionInfo.getHost(), th.getMessage());
              LOGGER.error(msg, th);
              notifyObservers(RabbitMQConnectionStatus.CREATION_FAILED, msg);
              errorState = true;
            }
          }
        }
        sleep();
      }
    }

    private void sleep()
    {
      try
      {
        Thread.sleep(timeout);
      }
      catch (InterruptedException e)
      {
        ;
      }
    }

    public void stop()
    {
      running = false;
    }
  }

  public abstract static class RabbitMQComponentBase extends RabbitMQObservable implements Observer
  {
    private RabbitMQConnectionBroker broker;
    protected RabbitMQExchange       exchange;
    protected volatile boolean       connected = false;
    private String                   details   = "";
    protected Channel                channel;

    public RabbitMQComponentBase(RabbitMQConnectionInfo connectionInfo, RabbitMQExchange exchange)
    {
      broker = new RabbitMQConnectionBroker(connectionInfo);
      broker.addObserver(this);
      this.exchange = exchange;
    }

    protected synchronized void init() throws RabbitMQTransportException
    {
      try
      {
        channel.addShutdownListener(new ShutdownListener()
          {
            @Override
            public void shutdownCompleted(ShutdownSignalException cause)
            {
              disconnect(cause.getMessage());
            }
          });

        channel.exchangeDeclare(exchange.getName(), exchange.getType().toString(), exchange.isDurable(), exchange.isAutoDelete(), null);
      }
      catch (IOException e)
      {
        String msg = LOGGER.translate("EXCHANGE_CREATE_ERROR", e.getMessage());
        LOGGER.error(msg, e);
        throw new RabbitMQTransportException(msg);
      }
    }

    public String getStatusDetails()
    {
      return details;
    }

    public boolean isConnected()
    {
      return connected;
    }

    protected synchronized void connect() throws RabbitMQTransportException
    {
      LOGGER.info("Connecting..");
      disconnect(null);
      if (broker.isConnected())
      {
    	LOGGER.info("Broker is connected");
    	if (channel == null) {
          LOGGER.info("Create Channel...");
          channel = broker.createChannel();
          LOGGER.info("Channel Created: " + channel.getChannelNumber());
        }
        init();
        details = "";
        connected = true;
      }
      else
      {
    	LOGGER.info("Broker is NOT connected");
        details = LOGGER.translate("CONNECTION_BROKEN_ERROR", broker.monitor.connectionInfo.getHost());
        LOGGER.error(details);
        throw new RabbitMQTransportException(details);
      }
    }

    protected synchronized void disconnect(String reason)
    {
      LOGGER.info("Disconnect.. : + " + reason);
      if (connected)
      {
        if (channel != null)
        {
          if (channel.isOpen())
          {
            try
            {
              channel.close();
            }
            catch (IOException | TimeoutException e)
            {
              String msg = LOGGER.translate("CHANNEL_CLOSE_ERROR", e.getMessage());
              LOGGER.error(msg, e);
            }
          }
          channel = null;
        }
      }
      connected = false;
      details = reason;
    }

    public void shutdown(String reason)
    {
      disconnect(reason);
      broker.deleteObserver(this);
      broker.shutdown();
    }

    @Override
    public void update(Observable observable, Object obj)
    {
      if (obj instanceof RabbitMQTransportEvent)
      {
        RabbitMQTransportEvent event = (RabbitMQTransportEvent) obj;
        notifyObservers(event.getStatus(), event.getDetails());
      }
    }
  }
}
