###########################################################
#   Remote state VPC                                      #
###########################################################
data "terraform_remote_state" "vpc" {
  backend = "s3"

  config = {
    profile = "doubledigit"
    bucket  = "${var.s3_bucket_prefix}-${var.environment}-${var.default_region}"
    key     = "state/${var.environment}/vpc/terraform.tfstate"
    region  = var.default_region
  }
}

###########################################################
#   Remote state to fetch s3 deploy bucket or log bucket  #
###########################################################
data "terraform_remote_state" "backend" {
  backend = "s3"

  config = {
    profile = "doubledigit"
    bucket  = "${var.s3_bucket_prefix}-${var.environment}-${var.default_region}"
    key     = "state/${var.environment}/aws/terraform.tfstate"
    region  = var.default_region
  }
}

data "template_file" "kms_policy" {
  template = file("${path.module}/scripts/kms_policy.json.tpl")

  vars = {
    account_id = data.aws_caller_identity.current.id
    emr_service_role = aws_iam_role.emr_rsvp_processor_service_role.arn
    rsvp_ec2_processor_role = aws_iam_role.emr_rsvp_processor_ec2_role.arn
  }
}

data "template_file" "security_configuration" {
  template = file("${path.module}/scripts/security_config.json.tpl")

  vars = {
    kms_key_id = aws_kms_key.emr_encryption_key.key_id
  }
}

data "template_file" "configuration" {
  template = file("${path.module}/scripts/config.json.tpl")

  vars = {
    enable_dynamic_allocation = var.enable_dynamic_allocation
    num_exec_instances        = var.num_exec_instances
    spark_mem_frac            = var.spark_mem_frac
    spark_storage_mem_frac    = var.spark_storage_mem_frac
    exec_java_opts            = var.exec_java_opts
    driver_java_opts          = var.driver_java_opts
    spark_storage_level       = var.spark_storage_level
    compression_enabled       = var.compression_enabled
    distinct_enabled          = var.distinct_enabled
    dynamic_partition_enabled = var.dynamic_partition_enabled

    enable_max_resource_alloc   = var.enable_max_resource_alloc
    enable_dynamic_alloc        = var.enable_dynamic_alloc
    spark_shuffle_partitions    = var.spark_shuffle_partitions
    yarn_exec_memory_overhead   = var.yarn_exec_memory_overhead
    spark_parallelism           = var.spark_parallelism
    yarn_driver_memory_overhead = var.yarn_driver_memory_overhead

    environment = var.environment

    enable_s3_sse = var.enable_s3_sse
  }
}

data "template_file" "emr_steps" {
  template = file("${path.module}/scripts/steps.json.tpl")

  vars = {
    driver_memory = var.driver_memory
    driver_cores = var.driver_cores
    num_executors = var.num_executors
    executor_memory = var.executor_memory
    executor_cores = var.executor_cores
    rsvp_reader_class = var.rsvp_reader_class
    jar_location = var.jar_location
    first_step_name = var.first_step_name
  }
}

data "aws_caller_identity" "current" {}