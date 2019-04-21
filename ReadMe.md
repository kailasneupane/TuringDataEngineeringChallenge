# Turing Data Engineering Challenge
This project is made to accomplish the tasks mentioned on this [link](https://docs.google.com/document/d/1P9k1JcZ8RnXV9ylqlt7yhWBP1hueReX0oHHvhSCYCPs/edit#heading=h.rrar1dgps27e).

## Tech/Frameworks Used
- **Languages:** scala(used as major language), java </b>
- **libraries:** Apache Spark, JGit, Gson, ANTLR
- **build tool:** Scala Build Tool (SBT)

## Instructions
Before running the code, please be sure that Git, java, scala and SBT are already installed in the hadoop cluster.
<br><br>
Used Languages Versions:-
- java version: 1.8
- scala: 2.11
- SBT: 1.2.7

Process to run the code:

First, clone the repo in the hadoop cluster.
<br>
Then goto the root of project and run the command:

```bash
sbt assembly
```

This will create a runnable jar in:<br>
*projectRoot/target/scala-2.11/turingsPyGitAnalysis.jar*

Then run the jar using command:
```bash
hadoop jar path/to/turingsPyGitAnalysis.jar
```

After the task completes, the final file **results.json** will be generated in HDFS location:<br>

**_stage2/merged_jsons/results.json_** 

## Misc
Assuming that, you are running this jar in local PC with hadoop installed, you may run into OutOfMemory, HeapSize issue, or internet may go off.

In such condition, you can run the jar again. The jar will check if a python repo was already processed or not and saves the time by ignoring the pre-processed data if it exists in hadoop. Then at last of checking and processing repos, it will generate results.json by combining the individual json as json array.

## Contributing
If any bugs are observed, please inform me by opening an issue or you can contact me 
 on Google Hangouts. My gmail is **_kailasneupane@gmail.com_** .
 
 Thank you,