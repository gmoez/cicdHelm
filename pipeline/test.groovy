pipeline {
    agent { label 'testpod' }
    stages {
        stage('checkout code source') {
            steps {
                 git  url: 'https://github.com/gmoez/helloworld-spring.git', branch: 'master'
                script {
                    // Get the latest tag name
                    GIT_TAG_NAME = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', returnStdout: true).trim()
                }
            }
        }
      stage('Building Application Artifact') {
        steps {
          container('maven') {
            
            sh 'pwd'
            sh 'ls -l'
            sh 'mvn -version'
             sh "mvn versions:set -DnewVersion=${GIT_TAG_NAME}"
            sh "mvn clean install"
            sh "cp target/*.jar ."
          }
        }
      }
      stage('building docker images') {
        steps {
          container('docker') {
            
            sh 'pwd'
            sh 'ls -l'
            sh "docker build  --build-arg VERSION=${GIT_TAG_NAME} -t moezg/springboot:${GIT_TAG_NAME} ."
             sh 'docker image ls'
          }
        }
      }
      stage('pushing image to artifactory') {
        steps {
          container('docker') {
            
            withCredentials([usernamePassword(credentialsId: 'docker-login', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                sh 'docker login --username="${USERNAME}" --password="${PASSWORD}"'
                sh "docker push  moezg/springboot:${GIT_TAG_NAME}"
              } 
             
          }
        }
      }
      stage('Updating Helm Chart with new image tag') {
        steps {
            build job: 'deploy_dev', parameters: [string(name: 'TAG', value: GIT_TAG_NAME)]
        }
      }

      
    }   

  }