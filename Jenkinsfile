pipeline {
    agent {
        dockerContainer {
            image 'maven:3.8.5-openjdk-17'
        }
    }

    triggers {
        githubPush()
    }
     
    stages {
        stage('🔍 Checkout') {
            steps {
                script {
                    echo '=== Checking out source code ==='
                    echo "Branch: ${env.GIT_BRANCH}"
                    echo "Commit: ${env.GIT_COMMIT}"
                    echo "Triggered by: ${currentBuild.getBuildCauses()}"
                    checkout scm
                }
            }
        }
        
        stage('🏗️ Build Java Backend modules') {
            steps {
                script {
                    echo '=== Building modules ==='
                    
                    sh '''
                        echo "--- Building both modules ---"
                        mvn -B -U clean package -DskipTests
                    '''
                    
                    echo '✅ Builds completed'
                }
            }
        }

        stage('🧪 Prepare Test Environment') {
            steps {
                script {
                    sh '''
                        echo "Starting test environment..."
                        docker compose -f docker-compose.test.yml up -d
                    '''
                }
            }
        }

        stage('🧪 Run georef unit tests') {
            steps {
                script {
                    sh '''
                        mvn -pl georef-module test -Dspring.profiles.active=ci
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'docker compose -f docker-compose.test.yml down'
        }
    }
}