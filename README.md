IntelliJ IDEA annotations instrumenter maven plugin
==============================================

# NOTE: This plugin is discontinued and no longer maintained, development is done at https://github.com/osundblad/intellij-annotations-instrumenter-maven-plugin . Please use that version instead.

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
