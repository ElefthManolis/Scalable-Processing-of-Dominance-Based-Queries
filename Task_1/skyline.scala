import scala.collection.mutable
import org.apache.spark.{SparkConf, SparkContext}

object SkylineProblemSpark {
  def getSkyline(points: Array[Array[Int]]): List[List[Int]] = {
    if (points.isEmpty) {
      List()
    } else {
      // Combine the start and end points of points and sort them
      val points = points.flatMap(building => Array((building(0), -building(2)), (building(1), building(2))))
                             .sorted((a, b) => if (a._1 != b._1) a._1 - b._1 else a._2 - b._2)

      // Priority queue to keep track of current heights
      val pq = mutable.TreeMap(0 -> 1)
      var prevMaxHeight = 0
      var result = List[List[Int]]()

      for ((x, h) <- points) {
        if (h < 0) {
          // Start of a building, add its height to the priority queue
          pq.updateWith(-h) {
            case Some(count) => Some(count + 1)
            case None => Some(1)
          }
        } else {
          // End of a building, decrement its height in the priority queue
          pq.updateWith(h) {
            case Some(count) =>
              if (count > 1) Some(count - 1)
              else None
            case None => None
          }
        }

        // Get the current maximum height in the priority queue
        val currMaxHeight = pq.lastKey

        // If the current maximum height is different from the previous one, update the result
        if (currMaxHeight != prevMaxHeight) {
          result = result :+ List(x, currMaxHeight)
          prevMaxHeight = currMaxHeight
        }
      }

      result
    }
  }

  def main(args: Array[String]): Unit = {
    // Set up Spark configuration
    val conf = new SparkConf().setAppName("SkylineProblemSpark").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val dimensions = args(0)
    val num_of_points = args(1)

    val normal_distribution_file_location = "../distributions_scripts/datasets/sample_movielens_ratings.txt"
    val uniform_distribution_file_location = "sample_movielens_ratings.txt"
    val correlated_distribution_file_location = "sample_movielens_ratings.txt"
    val anticorrelated_distribution_file_location = "sample_movielens_ratings.txt"

    // Sample data
    val buildings = Array(Array(2, 9, 10), Array(3, 7, 15), Array(5, 12, 12), Array(15, 20, 10), Array(19, 24, 8))

    // Parallelize the buildings array into an RDD
    val buildingsRDD = sc.parallelize(buildings)

    // Use Spark to process the skyline
    val resultRDD = buildingsRDD.mapPartitions(iter => Iterator(getSkyline(iter.toArray)))

    // Collect the results from all partitions into a single list
    val result = resultRDD.collect().flatten.toList

    // Print the result
    result.foreach(println)

    // Stop the SparkContext
    sc.stop()
  }
}
