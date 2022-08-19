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
package io.github.rodm.teamcity.multinode.internal;

import com.github.rodm.teamcity.BaseTeamCityEnvironment;
import com.github.rodm.teamcity.internal.DefaultTeamCityEnvironments;
import io.github.rodm.teamcity.multinode.DatabaseConfiguration;
import io.github.rodm.teamcity.multinode.MultiNodeEnvironment;
import io.github.rodm.teamcity.multinode.NodeConfiguration;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public class DefaultMultiNodeEnvironment extends BaseTeamCityEnvironment implements MultiNodeEnvironment {

    private final Property<String> serverImage;
    private final Property<String> agentImage;
    private final Property<String> serverName;
    private final Property<String> agentName;

    private DatabaseConfiguration database;
    private final NamedDomainObjectContainer<NodeConfiguration> nodes;

    @Inject
    public DefaultMultiNodeEnvironment(String name, DefaultTeamCityEnvironments environments, ObjectFactory objects) {
        super(name, environments, objects);
        this.serverImage = objects.property(String.class).convention("jetbrains/teamcity-server");
        this.agentImage = objects.property(String.class).convention("jetbrains/teamcity-agent");
        this.serverName = objects.property(String.class).convention("teamcity-server");
        this.agentName = objects.property(String.class).convention("teamcity-agent");

        NamedDomainObjectFactory<NodeConfiguration> factory = n ->
                objects.newInstance(DefaultNodeConfiguration.class, n);
        nodes = objects.domainObjectContainer(NodeConfiguration.class, factory);
    }

    public String getServerImage() {
        return getServerImageProperty().get();
    }

    public void setServerImage(String serverImage) {
        validateImage(serverImage, "serverImage");
        this.serverImage.set(serverImage);
    }

    public Provider<String> getServerImageProperty() {
        return gradleProperty(propertyName("serverImage")).orElse(serverImage);
    }

    public String getAgentImage() {
        return getAgentImageProperty().get();
    }

    public void setAgentImage(String agentImage) {
        validateImage(agentImage, "agentImage");
        this.agentImage.set(agentImage);
    }

    public Provider<String> getAgentImageProperty() {
        return gradleProperty(propertyName("agentImage")).orElse(agentImage);
    }

    public String getServerName() {
        return getServerNameProperty().get();
    }

    public void setServerName(String serverName) {
        this.serverName.set(serverName);
    }

    public Provider<String> getServerNameProperty() {
        return gradleProperty(propertyName("serverName")).orElse(serverName);
    }

    public String getAgentName() {
        return getAgentNameProperty().get();
    }

    public void setAgentName(String agentName) {
        this.agentName.set(agentName);
    }

    public Provider<String> getAgentNameProperty() {
        return gradleProperty(propertyName("agentName")).orElse(agentName);
    }
    
    public void database(Action<DatabaseConfiguration> configuration) {
        if (database == null) {
            database = ((ExtensionAware) this).getExtensions().create(DatabaseConfiguration.class, "database", DefaultDatabaseConfiguration.class);
        }
        configuration.execute(database);
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public void nodes(Action<NamedDomainObjectContainer<NodeConfiguration>> configuration) {
        configuration.execute(nodes);
    }

    @Override
    public NamedDomainObjectContainer<NodeConfiguration> getNodes() {
        return nodes;
    }

    public String createDatabaseTaskName() {
        return "create" + capitalize(getName()) + "Database";
    }

    public String startDatabaseTaskName() {
        return "start" + capitalize(getName()) + "Database";
    }

    public String stopDatabaseTaskName() {
        return "stop" + capitalize(getName()) + "Database";
    }

    public String configureDatabaseTaskName() {
        return "configure" + capitalize(getName()) + "Database";
    }

    public String startNodeTaskName(String nodeName) {
        return "start" + capitalize(getName()) + capitalize(nodeName) + "Server";
    }

    public String stopNodeTaskName(String nodeName) {
        return "stop" + capitalize(getName()) + capitalize(nodeName) + "Server";
    }

    private String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private void validateImage(String image, String property) {
        if (image.contains(":")) {
            throw new InvalidUserDataException(property + " must not include a tag.");
        }
    }
}
