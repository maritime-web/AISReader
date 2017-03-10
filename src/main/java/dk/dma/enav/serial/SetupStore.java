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

import com.google.gson.Gson;
import dk.dma.enav.serial.types.SerialSetup;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by oliver on 3/10/17.
 */
public class SetupStore {
    private static File aisSerialSetup = new File("config.json");

    private static FileWriter setupWriter;

    private static SetupStore instance;

    private static Gson gson = new Gson();

    public static SetupStore getInstance() {
        if (instance == null) {
            instance = new SetupStore();
        }
        return instance;
    }

    private SetupStore() {
    }

    public boolean setupExists() {
        return aisSerialSetup.exists();
    }

    public synchronized void writeSetup(SerialSetup serialSetup) throws IOException {
        if (setupWriter == null) {
            setupWriter = new FileWriter(aisSerialSetup);
        }
        String setupJSON = gson.toJson(serialSetup);
        setupWriter.write(setupJSON);
        setupWriter.close();
    }

    public SerialSetup getSerialSetup() throws IOException {
        String setup = FileUtils.readFileToString(aisSerialSetup, Charset.defaultCharset());
        SerialSetup serialSetup = gson.fromJson(setup, SerialSetup.class);
        return serialSetup;
    }
}
