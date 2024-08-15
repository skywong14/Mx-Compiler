.PHONY: build
build:
	find ./src -name '*.java' | xargs javac -cp /usr/local/lib/antlr-4.13.2-complete.jar ./src/Main.java ./src/parser/*.java ./src/semantic/*.java ./src/semantic/ASTNodes/*.java ./src/semantic/error/*.java

.PHONY: run
run:
	java -cp ./src:/usr/local/lib/antlr-4.13.2-complete.jar Main
