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

import dk.dma.enav.serial.types.SerialSetup;
import org.apache.camel.spring.boot.FatJarRouter;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver on 01-12-2016.
 *
 * The main class of the project
 */
@SpringBootApplication
public class SerialFatJarRouter extends FatJarRouter {

    // if the serial route has been configured
    private boolean initialized;

    private SetupStore setupStore;

    private SerialRoute serialRoute;


    public SerialFatJarRouter() throws Exception {
    }

    // called on startup
    @Override
    public void configure() throws Exception {
        setupStore = SetupStore.getInstance();
        // create a SerialRoute and add it to the camel context
        serialRoute = new SerialRoute(setupStore);

        getContext().addRoutes(serialRoute);

        // when termination signal has been sent to application call the shutDown() function of the serial route
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                serialRoute.shutDown();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }));
    }

    // change the configuration of the device
    public void changeConfig(SerialSetup setup) throws Exception {
        // if a configuration already exists in the database update it, else save it as a new document
        if (setupStore.setupExists()) {
            initialized = true;
            setupStore.writeSetup(setup);
        } else {
            setupStore.writeSetup(setup);
        }
        if (initialized) {
            // route must be removed and then added again to change the configuration
            List<String> buffer = serialRoute.stop();
            getContext().stopRoute(serialRoute.getID());
            boolean removed = getContext().removeRoute(serialRoute.getID());
            if (removed) {
                serialRoute = new SerialRoute(buffer, setup, setupStore);
                getContext().addRoutes(serialRoute);
            } else {
                log.error("Sender could not be reconfigured");
            }
        } else {
            serialRoute = new SerialRoute(new ArrayList<>(), setup, setupStore);
            getContext().addRoutes(serialRoute);
            initialized = true;
        }
    }
}
