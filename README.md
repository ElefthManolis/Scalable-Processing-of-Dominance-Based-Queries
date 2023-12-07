# Scalable-Processing-of-Dominance-Based-Queries

## Distributions Generation

You have to run the following command in order to create the four distributions for specific number of dimensions and points.

```sh
python3 distributions_scripts/distributions_creator.py --dimensions <dimenions> --num_points <points> --show <visualization(only for two dimensions)>
```
The python script above will create the four distributions (normal, uniform, correlated, anticorrelated) for the number of dimensions and points that the user has selected

## Program Setup
The spark.sbt file containes all the necessary spark libraries the we want to build with Scala program. If you change the spark.sbt file you must run the command below 
```sh
sbt reload 
```

To compile your program run
```sh
sbt package 
```

In order to run the compiled program
```sh
spark-submit --class SkylineProblemSpark target/scala-<SCALA_VERSION>/simple-project_<SCALA_VERSION>-1.0.jar <dimensions> <points> <type_of_distribution>
```
