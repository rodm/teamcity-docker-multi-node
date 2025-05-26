/*
 * Copyright 2025 Rod MacKenzie
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
package io.github.rodm.teamcity.multinode.internal;

import io.github.rodm.teamcity.multinode.DatabaseConfiguration;
import io.github.rodm.teamcity.multinode.DatabaseOptions;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class MySQLDatabaseOptions implements DatabaseOptions {

    private final Property<String> image;
    private final Property<String> url;
    private final DatabaseConfiguration configuration;

    @Inject
    public MySQLDatabaseOptions(ObjectFactory factory, DatabaseConfiguration configuration) {
        this.image = factory.property(String.class).convention("mysql:9.3");
        this.url = factory.property(String.class).convention("jdbc:mysql://localhost:3306/teamcity");
        this.configuration = configuration;
    }

    @Override
    public String getImage() {
        return image.get();
    }

    @Override
    public void setImage(String image) {
        this.image.set(image);
    }

    @Override
    public String getUrl() {
        return url.get();
    }

    @Override
    public void setUrl(String url) {
        this.url.set(url);
    }

    @Override
    public Boolean getTestOnBorrow() {
        return Boolean.TRUE;
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        Map<String, String> variables = new HashMap<>();
        variables.put("MYSQL_ROOT_PASSWORD", "admin123");
        variables.put("MYSQL_DATABASE", "teamcity");
        variables.put("MYSQL_USER", configuration.getUsername());
        variables.put("MYSQL_PASSWORD", configuration.getPassword());
        return variables;
    }

    @Override
    public String getDatabasePath() {
        return "/var/lib/mysql";
    }

    @Override
    public int getDatabasePort() {
        return 3306;
    }
}
