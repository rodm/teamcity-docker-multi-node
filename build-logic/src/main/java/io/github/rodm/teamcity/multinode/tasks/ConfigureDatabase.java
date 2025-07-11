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

import com.github.rodm.teamcity.docker.DockerTask;
import io.github.rodm.teamcity.multinode.internal.ConfigureDatabaseContainerAction;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

public abstract class ConfigureDatabase extends DockerTask {

    private final WorkerExecutor executor;
    private final FileSystemOperations fileSystemOperations;

    @Inject
    public ConfigureDatabase(WorkerExecutor executor, FileSystemOperations fileSystemOperations) {
        this.executor = executor;
        this.fileSystemOperations = fileSystemOperations;
    }

    @Input
    public abstract Property<String> getDatabaseUrl();

    @Input
    public abstract Property<String> getUsername();

    @Input
    public abstract Property<String> getPassword();

    @Input
    public abstract Property<Boolean> getTestOnBorrow();

    @InputFiles
    public abstract ConfigurableFileCollection getDriver();

    @OutputFile
    public abstract RegularFileProperty getDatabaseProperties();

    @OutputDirectory
    public abstract DirectoryProperty getDriverDir();

    @TaskAction
    void configureDatabase() {
        WorkQueue queue = executor.classLoaderIsolation(spec -> spec.getClasspath().from(getClasspath()));
        queue.submit(ConfigureDatabaseContainerAction.class, params -> {
            params.getContainerName().set(getContainerName());
            params.getDatabaseUrl().set(getDatabaseUrl());
            params.getUsername().set(getUsername());
            params.getPassword().set(getPassword());
            params.getTestOnBorrow().set(getTestOnBorrow());
            params.getDatabaseProperties().set(getDatabaseProperties());
        });
        queue.await();

        fileSystemOperations.copy(copySpec -> {
            copySpec.from(getDriver());
            copySpec.into(getDriverDir());
        });
    }
}
