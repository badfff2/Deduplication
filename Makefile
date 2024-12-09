# Compiler and flags
JAVAC = javac
JAVA = java
JFLAGS = -g

# Source files
SOURCES = MyDedup.java RabinFingerPrint.java Store.java Index.java

# Class files (compiled sources)
CLASSES = $(SOURCES:.java=.class)

# Main class to execute
MAIN = MyDedup

# Default target: Compile all source files
all: $(CLASSES)

# Rule to compile .java files into .class files
%.class: %.java
	$(JAVAC) $(JFLAGS) $<

# Run the program
run:
	$(JAVA) $(MAIN)

# Run with arguments (e.g., make runargs ARGS="upload 64 128 256 testfile.txt")
runargs:
	$(JAVA) $(MAIN) $(ARGS)

# Clean up compiled files
clean:
	rm -f *.class

# Clean all generated files (including index and data directory)
cleanall: clean
	rm -f mydedup.index
	rm -rf data/