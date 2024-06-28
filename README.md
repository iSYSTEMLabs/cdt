# Eclipse CDT™ C/C++ Development Tools CMake fork


<img align="right" src="images/logo.png">

The Eclipse CDT™ Project provides a fully functional C and C++ Integrated Development Environment based on the Eclipse platform. Features include: support for project creation and managed build for various toolchains, standard make build, source navigation, various source knowledge tools, such as type hierarchy, call graph, include browser, macro definition browser, code editor with syntax highlighting, folding and hyperlink navigation, source code refactoring and code generation, visual debugging tools, including memory, registers, and disassembly viewers.

Highlights of recent releases and release notes are available in the [New & Noteworthy](NewAndNoteworthy/README.md).

See also https://projects.eclipse.org/projects/tools.cdt and https://eclipse.org/cdt

<img src="images/snapshots.gif" width="66%">

## Code of Conduct

This project follows the [Eclipse Community Code of Conduct](https://www.eclipse.org/org/documents/Community_Code_of_Conduct.php).

## What is changed
CDT does not fully support CMake. This fork modifies only the CMake part of CDT to satisfy a few needs:

- It provides a more user-friendly experience by consolidating all CMake configuration into a single project Properties page in Eclipse, with special attention given to the presets method of creating CMake projects.
- It attempts to remove most unnecessary OS-specific settings by only running CMake, leaving it up to the CMakeLists.txt to handle these specifics.


