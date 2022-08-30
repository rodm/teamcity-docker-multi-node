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

import com.github.rodm.teamcity.docker.DockerOperations;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

public abstract class ConfigureDatabaseContainerAction implements WorkAction<ConfigureDatabaseContainerAction.ConfigureContainerParameters> {

    public interface ConfigureContainerParameters extends WorkParameters {
        Property<String> getContainerName();
        Property<String> getDatabaseUrl();
        Property<String> getUsername();
        Property<String> getPassword();
        RegularFileProperty getDatabaseProperties();
    }

    @Override
    public void execute() {
        final ConfigureContainerParameters parameters = getParameters();
        final DockerOperations dockerOperations = new DockerOperations();

        String databaseIpAddress = dockerOperations.getIpAddress(parameters.getContainerName().get());
        String databaseUrl = parameters.getDatabaseUrl().get().replace("localhost", databaseIpAddress);
        File file = parameters.getDatabaseProperties().get().getAsFile();
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)){
            Properties props = new Properties();
            props.setProperty("connectionProperties.user", parameters.getUsername().get());
            props.setProperty("connectionProperties.password", parameters.getPassword().get());
            props.setProperty("connectionUrl", databaseUrl);
            props.store(writer, null);
        }
        catch (IOException e) {
            throw new GradleException("Failed to write properties file", e);
        }
    }
}
