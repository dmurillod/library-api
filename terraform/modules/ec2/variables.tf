variable "project_name" {
  description = "Project name"
  type        = string
}

variable "subnet_id" {
  description = "Subnet ID"
  type        = string
}

variable "security_group_id" {
  description = "Security group ID"
  type        = string
}

variable "public_key_path" {
  description = "Path to public key file"
  type        = string
  default     = "~/.ssh/library-api-tf-ed25519.pub"
}