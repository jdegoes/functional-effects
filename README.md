# zio-intro-game

An introduction to ZIO using Game Programming!

# Usage

## From the UI

1. Download the repository as a [zip archive](https://github.com/jdegoes/zio-intro-game/archive/master.zip).
2. Unzip the archive, usually by double-clicking on the file.
3. Configure the source code files in the IDE or text editor of your choice.

## From the Command Line

1. Open up a terminal window.

2. Clone the repository.

    ```bash
    git clone https://github.com/jdegoes/zio-intro-game
    ```
5. Launch `sbt`.

    ```bash
    ./sbt
    ```
6. Enter continuous compilation mode.

    ```bash
    sbt:zio-intro-game> ~ test:compile
    ```

Hint: You might get the following error when starting sbt:

> [error] 	typesafe-ivy-releases: unable to get resource for com.geirsson#sbt-scalafmt;1.6.0-RC4: res=https://repo.typesafe.com/typesafe/ivy-releases/com.geirsson/sbt-scalafmt/1.6.0-RC4/jars/sbt-scalafmt.jar: javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested targe

It's because you have an outdated Java version, missing some newer certificates. Install a newer Java version, e.g. using [Jabba|https://github.com/shyiko/jabba], a Java version manager. See [Stackoverflow|https://stackoverflow.com/a/58669704/1885392] for more details about the error.

# Legal

Copyright&copy; 2019 John A. De Goes. All rights reserved.
