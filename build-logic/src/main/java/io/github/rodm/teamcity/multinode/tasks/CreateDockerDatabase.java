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

import com.github.rodm.teamcity.docker.ContainerConfiguration;
import com.github.rodm.teamcity.docker.CreateContainerAction;
import com.github.rodm.teamcity.docker.DockerTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

public abstract class CreateDockerDatabase extends DockerTask {

    private final WorkerExecutor executor;

    @Inject
    public CreateDockerDatabase(WorkerExecutor executor) {
        this.executor = executor;
    }

    @Input
    public abstract Property<String> getImageName();
    
    @Input
    public abstract Property<String> getUsername();

    @Input
    public abstract Property<String> getPassword();

    @TaskAction
    void createDatabase() {
        ContainerConfiguration configuration = ContainerConfiguration.builder()
                .image(getImageName().get())
                .name(getContainerName().get())
                .environment("MYSQL_DATABASE", "teamcity")
                .environment("MYSQL_USER", getUsername().get())
                .environment("MYSQL_PASSWORD", getPassword().get())
                .bindPort("3306", "3306");

        WorkQueue queue = executor.classLoaderIsolation(spec -> spec.getClasspath().from(getClasspath()));
        queue.submit(CreateContainerAction.class, params -> {
            params.getConfiguration().set(configuration);
            params.getDescription().set("TeamCity Database");
        });
        queue.await();
    }
}
