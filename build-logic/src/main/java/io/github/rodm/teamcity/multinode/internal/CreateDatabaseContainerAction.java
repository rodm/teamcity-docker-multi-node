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

import com.github.rodm.teamcity.docker.ContainerConfiguration;
import com.github.rodm.teamcity.docker.DockerOperations;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import static com.github.rodm.teamcity.docker.DockerOperations.IMAGE_NOT_AVAILABLE;
import static java.lang.String.format;

public abstract class CreateDatabaseContainerAction implements WorkAction<CreateDatabaseContainerAction.CreateContainerParameters> {

    private static final Logger LOGGER = Logging.getLogger(CreateDatabaseContainerAction.class);

    public interface CreateContainerParameters extends WorkParameters {
        Property<String> getImageName();
        Property<String> getContainerName();
        Property<String> getUsername();
        Property<String> getPassword();
    }

    @Override
    public void execute() {
        final CreateContainerParameters parameters = getParameters();
        final DockerOperations dockerOperations = new DockerOperations();
        
        String image = parameters.getImageName().get();
        if (!dockerOperations.isImageAvailable(image)) {
            throw new GradleException(format(IMAGE_NOT_AVAILABLE, image));
        }

        String containerId = parameters.getContainerName().get();
        if (dockerOperations.isContainerAvailable(containerId)) {
            LOGGER.info("Database container {} already exists", containerId);
            return;
        }

        ContainerConfiguration configuration = ContainerConfiguration.builder()
                .image(image)
                .name(containerId)
                .environment("MYSQL_DATABASE", "teamcity")
                .environment("MYSQL_USER", parameters.getUsername().get())
                .environment("MYSQL_PASSWORD", parameters.getPassword().get())
                .bindPort("3306", "3306");

        String id = dockerOperations.createContainer(configuration);
        LOGGER.info("Created database container with id: {}", id);
    }
}
