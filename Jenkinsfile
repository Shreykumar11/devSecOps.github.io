def remote = [:]
def scan_type

pipeline
{
    agent any
    
	tools
	{
        maven "maven"
        //jdk "Java"
    }
    
    parameters 
                {
                    choice  choices: ["Baseline", "Full"],
                    description: 'Type of scan that is going to perform inside the container',
                    name: 'SCAN_TYPE'
                }
	
	/*post {
             always {
                 echo "Removing container"
                 sh '''
                     docker stop owasp
                     docker rm owasp
                 '''
             }
         }*/
        
	stages
	{
		stage("Checkout code")
		{
			steps
			{
				checkout([$class: 'GitSCM', branches: [[name: '*/branch1']], extensions: [], userRemoteConfigs: 
				[[credentialsId: 'DemoAssignment3', url: 
				'https://git.nagarro.com/freshertraining2022/shreysaxena.git']]])
			}
		}
		
		stage("Execute Maven & SonarQube Scanner")
		{
			steps 
			{
			    withSonarQubeEnv(installationName: 'Sonar', credentialsId: 'SonarQubeToken') 
                {
                    sh 'mvn -f Assignment1/JavaController/pom.xml clean install sonar:sonar'
                }
            }
		}
		
		stage("PMD Scanner & Checkstyle Scanner & SpotBugs Scanner (PCS) for GitLab")
		{
		    steps
		    {
		        sh 'mvn -f Assignment1/JavaController/pom.xml pmd:pmd'
		        sh 'mvn -f Assignment1/JavaController/pom.xml checkstyle:checkstyle'
		        sh 'mvn -f Assignment1/JavaController/pom.xml spotbugs:spotbugs'
		    }
		}
		
		stage("Pubish PCS Reports")
		{
		    steps
		    {
		        recordIssues(enabledForFailure: true, aggregatingResults: true, 
                    tools: [java(), checkStyle(pattern: '**/target/checkstyle-result.xml', reportEncoding: 'UTF-8')])
            
                recordIssues enabledForFailure: true, tool: pmdParser(pattern: '**/target/pmd.xml')
                recordIssues enabledForFailure: true, tool: spotBugs(pattern: '**/target/spotbugsXml.xml')
            }
        }
		
		stage("Run Unit Test Cases")
		{
			steps
			{
				sh 'mvn -f Assignment1/JavaController/pom.xml test'
			}
		}
		
		stage("Publish Junit test results")
		{
			steps
			{
				junit 'Assignment1/JavaController/target/surefire-reports/*.xml'
				jacoco()
			}
		}
		
		stage("Artifactory Server")
		{
		    steps
		    {
		        rtServer(id: "Artifactory",
		                url: 'http://192.168.56.105:8082/artifactory',
		                bypassProxy: true,
		                timeout: 300)
		    }
		}
		
		stage("Artifactory Upload")
		{
		    steps
		    {
		        rtUpload(serverId: "Artifactory",
		                spec: '''
		                {
		                    "files": [{"pattern": "Assignment1/JavaController/target/*.war",
		                    "target": "artifactory-repos-libs-snapshot-local",
		                    "target": "artifactory-repos-libs-snapshot-local/Version_${BUILD_TIMESTAMP}_${BUILD_ID}.war"}]
		                }''',)
		    }
		}
		
		stage('DAST for Archives') 
		{
            parallel 
            {
                stage("OWASP ZAP2")
                {
                    steps
                    {
                        script 
                        {
                            scan_type = "${params.SCAN_TYPE}"
                            
                            if(scan_type == "Baseline")
                            {
                                echo '**********BASELNE SCAN**********'
		                        sh 'sudo docker run -t owasp/zap2docker-stable zap-baseline.py -t http://192.168.56.105:8082/artifactory/artifactory-repos-libs-snapshot-local/JavaController-0.0.1-SNAPSHOT.war || true'
		                    }
		                    
		                    else if(scan_type == "Full")
                            {
		                        echo '**********FULL SCAN**********'
		                        sh 'sudo docker run -t owasp/zap2docker-stable zap-full-scan.py -t http://192.168.56.105:8082/artifactory/artifactory-repos-libs-snapshot-local/JavaController-0.0.1-SNAPSHOT.war || true'
		                    }
		                    
		                    else
		                    {
		                        echo 'Something went wrong...'
		                    }
		                }
		            }
		        }
		    }
		}
		
		stage("Artifactory Publish")
		{
		    steps
		    {
		        rtPublishBuildInfo(serverId: "Artifactory")
		    }
		}   
		
		/*stage("Artifactory Pull")
		{
		    steps
		    {
		        withCredentials([string(credentialsId: 'Artifactpass', variable: 'Artifacts')]) 
		        {
                    sh "sudo curl -u admin:${Artifacts} -o /root/dockerfile/JavaController-0.0.1-SNAPSHOT.war http://192.168.56.105:8082/artifactory/artifactory-repos-libs-snapshot-local/JavaController-0.0.1-SNAPSHOT.war"
                }
		        sh 'wget -P /root/ http://192.168.56.105:8082/artifactory/artifactory-repos-libs-snapshot-local/JavaController-0.0.1-SNAPSHOT.war'
		    }
		}*/
		
		stage('Docker Build & Tag') 
        {
            steps
            {
                sh "if sudo docker ps -a | grep myProject; then sudo docker rm -f myProject; fi"
                sh "if sudo docker images | grep latest; then sudo docker rmi -f shreysaxena/assignment7:latest; fi"
                sh "if sudo docker images | grep Vrsn_${currentBuild.number -1}; then sudo docker rmi -f shreysaxena/assignment7:Vrsn_${currentBuild.number -1}; fi"
                
                sh 'sudo docker build -t shreysaxena/assignment7:latest /var/lib/jenkins/workspace/DevSecOps-Pipeline/Assignment1/JavaController/.'
                sh 'sudo docker tag shreysaxena/assignment7:latest shreysaxena/assignment7:Vrsn_${BUILD_NUMBER}'
            }
        }
        
        stage("Trivy Scanner for Docker Image")
        {
            steps
            {
                //sh 'trivy image --exit-code 1 --severity CRITICAL shreysaxena/assignment7:latest'
                
                sh 'trivy image --no-progress shreysaxena/assignment7:latest'
            }
        } 
     
        stage('Push Docker image') 
        {
            steps 
            {
                withCredentials([string(credentialsId: 'docker-pass', variable: 'DockerHubPass')]) 
                {
                    sh "sudo docker login -u shreysaxena -p ${DockerHubPass}"
                }
                
                sh  'sudo docker push shreysaxena/assignment7:Vrsn_${BUILD_NUMBER}'
                sh  'sudo docker push shreysaxena/assignment7:latest' 
            }
        }
     
        stage('Run Docker container on Jenkins Agent') 
        {
            steps 
            {
                sh "sudo docker run -d -p 7050:8080 --name myProject shreysaxena/assignment7:latest"
            }
        }   
        
        /*stage("Start ec2-Instance")
        {
            steps
            {
                sh 'if aws ec2 describe-instance-status --instance-ids i-0af82344c97860de4 | grep Code | Code == 16; then aws ec2 start-instances --region us-east-1 --instance-ids i-0af82344c97860de4; fi'
            }
        }*/
        
        stage("Ansible-Lint")
        {
            steps
            {
                sh 'sudo ansible-lint Assignment1/JavaController/ansible/shreyAws.yml'
                echo 'PASSED'
            }
        }
        
        stage("Ansible Playbook")
        {
            steps
            {
                sh 'sudo ansible-playbook Assignment1/JavaController/ansible/shreyAws.yml -i Assignment1/JavaController/ansible/inventory.txt'
            }
        }
        
        stage("Run Docker container on AWS EC2 Instance")
		{
		    steps
		    {
		        withCredentials([sshUserPrivateKey(credentialsId: 'AWS-Trng', keyFileVariable: 'AWS', usernameVariable: 'ec2-user')])
		        {
                    script
                    {
                        remote.name = 'shrey'
                        remote.user = 'ec2-user'
                        remote.host = '52.21.252.144'
                        remote.allowAnyHosts = true
                        remote.identityFile = AWS
                    }
                    
                    sshCommand remote: remote, command:
                    '''
                    if sudo docker ps -a | grep myProject; then sudo docker rm -f myProject; fi
                    sudo docker run -d -p 7050:8080 --name myProject shreysaxena/assignment7:latest
                    '''
                }
            }
        }   
        
        stage("AWS Tomcat (W/O Docker)")
		{
		    steps
		    {
                echo "Process done through ansible-playbook"
            }
        }
        
        stage("Checkov Scanner for Terraform")
        {
            steps
            {
                //sh 'docker pull bridgecrew/checkov:2.0.1024'
                sh 'sudo python3 -m pip install checkov'
                sh 'checkov --version'
                sh 'checkov -d Assignment1/JavaController/shrey-terraform'
                //echo 'PASSED'
            }
        } 
        
        stage("Terraform")
        {
            steps
            {
                sh 'sudo cp -R /var/lib/jenkins/workspace/DevSecOps-Pipeline/Assignment1/JavaController/shrey-terraform/. /var/lib/jenkins/workspace/DevSecOps-Pipeline'
                sh 'sudo terraform init'
                sh 'sudo terraform plan'
                echo "Instance created"
                sh "sudo terraform apply --auto-approve"
            }
        }
        
        stage("Run Docker container on AWS Terraform EC2 Instance")
		{
		    environment
		    { 
		        name = sh (script:" terraform output -json public_ip | jq -r '.[0]'", returnStdout: true).trim() 
		        //name = sh (script: 'terraform output public_ip', returnStdout: true).trim()
		        value = sh (script: 'terraform output instance_id', returnStdout: true).trim()
		    }
		    
		    steps
		    {
                echo "My terraform public ip is = ${name}"
                echo "My terraform instance id is = ${value}"
		        
		        withCredentials([sshUserPrivateKey(credentialsId: 'AWS-Trng', keyFileVariable: 'AWS', usernameVariable: 'ec2-user')])
		        {
                    script
                    {
                        remote.name = 'shrey-terraform'
                        remote.user = 'ec2-user'
                        remote.host = "${name}"
                        remote.allowAnyHosts = true
                        remote.identityFile = AWS
                    }
                    echo "My terraform public ip is = ${name}"
                    sshCommand remote: remote, command:
                    '''
                    
                    sudo yum update -y
                    sudo yum install docker -y
                    sudo systemctl start docker
                    sudo systemctl enable docker
                    if sudo docker ps -a | grep myProject; then sudo docker rm -f myProject; fi
                    sudo docker run -d -p 7050:8080 --name myProject shreysaxena/assignment7:latest
                    '''
                }
            }
        }
        
        stage("Destroy Terraform instance")
        {
            environment
		    { 
		        value = sh (script: 'terraform output instance_id', returnStdout: true).trim()
		    }
		    
            steps
            {
                echo "destroy the Terraform instance --> ${value}"
                sh "sudo terraform destroy --auto-approve"
            }
        }
        
        stage("Centos 7 System Security Scan")
		{
		    steps
		    {
		        sh 'sudo yum install rkhunter -y'
		        sh 'sudo rkhunter --propupd'
		        sh 'sudo rkhunter --checkall --sk || true'
		        sh 'sudo cat /var/log/rkhunter/rkhunter.log | grep -i warning'
		    }
		}   
	}
}
