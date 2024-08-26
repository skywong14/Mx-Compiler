.PHONY: build
build:
	find src -name '*.java' | xargs javac -d bin -cp /ulib/antlr-4.13.2-complete.jar
.PHONY: run
run:
	java -cp ./src:/ulib/antlr-4.13.2-complete.jar Main

.PHONY: outputFile
outputFile:
	java -cp /ulib/antlr-4.13.2-complete.jar:bin Main < test.mx > output.ll -emit-llvm

.PHONY: testSingleIR
testSingleIR:
	testcases/codegen/scripts/test_llvm_ir.bash 'java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -emit-llvm' testcases/codegen/e3.mx src/IR/builtin/builtin.ll

.PHONY: testAllIR
testAllIR:
	testcases/codegen/scripts/test_llvm_ir_all.bash 'java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -emit-llvm' testcases/codegen src/IR/builtin/builtin.ll