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

import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.TransportDefinitionBase;
import com.esri.ges.transport.TransportType;

import java.util.ArrayList;
import java.util.List;

public class RabbitMQInboundTransportDefinition extends TransportDefinitionBase
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQInboundTransport.class);

  public RabbitMQInboundTransportDefinition()
  {
    super(TransportType.INBOUND);
    try
    {
      // Connection properties
      propertyDefinitions.put("host", new PropertyDefinition("host", PropertyType.String, "localhost", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_HOST_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_HOST_DESC}", true, false));
      propertyDefinitions.put("port", new PropertyDefinition("port", PropertyType.Integer, "5672", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_PORT_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_PORT_DESC}", true, false));
      propertyDefinitions.put("virtualHost", new PropertyDefinition("virtualHost", PropertyType.String, null, "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_VIRTUAL_HOST_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_VIRTUAL_HOST_DESC}", false, false));
      propertyDefinitions.put("username", new PropertyDefinition("username", PropertyType.String, null, "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_USERNAME_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_USERNAME_DESC}", false, false));
      propertyDefinitions.put("password", new PropertyDefinition("password", PropertyType.Password, null, "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_PASSWORD_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_PASSWORD_DESC}", false, false));
      propertyDefinitions.put("ssl", new PropertyDefinition("ssl", PropertyType.Boolean, false, "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_SSL_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_SSL_DESC}", true, false));

      // Exchange properties
      propertyDefinitions.put("exchangeName", new PropertyDefinition("exchangeName", PropertyType.String, null, "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_EXCHANGE_NAME_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_EXCHANGE_NAME_DESC}", true, false));

      List<LabeledValue> exchangeTypeAllowedValues = new ArrayList<LabeledValue>();
      exchangeTypeAllowedValues.add(new LabeledValue(RabbitMQExchangeType.direct.toString(), RabbitMQExchangeType.direct.toString()));
      exchangeTypeAllowedValues.add(new LabeledValue(RabbitMQExchangeType.fanout.toString(), RabbitMQExchangeType.fanout.toString()));
      exchangeTypeAllowedValues.add(new LabeledValue(RabbitMQExchangeType.topic.toString(), RabbitMQExchangeType.topic.toString()));
      propertyDefinitions.put("exchangeType", new PropertyDefinition("exchangeType", PropertyType.String, RabbitMQExchangeType.direct.toString(), "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_EXCHANGE_TYPE_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_EXCHANGE_TYPE_DESC}", true, false, exchangeTypeAllowedValues));

      List<LabeledValue> exchangeDurabilityAllowedValues = new ArrayList<LabeledValue>();
      exchangeDurabilityAllowedValues.add(new LabeledValue(RabbitMQDurability.Transient.toString(), RabbitMQDurability.Transient.toString()));
      exchangeDurabilityAllowedValues.add(new LabeledValue(RabbitMQDurability.Durable.toString(), RabbitMQDurability.Durable.toString()));
      propertyDefinitions.put("exchangeDurability", new PropertyDefinition("exchangeDurability", PropertyType.String, RabbitMQDurability.Transient.toString(), "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_EXCHANGE_DURABILITY_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_EXCHANGE_DURABILITY_DESC}", true, false, exchangeDurabilityAllowedValues));

      propertyDefinitions.put("exchangeAutoDelete", new PropertyDefinition("exchangeAutoDelete", PropertyType.Boolean, "true", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_EXCHANGE_AUTO_DELETE_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_EXCHANGE_AUTO_DELETE_DESC}", true, false));

      // Queue properties
      propertyDefinitions.put("queueName", new PropertyDefinition("queueName", PropertyType.String, null, "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QUEUE_NAME_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QUEUE_NAME_DESC}", true, false));

      List<LabeledValue> queueDurabilityAllowedValues = new ArrayList<LabeledValue>();
      queueDurabilityAllowedValues.add(new LabeledValue(RabbitMQDurability.Transient.toString(), RabbitMQDurability.Transient.toString()));
      queueDurabilityAllowedValues.add(new LabeledValue(RabbitMQDurability.Durable.toString(), RabbitMQDurability.Durable.toString()));
      propertyDefinitions.put("queueDurability", new PropertyDefinition("queueDurability", PropertyType.String, RabbitMQDurability.Transient.toString(), "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QUEUE_DURABILITY_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QUEUE_DURABILITY_DESC}", true, false, queueDurabilityAllowedValues));

      propertyDefinitions.put("queueExclusive", new PropertyDefinition("queueExclusive", PropertyType.Boolean, "false", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QUEUE_EXCLUSIVE_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QUEUE_EXCLUSIVE_DESC}", true, false));
      propertyDefinitions.put("queueAutoDelete", new PropertyDefinition("queueAutoDelete", PropertyType.Boolean, "true", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QUEUE_AUTO_DELETE_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QUEUE_AUTO_DELETE_DESC}", true, false));

      propertyDefinitions.put("prefetchCount", new PropertyDefinition("prefetchCount", PropertyType.Integer, 1, "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QOS_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_QOS_DESC}", false, false));
      propertyDefinitions.put("routingKey", new PropertyDefinition("routingKey", PropertyType.String, "", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_ROUTING_KEY_LBL}", "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_ROUTING_KEY_DESC}", false, false));
    }
    catch (PropertyException e)
    {
      String errorMsg = LOGGER.translate("TRANSPORT_IN_INIT_ERROR", e.getMessage());
      LOGGER.error(errorMsg, e);
      throw new RuntimeException(errorMsg, e);
    }
  }

  @Override
  public String getName()
  {
    return "RabbitMQ";
  }

  @Override
  public String getDomain()
  {
    return "com.esri.geoevent.transport.inbound";
  }

  @Override
  public String getLabel()
  {
    return "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_LABEL}";
  }

  @Override
  public String getDescription()
  {
    return "Release ${com.esri.geoevent.transport.rabbitmq-transport.PROJECT_RELEASE}: ${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_DESC}";
  }
}
