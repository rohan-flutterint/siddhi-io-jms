/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package io.siddhi.extension.io.jms.source;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.SiddhiTestHelper;
import io.siddhi.extension.io.jms.source.client.JMSClient;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class implementing the Test cases for JMS Source.
 */
public class JMSSourceTestCase {
    private static final String PROVIDER_URL = "vm://localhost?broker.persistent=false,";
    private static final Logger log = (Logger) LogManager.getLogger(JMSSourceTestCase.class);
    private List<String> receivedEventNameList;
    private int waitTime = 50;
    private int timeout = 30000;

    /**
     * Test for configure the JMS source to connect to an ActiveMQ topic and receive messages.
     */
    @Test
    public void testJMSTopicSource1() throws InterruptedException {
        AtomicInteger eventCount = new AtomicInteger(0);
        receivedEventNameList = new ArrayList<>(2);

        // starting the ActiveMQ broker
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = ""
                + "@source(type='jms', @map(type='xml'), "
                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                + "provider.url='vm://localhost',"
                + "destination='DAS_JMS_TEST', "
                + "connection.factory.type='topic',"
                + "connection.factory.jndi.name='TopicConnectionFactory',"
                + "transport.jms.SubscriptionDurable='true', "
                + "transport.jms.DurableSubscriberClientID='wso2dasclient1'"
                + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;");
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
        List<String> messageList = new ArrayList<>(2);
        messageList.add("<events>\n"
                + "    <event>\n"
                + "        <name>John</name>\n"
                + "        <age>22</age>\n"
                + "        <country>US</country>\n"
                + "    </event>\n"
                + "    <event>\n"
                + "        <name>Mike</name>\n"
                + "        <age>24</age>\n"
                + "        <country>US</country>\n"
                + "    </event>\n"
                + "</events>");
        // publishing events
        publishEvents("DAS_JMS_TEST", null, "activemq", "text", messageList);
        List<String> expected = new ArrayList<>(2);
        expected.add("John");
        expected.add("Mike");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(eventCount.intValue(), 2, "Event count should be equal to two.");
        AssertJUnit.assertEquals("JMS Source expected input not received", expected, receivedEventNameList);
        siddhiManager.shutdown();
    }

    @Test(dependsOnMethods = "testJMSTopicSource1")
    public void testJMSTopicSource2() throws InterruptedException {
        AtomicInteger eventCount = new AtomicInteger(0);
        receivedEventNameList = new ArrayList<>(2);

        // starting the ActiveMQ broker
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = ""
                + "@source(type='jms', @map(type='xml'), "
                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                + "provider.url='vm://localhost',"
                + "destination='DAS_JMS_TEST', "
                + "connection.factory.type='topic',"
                + "connection.factory.jndi.name='TopicConnectionFactory',"
                + "transport.jms.SubscriptionDurable='true', "
                + "transport.jms.DurableSubscriberClientID='wso2dasclient1'"
                + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;");
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
        List<String> messageList = new ArrayList<>(2);
        messageList.add("<events>\n"
                + "    <event>\n"
                + "        <name>John</name>\n"
                + "        <age>22</age>\n"
                + "        <country>US</country>\n"
                + "    </event>\n" + "    <event>\n"
                + "        <name>Mike</name>\n"
                + "        <age>24</age>\n"
                + "        <country>US</country>\n"
                + "    </event>\n"
                + "</events>");
        // publishing events
        publishEvents("DAS_JMS_TEST", null, "activemq", "text", messageList);
        List<String> expected = new ArrayList<>(2);
        expected.add("John");
        expected.add("Mike");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        AssertJUnit.assertEquals("JMS Source expected input not received", expected, receivedEventNameList);
        siddhiManager.shutdown();
    }

    /**
     * Test for configure the JMS source to connect to an ActiveMQ queue and receive messages.
     */
    @Test(dependsOnMethods = "testJMSTopicSource2")
    public void testJMSTopicSource3() throws InterruptedException {
        AtomicInteger eventCount = new AtomicInteger(0);
        receivedEventNameList = new ArrayList<>(2);

        // starting the ActiveMQ broker
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = ""
                + "@source(type='jms', @map(type='json'), "
                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                + "provider.url='vm://localhost',"
                + "destination='DAS_JMS_TEST' "
                + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;");
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
        List<String> messageList = new ArrayList<>(2);
        messageList.add("[ {\"event\":{\"name\":\"John\",\"age\":50,\"country\":\"US\"}},\n"
                + " {\"event\":{\"name\":\"Mike\",\"age\":23,\"country\":\"US\"}}\n" + "]");
        // publishing events
        publishEvents(null, "DAS_JMS_TEST", "activemq", "text", messageList);
        List<String> expected = new ArrayList<>(2);
        expected.add("John");
        expected.add("Mike");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(receivedEventNameList, expected, "JMS Source expected input not received");
        siddhiManager.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class,
            dependsOnMethods = "testJMSTopicSource3")
    public void testJMSTopicSource4() throws InterruptedException {
        AtomicInteger eventCount = new AtomicInteger(0);
        receivedEventNameList = new ArrayList<>(2);

        // starting the ActiveMQ broker
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" + "@source(type='jms', @map(type='xml'), "
                + "provider.url='vm://localhost',"
                + "destination='DAS_JMS_TEST', "
                + "connection.factory.type='topic',"
                + "connection.factory.jndi.name='TopicConnectionFactory',"
                + "transport.jms.SubscriptionDurable='true', "
                + "transport.jms.DurableSubscriberClientID='wso2dasclient1'"
                + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;");
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
        siddhiManager.shutdown();
    }

    /**
     * Test for configure the JMS source with pausing and resuming functionality.
     */
    @Test
    public void testJMSTopicSourcePause() throws InterruptedException {
        AtomicInteger eventCount = new AtomicInteger(0);
        receivedEventNameList = new ArrayList<>(1);

        // starting the ActiveMQ broker
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);
        List<String> messageList = new ArrayList<>(1);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" + "@source(type='jms', @map(type='xml'), "
                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                + "provider.url='vm://localhost'," + "destination='DAS_JMS_TEST', "
                + "connection.factory.type='topic',"
                + "connection.factory.jndi.name='TopicConnectionFactory',"
                + "transport.jms.SubscriptionDurable='true', "
                + "transport.jms.DurableSubscriberClientID='wso2dasclient1'" + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;");
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        Collection<List<Source>> sources = executionPlanRuntime.getSources();
        executionPlanRuntime.start();
        // pause
        sources.forEach(e -> e.forEach(Source::pause));
        // send few events
        messageList.clear();
        messageList.add("<events>\n"
                + "    <event>\n"
                + "        <name>John</name>\n"
                + "        <age>22</age>\n"
                + "        <country>US</country>\n"
                + "    </event>\n"
                + "</events>");
        Thread.sleep(1000);
        // publishing events
        publishEvents("DAS_JMS_TEST", null, "activemq", "text", messageList);

        //resume
        sources.forEach(e -> e.forEach(Source::resume));
        // send few more events
        messageList.clear();
        messageList.add("<events>\n"
                + "    <event>\n"
                + "        <name>Johny</name>\n"
                + "        <age>22</age>\n"
                + "        <country>US</country>\n"
                + "    </event>\n"
                + "</events>");
        // publishing events
        publishEvents("DAS_JMS_TEST", null, "activemq", "text", messageList);

        List<String> expected = new ArrayList<>(1);
        expected.add("John");
        expected.add("Johny");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(eventCount.intValue(), 2, "Event count should be equal to two.");
        AssertJUnit.assertEquals("JMS Source expected input not received", expected, receivedEventNameList);
        siddhiManager.shutdown();
    }

    /**
     * Test the Connection Unavailable Exception
     */
    @Test
    public void testJMSTopicSource5() throws InterruptedException {
        log.info("Test with connection unavailable exception");

        UnitTestAppender appender = new UnitTestAppender("UnitTestAppender", null);
        final Logger logger = (Logger) LogManager.getRootLogger();
        logger.setLevel(Level.ALL);
        logger.addAppender(appender);
        appender.start();
        
        receivedEventNameList = new ArrayList<>(2);

        // starting the ActiveMQ broker
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" + "@source(type='jms', @map(type='xml'),"
                + "factory.initial='org.apache.mb.jndi.MBInitialContextFactory', "
                + "provider.url='vm://localhost',"
                + "destination='DAS_JMS_TEST', "
                + "connection.factory.type='topic',"
                + "connection.factory.jndi.name='TopicConnectionFactory',"
                + "transport.jms.SubscriptionDurable='true', "
                + "transport.jms.DurableSubscriberClientID='wso2dasclient1'"
                + ")"
                + "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;");
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        executionPlanRuntime.start();
        List<String> messageList = new ArrayList<>(2);
        messageList.add("<events>\n"
                + "    <event>\n"
                + "        <name>John</name>\n"
                + "        <age>22</age>\n"
                + "        <country>US</country>\n"
                + "    </event>\n"
                + "</events>");
        Thread.sleep(1000);
        AssertJUnit.assertTrue(((UnitTestAppender) logger.getAppenders().
                get("UnitTestAppender")).getMessages().contains("Exception in starting the JMS receiver for stream"));
        siddhiManager.shutdown();
    }

    /**
     * Test for configure the JMS source to connect to an ActiveMQ topic and receive messages,
     * when message is an instanceof Map.
     */
    @Test
    public void testJMSTopicSource6() throws InterruptedException {
        AtomicInteger eventCount = new AtomicInteger(0);
        receivedEventNameList = new ArrayList<>(2);

        // starting the ActiveMQ broker
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" + "@source(type='jms', @map(type='keyvalue'), "
                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                + "provider.url='vm://localhost'," + "destination='DAS_JMS_TEST', "
                + "connection.factory.type='topic',"
                + "connection.factory.jndi.name='TopicConnectionFactory',"
                + "transport.jms.SubscriptionDurable='false', "
                + "transport.jms.DurableSubscriberClientID='wso2dasclient1'"
                + ")"
                + "define stream inputStream (name string, age string, country string);";
        String query = ("@info(name = 'query1') "
                + "from inputStream "
                + "select *  "
                + "insert into outputStream;");
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
        List<String> messageList = new ArrayList<String>(2);
        messageList.add("name(name):John\nage(age):23\ncountry(country):us");
        messageList.add("name(name):Mike\nage(age):23\ncountry(country):us");
        // publishing events
        publishEvents("DAS_JMS_TEST", "queue", "activemq", "map", messageList);
        List<String> expected = new ArrayList<>(2);
        expected.add("John");
        expected.add("Mike");
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertEquals(eventCount.intValue(), 2, "Event count should be equal to two.");
        AssertJUnit.assertEquals("JMS Source expected input not received", expected, receivedEventNameList);
        siddhiManager.shutdown();
    }

    @Test
    public void testJMSTopicSource7() throws InterruptedException {
        AtomicInteger eventCount = new AtomicInteger(0);
        receivedEventNameList = new ArrayList<>(2);

        // starting the ActiveMQ broker
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition =
                "@source(type='jms',"
                        + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                        + "provider.url='vm://localhost',"
                        + "destination='DAS_JMS_TEST', "
                        + "connection.factory.type='topic',"
                        + "connection.factory.jndi.name='TopicConnectionFactory',"
                        + "transport.jms.SubscriptionDurable='false', "
                        + "transport.jms.DurableSubscriberClientID='wso2dasclient1',"
                        + "@map(type='xml',@attributes(name='trp:JMS_DESTINATION',"
                        + "age='age',country='country')))"
                        + "define stream inputStream (name string, age string, country string);";
        String query = ("@info(name = 'query1') "
                + "from inputStream " + "select *  "
                + "insert into outputStream;");
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });

        executionPlanRuntime.start();
        List<String> messageList = new ArrayList<String>(3);
        messageList.add("<event>\n"
                + "        <name>John</name>\n"
                + "        <age>22</age>\n"
                + "        <country>US</country>\n"
                + "    </event>\n");
        // publishing events
        publishEvents("DAS_JMS_TEST", "queue", "activemq", "text", messageList);
        List<String> expected = new ArrayList<>(2);
        expected.add("DAS_JMS_TEST");
        SiddhiTestHelper.waitForEvents(waitTime, 1, eventCount, timeout);
        Assert.assertEquals(eventCount.intValue(), 1, "Event count should be equal to one.");
        AssertJUnit.assertEquals("JMS Source expected input not received", expected, receivedEventNameList);
        siddhiManager.shutdown();
    }

    private void publishEvents(String topicName, String queueName, String broker, String format, String filePath)
            throws InterruptedException {
        JMSClient jmsClient = new JMSClient();
        jmsClient.sendJMSEvents(filePath, topicName, queueName, format, broker, PROVIDER_URL);
    }

    private void publishEvents(String topicName, String queueName, String broker, String format,
            List<String> messageList) throws InterruptedException {
        JMSClient jmsClient = new JMSClient();
        jmsClient.sendJMSEvents(messageList, topicName, queueName, format, broker, PROVIDER_URL);
    }
}
