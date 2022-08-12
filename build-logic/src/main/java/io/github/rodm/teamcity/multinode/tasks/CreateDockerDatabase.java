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

import com.github.rodm.teamcity.internal.DockerTask;
import io.github.rodm.teamcity.multinode.internal.CreateDatabaseContainerAction;
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
        WorkQueue queue = executor.classLoaderIsolation(spec -> spec.getClasspath().from(getClasspath()));
        queue.submit(CreateDatabaseContainerAction.class, params -> {
            params.getImageName().set(getImageName());
            params.getContainerName().set(getContainerName());
            params.getUsername().set(getUsername());
            params.getPassword().set(getPassword());
        });
        queue.await();
    }
}
