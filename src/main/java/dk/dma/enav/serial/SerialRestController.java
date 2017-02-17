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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Created by Oliver on 01-12-2016.
 *
 * Defines a set of REST endpoints
 */
@RestController
@RequestMapping("/rest")
public class SerialRestController {

    @Autowired
    private SerialFatJarRouter serialFatJarRouter;

    // endpoint for configuration of the device
    @RequestMapping(path = "/setup", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setupSerial(@RequestBody SerialSetup setup) {
        try {
            serialFatJarRouter.changeConfig(setup);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    // endpoint for shutting down the device
    @RequestMapping(path = "/shutdown", method = RequestMethod.GET)
    public ResponseEntity shutdown(@RequestParam(name = "areYouSure") String areYouSure) {
        if (areYouSure.equals("yes")) {
            try {
                serialFatJarRouter.getContext().stop();
                Runtime.getRuntime().exec("sudo shutdown -h now");
                return ResponseEntity.ok().build();
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Device could not be shut down");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
