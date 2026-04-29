# 🏗️ Terraform - Library API Infrastructure

Infraestructura de la Library API en AWS usando Terraform.

## Servicios creados

| Servicio | Descripción |
|---|---|
| VPC + Subnets + IGW | Red privada con acceso a internet |
| Security Groups | Reglas de firewall para EC2 y RDS |
| EC2 t3.micro | Servidor para correr la aplicación |
| RDS PostgreSQL | Base de datos en la nube |
| ECR | Repositorio de imágenes Docker |

## Requisitos

- Terraform >= 1.0
- AWS CLI configurado
- Credenciales AWS con permisos de EC2, RDS y ECR

## Cómo ejecutar

### 1. Generar key pair SSH
```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/library-api-tf
```

### 2. Inicializar Terraform
```bash
cd terraform
terraform init
```

### 3. Ver plan de ejecución
```bash
terraform plan
```

### 4. Crear infraestructura
```bash
terraform apply
```

### 5. Conectarse a EC2 por SSH
```bash
ssh -i ~/.ssh/library-api-tf ec2-user@<EC2_PUBLIC_IP>
```

### 6. Ejecutar scripts SQL
```bash
# Crear tablas
psql -h <RDS_ENDPOINT> -U postgres -d library_api_tf -f /scripts/01_create_tables.sql

# Llenar con datos
psql -h <RDS_ENDPOINT> -U postgres -d library_api_tf -f /scripts/02_seed_data.sql

# Eliminar BD
psql -h <RDS_ENDPOINT> -U postgres -d library_api_tf -f /scripts/03_drop_database.sql
```

### 7. Destruir infraestructura
```bash
terraform destroy
```

## Estructura

```
terraform/
├── main.tf           # Orquesta los módulos
├── variables.tf      # Variables globales
├── outputs.tf        # Salidas globales
├── provider.tf       # Configuración AWS
├── terraform.tfvars  # Valores de variables
├── modules/
│   ├── network/      # VPC, Subnets, IGW
│   ├── security/     # Security Groups
│   ├── ec2/          # Instancia EC2
│   ├── rds/          # Base de datos
│   └── ecr/          # Docker Registry
└── scripts/
    ├── 01_create_tables.sql
    ├── 02_seed_data.sql
    └── 03_drop_database.sql
```