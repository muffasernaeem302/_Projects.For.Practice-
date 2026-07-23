@echo off
echo Compiling FINANCE_CLI GUI App...
if not exist bin mkdir bin
dir /s /B *.java > sources.txt
javac -d bin @sources.txt
del sources.txt
echo Starting FINANCE_CLI GUI in a new window...
start "FINANCE_CLI GUI" java -cp bin com.expensetracker.gui.SwingApp
