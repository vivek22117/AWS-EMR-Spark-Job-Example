//Global Variables
variable "profile" {
  type        = string
  description = "AWS Profile name for credentials"
}

variable "environment" {
  type        = string
  description = "AWS Profile name for credentials"
}

variable "owner_team" {
  type        = string
  description = "Name of owner team"
}

variable "component_name" {
  type        = string
  description = "Component name for resources"
}

######################################
# Runtime Variables                  #
######################################


#######################################
#  Default Variables                  #
#######################################
variable "default_region" {
  type    = string
  default = "us-east-1"
}

variable "dyanamoDB_prefix" {
  type    = string
  default = "teamconcept-tfstate"
}

variable "s3_bucket_prefix" {
  type    = string
  default = "teamconcept-tfstate"
}


####===================EMR Cluster variables========================#####
variable "cluster_name" {
  type        = string
  description = "Name of the cluster to create"
}

variable "emr_release" {
  type        = string
  description = "AWS EMR release label"
}

variable "bid_price" {
  type        = string
  description = "Bid price for slave instances"
}

variable "kms_description" {
  type        = string
  description = "KMS key to encrypt data at rest in HDFS"
}

variable "deletion_time_limit" {
  type        = number
  description = "The duration in days after which the key is deleted after destruction of the resource"
}

variable "is_key_enabled" {
  type        = bool
  description = "Set to false to prevent the module from creating any resources"
}

variable "key_rotation_enabled" {
  type        = bool
  description = "Specifies whether key rotation is enabled"
}

variable "enable_visibility" {
  type = bool
  description = "Whether the job flow is visible to all IAM users of the AWS account associated with the job flow"
}

####==================Master Instance Variables=====================###
variable "master_instance_type" {
  type        = string
  description = "Master instance type"
}

variable "master_ebs_volume_size" {
  type        = number
  description = "Master EBS volume size"
}

variable "master_volume_type" {
  type        = string
  description = "Master EBS volume type"
}

####==================Salves Instance Variables=====================###
variable "ssh_key_name" {
  type        = string
  description = "SSH key pair name to access EC2"
}

variable "core_instance_type" {
  type        = string
  description = "Slave instance type"
}

variable "ebs_volume_size" {
  type        = number
  description = "EBS volume size"
}

variable "volume_type" {
  type        = string
  description = "Volume type"
}

variable "core_instance_count" {
  type        = string
  description = "Number of Instance type"
}

###=========================Configuration Variables=================##
variable "enable_dynamic_allocation" {
  type        = string
  description = "Whether dynamic allocation of resources is enabled or not"
}

variable "num_exec_instances" {
  type        = string
  description = "Number of executor instances"
}

variable "spark_mem_frac" {
  type        = string
  description = "Fraction of memory allocate to spark"
}

variable "spark_storage_mem_frac" {
  type        = string
  description = "Fraction of memory allocate for spark storage"
}

variable "exec_java_opts" {
  type        = string
  description = "Executor extra options to improve garbage collectior performance"
}

variable "driver_java_opts" {
  type        = string
  description = "Driver extra options to improve garbage collector performance"
}

variable "spark_storage_level" {
  type        = string
  description = "Storage level applicable for spark processor"
}

variable "compression_enabled" {
  type        = string
  description = "Compression enabled for rdd, shuffling, spill"
}

###=======================Spark Configuration========================================####
variable "enable_max_resource_alloc" {
  type        = string
  description = "Enable maximumResourceAllocation for Spark Cluster"
}

variable "enable_dynamic_alloc" {
  type        = string
  description = "Enable dynamic allocation"
}

variable "spark_shuffle_partitions" {
  type        = string
  description = "Number of partition at the time of shuffling"
}

variable "yarn_exec_memory_overhead" {
  type        = string
  description = "Memory overhead allocation to yarn executor"
}

variable "spark_parallelism" {
  type        = string
  description = "Default parallelism for spark processing"
}

variable "yarn_driver_memory_overhead" {
  type        = string
  description = "Memory overhead allocateion to yarn driver"
}

variable "enable_s3_sse" {
  type        = string
  description = "Enable server side encryption for S3 emrfs"
}

variable "dynamic_partition_enabled" {
  type = string
  description = "Dynamic partition pruning improves job performance by selecting specific partitions"
}

variable "distinct_enabled" {
  type = string
  description = "This optimization eliminates duplicate values in each collection before computing the intersection"
}

###=======================EMR Steps Configuration========================================####
variable "driver_memory" {
  type = string
  description = "Driver memory allocated to Maseter instance for spark configuration"
}

variable "driver_cores" {
  type = string
  description = "Driver cores allocated to master instance"
}

variable "num_executors" {
  type = string
  description = "Number of executors created using core instances"
}

variable "executor_memory" {
  type = string
  description = "Memory allocated to each executor created"
}

variable "executor_cores" {
  type = string
  description = "Number of cores in each executor"
}

variable "rsvp_reader_class" {
  type = string
  description = "Class name for RSVP record reader"
}

variable "jar_location" {
  type = string
  description = "S3 location for spark jar"
}

variable "rsvp_emr_jar_key" {
  type = string
  description = "Bucket key for rsvp processor jar"
}

variable "first_step_name" {
  type = string
  description = "First emr step name"
}
//Local variables
locals {
  common_tags = {
    owner       = "Vivek"
    team        = "TeamConcept"
    environment = var.environment
  }
}