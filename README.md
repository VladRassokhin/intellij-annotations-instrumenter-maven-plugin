intellij-annotations-instrumenter-maven-plugin
==============================================

IntelliJ IDEA annotations instrumenter maven plugin

This code is based on Vlad Rassokhin's intellij-annotations-instrumenter-maven-plugin. The following
significant changes have been made:
* Added Java 8 bytecode support
* Added configuration: which NotNull/Nullable annotations to instrument (default is still @org.jetbrains.annotations.NotNull and  @org.jetbrains.annotations.Nullable)
* Added basic unit and functional tests
* Isolated Maven plugin dependencies to allow usage without Maven
* Added implicit NotNull option

Usage
==============================================
Just update your pom.xml with following: 

    <dependencies>
        <dependency>
            <groupId>org.jetbrains</groupId>
            <artifactId>annotations</artifactId>
            <version>13.0</version>
        </dependency>
        ...
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>se.eris</groupId>
                <artifactId>notnull-instrumenter-maven-plugin</artifactId>
                <version>0.4.2</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>instrument</goal>
                            <goal>tests-instrument</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
    </build>

Use other and/or multiple annotations
==============================================
By default only the annotation org.jetbrains.annotations.NotNull is supported if you
want to one or more other annotations add them to configuration, for example:

    <build>
        <plugins>
            <plugin>
                <groupId>se.eris</groupId>
                <artifactId>notnull-instrumenter-maven-plugin</artifactId>
                <version>0.4.2</version>
                <executions>
                    <execution>
                        <id>instrument</id>
                        <goals>
                            <goal>instrument</goal>
                            <goal>tests-instrument</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <annotations>
                        <param>org.jetbrains.annotations.NotNull</param>
                        <param>javax.validation.constraints.NotNull</param>
                    </annotations>
                </configuration>
            </plugin>
        </plugins>
    </build>

Will instrument both jetbrains and javax annotations.

Note that configuration will replace the default annotations, so org.jetbrains.annotations.NotNull will
no longer be included by default thus it must be added again if used (as in the above example).


Implicit NotNull instrumentation
==============================================
If you don't like to have @NotNull on 99.99% of your parameters and methods turn on the implicit instrumentation:

    <build>
        <plugins>
            <plugin>
                <groupId>se.eris</groupId>
                <artifactId>notnull-instrumenter-maven-plugin</artifactId>
                <version>0.4.2</version>
                <executions>
                    <execution>
                        <id>instrument</id>
                        <goals>
                            <goal>instrument</goal>
                            <goal>tests-instrument</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <implicit>true</implicit>
                    <annotations>
                        <param>org.jetbrains.annotations.Nullable</param>
                    </annotations>
                </configuration>
            </plugin>
        </plugins>
    </build>

Will instrument all parameters and return values with NotNull unless annotated with @Nullable (org.jetbrains.annotations.Nullable). 

**Note** that when using implicit you need to specify the Nullable annotation (not NotNull).


License Information
==============================================
Copyright 2013-2016 Eris IT AB

Licensed under the Apache License, Version 2.0


Original License:

Copyright 2000-2012 JetBrains s.r.o.

Licensed under the Apache License, Version 2.0
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
