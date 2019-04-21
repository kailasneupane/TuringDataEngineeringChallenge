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

**_stage2/merged_repos/results.json_** 

## Misc
Assuming that, you are running this jar in local PC with hadoop installed, you may run into OutOfMemory, HeapSize issue, or internet may go off.

In such condition, You can see `output/uber_repo/url_list.csv` beside **turingsPyGitAnalysis.jar**.
<br>
you just need to update url_list.csv and remove those repo list whose operation are already completed. Then run the jar again.

## Contributing
If any bugs are observed, please inform me by opening an issue or you can contact me 
 on Google Hangouts. My gmail is **_kailasneupane@gmail.com_** .
 
 Thank you,