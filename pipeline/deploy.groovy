def HELM_CHART_DIRECTORY = "demo"
pipeline {
    agent { label 'deploypod' }
    stages {
        stage('Get latest version of code') {
            steps {
                 git credentialsId: 'git_cred', url: 'https://github.com/gmoez/cicdHelm.git', branch: 'main'
                script {
                    // Get the latest tag name
                    GIT_TAG_NAME = params.TAG
                    HELM_CHART_DIRECTORY = "demo"
                }
            }
        }
      stage('Check running containers') {
        steps {
          
          container('helm') {

            sh """yq eval '.image.tag = "${GIT_TAG_NAME}"' -i ${HELM_CHART_DIRECTORY}/values.yaml"""

          }
        }
      }
      stage('commit and push Helm change to repository') {
            steps {
                //git credentialsId: 'git_cred', url: 'https://github.com/gmoez/cicdHelm.git', branch: 'main'
                 withCredentials([string(credentialsId: 'git_pat', variable: 'GIT_PAT')]) {
                    sh "git config --global user.email 'moez.gammoua@outlook.fr'"
                    sh "git config --global user.name 'moezg'"
                    sh 'echo "#!/bin/sh" > askpass.sh'
                    sh "echo 'echo ${GIT_PAT}' >> askpass.sh"
                    sh 'chmod +x askpass.sh'
                    sh "env GIT_ASKPASS='./askpass.sh' git commit -am 'Update image.tag to ${GIT_TAG_NAME}'"
                    sh "env GIT_ASKPASS='./askpass.sh' git push --set-upstream origin main"
                } 
                
            }
        }
      
      

      
    }   

  }