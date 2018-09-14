#!groovy

/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
 @Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

 withCredentials([usernameColonPassword(credentialsId: 'artifactory_publish', variable: 'artifactory_publish'),
                  string(credentialsId: 'artifactory_npm_token', variable: 'npm_token'),
                  usernamePassword(credentialsId: 'github_integration', passwordVariable: 'githubPassword', usernameVariable: 'githubUser'),
                  usernamePassword(credentialsId: 'github_integration_2', passwordVariable: 'githubPassword2', usernameVariable: 'githubUser2'),
                  string(credentialsId: 'atlas_node_release_coveralls_token', variable: 'coveralls_token')]) {

     def testEnvironment = [
                                'osx&&node' : [
                                            "artifactoryCredentials=${artifactory_publish}",
                                            "artifactory_npm_token=${npm_token}",
                                            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser}",
                                            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword}"
                                ],
                                'windows&&node' : [
                                            "artifactoryCredentials=${artifactory_publish}",
                                            "artifactory_npm_token=${npm_token}",
                                            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser2}",
                                            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword2}"
                                ]
                            ]

     buildGradlePlugin plaforms: ['osx&&node','windows&&node'], coverallsToken: coveralls_token, testEnvironment: testEnvironment
 }
