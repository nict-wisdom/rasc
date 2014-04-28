RaSC (Rapid Service Connector)
==============================

Documents are available at http://alaginrc.nict.go.jp/rasc/

Overview
--------

RaSC is a free middleware that runs existing user programs fast and in parallel. RaSC is developed by Information Analysis Laboratory, National Institute of Information and Communications Technology (NICT).

RaSC was originally developed to apply such existing programs as morphological analyzers and dependency parsers to a huge amount of Web pages. To this end, RaSC runs a wide variety of user programs and connects them in a parallel and distributed manner. One major feature of RaSC is to start several processes of user programs and process input data given as files or streaming data using the user programs. This makes processing on a large-scale data faster. RaSC was originally designed for natural language processing (NLP) programs, but can be used for a wide variety of user programs, not limited to the NLP tools. RaSC can execute most programs which takes input from a file or the standard input and ouputs to files or the standard output in parallel with small or no modification of the programs.

RaSC keeps processes of user programs running on a computational node. This enables us to efficiently run a program that takes a long time to start due to loading a large file for initialization, such as NLP programs that loads large dictionary files. The user can invoke the user programs running on RaSC via network. Multiple input can be splited and sent to RaSC processes on multiple computational nodes. The user can easily connect the programs using streaming like using UNIX pipes. Moreover, parallel execution of user programs running on RaSC is transparent to user.

Example
-------

Here is an example, that executes Japanese Dependency and Case Structure Analyzer KNP on RaSC. Given 500 sentences in the input file, RaSC splits them and assigns the fragraments to several KNP processes. This makes execution 5 times faster using multi-core CPUs (8 threaded execution on Intel Xeon X5675*2). The order of input sentences (INPUT_TXT) are preserved in the output (OUTPUT.TXT). 
```bash
$ time cat INPUT_TXT | juman | knp > OUTPUT_TXT  # Run user programs without RaSC
real    2m28.456s   # Single-threaded execution
user    2m17.557s
sys     0m1.011s
$ ./server.sh KNPService 19999 start # Start RaSC service for KNP
$ time cat INPUT_TXT | java -cp ./lib/*: RaSCClient localhost 19999 > OUTPUT_TXT
real    0m29.402s   # multi-threaded execution on multi-core CPUs (8 threads on Intel Xeon X5675*2）
user    0m0.566s
sys     0m0.045s
```

RaSC was originally developed for WISDOM X, a large-scale Web information analysis system, and is currently used to apply various analyses including dependency parsing, sentiment extraction, and causality extraction to more than 20 million Web documents per day.

License
-------

Copyright &copy; 2014 Information Analysis Laboratory, NICT
Licensed under the [LGPL v2.1][LGPL]
 
[LGPL]: https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html
