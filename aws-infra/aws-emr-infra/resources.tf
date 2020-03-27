#############################################
# adding the zip/jar to the defined bucket  #
#############################################
resource "aws_s3_bucket_object" "rsvp_emr_jar" {
  bucket                 = data.terraform_remote_state.backend.outputs.deploy_bucket_name
  key                    = var.rsvp_emr_jar_key
  source                 = "${path.module}/../../rsvp-processor/target/emr-rsvp-processor-1.0-SNAPSHOT-jar-with-dependencies.jar"
  etag   = filemd5("${path.module}/../../rsvp-processor/target/emr-rsvp-processor-1.0-SNAPSHOT-jar-with-dependencies.jar")
}

resource "aws_emr_security_configuration" "security_configuration" {
  name = "${var.cluster_name}-${var.environment}"

  configuration = data.template_file.security_configuration.rendered
}

resource "aws_emr_cluster" "cluster" {
  name          = "${var.cluster_name}-${var.environment}"
  release_label = var.emr_release
  applications  = ["Spark", "Zeppelin", "Hadoop", "Ganglia"]
  log_uri       = "s3n://${data.terraform_remote_state.backend.outputs.log_bucket}/emr/rsvp/logs/"

  termination_protection            = false
  keep_job_flow_alive_when_no_steps = true
  visible_to_all_users = var.enable_visibility

  ec2_attributes {
    subnet_id                         = data.terraform_remote_state.vpc.outputs.private_subnets[0]
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

  tags = {
    Name        = "${var.cluster_name}-${var.environment}"
    Environment = var.environment
    Region      = var.default_region
  }

  service_role           = aws_iam_role.emr_rsvp_processor_service_role.arn
  security_configuration = aws_emr_security_configuration.security_configuration.name
  configurations         = data.template_file.configuration.rendered

  lifecycle {
    ignore_changes = ["kerberos_attributes", "step"]
    create_before_destroy = true
  }

  dynamic "step" {
    for_each = jsonencode(templatefile(data.template_file.emr_steps.*.rendered, {}))
    content {
      action_on_failure = step.value.action_on_failure
      name = step.value.name
      hadoop_jar_step = step.value.hadoop_jar_step
    }
  }
}
