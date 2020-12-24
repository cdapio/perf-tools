variable "credentials_path" {
  description = "Path to the credential file"
}

variable "project_id" {
  description = "Project ID"
}

variable "default_region" {
  description = "Default Region"
  default     = "us-west1"
}

variable "default_zone" {
  description = "Default Zone"
  default     = "us-west1-a"
}

variable "cdf_vpc_subnet_cidr_range" {
  description = "CIDR range for the VPC subnet"
  default     = "10.0.0.0/20"
}

variable "database_vpc_subnet_cidr_range" {
  description = "CIDR range for the VPC subnet"
  default     = "10.10.0.0/20"
}

variable "cdf_uc1_enable_logging" {
  description = "Whether to enable Stackdriver logging for the Use Case 1 Data Fusion instance"
  default     = true
}

variable "cdf_uc1_enable_monitoring" {
  description = "Whether to enable Stackdriver monitoring for the Use Case 1 Data Fusion instance"
  default     = true
}
