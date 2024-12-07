/*
 * Copyright 2022 Rod MacKenzie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id ("java")
    id ("io.github.rodm.teamcity-server") version "1.5.5"
}

group = "com.github.rodm.teamcity"
version = "1.0-SNAPSHOT"

val vendorName by extra("rodm")
val teamcityVersion by extra("2020.1")

teamcity {
    version = teamcityVersion

    server {
        archiveName = "teamcity-plugin.zip"
        descriptor {
            name = "multi-node-plugin"
            displayName = "Example Plugin"
            description = "Example multi-node plugin"
            version = project.version as String
            vendorName = extra["vendorName"] as String
            vendorUrl = "https://github.com/rodm"
            email = "rod.n.mackenzie@gmail.com"
            useSeparateClassloader = true

            parameters {
                parameter ("param", "example server value")
            }
        }
    }
}
