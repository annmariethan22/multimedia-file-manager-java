# multimedia-file-manager-java

A lightweight Java CLI utility to list, group, and organize multimedia files by type.

## Features
- Recursively list files from a directory.
- Group files into `IMAGE`, `VIDEO`, `AUDIO`, `DOCUMENT`, `OTHER`.
- Organize files into destination subfolders by media type.
- Support copy mode (default) and move mode (`--move`).

## Requirements
- Java 17+
- Maven 3.8+

## Build & test
```bash
mvn clean test
```

## Run
```bash
# List files
mvn -q exec:java -Dexec.mainClass=com.example.mfm.Main -Dexec.args="list /path/to/source"

# Group files by media type
mvn -q exec:java -Dexec.mainClass=com.example.mfm.Main -Dexec.args="group /path/to/source"

# Copy files into media type folders
mvn -q exec:java -Dexec.mainClass=com.example.mfm.Main -Dexec.args="organize /path/to/source /path/to/destination"

# Move files into media type folders
mvn -q exec:java -Dexec.mainClass=com.example.mfm.Main -Dexec.args="organize /path/to/source /path/to/destination --move"
```

Destination folder structure example:
```text
/path/to/destination/
  image/
  video/
  audio/
  document/
  other/
```
