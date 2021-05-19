pipeline {
    agent any

    environment {
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "localhost:8081"
        NEXUS_REPOSITORY = "maven-snapshot"
        NEXUS_CREDENTIAL_ID = "nexus"
        PROJECT_VERSION = "1.0.0-SNAPSHOT"
    }

    stages {
        stage("Clean") {
            steps {
                sh "chmod +x ./gradlew";
                sh "./gradlew clean";
            }
        }
        stage("Build") {
            steps {
                sh "./gradlew build";
            }
            post {
                success {
                    archiveArtifacts artifacts: 'backend/build/libs/backend.jar', fingerprint: true
                }
            }
        }
        stage("Docs") {
            steps {
                sh "./gradlew dokkaHtmlMultiModule";
                sh "rm -r /var/www/docs/backend-v1.0.0"
                sh "mkdir /var/www/docs/backend-v1.0.0"
                sh "cp -r build/backend-v1.0.0 /var/www/docs/"
            }
        }
        stage("Sources") {
            steps {
                sh "./gradlew sourcesJar";
            }
            post {
                success {
                    archiveArtifacts artifacts: 'backend/build/libs/backend-sources.jar', fingerprint: true
                }
            }
        }
        stage("Publish") {
            steps {
                script {
                    nexusArtifactUploader(
                            nexusVersion: NEXUS_VERSION,
                            protocol: NEXUS_PROTOCOL,
                            nexusUrl: NEXUS_URL,
                            groupId: "eu.vironlab.mc",
                            version: PROJECT_VERSION,
                            repository: NEXUS_REPOSITORY,
                            credentialsId: NEXUS_CREDENTIAL_ID,
                            artifacts:
                                    [
                                            [
                                                    artifactId: "backend",
                                                    classifier: '',
                                                    file      : "backend/build/libs/backend.jar",
                                                    type      : "jar"
                                            ],
                                            [
                                                    artifactId: "backend",
                                                    classifier: 'sources',
                                                    file      : "backend/build/libs/backend-sources.jar",
                                                    type      : "jar"
                                            ],
                                            [
                                                    artifactId: "backend",
                                                    classifier: '',
                                                    file      : "backend/build/pom/pom.xml",
                                                    type      : "pom"
                                            ],
                                            [
                                                    artifactId: "backend-api",
                                                    classifier: '',
                                                    file      : "backend-api/build/libs/backend-api.jar",
                                                    type      : "jar"
                                            ],
                                            [
                                                    artifactId: "backend-api",
                                                    classifier: 'sources',
                                                    file      : "backend-api/build/libs/backend-api-sources.jar",
                                                    type      : "jar"
                                            ],
                                            [
                                                    artifactId: "backend-api",
                                                    classifier: '',
                                                    file      : "backend-api/build/pom/pom.xml",
                                                    type      : "pom"
                                            ]
                                    ]
                    );
                }
            }
        }
    }
}