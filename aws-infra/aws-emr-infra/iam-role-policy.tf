###=================EMR IAM Resources=====================###
resource "aws_iam_role" "emr_rsvp_processor_service_role" {
  name = "RSVPProcessorEMRServiceRole"
  path = "/"

  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
               "Service": "elasticmapreduce.amazonaws.com"
            },
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource "aws_iam_policy" "emr_rsvp_service_policy" {
  name        = "RSVPProcessorEMRServicePolicy"
  path        = "/"

  policy = data.template_file.emr_service_policy.rendered
}

resource "aws_iam_role_policy_attachment" "emr_rsvp_service_role" {
  role = aws_iam_role.emr_rsvp_processor_service_role.name
  policy_arn = aws_iam_policy.emr_rsvp_service_policy.arn
}

###=================EMR IAM EC2 Resources=====================###
resource "aws_iam_role" "emr_rsvp_processor_ec2_role" {
  name = "RSVPProcessorEMREC2Role"
  path = "/"

  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
               "Service": "ec2.amazonaws.com"
            },
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource "aws_iam_policy" "emr_rsvp_ec2_policy" {
  name        = "RSVPProcessorEMREC2Policy"
  path        = "/"

  policy = data.template_file.emr_ec2_policy.rendered
}

resource "aws_iam_role_policy_attachment" "emr_ec2_role_policy_att" {
  role       = aws_iam_role.emr_rsvp_processor_ec2_role.name
  policy_arn = aws_iam_policy.emr_rsvp_ec2_policy.arn
}

resource "aws_iam_instance_profile" "emr_ec2_instance_profile" {
  name = "RSVPProcessorEMREC2Profile"
  role = aws_iam_role.emr_rsvp_processor_ec2_role.name
}