
pipeline {
    agent { label 'testpod' }
    stages {
        stage('Get latest version of code') {
            steps {
                 git(
                            url: 'https://github.com/gmoez/helloworld-spring.git',
                            branch: "${params.Branch}"
                        ) 
                
                   
                script {
                    // Get the latest tag name
                    GIT_TAG_NAME = sh(script: 'git describe --tags $(git rev-list --tags --max-count=1)', returnStdout: true).trim()
                    GIT_BRANCH=params.Branch
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
            sh "docker build  --build-arg VERSION=${GIT_TAG_NAME} -t moezg/springboot:${GIT_BRANCH}_${GIT_TAG_NAME} ."
             sh 'docker image ls'
          }
        }
      }
      stage('pushing image to artifact repository') {
        steps {
          container('docker') {
            
            withCredentials([usernamePassword(credentialsId: 'docker-login', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                sh 'docker login --username="${USERNAME}" --password="${PASSWORD}"'
                sh "docker push  moezg/springboot:${GIT_BRANCH}_${GIT_TAG_NAME}"
              } 
             
          }
        }
      }
      
      stage('Deploying app in live preview mode') {
        steps {
            build job: 'deploy_live_preview', parameters: [string(name: 'Branch', value: GIT_BRANCH),string(name: 'TAG', value: GIT_TAG_NAME)]
        }
      }

      
    }   

  }