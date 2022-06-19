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
package io.github.rodm.teamcity.multinode;

import com.github.rodm.teamcity.TeamCityEnvironment;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;

public interface MultiNodeEnvironment extends TeamCityEnvironment {

    String getServerImage();
    void setServerImage(String serverImage);

    String getAgentImage();
    void setAgentImage(String agentImage);

    String getServerName();
    void setServerName(String serverName);

    String getAgentName();
    void setAgentName(String agentName);

    void database(Action<DatabaseConfiguration> configuration);
    DatabaseConfiguration getDatabase();

    void nodes(Action<NamedDomainObjectContainer<NodeConfiguration>> configuration);
    NamedDomainObjectContainer<NodeConfiguration> getNodes();
}

