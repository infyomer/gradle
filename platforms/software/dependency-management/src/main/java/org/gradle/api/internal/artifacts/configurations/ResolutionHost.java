/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.api.internal.artifacts.configurations;

import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.problems.internal.InternalProblems;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.internal.component.resolution.failure.ReportableAsProblem;
import org.gradle.internal.exceptions.MultiCauseException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

/**
 * The "Host" or owner of a resolution -- the thing in charge of the resolution, or the thing being resolved.
 *
 * <p>The purpose of this type is to be a configuration-cache compatible representation of the thing
 * being resolved. This type should remain as minimal as possible.</p>
 *
 * TODO: Split the interface into two: one for tracking what is doing the resolving and one for mapping resolution problems
 */
public interface ResolutionHost {

    DisplayName displayName();

    default String getDisplayName() {
        return displayName().getDisplayName();
    }

    default DisplayName displayName(String type) {
        return Describables.of(displayName(), type);
    }

    /**
     * Returns the problems service this host can use for reporting failures.
     *
     * @return the problems service
     */
    InternalProblems getProblems();

    /**
     * Rethrows the provided failures, doing nothing if the list of failures is empty.
     * <p>
     * If any of the failures (or their ancestor causes) are {@link ReportableAsProblem}, they will all be reported to the problems
     * service available on this type via {@link #getProblems()}.
     *
     * @param resolutionType what was resolved, e.g. "dependencies", "artifacts", "files"
     * @param failures the exceptions encountered during resolution
     */
    default void rethrowFailuresAndReportProblems(String resolutionType, Collection<Throwable> failures) {
        reportProblems(failures);
        consolidateFailures(resolutionType, failures).ifPresent(e -> {
            throw e;
        });
    }

    /**
     * Consolidates the given failures into a single exception, if there are any.
     *
     * @param resolutionType what was resolved, e.g. "dependencies", "artifacts", "files"
     * @param failures the exceptions encountered during resolution
     * @return an {@link org.gradle.internal.exceptions.MultiCauseException} containing all the failures, or {@link Optional#empty()} if there are no failures
     */
    Optional<? extends ResolveException> consolidateFailures(String resolutionType, Collection<Throwable> failures);

    /**
     * If the given failure (or their ancestor causes) are {@link ReportableAsProblem}, they will all be reported to the problems
     * service available on this type via {@link #getProblems()}.
     *
     * @param failure the exception to inspect
     */
    default void reportProblems(Throwable failure) {
        reportProblems(Collections.singleton(failure));
    }

    /**
     * If the given failures (or their ancestor causes) are {@link ReportableAsProblem}, they will all be reported to the problems
     * service available on this type via {@link #getProblems()}.
     *
     * @param failures the exceptions to inspect
     */
    default void reportProblems(Collection<Throwable> failures) {
        Queue<Throwable> exceptionQueue = new LinkedList<>(failures);
        Throwable current = exceptionQueue.poll();

        while (current != null) {
            if (current instanceof ReportableAsProblem) {
                ((ReportableAsProblem<?>) current).reportAsProblem(getProblems());
            }

            if (current instanceof MultiCauseException) {
                exceptionQueue.addAll(((MultiCauseException) current).getCauses());
            } else {
                if (current.getCause() != null) {
                    exceptionQueue.add(current.getCause());
                }
            }

            current = exceptionQueue.poll();
        }
    }
}
