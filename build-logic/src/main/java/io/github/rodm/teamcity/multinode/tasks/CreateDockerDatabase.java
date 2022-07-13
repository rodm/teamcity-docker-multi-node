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
package io.github.rodm.teamcity.multinode.tasks;

import com.github.rodm.teamcity.internal.ContainerConfiguration;
import com.github.rodm.teamcity.internal.DockerOperations;
import com.github.rodm.teamcity.internal.DockerTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import static java.lang.String.format;

public abstract class CreateDockerDatabase extends DockerTask {

    @Input
    public abstract Property<String> getImageName();

    @TaskAction
    void createDatabase() {
        DockerOperations dockerOperations = new DockerOperations();

        String image = getImageName().get();
        if (!dockerOperations.isImageAvailable(image)) {
            throw new GradleException(format(IMAGE_NOT_AVAILABLE, image));
        }

        ContainerConfiguration configuration = ContainerConfiguration.builder()
                .image(image)
                .name(getContainerName().get())
                .environment("MYSQL_DATABASE", "teamcity")
                .environment("MYSQL_USER", "teamcity")
                .environment("MYSQL_PASSWORD", "teamcity")
                .bindPort("3306", "3306");

        String id = dockerOperations.createContainer(configuration);
        getLogger().info("Created database container with id: {}", id);
    }
}
