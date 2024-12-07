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

import io.github.rodm.teamcity.multinode.MultiNodeEnvironment

plugins {
    id ("io.github.rodm.teamcity-multinode-environments")
}

repositories {
    mavenCentral()
}

val database by configurations.creating
val teamcityPlugins by configurations.creating

dependencies {
    database ("com.mysql:mysql-connector-j:8.4.0")
    teamcityPlugins (project(path = ":teamcity-plugin", configuration = "plugin"))
}

teamcity {
    environments {
        baseDataDir = "$rootDir/data"

        register("teamcity2024.12", MultiNodeEnvironment::class.java) {
            version = "2024.12"
            plugins = configurations["teamcityPlugins"]
            //agentTag = "2024.12-linux-sudo"
            database {
                image = "mysql/mysql-server:8.0"
                name = "tc-2024.12-db"
                url = "jdbc:mysql://localhost:3306/teamcity"
                username = "teamcity"
                password = "teamcity"
                driver = configurations["database"]
            }
            nodes {
                register("node1") {
                    port = "8211"
                }
                register("node2") {
                    port = "8311"
                    serverOptions = "-Dteamcity.server.nodeId=node2 -Dteamcity.server.rootURL=http://localhost:8311/"
                }
            }
        }
    }
}
