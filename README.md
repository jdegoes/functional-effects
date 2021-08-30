# Functional Effects

For years, actors have allowed Scala application developers to build async, concurrent, and distributed applications that are resilient, reactive, and scalable. Increasingly, however, functional effect systems like can are being used to create these types of applications, with greater type-safety, more flexibility, and increased testability.

In this course, Scala developers will learn how to solve complex problems in asynchronous, concurrent programming using the ZIO library. Upon completion of the course, attendees will be confident using the ZIO library (and similar libraries, like Monix or Cats IO) to build modern high-performance, asynchronous, concurrent applications that don't block threads, don't deadlock, and don't leak resources; and which follow best practices regarding error management, thread management, and dependency management.

### Who Should Attend

Scala developers who would like to write modern async, concurrent, and distributed applications that are robust, testable, and powerful.

### Prerequisites

Good working knowledge of Scala, including familiarity with immutable data, pattern matching, and basic recursion. Developers who have attended Functional Scala Foundations will be well-prepared for this course.

### Topics

- Laziness of functional effects
- Escaping callback hell
- Using the compiler to help deal with errors
- Separating recoverable errors from non-recoverable errors
- Separating blocking code from async code
- Safe handling of async and concurrent resources
- Efficient concurrency and parallelism
- Shared concurrent state without visibility issues, deadlocks, or race conditions
- Testing functional effects
- Retrying and repetition
- Resource-safe, effectful, async streaming
- Test-friendly dependency management

# Usage

## From the UI

1. Download the repository as a [zip archive](https://github.com/jdegoes/functional-effects/archive/master.zip).
2. Unzip the archive, usually by double-clicking on the file.
3. Configure the source code files in the IDE or text editor of your choice.

## From the Command Line

1. Open up a terminal window.

2. Clone the repository.

    ```bash
    git clone https://github.com/jdegoes/functional-effects
    ```
5. Launch project provided `sbt`.

    ```bash
    cd functional-effects; ./sbt
    ```
6. Enter continuous compilation mode.

    ```bash
    sbt:functional-effects> ~ test:compile
    ```

Hint: You might get the following error when starting sbt:

> [error] 	typesafe-ivy-releases: unable to get resource for com.geirsson#sbt-scalafmt;1.6.0-RC4: res=https://repo.typesafe.com/typesafe/ivy-releases/com.geirsson/sbt-scalafmt/1.6.0-RC4/jars/sbt-scalafmt.jar: javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested targe

It's because you have an outdated Java version, missing some newer certificates. Install a newer Java version, e.g. using [Jabba](https://github.com/shyiko/jabba), a Java version manager. See [Stackoverflow](https://stackoverflow.com/a/58669704/1885392) for more details about the error.

# Legal

Copyright&copy; 2019-2021 John A. De Goes. All rights reserved.
