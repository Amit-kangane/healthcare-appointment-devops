pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        // Application Metadata
        APP_NAME = 'healthcare-appointment-devops'
        K8S_NAMESPACE = 'default'
        K8S_DEPLOYMENT_NAME = 'healthcare-app'

        // Container Registry Configuration (Uses Jenkins Credentials - NO hardcoded secrets)
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_REPO = 'amitkangane2005/healthcare-appointment-devops'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        KUBECONFIG_CREDENTIALS_ID = 'k8s-kubeconfig'

        // Default immutable tag using Jenkins build number (guaranteed non-null)
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Initialize & Inspect') {
            steps {
                script {
                    // Ensure Maven Wrapper has executable permissions on Linux agents
                    if (isUnix()) {
                        sh 'chmod +x ./mvnw'
                    }

                    // Extract short Git commit hash if available, with safe fallback to BUILD_NUMBER
                    def tag = ''
                    try {
                        if (env.GIT_COMMIT) {
                            tag = env.GIT_COMMIT.take(7)
                        } else if (isUnix()) {
                            def gitCommit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                            if (gitCommit) {
                                tag = gitCommit
                            }
                        } else {
                            def raw = bat(script: '@git rev-parse --short HEAD', returnStdout: true).trim()
                            def lines = raw.readLines().collect { it.trim() }.findAll { !it.isEmpty() }
                            if (lines) {
                                tag = lines[-1]
                            }
                        }
                    } catch (Exception e) {
                        echo "Notice: Git commit could not be determined (${e.message}). Falling back to BUILD_NUMBER."
                    }

                    if (!tag || tag == 'null' || tag.trim() == '') {
                        tag = env.BUILD_NUMBER ?: 'latest'
                    }

                    env.IMAGE_TAG = tag

                    echo "=========================================================="
                    echo "BUILDING PIPELINE FOR: ${env.APP_NAME}"
                    echo "GIT BRANCH:            ${env.BRANCH_NAME ?: 'main'}"
                    echo "IMAGE TAG:             ${env.IMAGE_TAG}"
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
                script {
                    if (!env.IMAGE_TAG || env.IMAGE_TAG == 'null' || env.IMAGE_TAG.trim() == '') {
                        env.IMAGE_TAG = env.BUILD_NUMBER ?: 'latest'
                    }
                    echo "Building Docker image with immutable tag: ${env.DOCKER_REPO}:${env.IMAGE_TAG} and latest..."
                    if (isUnix()) {
                        sh "docker build -t ${env.DOCKER_REPO}:${env.IMAGE_TAG} -t ${env.DOCKER_REPO}:latest ."
                    } else {
                        bat "docker build -t ${env.DOCKER_REPO}:${env.IMAGE_TAG} -t ${env.DOCKER_REPO}:latest ."
                    }
                }
            }
        }

        stage('Image Publish') {
            steps {
                script {
                    if (!env.IMAGE_TAG || env.IMAGE_TAG == 'null' || env.IMAGE_TAG.trim() == '') {
                        env.IMAGE_TAG = env.BUILD_NUMBER ?: 'latest'
                    }
                    echo "Authenticating with container registry and pushing ${env.DOCKER_REPO}:${env.IMAGE_TAG}..."
                }
                // Using withCredentials prevents password leakage in logs
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}",
                                                 usernameVariable: 'DOCKER_USER',
                                                 passwordVariable: 'DOCKER_PASS')]) {
                    script {
                        if (isUnix()) {
                            sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                            sh "docker push ${env.DOCKER_REPO}:${env.IMAGE_TAG}"
                            sh "docker push ${env.DOCKER_REPO}:latest"
                            sh 'docker logout'
                        } else {
                            bat 'echo %DOCKER_PASS% | docker login -u "%DOCKER_USER%" --password-stdin'
                            bat "docker push ${env.DOCKER_REPO}:${env.IMAGE_TAG}"
                            bat "docker push ${env.DOCKER_REPO}:latest"
                            bat 'docker logout'
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    if (!env.IMAGE_TAG || env.IMAGE_TAG == 'null' || env.IMAGE_TAG.trim() == '') {
                        env.IMAGE_TAG = env.BUILD_NUMBER ?: 'latest'
                    }
                    echo "Updating Kubernetes deployment '${env.K8S_DEPLOYMENT_NAME}' to image ${env.DOCKER_REPO}:${env.IMAGE_TAG}..."
                }
                // Authenticates to Kubernetes cluster using Jenkins Secret File or Kubeconfig
                withCredentials([file(credentialsId: "${env.KUBECONFIG_CREDENTIALS_ID}", variable: 'KUBECONFIG')]) {
                    script {
                        if (isUnix()) {
                            sh "kubectl apply -f k8s/deployment.yaml -n ${env.K8S_NAMESPACE}"
                            sh "kubectl apply -f k8s/service.yaml -n ${env.K8S_NAMESPACE}"
                            sh "kubectl apply -f k8s/hpa.yaml -n ${env.K8S_NAMESPACE}"
                            sh "kubectl set image deployment/${env.K8S_DEPLOYMENT_NAME} ${env.K8S_DEPLOYMENT_NAME}=${env.DOCKER_REPO}:${env.IMAGE_TAG} -n ${env.K8S_NAMESPACE}"
                        } else {
                            bat "kubectl apply -f k8s/deployment.yaml -n ${env.K8S_NAMESPACE}"
                            bat "kubectl apply -f k8s/service.yaml -n ${env.K8S_NAMESPACE}"
                            bat "kubectl apply -f k8s/hpa.yaml -n ${env.K8S_NAMESPACE}"
                            bat "kubectl set image deployment/${env.K8S_DEPLOYMENT_NAME} ${env.K8S_DEPLOYMENT_NAME}=${env.DOCKER_REPO}:${env.IMAGE_TAG} -n ${env.K8S_NAMESPACE}"
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
            echo "Application image '${env.DOCKER_REPO}:${env.IMAGE_TAG}' is running in Kubernetes."
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
