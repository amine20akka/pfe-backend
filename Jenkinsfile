pipeline {
    agent {
        docker {
            image 'maven:3.8.5-openjdk-17'
            args '-v /root/.m2:/root/.m2'
        }
    }
    
    stages {
        stage('🔍 Checkout') {
            steps {
                echo '=== Checking out source code ==='
                checkout scm
            }
        }
        
        stage('🏗️ Build Java Backend modules') {
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