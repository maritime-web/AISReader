/*
 * Copyright 2017 Danish Maritime Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.dma.enav.serial;

import com.pi4j.io.serial.*;
import dk.dma.enav.serial.types.MessageWithTimeStamp;
import org.apache.camel.builder.RouteBuilder;
import org.lightcouch.CouchDbClient;
import dk.dma.enav.serial.types.SerialSetup;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Oliver on 01-12-2016.
 */
public class SerialRoute extends RouteBuilder {

    // if the serial route has been initialized
    private boolean initialized;

    // the serial connection
    private Serial serial;
    // the REST target to send messages to
    private WebTarget target;
    // the rate that the device will send messages to web service
    private String sendRate;
    // list to hold messages before they are sent
    private static List<MessageWithTimeStamp> messageBuffer;
    // the baud rate of the serial connection
    private Baud baud;
    // the number of data bits of the serial connection
    private DataBits dataBits;
    // the number of stop bits of the serial connection
    private StopBits stopBits;

    // ID for easier identification of this route
    private final String ID = "sender";

    // connection to the CouchDB database
    private CouchDbClient couchDbClient;

    public SerialRoute(List<MessageWithTimeStamp> oldBuffer, SerialSetup serialSetup, CouchDbClient couchDbClient) {
        init(serialSetup);
        messageBuffer.addAll(oldBuffer);
        this.couchDbClient = couchDbClient;
    }

    public SerialRoute(CouchDbClient couchDbClient) {
        this.couchDbClient = couchDbClient;
        // if the database contains a setup use it
        if (this.couchDbClient.contains(SerialSetup.ID)) {
            SerialSetup serialSetup = this.couchDbClient.find(SerialSetup.class, SerialSetup.ID);
            init(serialSetup);
        }
    }

    // function to be called when instantiating object to serial connection and REST client based on a configuration
    private void init(SerialSetup serialSetup) {
        Client client = ClientBuilder.newClient();
        String tempTarget = serialSetup.getTarget();
        this.target = client.target(tempTarget.startsWith("http://") ? tempTarget : "http://" + tempTarget);
        this.sendRate = serialSetup.getSendRate();
        this.messageBuffer = Collections.synchronizedList(new ArrayList<>());
        this.baud = Baud.getInstance(serialSetup.getBaud());
        this.dataBits = DataBits.getInstance(serialSetup.getDataBits());
        this.initialized = true;
        this.stopBits = StopBits.getInstance(serialSetup.getStopBits());
    }

    public String getID() {
        return ID;
    }

    // close the serial interface and return the content of the message buffer so it won't just be deleted
    public List<MessageWithTimeStamp> stop() throws IOException {
        serial.close();
        List<MessageWithTimeStamp> toBeReturned;
        synchronized (messageBuffer) {
            toBeReturned = new ArrayList<>(messageBuffer);
        }
        return toBeReturned;
    }

    // send all buffered messages before shutting down
    public void shutDown() throws IOException {
        List<MessageWithTimeStamp> messages;

        synchronized (messageBuffer) {
            messages = new ArrayList<>(messageBuffer);
        }

        send(messages);
    }

    // send a list of messages to the defined server
    private void send(List<MessageWithTimeStamp> messages) {
        // There is no need to send to the server if the list is empty
        if (!messages.isEmpty()) {
            log.info("Sending messages to server");
            String json = couchDbClient.getGson().toJson(messages);
            Response response = target.request().put(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE));
            int status = response.getStatus();
            if (status != 202) {
                log.error("Messages could not be sent. Code: " + status);
            } else {
                log.info("Messages successfully sent to server");
            }
        }
    }

    @Override
    public void configure() throws Exception {
        if (!initialized) {
            return;
        } else {
            // setup configuration for the serial connection
            serial = SerialFactory.createInstance();
            SerialConfig config = new SerialConfig();
            config.device(SerialPort.getDefaultPort()).baud(baud).dataBits(dataBits).parity(Parity.NONE).stopBits(stopBits)
                    .flowControl(FlowControl.NONE);
            // define listener for what should happen when a message is received on the serial port
            serial.addListener(event -> {
                try {
                    byte[] message = event.getBytes();
                    // if the message is empty or null don't do anything
                    if (message.length == 0 || message == null) {

                    } else {
                        MessageWithTimeStamp messageWithTimeStamp = new MessageWithTimeStamp(message);
                        messageBuffer.add(messageWithTimeStamp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            // open the serial connection using the configuration
            serial.open(config);

            // define route that sends buffered messages based on configured rate
            from("timer:sender?fixedRate=true&period=" + sendRate)
                    .id(ID)
                    .process(exchange -> {
                        List<MessageWithTimeStamp> messages;
                        // make sure that no elements are being written to the list while the contents of the buffer
                        // is being copied
                        synchronized (messageBuffer) {
                            messages = new ArrayList<>(messageBuffer);
                            messageBuffer.clear();
                        }

                        send(messages);
                    });
        }
    }


}
