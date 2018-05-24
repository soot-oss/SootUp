pipeline {
    agent {
        docker {
            image 'maven:3-alpine'
            args '-v $HOME/.m2:/root/.m2'
        }
    }
    stages {

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

	    stage('Test') {
	        steps {
	            sh 'mvn test'
	        }
	        post {
			    success {
			    	junit 'target/surefire-reports/**/*.xml' 
			    }
			}
		}

		stage('Deploy'){
		    when {
			    branch 'master'
			}
	        steps {
	            echo 'WHEN - Master Branch!'
	        }
		}

    }
}