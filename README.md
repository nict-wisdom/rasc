RaSC (Rapid Service Connector)
==============================

RaSC is a middleware that enables us to run a wide variety of user programs in a parallel/distributed manner. 
The documents including several tutorials are available at http://alaginrc.nict.go.jp/rasc/ .

Here is an example:
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

License
-------

Copyright &copy; 2014 Information Analysis Laboratory, NICT
Licensed under the [LGPL v2.1][LGPL]
 
[LGPL]: https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html
