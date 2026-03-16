pipeline {
    agent any

    tools {
        jdk "Temurin Java 21"
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Setup Gradle') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Remapping Classpath') {
            steps {
                withGradle {
                    sh './gradlew generateRemapClasspath'
                }
            }
        }

        stage('Build') {
            steps {
                withGradle {
                    sh './gradlew build'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true, onlyIfSuccessful: true
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
