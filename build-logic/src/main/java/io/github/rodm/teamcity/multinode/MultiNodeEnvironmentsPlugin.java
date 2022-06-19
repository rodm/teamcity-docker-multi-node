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
package io.github.rodm.teamcity.multinode;

import com.github.rodm.teamcity.BaseTeamCityEnvironment;
import com.github.rodm.teamcity.TeamCityEnvironment;
import com.github.rodm.teamcity.TeamCityPluginExtension;
import com.github.rodm.teamcity.TeamCityVersion;
import com.github.rodm.teamcity.internal.DefaultTeamCityEnvironments;
import com.github.rodm.teamcity.internal.DisablePluginAction;
import com.github.rodm.teamcity.internal.DockerInspectAction;
import com.github.rodm.teamcity.internal.EnablePluginAction;
import io.github.rodm.teamcity.multinode.internal.DefaultMultiNodeEnvironment;
import io.github.rodm.teamcity.multinode.internal.DefaultNodeConfiguration;
import io.github.rodm.teamcity.multinode.tasks.CreateDockerDatabase;
import io.github.rodm.teamcity.multinode.tasks.StartDockerDatabase;
import io.github.rodm.teamcity.multinode.tasks.StopDockerDatabase;
import com.github.rodm.teamcity.tasks.Deploy;
import com.github.rodm.teamcity.tasks.ServerPlugin;
import com.github.rodm.teamcity.tasks.StartDockerAgent;
import com.github.rodm.teamcity.tasks.StartDockerServer;
import com.github.rodm.teamcity.tasks.StopDockerAgent;
import com.github.rodm.teamcity.tasks.StopDockerServer;
import com.github.rodm.teamcity.tasks.Undeploy;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.rodm.teamcity.TeamCityPlugin.TEAMCITY_GROUP;
import static com.github.rodm.teamcity.TeamCityServerPlugin.SERVER_PLUGIN_TASK_NAME;
import static com.github.rodm.teamcity.TeamCityVersion.VERSION_2018_2;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class MultiNodeEnvironmentsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("com.github.rodm.teamcity-environments");

        TeamCityPluginExtension extension = project.getExtensions().getByType(TeamCityPluginExtension.class);

        ObjectFactory objects = project.getObjects();
        DefaultTeamCityEnvironments environments = (DefaultTeamCityEnvironments) extension.getEnvironments();
        NamedDomainObjectFactory<MultiNodeEnvironment> factory = name ->
                objects.newInstance(DefaultMultiNodeEnvironment.class, name, environments, objects);
        environments.registerFactory(MultiNodeEnvironment.class, factory);

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                DefaultTeamCityEnvironments environments = (DefaultTeamCityEnvironments) extension.getEnvironments();
                NamedDomainObjectContainer<TeamCityEnvironment> container = environments.getEnvironments();
                container.withType(MultiNodeEnvironment.class).all(environment -> {
                    configureDeploymentTasks(project, (BaseTeamCityEnvironment) environment);
                    configureDatabaseTasks(project, (DefaultMultiNodeEnvironment) environment);
                    configureDockerTasks(project, (DefaultMultiNodeEnvironment) environment);
                });
            }

            private void configureDeploymentTasks(Project project, BaseTeamCityEnvironment environment) {
                final TaskContainer tasks = project.getTasks();
                final TaskProvider<Deploy> deployPlugin = tasks.register(environment.deployTaskName(), Deploy.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.getPlugins().from(environment.getPlugins());
                    task.getPluginsDir().set(project.file(environment.getPluginsDirProperty()));
                    task.dependsOn(tasks.named(ASSEMBLE_TASK_NAME));
                });

                final TaskProvider<Undeploy> undeployPlugin = tasks.register(environment.undeployTaskName(), Undeploy.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.getPlugins().from(environment.getPlugins());
                    task.getPluginsDir().set(project.file(environment.getPluginsDirProperty()));
                });

                if (TeamCityVersion.version(environment.getVersion()).equalOrGreaterThan(VERSION_2018_2)) {
                    final File dataDir = project.file(environment.getDataDirProperty().get());
                    deployPlugin.configure(task -> {
                        Set<File> plugins = ((FileCollection) environment.getPlugins()).getFiles();
                        List<String> disabledPlugins = new ArrayList<>();
                        task.doFirst(new DisablePluginAction(project.getLogger(), dataDir, plugins, disabledPlugins));
                        task.doLast(new EnablePluginAction(project.getLogger(), dataDir, plugins, disabledPlugins));
                    });
                    undeployPlugin.configure(task -> {
                        Set<File> plugins = ((FileCollection) environment.getPlugins()).getFiles();
                        task.doFirst(new DisablePluginAction(project.getLogger(), dataDir, plugins, new ArrayList<>()));
                    });
                }

                tasks.withType(ServerPlugin.class, task -> {
                    if (((FileCollection) environment.getPlugins()).isEmpty()) {
                        environment.plugins(tasks.named(SERVER_PLUGIN_TASK_NAME));
                    }
                });
            }

            private void configureDatabaseTasks(Project project, DefaultMultiNodeEnvironment environment) {
                final TaskContainer tasks = project.getTasks();
                final String name = capitalize(environment.getName());

                DatabaseConfiguration database = environment.getDatabase();
                tasks.register("configure" + name + "Database", Copy.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.into(environment.getDataDirProperty().map(path -> path + "/lib/jdbc"));
                    task.from(database.getDriver());
                });
                tasks.register("create" + name + "Database", CreateDockerDatabase.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.getImageName().set(database.getImage());
                    task.getContainerName().set(database.getName());
                });
                tasks.register("start" + name + "Database", StartDockerDatabase.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.getContainerName().set(database.getName());
                });
                tasks.register("stop" + name + "Database", StopDockerDatabase.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.getContainerName().set(database.getName());
                });
            }

            private void configureDockerTasks(Project project, DefaultMultiNodeEnvironment environment) {
                final TaskContainer tasks = project.getTasks();
                NamedDomainObjectContainer<NodeConfiguration> nodes = environment.getNodes();
                nodes.all(node -> {
                    configureNodeTasks(project, environment, (DefaultNodeConfiguration) node);
                });

                NodeConfiguration mainNode = getMainNode(nodes).orElseThrow(() -> new GradleException("No main node"));
                Provider<String> mainNodeContainerName = environment.getServerNameProperty().map(cn -> cn + "-" + mainNode.getName());

                tasks.register(environment.startAgentTaskName(), StartDockerAgent.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.getVersion().set(environment.getVersion());
                    task.getDataDir().set(environment.getDataDirProperty());
                    task.getAgentOptions().set(environment.getAgentOptionsProvider());
                    task.getImageName().set(environment.getAgentImageProperty());
                    task.getContainerName().set(environment.getAgentNameProperty());
                    task.getServerContainerName().set(mainNodeContainerName);
                    task.doFirst(new DockerInspectAction());
//                    task.mustRunAfter(tasks.named(environment.startServerTaskName()));
                });

                tasks.register(environment.stopAgentTaskName(), StopDockerAgent.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.getContainerName().set(environment.getAgentName());
                });
            }

            private void configureNodeTasks(Project project, DefaultMultiNodeEnvironment environment, DefaultNodeConfiguration node) {
                final TaskContainer tasks = project.getTasks();
                final String name = capitalize(environment.getName());
                Provider<String> containerName = environment.getServerNameProperty().map(cn -> cn + "-" + node.getName());
                String startServerTaskName = "start" + name + capitalize(node.getName()) + "Server";
                tasks.register(startServerTaskName, StartDockerServer.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.getVersion().set(environment.getVersion());
                    task.getDataDir().set(environment.getDataDirProperty());
                    task.getLogsDir().set(environment.getDataDirProperty().map(path -> path + "/" + node.getName() + "/logs"));
                    task.getServerOptions().set(node.getServerOptionsProvider());
                    task.getImageName().set(environment.getServerImageProperty());
                    task.getContainerName().set(containerName);
                    task.getPort().set(node.getPort());
                    task.doFirst(t -> project.mkdir(environment.getDataDir()));
//                            task.dependsOn(tasks.named(environment.deployTaskName()));
                });

                String stopServerTaskName = "stop" + name + capitalize(node.getName()) + "Server";
                tasks.register(stopServerTaskName, StopDockerServer.class, task -> {
                    task.setGroup(TEAMCITY_GROUP);
                    task.getContainerName().set(containerName);
//                            task.finalizedBy(tasks.named(environment.undeployTaskName()));
                });
            }

            private Optional<NodeConfiguration> getMainNode(NamedDomainObjectContainer<NodeConfiguration> nodes) {
                String nodeId = "teamcity.server.nodeId";
                return nodes.stream()
                        .filter(node -> !((DefaultNodeConfiguration) node).getServerOptionsProvider().get().contains(nodeId))
                        .findFirst();
            }

            private String capitalize(String name) {
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            }
        });
    }
}
