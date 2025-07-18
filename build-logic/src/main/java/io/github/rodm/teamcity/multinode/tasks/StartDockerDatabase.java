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
import com.github.rodm.teamcity.docker.StartContainerAction;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.util.Map;

public abstract class StartDockerDatabase extends DockerTask {

    private final WorkerExecutor executor;

    @Inject
    public StartDockerDatabase(WorkerExecutor executor) {
        this.executor = executor;
    }

    @Input
    public abstract Property<String> getImageName();

    @Input
    public abstract Property<String> getUsername();

    @Input
    public abstract Property<String> getPassword();

    @Input
    public abstract Property<String> getDataDir();

    @Input
    public abstract MapProperty<String, String> getEnvironmentVariables();

    @Input
    public abstract Property<String> getDatabasePath();

    @Input
    public abstract Property<Integer> getDatabasePort();

    @TaskAction
    void startDatabase() {
        String port = getDatabasePort().get().toString();
        ContainerConfiguration configuration = ContainerConfiguration.builder()
                .image(getImageName().get())
                .name(getContainerName().get())
                .bind(getDataDir().get(), getDatabasePath().get())
                .bindPort(port, port)
                .autoRemove();
        Map<String, String> variables = getEnvironmentVariables().get();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            configuration.environment(entry.getKey(), entry.getValue());
        }

        WorkQueue queue = executor.classLoaderIsolation(spec -> spec.getClasspath().from(getClasspath()));
        queue.submit(CreateContainerAction.class, params -> {
            params.getConfiguration().set(configuration);
            params.getDescription().set("TeamCity Database");
        });
        queue.await();

        queue.submit(StartContainerAction.class, params -> {
            params.getContainerName().set(getContainerName());
            params.getDescription().set("TeamCity Database");
        });
        queue.await();
    }
}
