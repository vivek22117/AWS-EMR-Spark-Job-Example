####################################
#  KMS Key to encrypt data at rest #
####################################
resource "aws_kms_key" "emr_encryption_key" {
  description = var.kms_description

  key_usage = "ENCRYPT_DECRYPT"
  //  policy                  = data.template_file.kms_policy.rendered
  deletion_window_in_days = var.deletion_time_limit
  is_enabled = var.is_key_enabled
  enable_key_rotation = var.key_rotation_enabled

  tags = merge(local.common_tags, map("Name", "EMR_KMS_KEY"))

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Id": "kms-key-policy",
  "Statement": [
    {
      "Sid": "Enable IAM User Permissions",
      "Effect": "Allow",
      "Principal": {"AWS": "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"},
      "Action": "kms:*",
      "Resource": "*"
    },
    {
        "Sid": "Allow administration of the key",
        "Effect": "Allow",
        "Principal": { "AWS": "arn:aws:iam::${data.aws_caller_identity.current.account_id}:user/eks-user" },
        "Action": [
            "kms:Create*",
            "kms:Describe*",
            "kms:Enable*",
            "kms:List*",
            "kms:Put*",
            "kms:Update*",
            "kms:Revoke*",
            "kms:Disable*",
            "kms:Get*",
            "kms:Delete*",
            "kms:ScheduleKeyDeletion",
            "kms:CancelKeyDeletion"
         ],
         "Resource": "*"
      },
      {
        "Sid": "Allow use of the key",
        "Effect": "Allow",
        "Principal": {
          "AWS": [
            "${aws_iam_role.emr_rsvp_processor_service_role.arn}",
            "${aws_iam_role.emr_rsvp_processor_ec2_role.arn}",
            "arn:aws:iam::${data.aws_caller_identity.current.account_id}:user/eks-user"
            ]
        },
        "Action": [
            "kms:DescribeKey",
            "kms:Encrypt",
            "kms:Decrypt",
            "kms:ReEncrypt*",
            "kms:GenerateDataKey",
            "kms:GenerateDataKeyWithoutPlaintext"
        ],
        "Resource": "*"
      },
    {
      "Sid": "Allow attachment of persistent resources",
      "Effect": "Allow",
            "Principal": {
          "AWS": [
          "arn:aws:iam::${data.aws_caller_identity.current.account_id}:user/eks-user"
        ]
      },
      "Action": [
        "kms:CreateGrant",
        "kms:ListGrants",
        "kms:RevokeGrant"
      ],
      "Resource": "*"
    }
  ]
}

EOF
}

resource "aws_kms_alias" "emr_encryption_key_alias" {
  name = "alias/ddsolutions"

  target_key_id = aws_kms_key.emr_encryption_key.id
}