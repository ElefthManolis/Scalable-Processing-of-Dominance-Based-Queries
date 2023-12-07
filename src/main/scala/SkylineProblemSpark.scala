import scala.collection.mutable
import scala.reflect.io.File
import org.apache.spark
import org.apache.spark.rdd
import org.apache.spark.{SparkConf, SparkContext}

object SkylineProblemSpark {
  def getSkylineBaseline(points: rdd.MapPartitionsRDD[String, String]): Array[Array[Double]] = {
      points.take(10).show()
  }

  def getSkylineEfficient(points: Array[Array[Int]]): List[List[Int]] = {
      var y = List(List (1, 2), List(2, 1, -2))
      y
  }

  def main(args: Array[String]): Unit = {
    // Set up Spark configuration
    val conf = new SparkConf().setAppName("SkylineProblemSpark").setMaster("local[1]")
    val sc = new SparkContext(conf)
    val dimensions = args(0)
    val num_of_points = args(1)
    val distribution = args(2)

    val distribution_data_file = "/distributions_scripts/datasets/" + dimensions + "_" + num_of_points + "_" + distribution + ".txt"
    //println(distribution_data_file)
    val rddDistribution = sc.textFile(File(".").toAbsolute + distribution_data_file)

    getSkylineBaseline(rddDistribution)
    //rddDistribution.collect().foreach(println)



    

    // Stop the SparkContext
    sc.stop()
  }
}
