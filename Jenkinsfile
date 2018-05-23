#!groovy

// Mark the code build 'stage'....
node {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Checkout code from repository
   checkout scm

   stage 'Build'
   // Run the maven build
   mvn 'clean compile'
}

def mvn(args) {
   sh "${tool 'Maven 3.x'}/bin/mvn ${args}"
}