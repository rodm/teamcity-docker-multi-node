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

import com.github.rodm.teamcity.internal.DockerOperations;
import com.github.rodm.teamcity.internal.DockerTask;
import org.gradle.api.tasks.TaskAction;

public abstract class StartDockerDatabase extends DockerTask {

    @TaskAction
    void startDatabase() {
        DockerOperations dockerOperations = new DockerOperations();

        String containerId = getContainerName().get();
        if (dockerOperations.isContainerRunning(containerId)) {
            getLogger().info("Database container '{}' is already running", containerId);
            return;
        }

        dockerOperations.startContainer(getContainerName().get());
        getLogger().info("Started database container");
    }
}
