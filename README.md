# 在线编译器
&emsp;&emsp;利用spring作后端、利用docker作沙箱的在线编译器演示，可实现代码（只做了c++）的上传、编译、运行、调试(gdb)。
<br/>
&emsp;&emsp;运行前请先配置好docker环境：
* 根据dockerFile文件夹里面的内容，建立docker镜像。
* docker remote api 配置正确。
* 代码里面的docker相关和目录相关的代码修改正确，要修改的地方都打上了(!important)标记，可用搜索快速定位，并在代码中注释了如何修改。（文件包括MainController.java、MyHandler.java、ws.jsp）
<br/>
&emsp;&emsp;Ps:因为某些原因，MainController.java里面夹杂了一些OpenSSL加解密的代码。

---


&emsp;&emsp;This is an online compiler demo using Spring as backend and docker as sandbox. Function include code upload(c++ only), compiler, run and debug(gdb).
<br/>
&emsp;&emsp;Please finish setting up the docker environment before running this program:
* Build the docker image according to the files in dockerFile document.
* Enable docker remote api. 
* Change codes about docker and url which marked (!important). Files include MainController.java, MyHandler.java and ws.jsp.
<br/>
&emsp;&emsp;Ps:For some resons, there are some codes about encrypt and decrypt using OpenSSL in MainController.java which make no contribution to the online compiler.
