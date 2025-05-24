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
package io.github.rodm.teamcity.multinode.internal;

import io.github.rodm.teamcity.multinode.DatabaseConfiguration;
import io.github.rodm.teamcity.multinode.DatabaseOptions;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class DefaultDatabaseConfiguration implements DatabaseConfiguration {

    private final Property<String> name;
    private final Property<String> username;
    private final Property<String> password;
    private final ConfigurableFileCollection driver;

    private final DatabaseOptions options;

    @Inject
    public DefaultDatabaseConfiguration(ObjectFactory factory) {
        this.name = factory.property(String.class);
        this.username = factory.property(String.class);
        this.password = factory.property(String.class);
        this.driver = factory.fileCollection();
        this.options = factory.newInstance(MySQLDatabaseOptions.class, this);
    }

    @Override
    public DatabaseOptions getOptions() {
        return options;
    }

    @Override
    public void useMySQL(Action<? super DatabaseOptions> configure) {
        configure.execute(options);
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String getUsername() {
        return username.get();
    }

    @Override
    public void setUsername(String username) {
        this.username.set(username);
    }

    @Override
    public String getPassword() {
        return password.get();
    }

    @Override
    public void setPassword(String password) {
        this.password.set(password);
    }

    @Override
    public Object getDriver() {
        return driver;
    }

    @Override
    public void setDriver(Object driver) {
        this.driver.setFrom(driver);
    }

    @Override
    public void driver(Object driver) {
        this.driver.from(driver);
    }
}
