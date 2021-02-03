package com.dd.ria.processor.handler;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.Serializable;

import static com.dd.ria.processor.utility.AppUtil.readData;
import static org.apache.spark.sql.functions.*;

public class Combiner implements Serializable {


    private static final String DELEMITER = ";";
    public static final String SEPARATOR = "/";

    public Dataset<Row> processConditionsData(SparkSession sparkSession, Dataset<Row> nctIdDataset, String s3Bucket) {

        Dataset<Row> conditionsDataset = readConditionTable(sparkSession, s3Bucket);
        Dataset<Row> browseCDataset = readBConditionTable(sparkSession, s3Bucket);

        Dataset<Row> studiesWithBConditionAndConditions = nctIdDataset.join(browseCDataset,
                nctIdDataset.col("nct_id").equalTo(browseCDataset.col("nct_id")), "fullouter")
                .drop(browseCDataset.col("nct_id")).join(conditionsDataset,
                        nctIdDataset.col("nct_id").equalTo(conditionsDataset.col("nct_id")), "fullouter")
                .drop(conditionsDataset.col("nct_id"));


        conditionsDataset.unpersist();
        browseCDataset.unpersist();
        return studiesWithBConditionAndConditions;
    }

    public Dataset<Row> processInterventionsData(SparkSession sparkSession, Dataset<Row> nctIdDataset, String s3Bucket) {
        Dataset<Row> interventionData = readInterventionTable(sparkSession, s3Bucket);
        Dataset<Row> bInterventionData = readBInterventionTable(sparkSession, s3Bucket);
        Dataset<Row> bInterventionOtherDataset = readBInterventionOtherTable(sparkSession, s3Bucket);


        Dataset<Row> studiesWithInterventions = nctIdDataset.join(interventionData,
                nctIdDataset.col("nct_id").equalTo(interventionData.col("nct_id")), "fullouter")
                .drop(interventionData.col("nct_id")).join(bInterventionData,
                        nctIdDataset.col("nct_id").equalTo(bInterventionData.col("nct_id")), "fullouter")
                .drop(bInterventionData.col("nct_id")).join(bInterventionOtherDataset,
                        nctIdDataset.col("nct_id").equalTo(bInterventionOtherDataset.col("nct_id")), "fullouter")
                .drop(bInterventionOtherDataset.col("nct_id"));

        interventionData.unpersist();
        bInterventionData.unpersist();
        bInterventionOtherDataset.unpersist();
        return studiesWithInterventions;
    }

    private Dataset<Row> readConditionTable(SparkSession sparkSession, String s3Bucket) {
//        Dataset<Row> riaConditionsCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/conditions.txt");
        Dataset<Row> riaConditionsCollection = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/conditions.txt");

        Dataset<Row> aggregatedConditions = riaConditionsCollection.select(col("nct_id"), col("name"))
                .groupBy(col("nct_id"))
                .agg(concat_ws(",", collect_list("name")).alias("co_name"));
        return aggregatedConditions;
    }

    private Dataset<Row> readBConditionTable(SparkSession sparkSession, String s3Bucket) {
//        Dataset<Row> riaBConditionsCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/browse_conditions.txt");
        Dataset<Row> riaBConditionsCollection = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/browse_conditions.txt");

        Dataset<Row> aggregatedBConditions = riaBConditionsCollection.select(col("nct_id"), col("mesh_term"))
                .groupBy(col("nct_id"))
                .agg(concat_ws(",", collect_list("mesh_term")).alias("co_mesh_term"));
        return aggregatedBConditions;
    }

    private Dataset<Row> readInterventionTable(SparkSession sparkSession, String s3Bucket) {
//        Dataset<Row> riaInterventionCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/interventions.txt");
        Dataset<Row> riaInterventionCollection = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/interventions.txt");

        Dataset<Row> aggregatedInterventions = riaInterventionCollection.select(col("nct_id"), col("name"))
                .groupBy(col("nct_id"))
                .agg(concat_ws(",", collect_list("name")).alias("in_name"));

        return aggregatedInterventions;
    }

    private Dataset<Row> readBInterventionTable(SparkSession sparkSession, String s3Bucket) {
//        Dataset<Row> riaBInterventionCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/browse_interventions.txt");
        Dataset<Row> riaBInterventionCollection = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/browse_interventions.txt");

        Dataset<Row> aggregatedBInterventions = riaBInterventionCollection.select(col("nct_id"), col("mesh_term"))
                .groupBy(col("nct_id"))
                .agg(concat_ws(",", collect_list("mesh_term")).alias("in_mesh_term"));
        return aggregatedBInterventions;
    }

    private Dataset<Row> readBInterventionOtherTable(SparkSession sparkSession, String s3Bucket) {
//        Dataset<Row> riaBInterventionOtherCollection = readData(sparkSession, "ria-data-processor/src/main/resources/data/intervention_other_names.txt");
        Dataset<Row> riaBInterventionOtherCollection = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/intervention_other_names.txt");

        Dataset<Row> aggregatedBOInterventions = riaBInterventionOtherCollection.select(col("nct_id"), col("name"))
                .groupBy(col("nct_id"))
                .agg(concat_ws(",", collect_list("name")).alias("in_other_name"));
        return aggregatedBOInterventions;
    }

    public void populateOtherFields(SparkSession sparkSession, Dataset<Row> nctIdDataset, String s3Bucket) {
//        Dataset<Row> sponsorsDataset = readData(sparkSession, "ria-data-processor/src/main/resources/data/sponsors.txt");
        Dataset<Row> sponsorsDataset = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/sponsors.txt");
//        Dataset<Row> idInfoDataset = readData(sparkSession, "ria-data-processor/src/main/resources/data/id_information.txt");
        Dataset<Row> idInfoDataset = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/id_information.txt");
//        Dataset<Row> eligibilityDataset = readData(sparkSession, "ria-data-processor/src/main/resources/data/eligibilities.txt");
        Dataset<Row> eligibilityDataset = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/eligibilities.txt");
//        Dataset<Row> facilityDataset = readData(sparkSession, "ria-data-processor/src/main/resources/data/facilities.txt");
        Dataset<Row> facilityDataset = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/facilities.txt");
//        Dataset<Row> designDataset = readData(sparkSession, "ria-data-processor/src/main/resources/data/designs.txt");
        Dataset<Row> designDataset = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/designs.txt");
//        Dataset<Row> designOutcomeDataset = readData(sparkSession, "ria-data-processor/src/main/resources/data/design_outcomes.txt");
        Dataset<Row> designOutcomeDataset = readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/design_outcomes.txt");


        Dataset<String> designMap = designDataset.na().fill("N/A", new String[]{"allocation", "intervention_model",
                "observational_model", "primary_purpose", "time_perspective", "masking", "masking_description",
                "intervention_model_description", "subject_masked", "caregiver_masked", "investigator_masked",
                "outcomes_assessor_masked"})
                .map(new MapFunction<Row, String>() {
                    @Override
                    public String call(Row row) throws Exception {
                        String generatedString = generateString(row);
                        return row.getString(1) + "|" + generatedString;
                    }
                }, Encoders.STRING());

        Dataset<Row> partitionedDesignDataset = sparkSession.read()
                .option("inferSchema", "true")
                .option("header", "false")
                .option("delimiter", "|")
                .csv(designMap).withColumnRenamed("_c0", "nct_id").withColumnRenamed("_c1", "Designs");

        Dataset<Row> aggregatedIdValues = idInfoDataset.select(col("nct_id"), col("id_value"))
                .groupBy(col("nct_id"))
                .agg(concat_ws(",", collect_list("id_value")).alias("Other Ids"));

        Dataset<Row> aggregatedSponsorsNameAndFunder = sponsorsDataset.select(col("nct_id"), col("name"), col("agency_class"))
                .groupBy(col("nct_id"))
                .agg(concat_ws(",", collect_list("name")).alias("Sponsors/Collaborator"),
                        concat_ws(",", collect_list("agency_class")).alias("Funder Type"));

        Dataset<Row> outcomeMeasuresDataset = designOutcomeDataset.filter(col("outcome_type").equalTo("primary"))
                .select(col("nct_id"), col("measure"))
                .groupBy(col("nct_id")).agg(concat_ws("|", collect_list(col("measure"))).alias("Outcome Measures"));

        Dataset<Row> genderDataset = eligibilityDataset.select(col("nct_id"), col("gender").alias("Gender"),
                col("gender_description").alias("Gender Description"),
                concat_ws("-", col("minimum_age"), col("maximum_age")).alias("Age"));

        Dataset<Row> locationDataset = facilityDataset.select(col("nct_id"),
                concat_ws(",", col("name"), col("city"), col("state"), col("zip"), col("country")).alias("locations"))
                .groupBy(col("nct_id")).agg(concat_ws("|", collect_list("locations")).alias("Locations"));

        Dataset<Row> otherFieldsAggregatedDataset = nctIdDataset.join(aggregatedIdValues,
                nctIdDataset.col("nct_id").equalTo(aggregatedIdValues.col("nct_id")), "fullouter")
                .drop(aggregatedIdValues.col("nct_id"))
                .join(aggregatedSponsorsNameAndFunder,
                        nctIdDataset.col("nct_id").equalTo(aggregatedSponsorsNameAndFunder.col("nct_id")), "fullouter")
                .drop(aggregatedSponsorsNameAndFunder.col("nct_id"))
                .join(outcomeMeasuresDataset,
                        nctIdDataset.col("nct_id").equalTo(outcomeMeasuresDataset.col("nct_id")), "fullouter")
                .drop(outcomeMeasuresDataset.col("nct_id"))
                .join(genderDataset,
                        nctIdDataset.col("nct_id").equalTo(genderDataset.col("nct_id")), "fullouter")
                .drop(genderDataset.col("nct_id"))
                .join(locationDataset,
                        nctIdDataset.col("nct_id").equalTo(locationDataset.col("nct_id")), "fullouter")
                .drop(locationDataset.col("nct_id"))
                .join(partitionedDesignDataset,
                        nctIdDataset.col("nct_id").equalTo(partitionedDesignDataset.col("nct_id")), "fullouter")
                .drop(partitionedDesignDataset.col("nct_id"));

        otherFieldsAggregatedDataset.repartition(1)
                .write()
                .format("json")
                .save("hdfs:///temp" + SEPARATOR + "data/output/other-fields");
        //Intermediate dataset which we are going to re-use.
//                .save("data/output/other-fields");
    }

    private String generateString(Row row) {
        StringBuilder builder = new StringBuilder();
        builder.append("Allocation:").append(row.getString(2)).append(DELEMITER)
                .append("Intervention Model:").append(row.getString(3)).append(DELEMITER)
                .append("Observational Model:").append(row.getString(4)).append(DELEMITER)
                .append("Primary Purpose:").append(row.getString(5)).append(DELEMITER)
                .append("Time Perspective:").append(row.getString(6)).append(DELEMITER)
                .append("Masking:").append(row.getString(7)).append(DELEMITER)
                .append("Masking Description:").append(row.getString(8)).append(DELEMITER)
                .append("Intervention Model Description:").append(row.getString(9)).append(DELEMITER)
                .append("Subject Masked:").append(row.getString(10)).append(DELEMITER)
                .append("Caregiver Masked:").append(row.getString(11)).append(DELEMITER)
                .append("Investigator Masked:").append(row.getString(12)).append(DELEMITER)
                .append("Outcomes Assessor Masked:").append(row.getString(13));
        return builder.toString();
    }
}
