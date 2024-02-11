import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import scala.util.Random
import scala.collection.mutable.PriorityQueue
import scala.math.Ordering


case class Point(coordinates: List[Double])

object SkylineProblemSpark {

  def findSkylinePoints(coordinates: RDD[Point]): RDD[Point] = {
    coordinates
      .mapPartitions(iter => {
        iter.foldLeft(List.empty[Point]) { (result, current) =>
          // Check if the current point is not dominated by any point in the result
          if (!result.exists(p => p.coordinates.zip(current.coordinates).forall { case (px, cx) => px <= cx })) {
            // Remove points in the result dominated by the current point
            val updatedResult = result.filterNot(p => p.coordinates.zip(current.coordinates).forall { case (px, cx) => cx <= px })
            // Add the current point to the skyline
            current :: updatedResult
          } else {
            result
          }
        }.iterator
      })
      .distinct() // Ensure uniqueness of skyline points
  }
  
  
  def topKDominantPoints(coordinates: RDD[Point], k: Int): RDD[Point] = {
    coordinates
      .mapPartitions(iter => {
        val points = iter.toList
        val dominantScores = points.map(point => (point, dominantScore(point, points)))
        dominantScores.sortBy(-_._2).take(k).map(_._1).iterator
      })
      .distinct() // Ensure uniqueness of selected points
  }
  
  
  def dominantScore(point: Point, otherPoints: List[Point]): Int = {
    // Compute the dominant score for the given point
    otherPoints.count(otherPoint => point.coordinates.zip(otherPoint.coordinates).forall { case (px, cx) => cx >= px })
  }
  
  
  
def topKDominantPointsImportanceSampling(coordinates: RDD[Point], k: Int): RDD[Point] = {
  val sampleSize = math.min(k * 10, coordinates.count()).toInt
  val sample = coordinates.takeSample(false, sampleSize)

  // Calculate dominance scores for sampled points
  val dominanceScores = sample.map(point => (point, dominantScoreApproximate(point, sample)))

  // Compute importance weights based on dominance scores
  val totalScore = dominanceScores.map(_._2).sum.toDouble
  val weights = dominanceScores.map { case (point, score) => (point, score.toDouble / totalScore) }.toMap

  // Sample points with importance weights
  val sampledPoints = sample.filter(point => Random.nextDouble() < weights.getOrElse(point, 0.0))


  // Compute dominance scores for sampled points
  val sampledDominanceScores = sampledPoints.map(point => (point, dominantScoreApproximate(point, sampledPoints)))

  // Maintain a priority queue to track top-k dominant points
  implicit val ordering: Ordering[(Point, Int)] = Ordering.by[(Point, Int), Int](_._2).reverse
  val topKQueue = sampledDominanceScores.foldLeft(PriorityQueue.empty[(Point, Int)](ordering))(updateTopKQueue(_, _, k))


  // Extract the top-k dominant points from the queue
  val topKPoints = topKQueue.toList.map(_._1)

  // Return the RDD of top-k dominant points
  coordinates.context.parallelize(topKPoints)
}

// Helper function to update the priority queue with new points
def updateTopKQueue(queue: PriorityQueue[(Point, Int)], pointScore: (Point, Int), k: Int): PriorityQueue[(Point, Int)] = {
  if (queue.size < k || pointScore._2 > queue.head._2) {
    queue.enqueue(pointScore)
    if (queue.size > k) {
      queue.dequeue()
    }
  }
  queue
}

def dominantScoreApproximate(point: Point, otherPoints: Seq[Point]): Int = {
  // Compute the dominant score for the given point
  otherPoints.count(otherPoint => point.coordinates.zip(otherPoint.coordinates).forall { case (px, cx) => cx >= px })
}






  def main(args: Array[String]): Unit = {
    println("Start")

    // Set up Spark configuration
    val conf = new SparkConf().setAppName("SkylineProblemSpark").setMaster("local[1]")
    val sc = new SparkContext(conf)
    
    val dimensions = args(0)
    val num_of_points = args(1)
    val distribution = args(2)
    
    val relativePath = "datasets/" + dimensions + "_" + num_of_points + "_" + distribution + ".txt"
    val filePath = new java.io.File(relativePath).getAbsolutePath
    println(filePath)

    // Load data from file
    val pointRDD = sc.textFile(filePath).map(line => Point(line.split(" ").map(_.toDouble).toList))
    pointRDD.take(5).foreach(println)

    // Find and print the skyline points
    println("Skyline Set:")
    val skylineRDD = findSkylinePoints(pointRDD)
    skylineRDD.collect().foreach(println)
    
    // Find and print the k most dominant points
    println("K-dominant points Set:")
    val kdominantRDD = topKDominantPoints(pointRDD,10)
    kdominantRDD.collect().foreach(println)
    
    
    // Find and print the k most dominant points using approximation algorithm
    println("K-dominant points Set with approximation:")
    //val kdominantRDDAppr = topKDominantPointsImportanceSampling(pointRDD,10)
    //kdominantRDDAppr.collect().foreach(println)
    

    
    
    //Find and print the k most dominant points in skyline
    println("K-dominant points in skyline Set:")
    val kdominantskylineRDD = topKDominantPoints(skylineRDD,2)
    kdominantskylineRDD.collect().foreach(println)
    
    
    

    // Stop the SparkContext
    println("Finish")
    sc.stop()
  }
}

