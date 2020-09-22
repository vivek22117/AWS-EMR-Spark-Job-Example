####################################################
#             Bastion host AMI config              #
####################################################
//data "aws_ami" "emr" {
//  owners      = ["self"]
//  most_recent = true
//
//  filter {
//    name   = "name"
//    values = ["emr-ami"]
//  }
//}