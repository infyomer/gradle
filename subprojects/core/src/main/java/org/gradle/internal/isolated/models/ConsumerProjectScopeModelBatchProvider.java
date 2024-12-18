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

package org.gradle.internal.isolated.models;

import org.gradle.api.internal.provider.AbstractMinimalProvider;
import org.gradle.internal.evaluation.EvaluationScopeContext;

import java.util.List;

public class ConsumerProjectScopeModelBatchProvider<T> extends AbstractMinimalProvider<List<T>> {

    private final ProjectModelController projectModelController;
    private final ProjectScopeModelBatchRequest<T> request;

    public ConsumerProjectScopeModelBatchProvider(ProjectModelController projectModelController, ProjectScopeModelBatchRequest<T> request) {
        this.projectModelController = projectModelController;
        this.request = request;
    }

    @Override
    public boolean calculatePresence(ValueConsumer consumer) {
        return projectModelController.calculateBatchPresence(request);
    }

    @Override
    protected Value<? extends List<T>> calculateOwnValue(ValueConsumer consumer) {
        try (EvaluationScopeContext ignored = openScope()) {
            return projectModelController.calculateBatchValue(request);
        }
    }

    @Override
    public Class<List<T>> getType() {
        // TODO: provide the type of the aggregate
        return null;
    }
}
