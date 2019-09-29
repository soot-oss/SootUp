pipeline {
    agent none

    stages {

        stage('Check Format') {
            agent {
                  docker {
                    image 'maven:3-jdk-8-alpine'
                    args '-v $HOME/.m2:/root/.m2'
                  }
            }

            steps {
              sh 'mvn com.coveo:fmt-maven-plugin:check'
            }
        }

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
                  image 'maven:3-jdk-9-slim'
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
              sh 'mvn verify -PJava8'
            }

            post {
              always {
                junit '*/target/surefire-reports/**/*.xml'
                stash includes: '**/target/coverage-reports/*', name: 'reports1'

              }
            }
          }

	        stage('Test JDK9'){

            agent {
              docker {
                image 'maven:3-jdk-9-slim'
                args '-v $HOME/.m2:/root/.m2'
              }
            }

            steps {
              sh 'mvn verify -PJava9'
            }
            post {
              always {
                junit '*/target/surefire-reports/**/*.xml'
                stash includes: '**/target/coverage-reports/*', name: 'reports2'
              }
            }
          }


	       }
		}


		stage('Report'){
		     agent {
                      docker {
                        image 'maven:3-jdk-9-slim'
                        args '-v $HOME/.m2:/root/.m2'
                      }
                    }
          steps {
                      unstash 'reports1'
                      sh "mv de.upb.soot.tests/target/coverage-reports/jacoco-ut.exec de.upb.soot.tests/target/coverage-reports/jacoco-ut-jdk8.exec"
                      sh "rm -f de.upb.soot.tests/target/coverage-reports/aggregate.exec"

                      unstash 'reports2'
                      sh "mv de.upb.soot.tests/target/coverage-reports/jacoco-ut.exec de.upb.soot.tests/target/coverage-reports/jacoco-ut-jdk9.exec"
                      sh "rm -f de.upb.soot.tests/target/coverage-reports/aggregate.exec"

                      sh 'mvn validate' // Invokes the jacoco merge goal

                      jacoco(execPattern: 'de.upb.soot.tests/target/coverage-reports/aggregate.exec',
                             classPattern: '*/classes',
                             sourcePattern: '*/src/main/java',
                             exclusionPattern: '*/src/test*',
                             changeBuildStatus: false,
                             minimumMethodCoverage: "50",
                             maximumMethodCoverage: "70",
                             deltaMethodCoverage: "10")
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
