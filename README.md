#intellij-annotations-instrumenter-maven-plugin

IntelliJ IDEA annotations instrumenter maven plugin

This code is based on Vlad Rassokhin's intellij-annotations-instrumenter-maven-plugin. The following
significant changes have been made:
* Added Java 8 bytecode support
* Added configuration: which NotNull/Nullable annotations to instrument (default is still @org.jetbrains.annotations.NotNull and  @org.jetbrains.annotations.Nullable)
* Added basic unit and functional tests
* Isolated Maven plugin dependencies to allow usage without Maven
* Added implicit NotNull option
* Added flag to turn instrumentation of

##Usage

Just update your pom.xml with following: 

    <dependencies>
        <dependency>
            <groupId>org.jetbrains</groupId>
            <artifactId>annotations</artifactId>
            <version>15.0</version>
        </dependency>
        ...
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>se.eris</groupId>
                <artifactId>notnull-instrumenter-maven-plugin</artifactId>
                <version>0.6.5</version>
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

And start adding @NotNull/@Nullable annotations to your code.


##Use other and/or multiple annotations

By default only the annotation org.jetbrains.annotations.NotNull is supported if you
want to one or more other annotations add them to configuration, for example:

    <build>
        <plugins>
            <plugin>
                <groupId>se.eris</groupId>
                <artifactId>notnull-instrumenter-maven-plugin</artifactId>
                <version>0.6.5</version>
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
                    <notNull>
                        <param>org.jetbrains.annotations.NotNull</param>
                        <param>javax.validation.constraints.NotNull</param>
                    </notNull>
                </configuration>
            </plugin>
        </plugins>
    </build>

Will instrument both jetbrains and javax annotations.

Note that configuration will replace the default annotations, so org.jetbrains.annotations.NotNull will
no longer be included by default thus it must be added again if used (as in the above example).


##Implicit NotNull instrumentation

If you don't like to have @NotNull on 99.99% of your parameters and methods turn on the implicit instrumentation:

    <build>
        <plugins>
            <plugin>
                <groupId>se.eris</groupId>
                <artifactId>notnull-instrumenter-maven-plugin</artifactId>
                <version>0.6.5</version>
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
                    <nullable>
                        <param>org.jetbrains.annotations.Nullable</param>
                    </nullable>
                </configuration>
            </plugin>
        </plugins>
    </build>

Will instrument all parameters and return values with NotNull unless annotated with @Nullable (org.jetbrains.annotations.Nullable). Ie:

    public String implicit(String a, String b) {
        if (a.equals(b)) {
            return null;
        }
        return a + b;
    }

will throw an IllegalArgumentException if either the a or b parameter is null, and will throw an 
IllegalStateException if a equals b (since it is not allowed to return null). To allow nulls you would 
have to annotate the parameters/return value like this:

    @Nullable
    public String implicit(@Nullable String a, @Nullable String b) {
        if (a.equals(b)) {
            return null;
        }
        return a + b;
    }

which would throw a NullPointerException if a is null, return null if a equals b, and otherwise append the 
Strings (or a + null if b is null).

**Note** that when using implicit you need to specify the Nullable annotation (not the NotNull).


##Turn off Instrumentation

The property `se.eris.notnull.instrument=true/false` turns on/off the instrumentation. This may seem like a 
stupid feature but it is really useful when you have multiple maven profiles and only one of them, eg Sonar/Findbugs, 
should build without instrumentation since it messes up the statistics (branch coverage, complexity, etc).

`mvn clean install -Dse.eris.notnull.instrument=false`

##Exclusion

To ease migration to implicit it is now possible to exclude certain class files from instrumentation. This 
is still a bit experimental the exclusion rules might change (depending on feedback).

There are three patterns

* . matching package boundary
* * matching anything except package boundaries
* ** matching anything (including package boundaries)
* .** matching any number of package levels

Example:

    <configuration>
        <implicit>true</implicit>
        <excludes>
            <classes>**.wsdl.**</classes>
            <classes>com.*.*Test</classes>
        </excludes>
    </configuration>

Would exclude all files which have wsdl in any package part and classes with names ending in Test under 
com.&lt;exactlyonepackagelevel&gt; for example com.a.UnitTest but not further down ie not com.a.b.UnitTest or 
com.UnitTest.

Things I am thinking about (want feedback):

* Automatically adding the (\\$[0-9]+)? regexp to the end, to match anonymous inner classes.
* Allow full regexp.
