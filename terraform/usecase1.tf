# VPC network for the CDF instance
resource "google_compute_network" "cdf_vpc" {
  name                    = "cdf-benchmarking-cdf-vpc"
  auto_create_subnetworks = false
  project                 = var.project_id
}

# Subnet for the CDF VPC Network
resource "google_compute_subnetwork" "cdf_vpc_subnet" {
  name          = "cdf-benchmarking-cdf-vpc-subnet1"
  project       = var.project_id
  ip_cidr_range = var.cdf_vpc_subnet_cidr_range
  region        = var.default_region
  network       = google_compute_network.cdf_vpc.name
}

# Private CDF instance for Use Case 1
resource "google_data_fusion_instance" "cdf_instance_uc1" {
  provider                      = google-beta
  name                          = "cdf-benchmarking-uc1"
  project                       = var.project_id
  region                        = var.default_region
  type                          = "ENTERPRISE"
  version                       = "6.2.2"
  private_instance              = true
  enable_stackdriver_logging    = var.cdf_uc1_enable_logging
  enable_stackdriver_monitoring = var.cdf_uc1_enable_monitoring

  network_config {
    ip_allocation = "${google_compute_global_address.private_ip_alloc.address}/${google_compute_global_address
                    .private_ip_alloc.prefix_length}"
    network       = google_compute_network.cdf_vpc.name
  }
}

# Allocates a private IP range for the private CDF instance
resource "google_compute_global_address" "private_ip_alloc" {
  name          = "cdf-benchmarking-private-ip-range"
  project       = var.project_id
  network       = google_compute_network.cdf_vpc.id
  address_type  = "INTERNAL"
  purpose       = "VPC_PEERING"
  prefix_length = 22
}

# Peering connection between private CDF instance VPC and the Tenant project VPC
resource "google_compute_network_peering" "cdf_tenant_peering" {
  name                 = "cdf-benchmarking-cdf-to-tenant"
  network              = google_compute_network.cdf_vpc.id
  # uses the regex function to extract the private CDF tenant project id
  peer_network         = "projects/${element(regex("@([a-zA-Z0-9-]+).", google_data_fusion_instance.cdf_instance_uc1
                         .service_account), 0)}/global/networks/${var.default_region}-${google_data_fusion_instance
                         .cdf_instance_uc1.name}"
  export_custom_routes = true
  import_custom_routes = true
}

# VPC network for the Database VMs
resource "google_compute_network" "db_vpc" {
  name                    = "cdf-benchmarking-database-vpc"
  auto_create_subnetworks = false
  project                 = var.project_id
}

# Subnet for the Database VPC Network
resource "google_compute_subnetwork" "db_vpc_subnet" {
  name          = "cdf-benchmarking-database-vpc-subnet1"
  project       = var.project_id
  ip_cidr_range = var.database_vpc_subnet_cidr_range
  region        = var.default_region
  network       = google_compute_network.db_vpc.name
}

# Allows traffic to port 22 for the Database VPC
resource "google_compute_firewall" "db_ssh_firewall" {
  name     = "cdf-benchmarking-database-vpc-allow-ssh"
  project  = var.project_id
  network  = google_compute_network.db_vpc.name
  priority = 100

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }
}

# Peering connection between the Database VPC and private CDF instance VPC
resource "google_compute_network_peering" "db_to_cdf_peering" {
  name                 = "cdf-benchmarking-database-to-cdf"
  network              = google_compute_network.db_vpc.id
  peer_network         = google_compute_network.cdf_vpc.id
  export_custom_routes = true
  import_custom_routes = true
}

# Peering connection between the private CDF VPC and Database VPC
resource "google_compute_network_peering" "cdf_to_db_peering" {
  name                 = "cdf-benchmarking-cdf-to-database"
  network              = google_compute_network.cdf_vpc.id
  peer_network         = google_compute_network.db_vpc.id
  export_custom_routes = true
  import_custom_routes = true
}

# Static IP address for VM with 32 CPUs
resource "google_compute_address" "sqlserver_static_ip_uc1_32cpus" {
  name    = "sql-server-uc1-32cpus-ip"
  project = var.project_id
  region  = var.default_region
}

# Static IP address for VM with 64 CPUs
resource "google_compute_address" "sqlserver_static_ip_uc1_64cpus" {
  name    = "sql-server-uc1-64cpus-ip"
  project = var.project_id
  region  = var.default_region
}

# Compute Engine VM (32 CPUs) with SQL Server preinstalled using a startup script
resource "google_compute_instance" "sqlserver_vm_uc1_32cpus" {
  name                    = "sql-server-uc1-32cpus"
  project                 = var.project_id
  zone                    = var.default_zone
  machine_type            = "n2-standard-32"
  metadata_startup_script = file("./installation-scripts/install-sql-server.sh")

  boot_disk {
    initialize_params {
      image = "ubuntu-os-cloud/ubuntu-1804-lts"
      size  = "2500"
    }
  }

  network_interface {
    network    = google_compute_network.db_vpc.name
    subnetwork = google_compute_subnetwork.db_vpc_subnet.name
    access_config {
      nat_ip = google_compute_address.sqlserver_static_ip_uc1_32cpus.address
    }
  }
}

# Compute Engine VM (64 CPUs) with SQL Server preinstalled using a startup script
resource "google_compute_instance" "sqlserver_vm_uc1_64cpus" {
  name                    = "sql-server-uc1-64cpus"
  project                 = var.project_id
  zone                    = var.default_zone
  machine_type            = "n2-standard-64"
  metadata_startup_script = file("./installation-scripts/install-sql-server.sh")

  boot_disk {
    initialize_params {
      image = "ubuntu-os-cloud/ubuntu-1804-lts"
      size  = "2500"
    }
  }

  network_interface {
    network    = google_compute_network.db_vpc.name
    subnetwork = google_compute_subnetwork.db_vpc_subnet.name
    access_config {
      nat_ip = google_compute_address.sqlserver_static_ip_uc1_64cpus.address
    }
  }
}
