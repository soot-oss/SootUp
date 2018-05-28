<<<<<<< HEAD
def rtMaven

=======
>>>>>>> dc502c49523d403a24df5838eaa2d3a99176c858
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
                withCredentials([
                    [$class: 'StringBinding', credentialsId: 'nexusUsername', variable: 'MVN_SETTINGS_nexusUsername'],
                    [$class: 'StringBinding', credentialsId: 'nexusPassword', variable: 'MVN_SETTINGS_nexusPassword']
                  ]) {
                    withEnv([
                      'nexusPublic=https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-releases/'
                    ]) {
                      sh 'mvn -s settings.xml clean build'
                    }
                  }
	        }
		}

    }
}