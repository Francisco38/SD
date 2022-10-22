# SD

SD project

how to run:

1º-javac *.java na pasta sd-main
2º-run rmiregistry 5000
3º-correr servidor primario:java -cp . ucDriveServer
4º-correr servidor secundario:java -cp . ucDriveServer2

depois de isto pode-se criar o numero de clientes e admin que se desejar atraves dos seguintes comandos:
java -cp . ucDriveClient <adress>
java -cp . ucDriveAdmin