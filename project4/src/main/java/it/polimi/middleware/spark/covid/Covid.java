package it.polimi.middleware.spark.covid;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.desc;
import static org.apache.spark.sql.functions.from_json;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.min;
import static org.apache.spark.sql.functions.sum;
import static org.apache.spark.sql.functions.mean;
import static org.apache.spark.sql.functions.last;
import static org.apache.spark.sql.functions.first;
import static org.apache.spark.sql.functions.window;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class Covid {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final int waitingTime = 5; //seconds
        final String serverAddr = "localhost:9092";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("WindowedCount")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        //Creating Database Schema
        final List<StructField> mySchemaFields = new ArrayList<>();
        mySchemaFields.add(DataTypes.createStructField("dayCount", DataTypes.IntegerType, true));
        mySchemaFields.add(DataTypes.createStructField("dateReported", DataTypes.DateType, true));
        mySchemaFields.add(DataTypes.createStructField("countryCode", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("countryName", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("countryArea", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("newCases", DataTypes.IntegerType, true));
        mySchemaFields.add(DataTypes.createStructField("cumulativeCases", DataTypes.IntegerType, true));
        mySchemaFields.add(DataTypes.createStructField("newDeaths", DataTypes.IntegerType, true));
        mySchemaFields.add(DataTypes.createStructField("cumulativeDeaths", DataTypes.IntegerType, true));
        final StructType mySchema = DataTypes.createStructType(mySchemaFields);

        Dataset<Row> input_df = spark
            .readStream()
            .format("kafka")
            .option("kafka.bootstrap.servers", serverAddr)
            .option("subscribe", "topicCovid")
            .option("startingOffsets", "earliest")
            .load();    

        // Transform to Output DataFrame
        final Dataset<Row> value_df = input_df.select(col("timestamp"),from_json(col("value").cast("string"),mySchema).alias("value"));

        final Dataset<Row> exploded_df = value_df.selectExpr("timestamp","value.dayCount", "value.dateReported","value.countryCode","value.countryName","value.countryArea",
            "value.newCases","value.cumulativeCases","value.newDeaths","value.cumulativeDeaths");

        exploded_df.withWatermark("timestamp", "1 hour");
        final Dataset<Row> aggregated_df = exploded_df
            .groupBy(
                window(col("timestamp"), 8*waitingTime + " seconds", waitingTime + " seconds"), //change to 1 day to have a real world application
                col("countryCode"), col("countryName"), col("countryArea")
            )
            .agg(
                max("dateReported").alias("Date"),
                max("dayCount").alias("dayCount"),
                count(lit(1)).alias("NumOfRecords"),
                sum("newCases").minus(last("newCases")).divide(7).alias("oldMean"),
                sum("newCases").minus(first("newCases")).divide(7).alias("MovingAverage")
            )
            .withColumn("Percentage", col("MovingAverage").divide(col("oldMean")).multiply(100).minus(100))
            .filter("NumOfRecords == 8")
            .drop("NumOfRecords")
            .drop("oldMean");
            
        final StreamingQuery query = aggregated_df
            .sort(desc("Date"), col("countryName"))
            .writeStream()
            .format("console")
            .outputMode("complete")
            .start();

        try {
            query.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }

}