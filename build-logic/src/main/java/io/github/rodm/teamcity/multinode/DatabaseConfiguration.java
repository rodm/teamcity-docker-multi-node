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

import org.gradle.api.Action;

public interface DatabaseConfiguration {
    DatabaseOptions getOptions();
    void useMySQL();
    void useMySQL(Action<? super DatabaseOptions> configure);
    void usePostgreSQL();
    void usePostgreSQL(Action<? super DatabaseOptions> configure);

    String getName();
    void setName(String name);

    String getUsername();
    void setUsername(String username);

    String getPassword();
    void setPassword(String password);

    Object getDriver();
    void setDriver(Object driver);
    void driver(Object driver);
}
