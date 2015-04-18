@echo off


REM java Pubmed Search_and_Email_Articles engine  batch file
REM ========================================================

CLS

echo Pubmed Search_and_Email_Articles engine started at %time%


echo Set ClassPath

call SetClassPath.bat

echo run java pubmed
java pubmed

echo Pubmed Search_and_Email_Articles engine completed at %time%



