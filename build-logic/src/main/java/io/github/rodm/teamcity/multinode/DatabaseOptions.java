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
package io.github.rodm.teamcity.multinode;

import java.util.Map;

public interface DatabaseOptions {
    String getImage();
    void setImage(String image);

    String getUrl();
    void setUrl(String url);

    Boolean getTestOnBorrow();
    Map<String, String> getEnvironmentVariables();

    String getDatabasePath();
    int getDatabasePort();
}
