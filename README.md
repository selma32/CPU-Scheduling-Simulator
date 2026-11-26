# CPU-Scheduling-Simulator

## Desktop application

The project includes a Swing desktop interface that runs the existing Preemptive SJF and ACS schedulers and displays their timelines, per-step queue states, and process metrics.

From the project root, compile and launch it with a JDK:

```powershell
javac -d out src\*.java
java -cp out Main
```

`Main` now opens the Swing application. `SimulatorApp` remains its direct entry point.
In VS Code, run the first launch configuration, **CPU Scheduling Simulator (GUI)**,
or press F5 with it selected. The separate **CPU Scheduling Simulator (Console)**
configuration launches the original terminal workflow. From a terminal, run that
workflow with `java -cp out ConsoleMain`.