####===================Security Group for Spark Driver===========####
resource "aws_security_group" "driver_sg" {
  name        = "spark-driver-sg"
  description = "Allow traffic from port elb and enable SSH"
  vpc_id      = data.terraform_remote_state.vpc.outputs.vpc_id

  lifecycle {
    create_before_destroy = true
  }
  tags = local.common_tags
}

resource "aws_security_group_rule" "allow_all_outbound_traffic" {
  type                     = "egress"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
  security_group_id        = aws_security_group.driver_sg.id
  cidr_blocks =   ["0.0.0.0/0"]
}

###================Master/Driver Inbound Rules=================###
resource "aws_security_group_rule" "allow_ssh_traffic" {
  type                     = "ingress"
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
  security_group_id        = aws_security_group.driver_sg.id
  source_security_group_id = data.terraform_remote_state.vpc.outputs.bastion_sg
}

resource "aws_security_group_rule" "allow_8443_from_service_sg" {
  type                     = "ingress"
  from_port                = 8443
  to_port                  = 8443
  protocol                 = "tcp"
  security_group_id        = aws_security_group.driver_sg.id
  source_security_group_id = aws_security_group.service_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_slave_sg_for_tcp" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "tcp"
  security_group_id        = aws_security_group.driver_sg.id
  source_security_group_id = aws_security_group.nodes_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_slave_sg_for_upd" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "udp"
  security_group_id        = aws_security_group.driver_sg.id
  source_security_group_id = aws_security_group.nodes_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_slave_sg_for_icmp" {
  type                     = "ingress"
  from_port                = -1
  to_port                  = -1
  protocol                 = "icmp"
  security_group_id        = aws_security_group.driver_sg.id
  source_security_group_id = aws_security_group.nodes_sg.id
}


###===============Specify custom managed security groups to restrict cross-cluster access====####
resource "aws_security_group_rule" "allow_all_ports_from_driver_sg_for_tcp" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "tcp"
  security_group_id        = aws_security_group.driver_sg.id
  source_security_group_id = aws_security_group.driver_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_driver_sg_for_upd" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "udp"
  security_group_id        = aws_security_group.driver_sg.id
  source_security_group_id = aws_security_group.driver_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_driver_sg_for_icmp" {
  type                     = "ingress"
  from_port                = -1
  to_port                  = -1
  protocol                 = "icmp"
  security_group_id        = aws_security_group.driver_sg.id
  source_security_group_id = aws_security_group.driver_sg.id
}

###===================Security Group for Spark Nodes===============###
resource "aws_security_group" "nodes_sg" {
  name        = "spark-nodes-sg"
  description = "Allow traffic from port elb and enable SSH"
  vpc_id      = data.terraform_remote_state.vpc.outputs.vpc_id

  lifecycle {
    create_before_destroy = true
  }
  tags = local.common_tags
}

resource "aws_security_group_rule" "allow_all_outbound_traffic_in_node_sg" {
  type                     = "egress"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
  security_group_id        = aws_security_group.nodes_sg.id
  cidr_blocks =   ["0.0.0.0/0"]
}

###================Master/Driver Inbound Rules=================###
resource "aws_security_group_rule" "allow_8443_from_service_sg_for_node_sg" {
  type                     = "ingress"
  from_port                = 8443
  to_port                  = 8443
  protocol                 = "tcp"
  security_group_id        = aws_security_group.nodes_sg.id
  source_security_group_id = aws_security_group.service_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_master_sg_for_tcp" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "tcp"
  security_group_id        = aws_security_group.nodes_sg.id
  source_security_group_id = aws_security_group.driver_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_master_sg_for_upd" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "udp"
  security_group_id        = aws_security_group.nodes_sg.id
  source_security_group_id = aws_security_group.driver_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_master_sg_for_icmp" {
  type                     = "ingress"
  from_port                = -1
  to_port                  = -1
  protocol                 = "icmp"
  security_group_id        = aws_security_group.nodes_sg.id
  source_security_group_id = aws_security_group.driver_sg.id
}

###===============Specify custom managed security groups to restrict cross-cluster access====####
resource "aws_security_group_rule" "allow_all_ports_from_node_sg_for_tcp" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "tcp"
  security_group_id        = aws_security_group.nodes_sg.id
  source_security_group_id = aws_security_group.nodes_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_node_sg_for_upd" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "udp"
  security_group_id        = aws_security_group.nodes_sg.id
  source_security_group_id = aws_security_group.nodes_sg.id
}

resource "aws_security_group_rule" "allow_all_ports_from_node_sg_for_icmp" {
  type                     = "ingress"
  from_port                = -1
  to_port                  = -1
  protocol                 = "icmp"
  security_group_id        = aws_security_group.nodes_sg.id
  source_security_group_id = aws_security_group.nodes_sg.id
}

####===================Security Group for Spark Driver==================####
resource "aws_security_group" "service_sg" {
  name        = "spark-emr-service-sg"
  description = "Allow outbound traffic from Driver and Nodes sg"
  vpc_id      = data.terraform_remote_state.vpc.outputs.vpc_id

  lifecycle {
    create_before_destroy = true
  }
  tags = local.common_tags
}

resource "aws_security_group_rule" "allow_all_outbound_traffic_in_service_sg" {
  type                     = "egress"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
  security_group_id        = aws_security_group.service_sg.id
  cidr_blocks =   ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "allow_port_9443_from_Master" {
  type                     = "ingress"
  from_port                = 9443
  to_port                  = 9443
  protocol                 = "tcp"
  security_group_id        = aws_security_group.service_sg.id
  source_security_group_id = aws_security_group.driver_sg.id
}

resource "aws_security_group_rule" "allow_port_8443_to_node_sg" {
  type                     = "egress"
  from_port                = 8443
  to_port                  = 8443
  protocol                 = "tcp"
  security_group_id        = aws_security_group.service_sg.id
  source_security_group_id = aws_security_group.nodes_sg.id
}

resource "aws_security_group_rule" "allow_port_8443_to_driver_sg" {
  type                     = "egress"
  from_port                = 8443
  to_port                  = 8443
  protocol                 = "tcp"
  security_group_id        = aws_security_group.service_sg.id
  source_security_group_id = aws_security_group.driver_sg.id
}
