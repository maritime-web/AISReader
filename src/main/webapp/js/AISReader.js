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

/**
 * Created by Oliver on 02-12-2016.
 */
$(document).ready(function () {
    // click listener for the submit button
    $("#submit").click(function () {
        var baudRate = $("#baud").val();
        var dataBits = $("#databits").val();
        var stopBits = $("#stopbits").val();
        var sendRate = $("#send").val();
        var webTarget = $("#target").val();

        baudRate = !baudRate ? 38400 : baudRate;
        dataBits = !dataBits ? 8 : dataBits;
        stopBits = !stopBits ? 1 : stopBits;
        sendRate = !sendRate ? "5m" : sendRate;

        if (!webTarget) {
            alert("Web Target cannot be empty!");
        } else {
            // object for holding the setup to be sent
            var request = {
                baud: baudRate,
                dataBits: dataBits,
                stopBits: stopBits,
                sendRate: sendRate,
                target: webTarget
            };
            // send the setup using a PUT request to the server
            $.ajax({
                type: "PUT",
                url: "rest/setup",
                data: JSON.stringify(request),
                contentType: "application/json",
                success: function () {
                    alert("Setup successfully posted!");
                },
                error: function () {
                    alert("Something went wrong");
                }
            });
        }
    });
    // click listener for the shutdown button
    $("#shutdown").click(function () {
        var confirmed = confirm("Are you sure you want to shut down?");
        if (confirmed) {
            $.ajax({
               type: "GET",
                url: "rest/shutdown",
                data: {areYouSure: "yes"},
                error: function () {
                    alert("AISReader could not be shut down");
                }
            });
        }
    });
});