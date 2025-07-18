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
    database ("com.mysql:mysql-connector-j:9.3.0")
//    database ("org.postgresql:postgresql:42.7.5")
    teamcityPlugins (project(path = ":teamcity-plugin", configuration = "plugin"))
}

teamcity {
    environments {
        baseDataDir = "$rootDir/data"

        register("teamcity", MultiNodeEnvironment::class.java) {
            version = "2025.03.2"
            plugins = configurations["teamcityPlugins"]
            //agentTag = "2025.03.2-linux-sudo"
            database {
                useMySQL()
//                useMySQL {
//                    image = "mysql:9.3"
//                    url = "jdbc:mysql://localhost:3306/teamcity"
//                }
//                usePostgreSQL()
//                usePostgreSQL {
//                    image = 'postgres:17.5'
//                    url = 'jdbc:postgresql://localhost:5432/teamcity'
//                }

                name = "teamcity-database"
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
