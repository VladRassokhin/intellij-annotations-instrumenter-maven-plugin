intellij-annotations-instrumenter-maven-plugin
==============================================

IntelliJ IDEA annotations instrumenter maven plugin

Usage
==============================================
Just update your pom.xml with following: 
```xml
    <pluginRepositories>
        <pluginRepository>
            <id>repository.jetbrains.com</id>
            <name>repository.jetbrains.com-all</name>
            <url>http://repository.jetbrains.com/all</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
    <build>
        <plugins>
            <plugin>
                <groupId>com.intellij</groupId>
                <artifactId>notnull-instrumenter-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>instrument</goal>
                            <goal>tests-instrument</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

Use other and/or multiple annotations
==============================================
By default only the annotation org.jetbrains.annotations.NotNull is supported if you
want to one or more other annotations add them to configuration, for example:
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>com.intellij</groupId>
                <artifactId>notnull-instrumenter-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
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
```
Will instrument both jetbrains and javax annotations.

Note that configuration will replace the default annotations, so org.jetbrains.annotations.NotNull will
no longer be included by default thus it must be added again if used (as in the above example).

License Information
==============================================
Copyright 2000-2012 JetBrains s.r.o.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.