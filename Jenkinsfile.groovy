#!Groovy Pipeline Script

properties([
        parameters([
                choice(
                        choices: ['6.0.4', '6.0.3', '6.0.0'].join("\n"),
                        description: 'Build Version',
                        name: 'buildVersion'
                       ),
                        string(defaultValue: "develop",
                                description: "Git Branch",
                                name: "gitBranch"
                        ),
                        string(defaultValue: "https://github.com/sonurepos91/jenkins-docker-demo.git",
                                description: "Git URL",
                                name: "gitUrl"
                        ),
                        string(defaultValue: "",
                                description: "Container Port",
                                name: "containerPort"
                        ),
                        string(defaultValue: "9002",
                                description: "Expose Port",
                                name: "exposePort"
                        ),
                        booleanParam(defaultValue: false,
                                description: "Git Poll Enabled",
                                name: "gitPoll"
                        ),
                        booleanParam(defaultValue: false,
                                description: "Git ChangeLog Enabled",
                                name: "changeLog"
                        )
        ])
])
pipeline {

    agent any
    options {
        skipDefaultCheckout true
    }
    environment {

        registry = "sonukumar9939/test9939"
        registryCredential = 'dockerHub_ID'
    }

    stages {
        stage("scmCheckout") {
            steps {
                script {
                    echo "Git  Checkout Started...... "
                    String gitBranch = params.gitBranch
                    String gitUrl = params.gitUrl
                    Boolean gitPoll = params.gitPoll
                    Boolean changeLog = params.changeLog
                    scmCheckout(gitBranch, gitUrl, gitPoll, changeLog)
                    echo "Git  Checkout Completed...... "
                }
            }
        }
        stage("Build") {
            steps {
                script {
                    echo "Maven Build Started...... "
                    List PROJECT_WIN_VARS = ["JAVA_HOME=C:\\Program Files\\Java\\jdk-17\\",
                                             "MAVEN_HOME=C:\\Softwares\\apache-maven-3.9.5-bin\\apache-maven-3.9.5\\",
                                             "PATH=C:\\WINDOWS\\SYSTEM32;C:\\Softwares\\apache-maven-3.9.5-bin\\apache-maven-3.9.5\\bin;%PATH%"

                    ]
                    withEnv(PROJECT_WIN_VARS) {

                        String PROJECT_PARAMS = "-Dmaven.test.failure.ignore=true"
                        dir(env.WORKSPACE + '\\Project') {
                            bat "mvn ${PROJECT_PARAMS} clean"
                            bat "mvn ${PROJECT_PARAMS} package"
                        }
                    }
                    echo "Maven Build Completed...... "
                }
            }
        }
        stage("Deploy") {
            steps {
                script {
                    echo "Deploy Started...... "
                    dir(env.WORKSPACE + '\\Project') {
                        bat "docker build -t pipeline:$BUILD_NUMBER ."
                        bat "docker create -it --name pipeline$BUILD_NUMBER -p 0.0.0.0:" + params.containerPort + ":" + params.exposePort + " pipeline:$BUILD_NUMBER"
                        bat "docker start pipeline$BUILD_NUMBER"
                    }
                    echo "Deploy Completed...... "
                }
            }
        }
    }
}

def scmCheckout(String gitBranch, String gitUrl, Boolean gitPoll, Boolean changeLog) {
    echo "Git CLone start with following config : GitBranch : " + gitBranch + " GitUrl : " + gitUrl + " gitPoll : " + gitPoll + " changeLog : " + changeLog
    def workspace = env.WORKSPACE
    echo "Workspace :" + workspace

    checkout changelog: false, poll: false,
            scm: scmGit(branches: [[name: '*/' + gitBranch]],
                    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: env.WORKSPACE + '\\Project']],
                    relativeTargetDir: env.WORKSPACE + '\\Project',
                    userRemoteConfigs: [[url: gitUrl]])
}
