###########################################################
#                Remote state VPC                         #
###########################################################
data "terraform_remote_state" "vpc" {
  backend = "s3"

  config = {
    profile = "admin"
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
    profile = "admin"
    bucket  = "${var.s3_bucket_prefix}-${var.environment}-${var.default_region}"
    key     = "state/${var.environment}/backend/terraform.tfstate"
    region  = var.default_region
  }
}

data "template_file" "kms_policy" {
  template = file("${path.module}/scripts/kms_policy.json")

  vars = {
    account_id = data.aws_caller_identity.current.id
    emr_service_role = aws_iam_role.emr_rsvp_processor_service_role.arn
    rsvp_ec2_processor_role = aws_iam_role.emr_rsvp_processor_ec2_role.arn
  }
}

data "template_file" "emr_service_policy" {
  template = file("${path.module}/scripts/emr_service_policy.json")

  vars = {
    emr_kms_arn = aws_kms_key.emr_encryption_key.arn
  }
}

data "template_file" "emr_ec2_policy" {
  template = file("${path.module}/scripts/emr_ec2_access_policy.json")

  vars = {
    emr_kms_arn = aws_kms_key.emr_encryption_key.arn
  }
}


data "template_file" "security_configuration" {
  template = file("${path.module}/scripts/security_config.json.tpl")

  vars = {
    kms_key_id = aws_kms_key.emr_encryption_key.arn
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