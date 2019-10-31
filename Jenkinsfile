pipeline {
  agent none
  options {
    parallelsAlwaysFailFast()
  }
  environment {
      MAVEN_OPTS = '-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xms1g -Xmx2g'
  }
  stages {
    stage('Check Format'){
      agent {
          docker {
            image 'maven:3-jdk-8-alpine'
            args '-v $HOME/.m2:/root/.m2'
          }
      }
        steps{
            sh 'mvn com.coveo:fmt-maven-plugin:check'
        }
    }


    stage('Work Work Work') {
      parallel {
        stage("JDK8") {
          agent {
              docker {
                image 'maven:3-jdk-8-alpine'
                args '-v $HOME/.m2:/root/.m2'
              }
          }
          stages {
            stage("Build (JDK8)") {
              steps {
                sh 'mvn clean compile -Dfmt.skip'
              }
            }
            stage("Test (JDK8)") {
              steps {
                sh 'mvn verify -Dfmt.skip -PJava8'
              }
              post {
                  always {
                    junit '*/target/surefire-reports/**/*.xml'
                  }
                  success{
                    stash includes: '**/target/coverage-reports/*', name: 'reports1'
                  }
              }
            }
          }
        }


        stage("JDK9") {
          agent {
            docker {
              image 'maven:3-jdk-9-slim'
              args '-v $HOME/.m2:/root/.m2'
            }
          }
          stages {
            stage("Build (JDK9)") {
              steps {
                sh 'mvn clean compile -Dfmt.skip'
              }
            }
            stage("Test (JDK9)") {
              steps {
                sh 'mvn verify -Dfmt.skip -PJava9'
              }
              post {
                  always {
                    junit '*/target/surefire-reports/**/*.xml'
                  }
                  success{
                    stash includes: '**/target/coverage-reports/*', name: 'reports2'
                  }
              }
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
        script{
          sootmodules = ['java.core','java.bytecode','java.sourcecode','callgraph'];
          sootmodules.each{ item ->
            sh "mkdir -p target/coverage-reports"
            unstash 'reports1'
            sh "mv de.upb.swt.soot.${item}/target/coverage-reports/jacoco-ut.exec target/coverage-reports/jacoco-ut-${item}-jdk8.exec"
            sh "rm -f de.upb.swt.soot.${item}/target/coverage-reports/aggregate.exec"

            unstash 'reports2'
            sh "mv de.upb.swt.soot.${item}/target/coverage-reports/jacoco-ut.exec target/coverage-reports/jacoco-ut-${item}-jdk9.exec"
            sh "rm -f de.upb.swt.soot.${item}/target/coverage-reports/aggregate.exec"
          }
        }
        sh 'mvn validate' // Invokes the jacoco merge goal

        jacoco(execPattern: '**/target/coverage-reports/aggregate.exec',
               classPattern: '**/classes',
               sourcePattern: 'src/main/java',
               exclusionPattern: 'src/test*',
               changeBuildStatus: false,
               minimumMethodCoverage: "50",
               maximumMethodCoverage: "70",
               deltaMethodCoverage: "10"
        )
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