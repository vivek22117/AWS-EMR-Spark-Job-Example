####################################
#  KMS Key to encrypt data at rest #
####################################
resource "aws_kms_key" "emr_encryption_key" {
  description = var.kms_description

  key_usage               = "ENCRYPT_DECRYPT"
  policy                  = data.template_file.kms_policy.rendered
  deletion_window_in_days = var.deletion_time_limit
  is_enabled              = var.is_key_enabled
  enable_key_rotation     = var.key_rotation_enabled

  tags = merge(local.common_tags, map("Name", "EMR_KMS_KEY"))
}

resource "aws_kms_alias" "emr_encryption_key_alias" {
  name          = "alias/ddsolitions"
  target_key_id = aws_kms_key.emr_encryption_key.id
}