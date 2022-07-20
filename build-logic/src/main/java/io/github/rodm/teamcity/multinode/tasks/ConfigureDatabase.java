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
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
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

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

public abstract class ConfigureDatabase extends DefaultTask {

    private final FileSystemOperations fileSystemOperations;

    @Inject
    public ConfigureDatabase(FileSystemOperations fileSystemOperations) {
        this.fileSystemOperations = fileSystemOperations;
    }

    @Input
    public abstract Property<String> getContainerName();

    @Input
    public abstract Property<String> getDatabaseUrl();

    @Input
    public abstract Property<String> getUsername();

    @Input
    public abstract Property<String> getPassword();

    @InputFiles
    public abstract ConfigurableFileCollection getDriver();

    @OutputFile
    public abstract RegularFileProperty getDatabaseProperties();

    @OutputDirectory
    public abstract DirectoryProperty getDriverDir();

    @TaskAction
    void configureDatabase() {
        DockerOperations dockerOperations = new DockerOperations();
        String databaseIpAddress = dockerOperations.getIpAddress(getContainerName().get());
        String databaseUrl = getDatabaseUrl().get().replace("localhost", databaseIpAddress);
        File file = getDatabaseProperties().get().getAsFile();
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)){
            Properties props = new Properties();
            props.setProperty("connectionProperties.user", getUsername().get());
            props.setProperty("connectionProperties.password", getPassword().get());
            props.setProperty("connectionUrl", databaseUrl);
            props.store(writer, null);
        }
        catch (IOException e) {
            throw new GradleException("Failed to write properties file", e);
        }

        fileSystemOperations.copy(copySpec -> {
            copySpec.from(getDriver());
            copySpec.into(getDriverDir());
        });
    }
}
