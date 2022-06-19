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
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;

public abstract class CreateDockerDatabase extends DockerTask {

    @Inject
    public CreateDockerDatabase(ExecOperations execOperations) {
        super(execOperations);
    }

    @Input
    public abstract Property<String> getImageName();

    @Override
    protected void configure(ExecSpec execSpec) {
        execSpec.args("create");
        execSpec.args("--name", getContainerName().get());
        execSpec.args("-p", "3306:3306");
        execSpec.args("-e", "MYSQL_DATABASE=teamcity");
        execSpec.args("-e", "MYSQL_USER=teamcity");
        execSpec.args("-e", "MYSQL_PASSWORD=teamcity");
        execSpec.args(getImageName().get());
    }
}
