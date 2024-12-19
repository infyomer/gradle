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

package org.gradle.api.problems;

import org.gradle.api.Incubating;
import org.gradle.api.problems.internal.InternalProblemSpec;
import org.gradle.api.problems.internal.Problem;

import java.io.Serializable;

/**
 * Marker interface for additional data that can be attached to a {@link Problem}.
 * It and all its contained Objects need to be Serializable to be able to be carried in a BuildEvent to the TAPI.
 * We use the {@link org.gradle.tooling.internal.provider.serialization.PayloadSerializer} to serialize the event.
 * If the serialization fails the build TAPI call will fail.
 * <p>
 * The internal list interfaces supported by the problems API are:
 * <ul>
 *     <li>{@link org.gradle.api.problems.internal.GeneralData}</li>
 *     <li>{@link org.gradle.api.problems.internal.TypeValidationData}</li>
 *     <li>{@link org.gradle.api.problems.internal.DeprecationData}</li>
 *     <li>{@link org.gradle.api.problems.internal.PropertyTraceData}</li>
 * </ul>
 *
 * @see InternalProblemSpec#additionalData(AdditionalData)
 * @since 8.13
 */
@Incubating
public interface AdditionalData extends Serializable {
}
