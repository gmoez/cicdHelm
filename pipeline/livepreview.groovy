def HELM_CHART_DIRECTORY = "demo"
def branch_name= params.Branch
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
                    branch_name= params.Branch
                    appname="myapp"+branch
                }
            }
        }
      stage('creating live preview') {
        steps {
          
          container('aws-cli') {

            sh """aws eks update-kubeconfig --region eu-central-1 --name demo"""
            sh "kubectl get ns"
            //sh """helm list | grep ${appname}"""

          }
        }
      } 
      /*stage('creating live preview') {
        steps {
          
          container('helm-install') {

            sh """helm upgrade ${appname}  --set path=/${branch_name},image.tag=${branch_name}_${GIT_TAG_NAME} ./${HELM_CHART_DIRECTORY} --install -n myapp"""
            //sh """helm list | grep ${appname}"""

          }
        }
      }   */         

      
    }   

  }