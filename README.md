IT-Project Environment configuration：
1. JDK1.8
2. Maven
3. IDEA (With Maven)
4. Linux (Need to configure Uxn environment)
5. Open the Springboot startup class
6. Enter web address: 127.0.0.1:8080
7. If it has following question:
java: duplicate class
com.leisurexi.codeeditor.classloader.FileClassLoader
Solution:
This may be because of a directory problem. Go to the directory of the project, not SRC or other directories, and compile it. This maybe useful.
