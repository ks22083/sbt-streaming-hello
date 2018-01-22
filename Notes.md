Notes
-----
# TODOs
Add link to *.md examples and tutorial

## TOC
* [SBT Notes](#sbt-notes)
  * [SBT Resources](#sbt-resources)

# SBT: Notes

```bash
$ sbt 
[info] Loading settings from idea.sbt ...
[info] Loading global plugins from /home/esi/.sbt/1.0/plugins
[info] Loading project definition from /home/esi/temp/TEMP.sbt-streaming-hello.TEMP/project
[info] Loading settings from build.sbt ...
[info] Set current project to sbt-streaming-hello (in build file:/home/esi/temp/TEMP.sbt-streaming-hello.TEMP/)
[info] sbt server started at local:///home/esi/.sbt/1.0/server/8400c48360dc66ed869f/sock
```
With console you can evaluate current project settings for example
```scala
sbt:sbt-streaming-hello> show libraryDependencies
[info] * org.scala-lang:scala-library:2.11.12
[info] * org.scalaj:scalaj-http:2.3.0
[info] * org.apache.spark:spark-core:2.2.0
[info] * org.apache.spark:spark-streaming:2.2.0
[info] * org.scalatest:scalatest:3.0.4:test
sbt:sbt-streaming-hello> show scalaVersion
[info] 2.11.12
sbt:sbt-streaming-hello> show scalaBinaryVersion
[info] 2.11
...
... change spark version from 2.2.0 to 2.2.1 in build sbt 
... reload is mandatory in this case
...
sbt:sbt-streaming-hello> reload
[info] Loading settings from idea.sbt ...
[info] Loading global plugins from /home/esi/.sbt/1.0/plugins
[info] Loading project definition from /home/esi/temp/TEMP.sbt-streaming-hello.TEMP/project
[info] Loading settings from build.sbt ...
[info] Set current project to sbt-streaming-hello (in build file:/home/esi/temp/TEMP.sbt-streaming-hello.TEMP/)
sbt:sbt-streaming-hello> show libraryDependencies
[info] * org.scala-lang:scala-library:2.11.12
[info] * org.scalaj:scalaj-http:2.3.0
[info] * org.apache.spark:spark-core:2.2.1
[info] * org.apache.spark:spark-streaming:2.2.1
[info] * org.scalatest:scalatest:3.0.4:test
```
## SBT Resources
* [Official tutorial](http://www.scala-sbt.org/1.x/docs/Getting-Started.html)
* [Another tutorial found on github](https://github.com/shekhargulati/52-technologies-in-2016/blob/master/02-sbt/README.md)
