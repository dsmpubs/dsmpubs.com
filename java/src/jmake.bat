@echo off
REM java make file for LING classes
CLS
echo java compilation started at %time%
echo Set Class Path
call SetClassPath.bat

echo compiling article.java
call javac article.java

echo compiling smtp.java
call javac smtp.java

echo compiling httpclient.java
call javac httpclient.java

echo compiling xmlparser.java
call javac xmlparser.java

echo compiling pubmed.java
call javac pubmed.java

echo make file complete at %time%...


