[
  {
    "Classification": "spark-defaults",
    "ConfigurationProperties": {
      "spark.dynamicAllocation.enabled": ${enable_dynamic_allocation}",
      "spark.executor.instances": "${num_exec_instances}",
      "spark.memory.fraction": "${spark_mem_frac}",
      "spark.memory.storageFraction": "${spark_storage_mem_frac}",
      "spark.executor.extraJavaOptions": "${exec_java_opts}",
      "spark.driver.extraJavaOptions": "${driver_java_opts}",
      "spark.storage.level": "${spark_storage_level}",
      "spark.rdd.compress": "${compression_enabled}",
      "spark.shuffle.compress": "${compression_enabled}",
      "spark.shuffle.spill.compress": "${compression_enabled}",
      "spark.sql.optimizer.distinctBeforeIntersect.enabled" : "${distinct_enabled}",
      "spark.sql.dynamicPartitionPruning.enabled" : "${dynamic_partition_enabled}"
    }
  },
  {
    "Classification": "mapred-site",
    "ConfigurationProperties": {
      "mapreduce.map.output.compress": "${compression_enabled}"
    }
  },
  {
    "Classification": "spark",
    "ConfigurationProperties": {
      "maximizeResourceAllocation": "${enable_max_resource_alloc}",
      "spark.dynamicAllocation.enabled": "${enable_dynamic_alloc}",
      "spark.sql.shuffle.partitions": "${spark_shuffle_partitions}",
      "spark.yarn.executor.memoryOverhead": "${yarn_exec_memory_overhead}",
      "spark.default.parallelism": "${spark_parallelism}",
      "spark.yarn.driver.memoryOverhead": "${yarn_driver_memory_overhead}",
      "spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version": 2,
      "spark.speculation": false
    }
  },
  {
    "Classification": "yarn-env",
    "ConfigurationProperties": {
    },
    "Configurations": [
      {
        "Classification": "export",
        "ConfigurationProperties": {
          "environment": "${environment}"
        }
      }
    ]
  },
  {
    "Classification": "emrfs-site",
    "ConfigurationProperties": {
      "fs.s3a.enableServerSideEncryption": "${enable_s3_sse}"
    }
  },
  {
    "Classification": "core-site",
    "ConfigurationProperties": {
      "fs.s3a.server-side-encryption-algorithm": "AES256"
    }
  },
  {
      "Classification": "yarn-site",
      "ConfigurationProperties": {
        "yarn.nodemanager.vmem-check-enabled":"false",
        "yarn.nodemanager.pmem-check-enabled":"false",
        "yarn.nodemanager.resource.memory-mb": "21500",
        "yarn.scheduler.maximum-allocation-vcores": "8"
     }
  }
]