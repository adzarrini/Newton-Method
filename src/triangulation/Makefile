# Makefile variables, compiler, virtual machine, flags, and FILE variable for parameter
JC = javac
JVM = java
JFLAGS = -g -d .
JLIB = -cp ".:lib/*"
FILE = 

# Clear default targets for building90-
.SUFFIXES: .java .class

#(dependency suffix target suffix) rule for builidng
.java.class:
	$(JC) $(JFLAGS) $(JLIB) $*.java

# classes for our java program
CLASSES = \
	Triangulate.java \
	TriangulationTests.java

# File containing the main method
MAIN = Triangulate

# default make target entry, our classes in this example
default: classes

# Target replacement within a macro to change .java to .class files
classes: $(CLASSES:.java=.class)

# target for running the program
run: $(MAIN).class
	$(JVM) $(JLIB) $(MAIN)

# clean up extra .class files
clean:
	$(RM) *.class
