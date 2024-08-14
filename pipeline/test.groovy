pipeline {
    agent { label 'testpod' }
    stages {
        stage('Get latest version of code') {
            steps {
                 git  url: 'https://github.com/gmoez/helloworld-spring.git', branch: 'master'
                script {
                    // Get the latest tag name
                    GIT_TAG_NAME = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', returnStdout: true).trim()
                }
            }
        }
      stage('Check running containers') {
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
      stage('build docker images') {
        steps {
          container('docker') {
            
            sh 'pwd'
            sh 'ls -l'
            sh "docker build  --build-arg VERSION=${GIT_TAG_NAME} -t moezg/springboot:${GIT_TAG_NAME} ."
             sh 'docker image ls'
          }
        }
      }
      stage('pushing image to artifact repository') {
        steps {
          container('docker') {
            
            withCredentials([usernamePassword(credentialsId: 'docker-login', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                sh 'docker login --username="${USERNAME}" --password="${PASSWORD}"'
                sh "docker push  moezg/springboot:${GIT_TAG_NAME}"
              } 
             
          }
        }
      }
      stage('delete old chekout and check from new repository') {
        steps {
            sh 'rm -rf *'
            git url: 'https://github.com/gmoez/cicdHelm.git', branch: 'master', clean: true
            sh 'ls -l'
                
            }
      }
      
    }   

  }