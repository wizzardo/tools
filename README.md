[![Build](https://github.com/wizzardo/tools/actions/workflows/gradle.yml/badge.svg)](https://github.com/wizzardo/tools/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/wizzardo/tools/branch/master/graph/badge.svg)](https://codecov.io/gh/wizzardo/tools)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.wizzardo.tools/tools/badge.svg)](https://mvnrepository.com/artifact/com.wizzardo.tools/tools/latest)


Tools
=========

It is a pack of useful and simple tools for java developers, such as

  - [http client]
  - [json parser]
  - xml parser
  - date parser (iso 8601)
  - groovy interpreter
  - image transformations
  - md5, sha-1 hashes
  - and others..


Installation
--------------
maven:
```xml
<dependency>
    <groupId>com.wizzardo.tools</groupId>
    <artifactId>tools</artifactId>
    <version>0.19</version>
</dependency>
```

gradle:
```
compile 'com.wizzardo.tools:tools:0.23'

// or by module:

def tools_version = "0.23"
compile "com.wizzardo.tools:tools-cache:${tools_version}"
compile "com.wizzardo.tools:tools-collections:${tools_version}"
compile "com.wizzardo.tools:tools-evaluation:${tools_version}"
compile "com.wizzardo.tools:tools-exception:${tools_version}"
compile "com.wizzardo.tools:tools-helpers:${tools_version}"
compile "com.wizzardo.tools:tools-http:${tools_version}"
compile "com.wizzardo.tools:tools-image:${tools_version}"
compile "com.wizzardo.tools:tools-interfaces:${tools_version}"
compile "com.wizzardo.tools:tools-io:${tools_version}"
compile "com.wizzardo.tools:tools-json:${tools_version}"
compile "com.wizzardo.tools:tools-math:${tools_version}"
compile "com.wizzardo.tools:tools-misc:${tools_version}"
compile "com.wizzardo.tools:tools-reflection:${tools_version}"
compile "com.wizzardo.tools:tools-security:${tools_version}"
compile "com.wizzardo.tools:tools-xml:${tools_version}"
```

License
----

MIT


[http client]:https://github.com/wizzardo/Tools/wiki/HttpClient
[json parser]:https://github.com/wizzardo/Tools/wiki/JsonTools
