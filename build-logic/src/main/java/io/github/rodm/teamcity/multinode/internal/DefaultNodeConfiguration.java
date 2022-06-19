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

import io.github.rodm.teamcity.multinode.NodeConfiguration;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultNodeConfiguration implements NodeConfiguration {

    private static final List<String> DEFAULT_SERVER_OPTIONS = Collections.unmodifiableList(
            Arrays.asList(
                    "-Dteamcity.development.mode=true",
                    "-Dteamcity.development.shadowCopyClasses=true",
                    "-Dteamcity.superUser.token.saveToFile=true",
                    "-Dteamcity.kotlinConfigsDsl.generateDslDocs=false"
            ));

    private final String name;
    private final Property<String> port;
    private final ListProperty<String> serverOptions;

    @Inject
    public DefaultNodeConfiguration(String name, ObjectFactory factory) {
        this.name = name;
        this.port = factory.property(String.class);
        this.serverOptions = factory.listProperty(String.class);
        this.serverOptions.addAll(DEFAULT_SERVER_OPTIONS);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPort() {
        return getPortProperty().get();
    }

    @Override
    public void setPort(String port) {
        this.port.set(port);
    }

    public Property<String> getPortProperty() {
        return port;
    }

    public Object getServerOptions() {
        return getServerOptionsProvider().get();
    }

    public void setServerOptions(Object options) {
        this.serverOptions.empty();
        if (options instanceof List) {
            this.serverOptions.addAll((Iterable<? extends String>) options);
        } else {
            this.serverOptions.add(options.toString());
        }
    }

    public void serverOptions(String... options) {
        this.serverOptions.addAll(options);
    }

    public Provider<String> getServerOptionsProvider() {
        return asStringProvider(serverOptions);
    }

    private Provider<String> asStringProvider(ListProperty<String> options) {
        return options.map(strings -> String.join(" ", strings));
    }
}
