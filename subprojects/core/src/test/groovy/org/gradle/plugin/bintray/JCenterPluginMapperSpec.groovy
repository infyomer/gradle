/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.plugin.bintray

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.plugin.resolve.internal.DefaultPluginRequest
import org.gradle.plugin.resolve.internal.InvalidPluginRequestException
import org.gradle.plugin.resolve.internal.JCenterPluginMapper
import spock.lang.Specification

class JCenterPluginMapperSpec extends Specification {

    public static final String TEST_PLUGIN_MAVEN_GROUP_ID = 'com.bintray.gradle.test'
    public static final String TEST_PLUGIN_MAVEN_ARTIFACT_ID = 'test-plugin'
    public static final String TEST_PLUGIN_EXPLICIT_VERSION = '1.0'
    public static final String TEST_PLUGIN_ID = 'gradle-test-plugin'

    private DependencyHandler getMockForVersion(String version) {
        Mock(DependencyHandler) {
            create("$TEST_PLUGIN_MAVEN_GROUP_ID:$TEST_PLUGIN_MAVEN_ARTIFACT_ID:$version") >> Stub(Dependency) {
                getGroup() >> TEST_PLUGIN_MAVEN_GROUP_ID
                getName() >> TEST_PLUGIN_MAVEN_ARTIFACT_ID
                getVersion() >> version
            }
            0 * _ //fail if create called with any other string
        }
    }

    JCenterPluginMapper mapper = new JCenterPluginMapper()


    def 'Latest version of plugin maps correctly from Bintray'() {
        when:
        def dependencyHandler = Mock(DependencyHandler)
        0 * dependencyHandler._
        mapper.map(new DefaultPluginRequest(TEST_PLUGIN_ID), dependencyHandler)

        then:
        def e = thrown InvalidPluginRequestException
        e.message.startsWith "No version number supplied for plugin '$TEST_PLUGIN_ID'"
    }

    def 'Explicit version of plugin maps correctly from Bintray'() {
        when:
        Dependency dependency = mapper.map(new DefaultPluginRequest(TEST_PLUGIN_ID, TEST_PLUGIN_EXPLICIT_VERSION), getMockForVersion(TEST_PLUGIN_EXPLICIT_VERSION))

        then:
        dependency.group == TEST_PLUGIN_MAVEN_GROUP_ID
        dependency.name == TEST_PLUGIN_MAVEN_ARTIFACT_ID
        dependency.version == TEST_PLUGIN_EXPLICIT_VERSION
    }

    def 'Query for non-existing plugin returns null'() {
        expect:
        mapper.map(new DefaultPluginRequest("not-exist"), getMockForVersion(TEST_PLUGIN_EXPLICIT_VERSION)) == null
    }
}