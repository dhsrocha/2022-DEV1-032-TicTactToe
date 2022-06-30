# Coding Kata: Tic-Tac-Toe

---

<p align="center">
  <a href="#resources"><b>Resources</b></a>
  &nbsp;&nbsp;&nbsp;•&nbsp;&nbsp;&nbsp;
  <a href="#requirements"><b>Requirements</b></a>
  &nbsp;&nbsp;&nbsp;•&nbsp;&nbsp;&nbsp;
  <a href="#tooling"><b>Tooling</b></a>
  &nbsp;&nbsp;&nbsp;•&nbsp;&nbsp;&nbsp;
  <a href="#modules"><b>Modules</b></a>
</p>

The project is about present coding skills by using most recent Spring stack's technologies.

## ⚙ Build and Run

### Resources

<sup>[Back to top](#coding-kata-tic-tac-toe)</sup>


The application can run locally with the following command line:

```shell
./mvnw -Pdev-local
```

### Requirements

<sup>[Back to top](#coding-kata-tic-tac-toe)</sup>

* An OS of [Linux](https://www.linux.org) or [Windows](https://www.microsoft.com/windows) families
  (_enforced by Maven plugin_).
* [Java 17](https://jdk.java.net/17) (_enforced by Maven plugin_).

### Tooling

<sup>[Back to top](#coding-kata-tic-tac-toe)</sup>

The shipped **[Maven Wrapper](https://maven.apache.org/wrapper/maven-wrapper-plugin/index.html)** is
the one of choice to build the project's contents, by running [`./mvnw`](./mvnw) or
[`./mvnw.cmd`](./mvnw.cmd).

The project has the following listed **build profiles** to support and complement the building
process, by using a combination of `-P<profile>` flags (_some of them will be automatically
activated already_):

> ℹ _The list is not exhaustive and should be endlessly improved as new build profiles take place._

* **OS-based setup**:
  * `os-unix`: Automatically activated in Unix OSes. Targeted to point to the `./mvnw` file.
  * `os-win`: Automatically activated in Windows OSes. Targeted to point to the `./mvnw.cmd` file.
* **Packaging**:
  * `mode-dev`: Produces a **development** mode artifact, set up to connect with it all backing
    services on which this is dependent, such as a database or a messaging broker, running
    in-memory or being mocked, targeted to run in a stand-alone way.
  * `mode-prod`: Produces a **production** mode artifact, set up to connect with all required
    configurations to integrate with other running subsystems, targeted to run when deployed into a
    clustered environment.
* **Continuous Integration**:
  * `with-testing`: Incrementally runs _unit_ and _integration_ tests and produces test results.
  * `with-quality-code`: Incrementally evaluates code quality-wise statically. It comes
    automatically enabled. Using `-D disable-check` flag disables it.
  * `with-quality-dependencies`: Incrementally evaluates dependencies for discovered vulnerabilities
    and cost-free license availability.
  * `with-quality-report`: Incrementally harvests data from quality checks to send it to a given
    quality gate (currently targeted to [SonarCloud](https://sonarcloud.io)).
  * `with-full-build`: Incrementally performs a full build on a given module and its dependencies.
* **Local development**:
  * `dev-local`: Executes a module to run in development mode. It is advisable to run it in local
    environments, outside a cluster, and in a stand-alone way.
  * `dev-version-update`: Displays and updates enlisted dependencies' versions.

---

<p align="center">
  <sub>Copyright 2022 © <a href="https://www.linkedin.com/in/dhsrocha">Diego Rocha</a></sub>
</p>
