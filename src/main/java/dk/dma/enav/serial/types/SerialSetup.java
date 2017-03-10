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

package dk.dma.enav.serial.types;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Oliver on 02-12-2016.
 *
 * Object to hold a configuration for the serial connection and the REST client
 */
@Getter
@Setter
public class SerialSetup {

    // the baud rate
    private int baud;

    // number of data bits
    private int dataBits;

    // number of stop bits
    private int stopBits;

    // the rate that the device will send messages to web service
    private String sendRate;

    // the target URL to where the device should send messages
    private String target;

    // ID for database entry of the setup
    private String _id = "SerialSetup";

    // revision key for database entry
    private String _rev;

    // ID for being able to easier find an existing setup in the database
    public final static String ID = "SerialSetup";
}
