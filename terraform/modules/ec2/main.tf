resource "aws_key_pair" "main" {
  key_name   = "${var.project_name}-key"
  public_key = file(var.public_key_path)
}

resource "aws_instance" "main" {
  ami                    = "ami-0aeb7c931a5a61206"
  instance_type          = "t3.micro"
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [var.security_group_id]
  key_name               = aws_key_pair.main.key_name

  tags = {
    Name = "${var.project_name}-server"
  }

  user_data = <<-EOF
    #!/bin/bash
    dnf update -y
    dnf install -y docker postgresql15
    systemctl start docker
    systemctl enable docker
    usermod -aG docker ec2-user
  EOF
}