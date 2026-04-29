module "network" {
  source       = "./modules/network"
  project_name = var.project_name
}

module "security" {
  source       = "./modules/security"
  project_name = var.project_name
  vpc_id       = module.network.vpc_id
}

module "ec2" {
  source            = "./modules/ec2"
  project_name      = var.project_name
  subnet_id         = module.network.public_subnet_id
  security_group_id = module.security.ec2_security_group_id
}

module "rds" {
  source             = "./modules/rds"
  project_name       = var.project_name
  db_username        = var.db_username
  db_password        = var.db_password
  db_name            = var.db_name
  subnet_ids         = module.network.subnet_ids
  security_group_id  = module.security.rds_security_group_id
}

module "ecr" {
  source       = "./modules/ecr"
  project_name = var.project_name
}