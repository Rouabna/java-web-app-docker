pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'rouabna/java-web-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DEPLOY_SERVER = 'deploy-server'
        DEPLOY_USER = 'deploy'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Pulling code from GitHub...'
                git url: 'https://github.com/Rouabna/java-web-app-docker.git', branch: 'master'
            }
        }

        stage('Build with Maven') {
            steps {
                echo 'Building project with Maven...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Push to DockerHub') {
            steps {
                echo 'Pushing image to DockerHub...'
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh "echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin"
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    sh "docker push ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Deploy to Server') {
            steps {
                echo 'Deploying to remote server...'
                sshagent(['deploy-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_SERVER} '
                            docker pull ${DOCKER_IMAGE}:latest
                            docker stop java-web-app || true
                            docker rm java-web-app || true
                            docker run -d --name java-web-app -p 8080:8080 ${DOCKER_IMAGE}:latest
                            docker ps | grep java-web-app
                        '
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo 'Verifying deployment...'
                sshagent(['deploy-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_SERVER} '
                            sleep 10
                            docker ps | grep java-web-app
                            curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/java-web-app/ || echo "App starting..."
                        '
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            sh 'docker logout || true'
        }
    }
}
