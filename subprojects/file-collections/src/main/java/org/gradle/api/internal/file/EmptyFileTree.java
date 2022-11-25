/*
 * Copyright 2022 the original author or authors.
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

package org.gradle.api.internal.file;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.util.PatternFilterable;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

final class EmptyFileTree extends AbstractFileTree {
    public static final FileTreeInternal INSTANCE = new EmptyFileTree(DEFAULT_TREE_DISPLAY_NAME);

    private final String displayName;

    public EmptyFileTree(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Set<File> getIntrinsicFiles() {
        return Collections.emptySet();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public FileTree matching(Closure filterConfigClosure) {
        return this;
    }

    @Override
    public FileTree matching(Action<? super PatternFilterable> filterConfigAction) {
        return this;
    }

    @Override
    public FileTreeInternal matching(PatternFilterable patterns) {
        return this;
    }

    @Override
    public FileTree visit(FileVisitor visitor) {
        return this;
    }

    @Override
    public void visitContentsAsFileTrees(Consumer<FileTreeInternal> visitor) {
    }

    @Override
    protected void visitContents(FileCollectionStructureVisitor visitor) {
    }
}
