/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.smalltalk;

import org.gradle.api.Incubating;
import org.gradle.api.provider.Provider;
import org.gradle.internal.service.scopes.Scope;
import org.gradle.internal.service.scopes.ServiceScope;

/**
 * TBD
 *
 * @since 8.10
 */
@ServiceScope(Scope.Build.class)
@Incubating
public interface SmalltalkModelRegistry {

    /**
     * TBD
     *
     * @since 8.10
     */
    <T> Provider<T> getModel(String key, Class<T> type);

    /**
     * TBD
     *
     * @since 8.10
     */
    <T> void registerModel(String key, Class<T> type, SmalltalkComputation<T> provider);

}
