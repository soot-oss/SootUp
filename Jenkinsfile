pipeline {
    agent none

    stages {

        stage('Build') {
          parallel{
            stage('Build with JDK8'){

              agent {
                docker {
                  image 'maven:3-jdk-8-alpine'
                  args '-v $HOME/.m2:/root/.m2'
                }
              }

              steps {
                sh 'mvn clean compile'
              }

            }


            stage('Build with JDK9'){

              agent {
                docker {
                  image 'maven:3-jdk-9-alpine'
                  args '-v $HOME/.m2:/root/.m2'
                }
              }

              steps {
                sh 'mvn clean compile'
              }

            }
          }
        }

	    stage('Test') {
        parallel {
	  
          stage('Test JDK8'){

            agent {
              docker {
                image 'maven:3-jdk-8-alpine'
                args '-v $HOME/.m2:/root/.m2'
              }
            }

            steps {
              sh 'mvn test'
              jacoco(   execPattern: '**/target/coverage-reports/jacoco-ut.exec',
              classPattern: '**/classes',
              sourcePattern: 'src/main/java',
              exclusionPattern: 'src/test*',
              changeBuildStatus: true,
              minimumMethodCoverage: "50",
              maximumMethodCoverage: "70",
              deltaMethodCoverage: "10"
              )
            }

            post {
              always {
                junit 'target/surefire-reports/**/*.xml'
              }
            }
          }

	        stage('Test JDK9'){
  
            agent {
              docker {
                image 'maven:3-jdk-9-alpine'
                args '-v $HOME/.m2:/root/.m2'
              }
            }

            steps {
              sh 'mvn test'
              jacoco(
              execPattern: '**/target/coverage-reports/jacoco-ut.exec',
              classPattern: '**/classes',
              sourcePattern: 'src/main/java',
              exclusionPattern: 'src/test*',
              changeBuildStatus: true,
              minimumMethodCoverage: "50",
              maximumMethodCoverage: "70",
              deltaMethodCoverage: "10"
              )
            }
            post {
              always {
                junit 'target/surefire-reports/**/*.xml'
              }
            }
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