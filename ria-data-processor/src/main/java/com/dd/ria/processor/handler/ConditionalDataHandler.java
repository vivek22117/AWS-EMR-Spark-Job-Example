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

public class ConditionalDataHandler implements Serializable {
    private static Logger logger = Logger.getLogger(ConditionalDataHandler.class);


    public Dataset<Row> processConditionsData(SparkSession sparkSession, Dataset<Row> studiesDataset) {

        Dataset<Row> conditionsDataset = readConditionTable(sparkSession);
        Dataset<Row> browseCDataset = readBConditionTable(sparkSession);

        Dataset<Row> studiesWithBConditionAndConditions = studiesDataset.join(browseCDataset,
                studiesDataset.col("nct_id").equalTo(browseCDataset.col("nct_id")), "fullouter")
                .drop(browseCDataset.col("nct_id")).join(conditionsDataset,
                        studiesDataset.col("nct_id").equalTo(conditionsDataset.col("nct_id")), "fullouter")
                .drop(conditionsDataset.col("nct_id"));


        studiesWithBConditionAndConditions.show();
        System.out.println(studiesWithBConditionAndConditions.count());
        studiesWithBConditionAndConditions.write().format("json").save("data/output/studiesC");

        conditionsDataset.unpersist();
        browseCDataset.unpersist();
        return studiesWithBConditionAndConditions;
    }

    private Dataset<Row> readConditionTable(SparkSession sparkSession) {
        Dataset<Row> riaConditionsCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/conditions.txt");
        Dataset<Row> aggregatedConditions = riaConditionsCollection.select(col("nct_id"), col("name"))
                .groupBy(col("nct_id"))
                .agg(collect_list(col("name")).alias("co_name"));
        return aggregatedConditions;
    }

    private Dataset<Row> readBConditionTable(SparkSession sparkSession) {
        Dataset<Row> riaBConditionsCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/browse_conditions.txt");
        Dataset<Row> aggregatedBConditions = riaBConditionsCollection.select(col("nct_id"), col("mesh_term"))
                .groupBy(col("nct_id"))
                .agg(collect_list(col("mesh_term")).alias("co_mesh_term"));
        return aggregatedBConditions;
    }
}
*/
