atlas-node-release
===========

[![Build Status](https://wooga-shields.herokuapp.com/jenkins/s/https/atlas-jenkins.wooga.com/job/atlas-plugins/job/atlas-node-release/job/master.svg?style=flat-square)](https://atlas-jenkins.wooga.com/job/atlas-plugins/job/atlas-node-release/job/master)
[![Test](https://wooga-shields.herokuapp.com/jenkins/t/https/atlas-jenkins.wooga.com/job/atlas-plugins/job/atlas-node-release/job/master.svg?style=flat-square)](https://atlas-jenkins.wooga.com/job/atlas-plugins/job/atlas-node-release/job/master)
[![GitHub contributors](https://wooga-shields.herokuapp.com/github/contributors/wooga/atlas-node-release.svg?style=flat-square)](https://github.com/wooga/atlas-node-release/graphs/contributors)
[![GitHub contributors](https://wooga-shields.herokuapp.com/github/contributors/wooga/atlas-node-release.svg?style=flat-square)](https://github.com/wooga/atlas-node-release/graphs/contributors)
[![GitHub language](https://wooga-shields.herokuapp.com/github/language/wooga/atlas-node-release.svg?style=flat-square)]()
[![GitHub tag](https://wooga-shields.herokuapp.com/github/tag/wooga/atlas-node-release.svg?style=flat-square)](https://github.com/wooga/atlas-node-release/tags)
[![GitHub release](https://wooga-shields.herokuapp.com/github/release/wooga/atlas-node-release.svg?style=flat-square)](https://github.com/wooga/atlas-node-release/releases/latest)
[![GitHub commits since latest](https://wooga-shields.herokuapp.com/github/commits-since-latest/wooga/atlas-node-release.svg?style=flat-square)](https://github.com/wooga/atlas-node-release/releases/latest)
[![Github issues](https://wooga-shields.herokuapp.com/github/issues/wooga/atlas-node-release.svg?style=flat-square)](https://github.com/wooga/atlas-node-release/issues)
[![Github closed issues](https://wooga-shields.herokuapp.com/github/issues-closed-raw/wooga/atlas-node-release.svg?style=flat-square)](https://github.com/wooga/atlas-node-release/issues?q=is%3Aissue+is%3Aclosed)
[![GitHub pull requests](https://wooga-shields.herokuapp.com/github/issues-pr-raw/wooga/atlas-node-release.svg?style=flat-square)](https://github.com/wooga/atlas-node-release/pulls)
[![GitHub closed pull requests](https://wooga-shields.herokuapp.com/github/issues-pr-closed-raw/wooga/atlas-node-release.svg?style=flat-square)](https://github.com/wooga/atlas-node-release/pulls)

This plugin hooks up [gradle-node-plugin](https://github.com/srs/gradle-node-plugin) with [nebula-release-plugin](https://github.com/nebula-plugins/nebula-release-plugin) to make [semver 2](https://semver.org/) versioned npm releases easier.

Applying the plugin
===================
**build.gradle**
```groovy
plugins {
    id 'net.wooga.node-release' version '0.+'
}
```

Conventions
===========
* Applies [nebula.release](https://github.com/nebula-plugins/nebula-release-plugin)
* Applies [com.moowork.node](https://github.com/nebula-plugins/nebula-release-plugin)
* Expects a valid `package.json` on project root.
* Expects `clean`, `test` and `build` task in `scripts` block of `package.json`
* Expects existence of a git repository with remote origin

Documentation
=============
- [API docs](https://wooga.github.io/atlas-node-release/docs/api/)
- [Release Notes](RELEASE_NOTES.md)


LICENSE
=======

Copyright 2018 Wooga GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
