resource "aws_instance" "My_ec2_Instance" {
    ami = "ami-0c02fb55956c7d316"
    key_name = "AWS DevOps Trng"
    instance_type = "t2.micro" //var.instance_type
    security_groups= ["launch-wizard-21"]
    root_block_device {
        encrypted     = true
    }
    metadata_options {
        http_endpoint = "enabled"
        http_tokens   = "required"
    }
    tags= {
        Name = "Shrey AWS Terraform"
        Project = "FT"
        Environment = "Dev"
    }
}

output "instance_id" {
    value = "${aws_instance.My_ec2_Instance.id}"
}

output "public_ip" {
    value = "${aws_instance.My_ec2_Instance.*.public_ip}"
}

