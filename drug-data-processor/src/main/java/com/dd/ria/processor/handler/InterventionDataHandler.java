/*
package com.dd.ria.processor.handler;

import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.Serializable;

import static com.dd.ria.processor.utility.AppUtil.readData;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.collect_list;

public class InterventionDataHandler implements Serializable {
    private static Logger logger = Logger.getLogger(InterventionDataHandler.class);


    public Dataset<Row> processInterventionsData(SparkSession sparkSession, Dataset<Row> studiesDataset) {
        Dataset<Row> interventionData = readInterventionTable(sparkSession);
        Dataset<Row> bInterventionData = readBInterventionTable(sparkSession);
        Dataset<Row> bInterventionOtherDataset = readBInterventionOtherTable(sparkSession);


        Dataset<Row> studiesWithInterventions = studiesDataset.join(interventionData,
                studiesDataset.col("nct_id").equalTo(interventionData.col("nct_id")), "fullouter")
                .drop(interventionData.col("nct_id")).join(bInterventionData,
                        studiesDataset.col("nct_id").equalTo(bInterventionData.col("nct_id")), "fullouter")
                .drop(bInterventionData.col("nct_id")).join(bInterventionOtherDataset,
                        studiesDataset.col("nct_id").equalTo(bInterventionOtherDataset.col("nct_id")), "fullouter")
                .drop(bInterventionOtherDataset.col("nct_id"));

        studiesWithInterventions.show();
        System.out.println(studiesWithInterventions.count());
        studiesWithInterventions.write().format("json").save("data/output/studiesI");

        interventionData.unpersist();
        bInterventionData.unpersist();
        bInterventionOtherDataset.unpersist();
        return null;
    }






    private Dataset<Row> readInterventionTable(SparkSession sparkSession) {
        Dataset<Row> riaInterventionCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/interventions.txt");
        Dataset<Row> aggregatedInterventions = riaInterventionCollection.select(col("nct_id"), col("name"))
                .groupBy(col("nct_id"))
                .agg(collect_list(col("name")).alias("in_name"));
        return aggregatedInterventions;
    }

    private Dataset<Row> readBInterventionTable(SparkSession sparkSession) {
        Dataset<Row> riaBInterventionCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/browse_interventions.txt");
        Dataset<Row> aggregatedBInterventions = riaBInterventionCollection.select(col("nct_id"), col("mesh_term"))
                .groupBy(col("nct_id"))
                .agg(collect_list(col("mesh_term")).alias("in_mesh_term"));
        return aggregatedBInterventions;
    }

    private Dataset<Row> readBInterventionOtherTable(SparkSession sparkSession) {
        Dataset<Row> riaBInterventionOtherCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/intervention_other_names.txt");
        Dataset<Row> aggregatedBOInterventions = riaBInterventionOtherCollection.select(col("nct_id"), col("name"))
                .groupBy(col("nct_id"))
                .agg(collect_list(col("name")).alias("in_name"));
        return aggregatedBOInterventions;
    }

}
*/
