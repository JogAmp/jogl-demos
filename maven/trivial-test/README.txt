Note: This sub-project's pom.xml file already includes the
below general setting for 'jogamp-remote'.

First, because the project isn't in the Central Repository yet, Maven
needs to be told to look at https://www.jogamp.org. Edit ~/.m2/settings.xml:

<settings>
  <profiles>
    <profile>
      <id>jogamp</id>
      <activation>
        <!-- Change this to false, if you don't like to have it on by default -->
        <activeByDefault>true</activeByDefault>
      </activation>
      <repositories>
        <repository>
          <id>jogamp-remote</id>
          <name>jogamp test mirror</name>
          <url>https://www.jogamp.org/deployment/maven/</url>
          <layout>default</layout>
        </repository>
      </repositories>
    </profile>
  </profiles>
</settings>

Then, run:

  $ mvn -U clean test

It should download all of the required packages (which may be quite a few
if you've not run Maven before) and then compile and run the included test
program.

The '-U' option somewhat was required to force an update.

