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
package example.teamcity;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ExampleServerPlugin extends BuildServerAdapter {

    private static final Logger logger = Logger.getLogger("jetbrains.buildServer.SERVER");

    private final SBuildServer server;

    private final PluginDescriptor descriptor;

    public ExampleServerPlugin(SBuildServer server, PluginDescriptor descriptor) {
        this.server = server;
        this.descriptor = descriptor;
    }

    public void register() {
        server.addListener(this);
        String name = getClass().getSimpleName();
        logger.info(String.format("%s: Plugin name: %s, version: %s", name, descriptor.getPluginName(), descriptor.getPluginVersion()));
        logger.info(String.format("%s: Plugin parameter: param=%s", name, descriptor.getParameterValue("param")));
    }

    @Override
    public void agentRegistered(@NotNull SBuildAgent agent, long currentlyRunningBuildId) {
        String name = getClass().getSimpleName();
        logger.info(String.format("%s: Agent '%s' registered", name, agent.getName()));
    }
}
