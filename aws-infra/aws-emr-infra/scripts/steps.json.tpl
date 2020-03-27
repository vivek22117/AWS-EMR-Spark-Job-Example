[
  {
    "action_on_failure" : "TERMINATE_CLUSTER",
    "name"              : "Setup Hadoop Debugging",
    "hadoop_jar_step" : {
      "jar" : "command-runner.jar",
      "args" : [
          "state-pusher-script"
      ]
    }
  },
  {
    "action_on_failure" : "TERMINATE_CLUSTER",
    "name"              : "${first_step_name}",
    "hadoop_jar_step" : {
      "jar" : "command-runner.jar",
      "args" : [
        "spark-submit",
        "--deploy-mode",
        "cluster",
        "--driver-memory",
        "${driver_memory}",
        "--driver-cores",
        "${driver_cores}",
        "--num-executors",
        "${num_executors}",
        "--executor-memory",
        "${executor_memory}",
        "--executor-cores",
        "${executor_cores}",
        "--class",
        "${rsvp_reader_class}",
        "${jar_location}"
      ]
    },
    "properties": [
      {
        "Key": "Type",
        "Value": "CUSTOM_JAR"
      },
      {
        "Key": "Jar",
        "Value": "command-runner.jar"
      }
    ]
  },
  {
      "action_on_failure" : "TERMINATE_CLUSTER",
      "name"              : "${s3_distCp_step}",
      "hadoop_jar_step" : {
        "jar" : "command-runner.jar",
        "args" : [
          "s3-dist-cp",
          "--src",
          "hdfs:///temp/rsvp-data",
          "--dest",
          "s3://teamconcept-tfstate-deploy/rsvp/processed-data",
          "--groupBy",
          ".*/temp/dsr-flat-csv/([a-z0-9]{11})/([a-z0-9]{7})/([a-z]{4}-[a-z]{3}-[a-z0-9]{7}-[a-z0-9]{11})/.*",
          "--targetSize",
          "1024",
          "--s3ServerSideEncryption",
          "--deleteOnSuccess"
        ]
      }
    }
]