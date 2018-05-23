pipeline {
    agent {
        docker {
            image 'maven:3-alpine'
            args '-v $HOME/.m2:/root/.m'2
        }
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
    }
}