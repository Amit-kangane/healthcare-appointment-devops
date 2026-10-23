pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        ansiColor('xterm')
    }

    environment {
        // Application Metadata
        APP_NAME = 'healthcare-appointment-devops'
        K8S_NAMESPACE = 'healthcare'
        K8S_DEPLOYMENT_NAME = 'healthcare-appointment'

        // Container Registry Configuration (Uses Jenkins Credentials - NO hardcoded secrets)
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_REPO = 'amitkangane/healthcare-appointment-devops'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        KUBECONFIG_CREDENTIALS_ID = 'k8s-kubeconfig'

        // Immutable Tag using short Git Commit SHA
        IMAGE_TAG = ""
    }

    stages {
        stage('Initialize & Inspect') {
            steps {
                script {
                    // Extract short Git commit hash for immutable versioning
                    if (isUnix()) {
                        IMAGE_TAG = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    } else {
                        IMAGE_TAG = bat(script: "@git rev-parse --short HEAD", returnStdout: true).trim()
                    }
                    echo "=========================================================="
                    echo "BUILDING PIPELINE FOR: ${env.APP_NAME}"
                    echo "GIT BRANCH:            ${env.BRANCH_NAME ?: 'main'}"
                    echo "GIT COMMIT SHA (TAG):  ${env.IMAGE_TAG}"
                    echo "TARGET REGISTRY IMAGE: ${env.DOCKER_REPO}:${env.IMAGE_TAG}"
                    echo "=========================================================="
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Compiling Java source code with Maven...'
                script {
                    if (isUnix()) {
                        sh './mvnw clean compile -B'
                    } else {
                        bat 'mvnw.cmd clean compile -B'
                    }
                }
            }
        }

        stage('Unit Test') {
            steps {
                echo 'Executing automated unit and integration tests with JUnit 5...'
                script {
                    if (isUnix()) {
                        sh './mvnw test -B'
                    } else {
                        bat 'mvnw.cmd test -B'
                    }
                }
            }
            post {
                always {
                    // Publishes JUnit test results to Jenkins dashboard even if tests fail
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: false
                }
                failure {
                    echo 'CRITICAL: Automated tests failed! Pipeline is aborting to prevent publishing broken code.'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application into executable JAR (tests already verified)...'
                script {
                    if (isUnix()) {
                        sh './mvnw package -DskipTests -B'
                    } else {
                        bat 'mvnw.cmd package -DskipTests -B'
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building Docker image with immutable tag: ${DOCKER_REPO}:${IMAGE_TAG} and latest..."
                script {
                    if (isUnix()) {
                        sh "docker build -t ${DOCKER_REPO}:${IMAGE_TAG} -t ${DOCKER_REPO}:latest ."
                    } else {
                        bat "docker build -t ${DOCKER_REPO}:${IMAGE_TAG} -t ${DOCKER_REPO}:latest ."
                    }
                }
            }
        }

        stage('Image Publish') {
            steps {
                echo 'Authenticating with container registry using Jenkins credentials...'
                // Using withCredentials prevents password leakage in logs
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}",
                                                 usernameVariable: 'DOCKER_USER',
                                                 passwordVariable: 'DOCKER_PASS')]) {
                    script {
                        if (isUnix()) {
                            sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                            sh "docker push ${DOCKER_REPO}:${IMAGE_TAG}"
                            sh "docker push ${DOCKER_REPO}:latest"
                            sh 'docker logout'
                        } else {
                            bat 'echo %DOCKER_PASS% | docker login -u "%DOCKER_USER%" --password-stdin'
                            bat "docker push ${DOCKER_REPO}:${IMAGE_TAG}"
                            bat "docker push ${DOCKER_REPO}:latest"
                            bat 'docker logout'
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo "Updating Kubernetes deployment '${K8S_DEPLOYMENT_NAME}' to image ${DOCKER_REPO}:${IMAGE_TAG}..."
                // Authenticates to Kubernetes cluster using Jenkins Secret File or Kubeconfig
                withCredentials([file(credentialsId: "${KUBECONFIG_CREDENTIALS_ID}", variable: 'KUBECONFIG')]) {
                    script {
                        if (isUnix()) {
                            sh "kubectl set image deployment/${K8S_DEPLOYMENT_NAME} ${K8S_DEPLOYMENT_NAME}=${DOCKER_REPO}:${IMAGE_TAG} -n ${K8S_NAMESPACE} --record=true"
                        } else {
                            bat "kubectl set image deployment/${K8S_DEPLOYMENT_NAME} ${K8S_DEPLOYMENT_NAME}=${DOCKER_REPO}:${IMAGE_TAG} -n ${K8S_NAMESPACE} --record=true"
                        }
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo 'Monitoring Kubernetes rollout status and verifying pod health...'
                withCredentials([file(credentialsId: "${KUBECONFIG_CREDENTIALS_ID}", variable: 'KUBECONFIG')]) {
                    script {
                        try {
                            if (isUnix()) {
                                sh "kubectl rollout status deployment/${K8S_DEPLOYMENT_NAME} -n ${K8S_NAMESPACE} --timeout=120s"
                            } else {
                                bat "kubectl rollout status deployment/${K8S_DEPLOYMENT_NAME} -n ${K8S_NAMESPACE} --timeout=120s"
                            }
                            echo 'Rollout successfully completed! All replicas are healthy.'
                        } catch (Exception e) {
                            echo "=================================================================="
                            echo "CRITICAL WARNING: Deployment rollout timed out or failed!"
                            echo "Initiating automatic rollback to previous stable deployment..."
                            echo "=================================================================="
                            if (isUnix()) {
                                sh "kubectl rollout undo deployment/${K8S_DEPLOYMENT_NAME} -n ${K8S_NAMESPACE}"
                            } else {
                                bat "kubectl rollout undo deployment/${K8S_DEPLOYMENT_NAME} -n ${K8S_NAMESPACE}"
                            }
                            error("Rollout failed. Automatic rollback executed. Please investigate pod logs.")
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "=================================================================="
            echo "PIPELINE COMPLETED SUCCESSFULLY!"
            echo "Application version '${IMAGE_TAG}' is running in Kubernetes."
            echo "=================================================================="
        }
        failure {
            echo "=================================================================="
            echo "PIPELINE FAILED! Check the stage logs above for failure details."
            echo "=================================================================="
        }
        always {
            cleanWs deleteDirs: true, notFailBuild: true
        }
    }
}
