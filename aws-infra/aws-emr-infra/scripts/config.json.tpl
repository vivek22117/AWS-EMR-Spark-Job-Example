[
  {
    "Classification": "spark-defaults",
    "Properties": {
      "spark.dynamicAllocation.enabled": "${enable_dynamic_allocation}",
      "spark.executor.instances": "${num_exec_instances}",
      "spark.memory.fraction": "${spark_mem_frac}",
      "spark.memory.storageFraction": "${spark_storage_mem_frac}",
      "spark.storage.level": "${spark_storage_level}",
      "spark.rdd.compress": "${compression_enabled}",
      "spark.shuffle.compress": "${compression_enabled}",
      "spark.shuffle.spill.compress": "${compression_enabled}",
      "spark.sql.analyzer.failAmbiguousSelfJoin": "false",
      "spark.sql.optimizer.distinctBeforeIntersect.enabled" : "${distinct_enabled}",
      "spark.sql.optimizer.dynamicPartitionPruning.enabled" : "${dynamic_partition_enabled}"
    }
  },
  {
    "Classification": "mapred-site",
    "Properties": {
      "mapreduce.map.output.compress": "${compression_enabled}"
    }
  },
  {
    "Classification": "spark",
    "Properties": {
      "maximizeResourceAllocation": "${enable_max_resource_alloc}",
      "spark.dynamicAllocation.enabled": "${enable_dynamic_alloc}",
      "spark.sql.shuffle.partitions": "${spark_shuffle_partitions}",
      "spark.yarn.executor.memoryOverhead": "${yarn_exec_memory_overhead}",
      "spark.default.parallelism": "${spark_parallelism}",
      "spark.yarn.driver.memoryOverhead": "${yarn_driver_memory_overhead}",
      "spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version": "2",
      "spark.speculation": "false",
      "spark.sql.analyzer.failAmbiguousSelfJoin": "false"
    }
  },
  {
    "Classification": "yarn-env",
    "ConfigurationProperties": {
    },
    "Configurations": [
      {
        "Classification": "export",
        "Properties": {
          "environment": "${environment}"
        }
      }
    ]
  },
  {
    "Classification": "emrfs-site",
    "Properties": {
      "fs.s3.enableServerSideEncryption": "${enable_s3_sse}"
    }
  },
  {
    "Classification": "core-site",
    "Properties": {
      "fs.s3a.server-side-encryption-algorithm": "AES256"
    }
  },
  {
      "Classification": "yarn-site",
      "Properties": {
        "yarn.nodemanager.vmem-check-enabled":"false",
        "yarn.nodemanager.pmem-check-enabled":"false",
        "yarn.nodemanager.resource.memory-mb": "21500",
        "yarn.scheduler.maximum-allocation-vcores": "8"
     }
  }
]