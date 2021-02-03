#############################################
# adding the zip/jar to the defined bucket  #
#############################################
//resource "aws_s3_bucket_object" "rsvp_emr_jar" {
//  bucket                 = data.terraform_remote_state.backend.outputs.artifactory_bucket_name
//  key                    = var.rsvp_emr_jar_key
//  source                 = "${path.module}/../../rsvp-processor/target/emr-rsvp-processor-1.0-SNAPSHOT-jar-with-dependencies.jar"
//  etag   = filemd5("${path.module}/../../rsvp-processor/target/emr-rsvp-processor-1.0-SNAPSHOT-jar-with-dependencies.jar")
//}

//resource "aws_s3_bucket_object" "ria_emr_jar" {
//  bucket                 = data.terraform_remote_state.backend.outputs.artifactory_bucket_name
//  key                    = var.rsvp_emr_jar_key
//  source                 = "${path.module}/../../ria-data-processor/target/ria-data-processor-jar-with-dependencies.jar"
//  etag   = filemd5("${path.module}/../../ria-data-processor/target/ria-data-processor-jar-with-dependencies.jar")
//}


resource "aws_emr_security_configuration" "security_configuration" {
  name = "${var.cluster_name}-${var.environment}"

  configuration = data.template_file.security_configuration.rendered
}

resource "aws_emr_cluster" "cluster" {
  name          = "${var.cluster_name}-${var.environment}"

  release_label = var.emr_release
  applications  = ["Spark", "Zeppelin", "Hadoop", "Ganglia"]

  log_uri       = "s3://${data.terraform_remote_state.backend.outputs.dataLake_bucket_name}/emr/rsvp/logs/"

  termination_protection            = false
  keep_job_flow_alive_when_no_steps = true
  visible_to_all_users = var.enable_visibility
//  custom_ami_id = data.aws_ami.emr.id

  ec2_attributes {
    subnet_id                         = data.terraform_remote_state.vpc.outputs.private_subnets[1]
    service_access_security_group     = aws_security_group.service_sg.id
    emr_managed_master_security_group = aws_security_group.driver_sg.id
    emr_managed_slave_security_group  = aws_security_group.nodes_sg.id
    instance_profile                  = aws_iam_instance_profile.emr_ec2_instance_profile.arn
    key_name                          = var.ssh_key_name
  }

  master_instance_group {
    instance_type = var.master_instance_type
    bid_price     = var.bid_price
    ebs_config {
      size = var.master_ebs_volume_size
      type = var.master_volume_type
    }
  }

  core_instance_group {
    instance_type  = var.core_instance_type
    instance_count = var.core_instance_count
    bid_price      = var.bid_price

    ebs_config {
      size                 = var.ebs_volume_size
      type                 = var.volume_type
      volumes_per_instance = 1
    }
  }

/*  bootstrap_action {
    path = "s3://doubledigit-aritifactory-qa-us-east-1/bootstrap-actions/setup-config.sh"
    name = "setup-config.sh"
  }*/

  tags = {
    Name        = "${var.cluster_name}-${var.environment}"
    Environment = var.environment
    Region      = var.default_region
    Project = "DoubleDigit-Solutions"
  }

  service_role           = aws_iam_role.emr_rsvp_processor_service_role.arn
  security_configuration = aws_emr_security_configuration.security_configuration.name
//  configurations         = data.template_file.configuration.rendered


  step_concurrency_level = 1

  lifecycle {
    create_before_destroy = true
  }

  dynamic "step" {
    for_each = jsondecode(data.template_file.emr_steps.rendered)
    content {
      action_on_failure = step.value.action_on_failure
      name = step.value.name
      hadoop_jar_step {
        jar = step.value.hadoop_jar_step.jar
        args = step.value.hadoop_jar_step.args
      }
    }
  }

  configurations_json = templatefile("${path.module}/scripts/config.json.tpl", {

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
  })
}
