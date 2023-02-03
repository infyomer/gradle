/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.api.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.java.WebApplication;
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.internal.DefaultWarPluginConvention;
import org.gradle.api.plugins.internal.JvmPluginsHelper;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.War;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.jvm.component.internal.JvmSoftwareComponentInternal;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * <p>A {@link Plugin} which extends the {@link JavaPlugin} to add tasks which assemble a web application into a WAR
 * file.</p>
 *
 * @see <a href="https://docs.gradle.org/current/userguide/war_plugin.html">WAR plugin reference</a>
 */
public abstract class WarPlugin implements Plugin<Project> {
    public static final String PROVIDED_COMPILE_CONFIGURATION_NAME = "providedCompile";
    public static final String PROVIDED_RUNTIME_CONFIGURATION_NAME = "providedRuntime";
    public static final String WAR_TASK_NAME = "war";
    public static final String WEB_APP_GROUP = "web application";

    private final ObjectFactory objectFactory;
    private final ImmutableAttributesFactory attributesFactory;

    private JvmSoftwareComponentInternal component;

    @Inject
    public WarPlugin(ObjectFactory objectFactory, ImmutableAttributesFactory attributesFactory) {
        this.objectFactory = objectFactory;
        this.attributesFactory = attributesFactory;
    }

    @Override
    public void apply(final Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        this.component = JvmPluginsHelper.getJavaComponent(project);

        final WarPluginConvention pluginConvention = objectFactory.newInstance(DefaultWarPluginConvention.class, project);
        project.getConvention().getPlugins().put("war", pluginConvention);

        project.getTasks().withType(War.class).configureEach(task -> {
            task.getWebAppDirectory().convention(project.getLayout().dir(project.provider(() -> pluginConvention.getWebAppDir())));
            task.from(task.getWebAppDirectory());
            task.dependsOn((Callable) () -> component.getSourceSet().getRuntimeClasspath());
            task.classpath((Callable) () -> {
                Configuration providedRuntime = project.getConfigurations().getByName(PROVIDED_RUNTIME_CONFIGURATION_NAME);
                return component.getSourceSet().getRuntimeClasspath().minus(providedRuntime);
            });
        });

        TaskProvider<War> war = project.getTasks().register(WAR_TASK_NAME, War.class, warTask -> {
            warTask.setDescription("Generates a war archive with all the compiled classes, the web-app content and the libraries.");
            warTask.setGroup(BasePlugin.BUILD_GROUP);
        });

        PublishArtifact warArtifact = new LazyPublishArtifact(war, ((ProjectInternal) project).getFileResolver(), ((ProjectInternal) project).getTaskDependencyFactory());
        project.getExtensions().getByType(DefaultArtifactPublicationSet.class).addCandidate(warArtifact);
        configureConfigurations(project.getConfigurations(), component);
        configureComponent(project, warArtifact);
    }

    /**
     * This method is intended for internal use and should not be called.
     *
     * @deprecated This method will be removed in Gradle 9.0.
     */
    @Deprecated
    public void configureConfigurations(ConfigurationContainer configurationContainer) {
        DeprecationLogger.deprecateMethod(WarPlugin.class, "configureConfigurations(ConfigurationContainer)")
            .willBeRemovedInGradle9()
            .withUpgradeGuideSection(8, "war_plugin_configure_configurations")
            .nagUser();

        configureConfigurations(configurationContainer, component);
    }

    private void configureConfigurations(ConfigurationContainer configurationContainer, JvmSoftwareComponentInternal component) {
        Configuration providedCompileConfiguration = configurationContainer.create(PROVIDED_COMPILE_CONFIGURATION_NAME).setVisible(false).
            setDescription("Additional compile classpath for libraries that should not be part of the WAR archive.");
        providedCompileConfiguration.setCanBeConsumed(false);

        Configuration providedRuntimeConfiguration = configurationContainer.create(PROVIDED_RUNTIME_CONFIGURATION_NAME).setVisible(false).
            extendsFrom(providedCompileConfiguration).
            setDescription("Additional runtime classpath for libraries that should not be part of the WAR archive.");
        providedRuntimeConfiguration.setCanBeConsumed(false);

        component.getImplementationConfiguration().extendsFrom(providedCompileConfiguration);
        component.getRuntimeClasspathConfiguration().extendsFrom(providedRuntimeConfiguration);
        component.getRuntimeElementsConfiguration().extendsFrom(providedRuntimeConfiguration);
        configurationContainer.getByName(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME).extendsFrom(providedRuntimeConfiguration);
    }

    private void configureComponent(Project project, PublishArtifact warArtifact) {
        AttributeContainer attributes = attributesFactory.mutable()
            .attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_RUNTIME));
        project.getComponents().add(objectFactory.newInstance(WebApplication.class, warArtifact, "master", attributes));
    }
}
