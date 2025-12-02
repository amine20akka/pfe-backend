pipeline {
    agent {
        dockerContainer {
            image 'maven:3.8.5-openjdk-17'
        }
    }

    triggers {
        // GitHub webhook trigger for push event
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
        
        stage('Build Java Backend modules') {
            steps {
                script {
                    echo '=== Building modules ==='
                    
                    // Multi-module build for backend
                    echo '--- Building 2 Modules ---'
                    dir('pfe-backend') {
                        sh 'mvn -B -U clean package -DskipTests'
                    }
                    
                    echo '✅ Builds completed'
                }
            }
        }
        
        stage('🧪 Run Tests') {
            steps {
                script {
                    echo '=== Running tests for georef module ==='
                    
                    dir('pfe-backend/georef-module') {
                        sh 'mvn test'
                    }
                    
                    echo '✅ Georef tests passed'
                }
            }
        }
    }
}