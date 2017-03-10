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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Created by Oliver on 02-12-2016.
 */
@Getter
public class MessageWithTimeStamp {

    // the AIS message as a byte array
    private byte[] message;
    // the time the message was received
    private String timeStamp;

    public MessageWithTimeStamp(byte[] message) {
        this.message = message;
        this.timeStamp = DateTime.now(DateTimeZone.UTC).toString();
    }
}
