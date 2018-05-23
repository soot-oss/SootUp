#!groovy

// Mark the code build 'stage'....
stage 'Build'
node {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Checkout code from repository
   checkout scm

   // Run the maven build
    mvn 'clean compile'
}

def mvn(args) {
    sh "${tool 'Maven 3.x'}/bin/mvn ${args}"
}